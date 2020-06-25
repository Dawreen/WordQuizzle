import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.*;

/**
 * Classe molto flessibile. In grado di ricevere signoli o multipli datagram e
 * rispondere a ciascuno.
 * Il metodo respond() può essere settato a piacimento a seconda delle necessità.
 */
public abstract class UDPServer implements Runnable {
    private final int bufferSize; //in bytes
    private final int port;
    private volatile boolean isShutDown = false;

    protected UDPServer(int port, int bufferSize) {
        this.bufferSize = bufferSize;
        this.port = port;
    }

    public UDPServer(int port) {
        this(port, 8192);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[bufferSize];
        try (DatagramSocket socket = new DatagramSocket(port)) {
            socket.setSoTimeout(10_000); // check every 10 seconds for shutdown
            while (true) {
                if (isShutDown) return;
                DatagramPacket incoming = new DatagramPacket(buffer,buffer.length);
                try {
                    socket.receive(incoming);
                    this.respond(socket, incoming); // check down
                } catch (SocketTimeoutException ex) {
                    if (isShutDown) return;
                } catch (IOException ex) {
                    System.err.println("IOexception in UDPServer 41");
                }
            }// end while
        } catch (SocketException ex) {
            System.out.println("Non è stato possibile fare il bind sulla porta " + port);
        }
    }

    /**
     * metodo da implementare per inviare le risposte.
     * @param socket sulla quale avviene la communicazione UDP
     * @param packet pacchetto che si è ricevuto
     * @throws IOException
     */
    public abstract void respond(DatagramSocket socket, DatagramPacket packet) throws IOException;

    /**
     * Chiude il server.
     */
    public void shutDown() {
        this.isShutDown = true;
    }

}
