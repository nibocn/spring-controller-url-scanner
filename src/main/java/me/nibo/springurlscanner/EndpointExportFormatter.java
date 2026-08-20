package me.nibo.springurlscanner;

import java.util.List;

final class EndpointExportFormatter {
    private static final String HEADER = "Type,Method,URL,Controller,Handler,Source";

    private EndpointExportFormatter() {
    }

    static String formatCsv(List<Endpoint> endpoints) {
        StringBuilder csv = new StringBuilder(HEADER).append('\n');
        for (Endpoint endpoint : endpoints) {
            csv.append(cell(endpoint.type())).append(',')
                    .append(cell(endpoint.httpMethod())).append(',')
                    .append(cell(endpoint.url())).append(',')
                    .append(cell(endpoint.controller())).append(',')
                    .append(cell(endpoint.handler())).append(',')
                    .append(cell(endpoint.source())).append('\n');
        }
        return csv.toString();
    }

    private static String cell(String value) {
        if (value == null) return "";
        boolean quote = value.indexOf(',') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;
        String escaped = value.replace("\"", "\"\"");
        return quote ? "\"" + escaped + "\"" : escaped;
    }
}
