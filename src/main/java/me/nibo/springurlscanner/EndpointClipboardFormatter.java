package me.nibo.springurlscanner;

import java.util.List;

final class EndpointClipboardFormatter {
    private static final String HEADER = "Type\tMethod\tURL\tController\tHandler\tSource";

    private EndpointClipboardFormatter() {
    }

    static String formatRows(List<Endpoint> endpoints) {
        StringBuilder text = new StringBuilder(HEADER);
        for (Endpoint endpoint : endpoints) {
            text.append('\n')
                    .append(cell(endpoint.type())).append('\t')
                    .append(cell(endpoint.httpMethod())).append('\t')
                    .append(cell(endpoint.url())).append('\t')
                    .append(cell(endpoint.controller())).append('\t')
                    .append(cell(endpoint.handler())).append('\t')
                    .append(cell(endpoint.source()));
        }
        return text.toString();
    }

    static String formatCell(Endpoint endpoint, int column) {
        return switch (column) {
            case 0 -> cell(endpoint.type());
            case 1 -> cell(endpoint.httpMethod());
            case 2 -> cell(endpoint.url());
            case 3 -> cell(endpoint.controller());
            case 4 -> cell(endpoint.handler());
            case 5 -> cell(endpoint.source());
            default -> "";
        };
    }

    private static String cell(String value) {
        if (value == null) return "";
        return value.replace('\t', ' ')
                .replace('\n', ' ')
                .replace('\r', ' ');
    }
}
