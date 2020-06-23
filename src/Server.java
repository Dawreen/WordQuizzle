import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements RegIntWQ, Runnable {
    private Registry registry;
    static final int PORT = 34522;
    protected UserCollection userInfo;
    protected Dictionary dictionary;

    private ServerSocket socketTCP;

    private ExecutorService executorTCP;
    private ExecutorService executorUDP;

    public Server(String userFile, String passFile, String dictionary) {
        this.userInfo = new UserCollection(userFile, passFile);
        this.dictionary = new Dictionary(dictionary);
    }

    @Override
    public void run() {
        this.executorTCP = null;

        try (ServerSocket socketTCP = new ServerSocket(PORT)) {
            this.socketTCP = socketTCP;

            this.executorTCP = Executors.newCachedThreadPool();
            this.executorUDP = Executors.newCachedThreadPool();

            while (!Thread.currentThread().isInterrupted()){
                Session session = new Session(socketTCP.accept(), this);
                executorTCP.submit(session);
            }

            executorTCP.shutdown();

        } catch (SocketException e) {
            if (executorTCP != null) executorTCP.shutdown();
            System.out.println("SERVER EXECUTOR - shutdown ok");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * inizializza un metodo RMI
     */
    public void connectionRMI(){
        try {
            RegIntWQ stub = (RegIntWQ) UnicastRemoteObject.exportObject(this, 0);
            LocateRegistry.createRegistry(1099);
            this.registry = LocateRegistry.getRegistry();
            registry.bind("Registration", stub);

            System.out.println("Server - RMI ready!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Chiude una connessione RMI in modo da rifiutare altre registrazioni
     */
    public void closeRMI() {
        try {
            this.registry.unbind("Registration");
            UnicastRemoteObject.unexportObject(this, true);

            System.out.println("RMI closed!");
        } catch ( Exception e) {
            e.printStackTrace();
        }
        /*
          possible exceptions:
          NotBoundException
          AcessException
          UnmarshalException
          RemoteException
         */
    }

    public int registration(String name, String password) {
        // non dovrebbe mai entrare in questo if...
        if (name.isBlank() || password.isBlank()) return 1;

        if (this.userInfo.addUser(name, password)) {
            // registrazione ok
            return 0;
        } else {
            // username già presente
            return 2;
        }
    }

    public void submitGame(UDPGameServer game) {
        this.executorUDP.submit(game); // test for UDP connection
    }

    public void shutdown() throws IOException {
        Thread.currentThread().interrupt();
        socketTCP.close();
        this.executorTCP.shutdown();
        this.executorUDP.shutdown();
        closeRMI();
    }

}
