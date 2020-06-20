import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server();

        server.connectionRMI();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(server); // server is being executed

        Scanner scan = new Scanner(System.in);
        String line = scan.nextLine();
        while (!line.equals("quit")) {
            line = scan.nextLine();
        }

        executor.shutdown();
        server.closeRMI();
    }
}
