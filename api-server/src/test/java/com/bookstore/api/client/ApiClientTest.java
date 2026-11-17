package com.bookstore.api.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiClientTest {
    @Test
    void readsPlainTextStringResponses() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/message", exchange -> {
            byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/api/customers/phone/0123456789", exchange -> {
            byte[] body = "{\"message\":\"Không tìm thấy khách hàng.\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(404, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        String oldBaseUrl = System.getProperty("bookstore.api.base-url");
        System.setProperty(
                "bookstore.api.base-url",
                "http://127.0.0.1:" + server.getAddress().getPort()
        );
        try {
            String response = ApiClient.get("/message", new TypeReference<String>() {
            });
            assertEquals("OK", response);
            assertNull(new CustomerApiClient().selectByPhone("0123456789"));
        } finally {
            server.stop(0);
            ApiClient.clearAccessToken();
            if (oldBaseUrl == null) {
                System.clearProperty("bookstore.api.base-url");
            } else {
                System.setProperty("bookstore.api.base-url", oldBaseUrl);
            }
        }
    }
}
