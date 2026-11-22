package com.bookstore.api.client;

import com.bookstore.dto.PromotionDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiClientTest {
    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    @Test
    void readsPlainTextStringResponses() throws Exception {
        withMockServer(server -> {
            server.createContext("/message", exchange ->
                    respond(exchange, 200, "text/plain; charset=UTF-8", "OK"));
            server.createContext("/api/customers/phone/0123456789", exchange ->
                    respond(exchange, 404, "application/json; charset=UTF-8", "{\"message\":\"Không tìm thấy khách hàng.\"}"));
            server.createContext("/empty-error", exchange -> {
                exchange.sendResponseHeaders(500, -1);
                exchange.close();
            });
        }, () -> {
            String response = ApiClient.get("/message", new TypeReference<String>() {
            });
            assertEquals("OK", response);
            assertNull(new CustomerApiClient().selectByPhone("0123456789"));

            ApiException emptyError = assertThrows(
                    ApiException.class,
                    () -> ApiClient.get("/empty-error", new TypeReference<String>() {
                    })
            );
            assertEquals(500, emptyError.getStatusCode());
            assertEquals("API trả về lỗi không có nội dung.", emptyError.getMessage());
        });
    }

    @Test
    void decodesPromotionsSerializedBySpringBootMapper() throws Exception {
        ObjectMapper springBootMapper = new ObjectMapper()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        Timestamp start = Timestamp.valueOf("2026-11-17 10:00:00");
        Timestamp end = Timestamp.valueOf("2026-12-17 10:00:00");
        List<PromotionDTO> serverPromotions = List.of(
                new PromotionDTO(1, "Giảm giá cuối năm", 15.0, start, end, 1),
                new PromotionDTO(2, "Chưa có ngày", 5.0, null, null, 0)
        );
        String payload = springBootMapper.writeValueAsString(serverPromotions);
        assertFalse(payload.contains("formattedDateRange"));

        withMockServer(server -> server.createContext("/api/promotions", exchange ->
                respond(exchange, 200, "application/json; charset=UTF-8", payload)), () -> {
            List<PromotionDTO> decoded = new PromotionApiClient().getAllPromotions();
            assertEquals(2, decoded.size());

            PromotionDTO first = decoded.get(0);
            assertEquals(1, first.getPromotionId());
            assertEquals("Giảm giá cuối năm", first.getPromotionName());
            assertEquals(15.0, first.getPercent());
            assertEquals(start, first.getStartDate());
            assertEquals(end, first.getEndDate());
            assertEquals(1, first.getStatus());

            PromotionDTO second = decoded.get(1);
            assertEquals(2, second.getPromotionId());
            assertNull(second.getStartDate());
            assertNull(second.getEndDate());
            assertEquals("", second.getFormattedDateRange());
        });
    }

    @Test
    void ignoresUnknownJsonFieldsInResponses() throws Exception {
        String legacyPayload = """
                [{
                  "promotionId": 9,
                  "promotionName": "Khuyến mãi cũ",
                  "percent": 12.5,
                  "startDate": "2026-11-17T03:00:00.000+00:00",
                  "endDate": "2026-12-17T03:00:00.000+00:00",
                  "status": 1,
                  "formattedDateRange": "17/11/2026 - 17/12/2026",
                  "unexpectedServerField": "ignored"
                }]
                """;

        withMockServer(server -> server.createContext("/api/promotions", exchange ->
                respond(exchange, 200, "application/json; charset=UTF-8", legacyPayload)), () -> {
            List<PromotionDTO> decoded = new PromotionApiClient().getAllPromotions();
            assertEquals(1, decoded.size());
            PromotionDTO promotion = decoded.get(0);
            assertEquals(9, promotion.getPromotionId());
            assertEquals("Khuyến mãi cũ", promotion.getPromotionName());
            assertEquals(12.5, promotion.getPercent());
            assertEquals(Timestamp.from(Instant.parse("2026-11-17T03:00:00.000Z")), promotion.getStartDate());
            assertEquals(Timestamp.from(Instant.parse("2026-12-17T03:00:00.000Z")), promotion.getEndDate());
            assertEquals(1, promotion.getStatus());
        });
    }

    @Test
    void throwsApiExceptionOnMalformedJsonResponse() throws Exception {
        withMockServer(server -> server.createContext("/malformed", exchange ->
                respond(exchange, 200, "application/json; charset=UTF-8", "{invalid-json")), () -> {
            ApiException error = assertThrows(
                    ApiException.class,
                    () -> ApiClient.get("/malformed", new TypeReference<PromotionDTO>() {
                    })
            );
            assertEquals("Không thể mã hóa hoặc giải mã dữ liệu API.", error.getMessage());
        });
    }

    @Test
    void encodesCustomerPhonePathSegmentAndPreventsPathTraversal() throws Exception {
        java.util.concurrent.atomic.AtomicReference<String> capturedRawPath = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicBoolean accountsHit = new java.util.concurrent.atomic.AtomicBoolean(false);

        withMockServer(server -> {
            server.createContext("/api/accounts", exchange -> {
                accountsHit.set(true);
                respond(exchange, 200, "application/json; charset=UTF-8", "[]");
            });
            server.createContext("/api/customers/phone/", exchange -> {
                capturedRawPath.set(exchange.getRequestURI().getRawPath());
                respond(exchange, 404, "application/json; charset=UTF-8", "{\"message\":\"Không tìm thấy khách hàng.\"}");
            });
        }, () -> {
            assertNull(new CustomerApiClient().selectByPhone("090 123"));
            assertEquals("/api/customers/phone/090%20123", capturedRawPath.get());

            assertNull(new CustomerApiClient().selectByPhone("../../accounts"));
            assertFalse(accountsHit.get());
            assertEquals("/api/customers/phone/..%2F..%2Faccounts", capturedRawPath.get());

            ApiException badPathError = assertThrows(
                    ApiException.class,
                    () -> ApiClient.get("/api/customers/phone/bad path with spaces", new TypeReference<String>() {
                    })
            );
            assertEquals("Đường dẫn yêu cầu API không hợp lệ.", badPathError.getMessage());
        });
    }

    private static void withMockServer(Consumer<HttpServer> configurator, ThrowingRunnable assertions) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        configurator.accept(server);
        server.start();

        String oldBaseUrl = System.getProperty("bookstore.api.base-url");
        System.setProperty(
                "bookstore.api.base-url",
                "http://127.0.0.1:" + server.getAddress().getPort()
        );
        try {
            assertions.run();
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

    private static void respond(HttpExchange exchange, int statusCode, String contentType, String text)
            throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
