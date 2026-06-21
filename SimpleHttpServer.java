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
    private int port;

    public SimpleHttpServer(int port) {
        this.port = port;
    }
    
    public void run() throws IOException, InterruptedException {
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

    private void handleClient(Socket clientSocket) throws IOException, InterruptedException {
        IO.println("Handling on thread " + Thread.currentThread().getName());
        try (clientSocket) {
            HttpRequest httpRequest = parseRequest(clientSocket.getInputStream());
            OutputStream output = clientSocket.getOutputStream();

            if (httpRequest == null) {
                sendResponse(output, new HttpResponse(400, "Bad Request", "text/plain", "Bad Request"));
                return;
            }

            HttpResponse httpResponse = route(httpRequest);

            sendResponse(output, httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HttpResponse route(HttpRequest httpRequest) {
        String method = httpRequest.method();
        String path = httpRequest.path();

        if (method.equals("GET") && path.equals("/")) {
            return HttpResponse.ok("Welcome to the home page");
        } 

        if (method.equals("GET") && path.equals("/hello")) {
            return HttpResponse.ok("Hello from Java");
        } 

        if (method.equals("GET") && path.equals("/html")) {
            return HttpResponse.html("<h1>Hello HTML</h1>");
        }
        
        return HttpResponse.notFound();
    }

    private void sendResponse(
        OutputStream output, 
        HttpResponse httpResponse
    ) throws IOException {
        byte[] bodyBytes = httpResponse.body().getBytes(StandardCharsets.UTF_8);

        String headers = 
                "HTTP/1.1 " + httpResponse.statusCode() + httpResponse.statusText() + "\r\n" +
                "Content-Type: " + httpResponse.contentType() + "; charset=UTF-8\r\n" +
                "Content-Length: " + bodyBytes.length + "\r\n" +
                "\r\n";

        output.write(headers.getBytes(StandardCharsets.UTF_8));
        output.write(bodyBytes);
        output.flush();
    }

    private HttpRequest parseRequest(InputStream inputStream) throws IOException {
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
