import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Classe contenente gli esecutori. Qui vengono accettate le connessioni ed eseguiti i
 * Runnable corrispondenti alle partite ei client.
 * Viene inoltre iniziallizzato il lato server del RMI della registrazione
 */
public class Server implements RegIntWQ, Runnable {
    private Registry registry;
    static final int PORT = 34522;
    protected UserCollection userInfo;
    protected Dictionary dictionary;

    private ServerSocket socketTCP;

    private ExecutorService executorTCP;
    private ExecutorService executorUDP;

    /**
     * Metodo costruttore
     * @param userFile stringa del path del file di configurazione degli utenti
     * @param passFile stringa del path del file e relative password
     * @param dictionary stringa del path del file del dizionario
     */
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

            // ciclo accettazioni nuovi connessioni TCP
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

    /**
     * Implementazione del metodo RMI.
     * @param name id con il quale si vuole registrare
     * @param password codice per effetuare l'accesso al login
     * @return
     */
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

    /**
     * Dopoaver creato una nuova partita essa viene eseguita nell'executorUDP
     * @param comunicator la partita che si vuole far iniziare
     */
    public void submitGame(UDPGameServer comunicator) {
        this.executorUDP.submit(comunicator); // test for UDP connection
    }

    /**
     * mentodo per la chiusura del server
     * @throws IOException in caso di errore durante la chiusura della socket.
     */
    public void shutdown() throws IOException {
        Thread.currentThread().interrupt();
        socketTCP.close();
        this.executorTCP.shutdown();
        this.executorUDP.shutdown();
        closeRMI();
    }

}
