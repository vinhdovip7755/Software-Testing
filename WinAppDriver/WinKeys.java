import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Keyboard input through WinAppDriver (default) or Java AWT Robot (-Dui.driver=robot). */
final class WinKeys implements AutoCloseable {
    private static final char KEY_NULL = '\uE000';
    private static final char KEY_BACKSPACE = '\uE003';
    private static final char KEY_ENTER = '\uE007';
    private static final char KEY_CONTROL = '\uE009';
    private static final char KEY_HOME = '\uE011';
    private static final char KEY_DOWN = '\uE015';

    private final boolean useRobot;
    private final Robot robot;
    private final ObjectMapper json;
    private final HttpClient client;
    private final String session;

    WinKeys() throws Exception {
        String driver = System.getProperty("ui.driver",
                System.getenv().getOrDefault("BOOKSTORE_UI_DRIVER", "winappdriver"));
        this.useRobot = "robot".equalsIgnoreCase(driver);
        if (useRobot) {
            this.robot = new Robot();
            this.robot.setAutoDelay(8);
            this.json = null;
            this.client = null;
            this.session = null;
        } else {
            this.robot = null;
            this.json = new ObjectMapper();
            this.client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(4)).build();
            JsonNode result = request("POST", "/session", Map.of("desiredCapabilities",
                    Map.of("app", "Root", "deviceName", "WindowsPC", "platformName", "Windows")));
            this.session = result.path("sessionId").asText();
            if (this.session.isBlank()) throw new IOException("WinAppDriver did not return a session id");
        }
    }

    boolean send(String keys) {
        if (useRobot) {
            sendViaRobot(keys);
            return true;
        }
        try {
            request("POST", "/session/" + session + "/keys", Map.of("value", List.of(keys)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void sendViaRobot(String keys) {
        boolean ctrlDown = false;
        try {
            for (int i = 0; i < keys.length(); i++) {
                char ch = keys.charAt(i);
                switch (ch) {
                    case KEY_CONTROL -> {
                        if (!ctrlDown) {
                            robot.keyPress(KeyEvent.VK_CONTROL);
                            ctrlDown = true;
                        }
                    }
                    case KEY_NULL -> {
                        if (ctrlDown) {
                            robot.keyRelease(KeyEvent.VK_CONTROL);
                            ctrlDown = false;
                        }
                    }
                    case KEY_BACKSPACE -> tap(KeyEvent.VK_BACK_SPACE, false);
                    case KEY_ENTER -> tap(KeyEvent.VK_ENTER, false);
                    case KEY_HOME -> tap(KeyEvent.VK_HOME, false);
                    case KEY_DOWN -> tap(KeyEvent.VK_DOWN, false);
                    default -> typeAsciiChar(ch);
                }
            }
        } finally {
            if (ctrlDown) robot.keyRelease(KeyEvent.VK_CONTROL);
        }
        robot.waitForIdle();
    }

    private void tap(int vk, boolean shift) {
        if (shift) robot.keyPress(KeyEvent.VK_SHIFT);
        try {
            robot.keyPress(vk);
            robot.keyRelease(vk);
        } finally {
            if (shift) robot.keyRelease(KeyEvent.VK_SHIFT);
        }
    }

    private void typeAsciiChar(char ch) {
        if (ch >= 'a' && ch <= 'z') { tap(KeyEvent.VK_A + (ch - 'a'), false); return; }
        if (ch >= 'A' && ch <= 'Z') { tap(KeyEvent.VK_A + (ch - 'A'), true); return; }
        if (ch >= '0' && ch <= '9') { tap(KeyEvent.VK_0 + (ch - '0'), false); return; }
        switch (ch) {
            case ' ' -> tap(KeyEvent.VK_SPACE, false);
            case '.' -> tap(KeyEvent.VK_PERIOD, false);
            case ',' -> tap(KeyEvent.VK_COMMA, false);
            case '-' -> tap(KeyEvent.VK_MINUS, false);
            case '_' -> tap(KeyEvent.VK_MINUS, true);
            case '=' -> tap(KeyEvent.VK_EQUALS, false);
            case '+' -> tap(KeyEvent.VK_EQUALS, true);
            case '/' -> tap(KeyEvent.VK_SLASH, false);
            case '?' -> tap(KeyEvent.VK_SLASH, true);
            case ';' -> tap(KeyEvent.VK_SEMICOLON, false);
            case ':' -> tap(KeyEvent.VK_SEMICOLON, true);
            case '@' -> tap(KeyEvent.VK_2, true);
            case '!' -> tap(KeyEvent.VK_1, true);
            case '#' -> tap(KeyEvent.VK_3, true);
            case '$' -> tap(KeyEvent.VK_4, true);
            case '%' -> tap(KeyEvent.VK_5, true);
            case '^' -> tap(KeyEvent.VK_6, true);
            case '&' -> tap(KeyEvent.VK_7, true);
            case '*' -> tap(KeyEvent.VK_8, true);
            case '(' -> tap(KeyEvent.VK_9, true);
            case ')' -> tap(KeyEvent.VK_0, true);
            default -> {
                int vk = KeyEvent.getExtendedKeyCodeForChar(ch);
                if (vk == KeyEvent.VK_UNDEFINED) {
                    throw new IllegalArgumentException("Unsupported Robot key char: " + ch);
                }
                tap(vk, Character.isUpperCase(ch));
            }
        }
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

    public void close() throws Exception {
        if (!useRobot && session != null) {
            request("DELETE", "/session/" + session, null);
        }
    }
}
