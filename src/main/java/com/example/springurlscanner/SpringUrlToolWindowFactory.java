package com.example.springurlscanner;

import com.intellij.openapi.application.ApplicationManager;
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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
        private List<Endpoint> all = List.of();

        EndpointPanel(Project project) {
            super(new BorderLayout(8, 8));
            this.project = project;

            JButton scan = new JButton("Scan Controllers");
            JCheckBox includeFeign = new JCheckBox("Include @FeignClient");
            includeFeign.setSelected(false);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            actions.add(scan);
            actions.add(includeFeign);

            JPanel top = new JPanel(new BorderLayout(8, 0));
            top.add(actions, BorderLayout.WEST);
            filter.getEmptyText().setText("Filter Type / URL / Controller / JAR...");
            top.add(filter, BorderLayout.CENTER);
            add(top, BorderLayout.NORTH);
            add(new JBScrollPane(table), BorderLayout.CENTER);

            table.setAutoCreateRowSorter(true);
            table.setFillsViewportHeight(true);
            table.getColumnModel().getColumn(0).setPreferredWidth(90);
            table.getColumnModel().getColumn(1).setPreferredWidth(60);
            table.getColumnModel().getColumn(2).setPreferredWidth(260);
            table.getColumnModel().getColumn(3).setPreferredWidth(260);
            table.getColumnModel().getColumn(4).setPreferredWidth(120);
            table.getColumnModel().getColumn(5).setPreferredWidth(180);

            scan.addActionListener(e -> scan(scan, includeFeign.isSelected()));
            filter.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
                @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
                @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
            });

            table.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() != 2) return;
                    int viewRow = table.rowAtPoint(e.getPoint());
                    if (viewRow < 0) return;
                    int row = table.convertRowIndexToModel(viewRow);
                    PsiMethod method = model.get(row).psiMethod();
                    if (method != null && method.isValid()) OpenSourceUtil.navigate(true, method);
                }
            });
        }

        private void scan(JButton button, boolean includeFeignClients) {
            button.setEnabled(false);
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
                }

                @Override public void onCancel() {
                    button.setEnabled(true);
                }

                @Override public void onThrowable(@NotNull Throwable error) {
                    button.setEnabled(true);
                }
            });
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
