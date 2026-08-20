package me.nibo.springurlscanner;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndpointClipboardFormatterTest {
    @Test
    void formatsSelectedEndpointsAsTabSeparatedRowsWithHeader() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("Controller", "GET", "/api/orders", "com.example.OrderController", "list", "project", null),
                new Endpoint("FeignClient", "POST", "/remote/orders", "com.example.OrderClient", "create", "orders-client.jar", null)
        );

        String text = EndpointClipboardFormatter.formatRows(endpoints);

        assertEquals("""
                Type\tMethod\tURL\tController\tHandler\tSource
                Controller\tGET\t/api/orders\tcom.example.OrderController\tlist\tproject
                FeignClient\tPOST\t/remote/orders\tcom.example.OrderClient\tcreate\torders-client.jar""", text);
    }

    @Test
    void sanitizesTabsAndLineBreaksInsideCellValues() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("Controller", "GET", "/api\torders", "com.example.Order\nController", "list\rOrders", "project", null)
        );

        String text = EndpointClipboardFormatter.formatRows(endpoints);

        assertEquals("""
                Type\tMethod\tURL\tController\tHandler\tSource
                Controller\tGET\t/api orders\tcom.example.Order Controller\tlist Orders\tproject""", text);
    }

    @Test
    void formatsSingleCellWithoutHeader() {
        Endpoint endpoint = new Endpoint("Controller", "GET", "/api\torders", "com.example.OrderController", "list", "project", null);

        String text = EndpointClipboardFormatter.formatCell(endpoint, 2);

        assertEquals("/api orders", text);
    }
}
