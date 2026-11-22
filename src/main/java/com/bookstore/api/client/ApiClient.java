package com.bookstore.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class ApiClient {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static volatile String accessToken;

    private ApiClient() {
    }

    public static <T> T post(String path, Object body, TypeReference<T> responseType) {
        return send("POST", path, body, responseType);
    }

    public static <T> T get(String path, TypeReference<T> responseType) {
        return send("GET", path, null, responseType);
    }

    public static <T> T put(String path, Object body, TypeReference<T> responseType) {
        return send("PUT", path, body, responseType);
    }

    public static void postNoContent(String path) {
        send("POST", path, null, null);
    }

    public static void postNoContent(String path, Object body) {
        send("POST", path, body, null);
    }

    public static void putNoContent(String path, Object body) {
        send("PUT", path, body, null);
    }

    public static void deleteNoContent(String path) {
        send("DELETE", path, null, null);
    }

    public static void setAccessToken(String token) {
        accessToken = token;
    }

    public static void clearAccessToken() {
        accessToken = null;
    }

    private static <T> T send(String method, String path, Object body, TypeReference<T> responseType) {
        String baseUrl = System.getProperty(
                "bookstore.api.base-url",
                System.getenv().getOrDefault("BOOKSTORE_API_BASE_URL", "http://127.0.0.1:8080")
        );

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl).resolve(path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json");

            String token = accessToken;
            if (token != null && !token.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            if (body == null) {
                requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                String jsonBody = OBJECT_MAPPER.writeValueAsString(body);
                requestBuilder.header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            }

            HttpResponse<String> response = HTTP_CLIENT.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(response.statusCode(), errorMessage(response.body()));
            }
            if (responseType == null) {
                return null;
            }
            if (responseType.getType().equals(String.class)) {
                return OBJECT_MAPPER.convertValue(response.body(), responseType);
            }
            return OBJECT_MAPPER.readValue(response.body(), responseType);
        } catch (ApiException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new ApiException("Đường dẫn yêu cầu API không hợp lệ.", e);
        } catch (JsonProcessingException e) {
            throw new ApiException("Không thể mã hóa hoặc giải mã dữ liệu API.", e);
        } catch (IOException e) {
            throw new ApiException("Không thể kết nối API tại " + baseUrl + ".", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Yêu cầu API bị gián đoạn.", e);
        }
    }

    private static String errorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "API trả về lỗi không có nội dung.";
        }
        try {
            JsonNode json = OBJECT_MAPPER.readTree(responseBody);
            if (json != null && json.isTextual()) {
                return json.asText();
            }
            if (json != null) {
                JsonNode message = json.get("message");
                if (message != null && message.isTextual()) {
                    return message.asText();
                }
            }
        } catch (JsonProcessingException ignored) {
            return responseBody;
        }
        return responseBody;
    }
}
