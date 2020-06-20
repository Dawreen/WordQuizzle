import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server();

        server.connectionRMI();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(server); // server is being executed

        executor.shutdown();
        server.closeRMI();
    }
}
