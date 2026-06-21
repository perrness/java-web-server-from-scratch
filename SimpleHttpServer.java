import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class SimpleHttpServer {
    void main(String[] args) throws IOException, InterruptedException {
        int port = 8080;


        try(ServerSocket serverSocket = new ServerSocket(port)) {
            IO.println("Server running on http://localhost:" + port);

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();

                    executor.submit(() -> {
                        try {
                            handleClient(clientSocket);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

        }
    }

    private static void handleClient(Socket clientSocket) throws IOException, InterruptedException {
        IO.println("Handling on thread " + Thread.currentThread().getName());
        try (clientSocket) {
            HttpRequest httpRequest = parseRequest(clientSocket.getInputStream());
            OutputStream output = clientSocket.getOutputStream();

            if (httpRequest == null) {
                sendResponse(output, 400, "Bad Request", "text/plain", "Bad Request");
                return;
            }

            String method = httpRequest.method();
            String path = httpRequest.path();

            if (method.equals("GET") && path.equals("/")) {
                sendResponse(output, 200, "OK", "text/plain", "Welcome to the home page");
            } else if (method.equals("GET") && path.equals("/hello")) {
                sendResponse(output, 200, "OK", "text/plain", "Hello from Java");
            } else if (method.equals("GET") && path.equals("/html")) {
                sendResponse(output, 200, "OK", "text/html", "<h1>Hello HTML</h1>");
            } else {
                sendResponse(output, 404, "Not Found", "text/plain", "404 Not Found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendResponse(
        OutputStream output, 
        int statusCode, 
        String statusText, 
        String contentType, 
        String body
    ) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        String headers = 
                "HTTP/1.1 " + statusCode + statusText + "\r\n" +
                "Content-Type: " + contentType + "; charset=UTF-8\r\n" +
                "Content-Length: " + bodyBytes.length + "\r\n" +
                "\r\n";

        output.write(headers.getBytes(StandardCharsets.UTF_8));
        output.write(bodyBytes);
        output.flush();
    }

    private static HttpRequest parseRequest(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String requestLine = reader.readLine();

        String[] requestParts = requestLine.split(" ");

        if (requestParts.length != 3) {
            return null;
        }

        String method = requestParts[0];
        String path = requestParts[1];
        String body = requestParts[2];
        Map<String, String> headers = new HashMap<>();

        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            String[] header = headerLine.split(" ");
            IO.println(header[0] + ": " + header[1]);
            headers.put(header[0], header[1]);
        }

        return new HttpRequest(method, path, headers, body);
    }
}
