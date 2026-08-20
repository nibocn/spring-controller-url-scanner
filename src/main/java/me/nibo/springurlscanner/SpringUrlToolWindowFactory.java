package me.nibo.springurlscanner;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpringUrlToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        EndpointPanel panel = new EndpointPanel(project);
        Content content = ContentFactory.getInstance().createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private static final class EndpointPanel extends JPanel {
        private final Project project;
        private final EndpointTableModel model = new EndpointTableModel();
        private final JBTable table = new JBTable(model);
        private final JBTextField filter = new JBTextField();
        private final JLabel status = new JLabel("Ready");
        private final AsyncProcessIcon loadingIcon = new AsyncProcessIcon("Spring URL Scanner");
        private List<Endpoint> all = List.of();

        EndpointPanel(Project project) {
            super(new BorderLayout(8, 8));
            this.project = project;

            JButton scan = new JButton("Scan Controllers");
            JButton export = new JButton("Export All");
            JCheckBox includeFeign = new JCheckBox("Include @FeignClient");
            includeFeign.setSelected(false);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            actions.add(scan);
            loadingIcon.setVisible(false);
            loadingIcon.suspend();
            actions.add(loadingIcon);
            actions.add(status);

            JPanel secondaryActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            secondaryActions.add(includeFeign);
            secondaryActions.add(export);

            JPanel filterRow = new JPanel(new BorderLayout(8, 0));
            filterRow.add(secondaryActions, BorderLayout.WEST);
            filter.getEmptyText().setText("Filter Type / URL / Controller / JAR...");
            filterRow.add(filter, BorderLayout.CENTER);

            JPanel top = new JPanel();
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
            actions.setAlignmentX(Component.LEFT_ALIGNMENT);
            filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(actions);
            top.add(Box.createVerticalStrut(6));
            top.add(filterRow);
            add(top, BorderLayout.NORTH);
            add(new JBScrollPane(table), BorderLayout.CENTER);

            table.setAutoCreateRowSorter(true);
            table.setFillsViewportHeight(true);
            table.setCellSelectionEnabled(true);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getColumnModel().getColumn(0).setPreferredWidth(90);
            table.getColumnModel().getColumn(1).setPreferredWidth(60);
            table.getColumnModel().getColumn(2).setPreferredWidth(260);
            table.getColumnModel().getColumn(3).setPreferredWidth(260);
            table.getColumnModel().getColumn(4).setPreferredWidth(120);
            table.getColumnModel().getColumn(5).setPreferredWidth(180);

            scan.addActionListener(e -> scan(scan, includeFeign.isSelected()));
            export.addActionListener(e -> exportAll());
            filter.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
                @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
                @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
            });

            JPopupMenu popup = new JPopupMenu();
            JMenuItem copyCellItem = new JMenuItem("Copy Cell");
            copyCellItem.addActionListener(e -> copySelectedCell());
            popup.add(copyCellItem);
            JMenuItem copyRowItem = new JMenuItem("Copy Row");
            copyRowItem.addActionListener(e -> copySelectedRow());
            popup.add(copyRowItem);

            table.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }

                @Override public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }

                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() != 2) return;
                    int viewRow = table.rowAtPoint(e.getPoint());
                    if (viewRow < 0) return;
                    int row = table.convertRowIndexToModel(viewRow);
                    PsiMethod method = model.get(row).psiMethod();
                    if (method != null && method.isValid()) OpenSourceUtil.navigate(true, method);
                }

                private void maybeShowPopup(MouseEvent e) {
                    if (!e.isPopupTrigger()) return;
                    int viewRow = table.rowAtPoint(e.getPoint());
                    int viewColumn = table.columnAtPoint(e.getPoint());
                    if (viewRow >= 0 && viewColumn >= 0) {
                        table.changeSelection(viewRow, viewColumn, false, false);
                    }
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            });

            KeyStroke copyShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
            table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(copyShortcut, "copySelectedCell");
            table.getActionMap().put("copySelectedCell", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    copySelectedCell();
                }
            });
        }

        private void scan(JButton button, boolean includeFeignClients) {
            button.setEnabled(false);
            setScanning(true);
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Scanning Spring Controller URLs", true) {
                private List<Endpoint> result = List.of();

                @Override public void run(@NotNull com.intellij.openapi.progress.ProgressIndicator indicator) {
                    try {
                        result = ApplicationManager.getApplication().runReadAction(
                                (com.intellij.openapi.util.Computable<List<Endpoint>>) () -> SpringEndpointScanner.scan(project, indicator, includeFeignClients)
                        );
                    } catch (Throwable t) {
                        ApplicationManager.getApplication().invokeLater(() ->
                                Messages.showErrorDialog(project, t.getMessage(), "Spring URL Scanner"));
                    }
                }

                @Override public void onSuccess() {
                    all = result;
                    applyFilter();
                    button.setEnabled(true);
                    setScanning(false);
                    status.setText(result.size() + " endpoints found");
                }

                @Override public void onCancel() {
                    button.setEnabled(true);
                    setScanning(false);
                    status.setText("Scan canceled");
                }

                @Override public void onThrowable(@NotNull Throwable error) {
                    button.setEnabled(true);
                    setScanning(false);
                    status.setText("Scan failed");
                }
            });
        }

        private void setScanning(boolean scanning) {
            loadingIcon.setVisible(scanning);
            if (scanning) {
                loadingIcon.resume();
            } else {
                loadingIcon.suspend();
            }
            if (scanning) status.setText("Scanning...");
            table.getEmptyText().setText(scanning ? "Scanning Spring URLs..." : "Nothing to show");
        }

        private void applyFilter() {
            String q = filter.getText().trim().toLowerCase(Locale.ROOT);
            if (q.isEmpty()) {
                model.setRows(all);
                return;
            }
            List<Endpoint> filtered = new ArrayList<>();
            for (Endpoint e : all) {
                String haystack = (e.type() + " " + e.httpMethod() + " " + e.url() + " " + e.controller() + " " + e.handler() + " " + e.source())
                        .toLowerCase(Locale.ROOT);
                if (haystack.contains(q)) filtered.add(e);
            }
            model.setRows(filtered);
        }

        private void copySelectedCell() {
            int viewRow = selectedViewRow();
            int viewColumn = selectedViewColumn();
            if (viewRow < 0 || viewColumn < 0) {
                Messages.showInfoMessage(project, "Select an endpoint cell to copy.", "Spring URL Scanner");
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            int modelColumn = table.convertColumnIndexToModel(viewColumn);
            CopyPasteManager.getInstance().setContents(new StringSelection(EndpointClipboardFormatter.formatCell(model.get(modelRow), modelColumn)));
        }

        private void copySelectedRow() {
            int viewRow = selectedViewRow();
            if (viewRow < 0) {
                Messages.showInfoMessage(project, "Select an endpoint row to copy.", "Spring URL Scanner");
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            CopyPasteManager.getInstance().setContents(new StringSelection(EndpointClipboardFormatter.formatRows(List.of(model.get(modelRow)))));
        }

        private void exportAll() {
            if (all.isEmpty()) {
                Messages.showInfoMessage(project, "Scan endpoints before exporting.", "Spring URL Scanner");
                return;
            }
            FileSaverDescriptor descriptor = new FileSaverDescriptor(
                    "Export Spring URLs",
                    "Export all scanned Spring URL results as CSV.",
                    "csv"
            );
            VirtualFileWrapper file = FileChooserFactory.getInstance()
                    .createSaveFileDialog(descriptor, project)
                    .save((VirtualFile) null, "spring-urls.csv");
            if (file == null) return;
            try {
                Files.writeString(file.getFile().toPath(), EndpointExportFormatter.formatCsv(all), StandardCharsets.UTF_8);
                status.setText("Exported " + all.size() + " endpoints");
            } catch (IOException ex) {
                Messages.showErrorDialog(project, ex.getMessage(), "Export Spring URLs");
            }
        }

        private int selectedViewRow() {
            return table.getSelectedRow();
        }

        private int selectedViewColumn() {
            return table.getSelectedColumn();
        }
    }

    private static final class EndpointTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Type", "Method", "URL", "Controller", "Handler", "Source"};
        private List<Endpoint> rows = List.of();

        void setRows(List<Endpoint> rows) {
            this.rows = List.copyOf(rows);
            fireTableDataChanged();
        }

        Endpoint get(int row) { return rows.get(row); }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int column) { return COLUMNS[column]; }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            Endpoint e = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> e.type();
                case 1 -> e.httpMethod();
                case 2 -> e.url();
                case 3 -> e.controller();
                case 4 -> e.handler();
                case 5 -> e.source();
                default -> "";
            };
        }
    }
}
