package me.nibo.springurlscanner;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndpointExportFormatterTest {
    @Test
    void formatsEndpointsAsCsvWithHeader() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("Controller", "GET", "/api/orders", "com.example.OrderController", "list", "project", null),
                new Endpoint("FeignClient", "POST", "/remote/orders", "com.example.OrderClient", "create", "orders-client.jar", null)
        );

        String csv = EndpointExportFormatter.formatCsv(endpoints);

        assertEquals("""
                Type,Method,URL,Controller,Handler,Source
                Controller,GET,/api/orders,com.example.OrderController,list,project
                FeignClient,POST,/remote/orders,com.example.OrderClient,create,orders-client.jar
                """, csv);
    }

    @Test
    void escapesCsvCellsThatContainCommasQuotesOrLineBreaks() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("Controller", "GET", "/api/orders,active", "com.example.\"OrderController\"", "list\nOrders", "project", null)
        );

        String csv = EndpointExportFormatter.formatCsv(endpoints);

        assertEquals("Type,Method,URL,Controller,Handler,Source\n" +
                "Controller,GET,\"/api/orders,active\",\"com.example.\"\"OrderController\"\"\",\"list\n" +
                "Orders\",project\n", csv);
    }
}
