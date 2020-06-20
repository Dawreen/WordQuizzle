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

    @Override
    public void run() {
        ExecutorService executorTCP = null;
        ExecutorService executorUDP;

        try (ServerSocket socketTCP = new ServerSocket(PORT)){
            executorTCP = Executors.newCachedThreadPool();
            executorUDP = Executors.newCachedThreadPool();

            executorUDP.submit(new UDPGameServer()); // test for UDP connection

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

    @SuppressWarnings("RedundantThrows")
    @Override
    public boolean registration(String name, String password) throws RemoteException {
// TODO: 20/06/2020 registration
        return false;
    }
}
