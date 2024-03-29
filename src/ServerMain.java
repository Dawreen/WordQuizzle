import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Classe main che avvia il lato server.
 */
public class ServerMain {
    public static void main(String[] args) throws IOException {
        Server server = new Server("users.json", "pass.json", "parole1159.txt");

        server.connectionRMI();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(server); // server is being executed

        Scanner scan = new Scanner(System.in);
        String line = scan.nextLine();
        while (!line.equals("quit")) {
            line = scan.nextLine();
        } // ciclo per chiudere il server in maniera "dolce"

        server.shutdown();
        executor.shutdown();
        System.exit(0);
    }
}
