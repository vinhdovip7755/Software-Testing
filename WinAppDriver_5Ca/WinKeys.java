import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Input through the WinAppDriver desktop session on the local Windows machine. */
final class WinKeys implements AutoCloseable {
    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    private final String session;

    WinKeys() throws Exception {
        JsonNode result = request("POST", "/session", Map.of("desiredCapabilities",
                Map.of("app", "Root", "deviceName", "WindowsPC", "platformName", "Windows")));
        session = result.path("sessionId").asText();
        if (session.isBlank()) throw new IOException("WinAppDriver did not return a session id");
    }

    void send(String keys) throws Exception {
        request("POST", "/session/" + session + "/keys", Map.of("value", List.of(keys)));
    }

    private JsonNode request(String method, String path, Object body) throws Exception {
        var payload = body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofByteArray(json.writeValueAsBytes(body));
        var req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:4723" + path))
                .timeout(Duration.ofSeconds(20)).header("Content-Type", "application/json")
                .method(method, payload).build();
        var response = client.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode result = json.readTree(response.body());
        if (response.statusCode() / 100 != 2 || result.path("status").asInt(0) != 0)
            throw new IOException("WinAppDriver " + method + " " + path + " HTTP " + response.statusCode()
                    + " status=" + result.path("status"));
        return result;
    }

    public void close() throws Exception { request("DELETE", "/session/" + session, null); }
}
