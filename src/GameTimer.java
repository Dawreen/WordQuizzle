import jdk.jfr.Timestamp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.TimerTask;

/**
 * Classe che viene richiamata dal timer della partita.
 * Invia al cliente il messaggio di fine partita per timeOut.
 */
public class GameTimer extends TimerTask {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    /**
     * Costruttore.
     * @param socket Datagramsocket sulla quale mandare il messaggio
     * @param address InetAddress per l'invio del datagramPacket
     * @param port porta sulla quale si stasvolgendo la partita (sempre per l'invio del pacchetto).
     */
    public GameTimer(DatagramSocket socket, InetAddress address, int port) {
        this.socket = socket;
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {
        byte[] data = "FINEtimeout".getBytes();
        DatagramPacket out = new DatagramPacket(data, data.length, this.address, this.port);
        try {
            socket.send(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
