import java.io.IOException;

public class Main {
    void main(String[] args) throws IOException, InterruptedException {
        int port = 8080;

        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(port);

        simpleHttpServer.run();
    }
}
