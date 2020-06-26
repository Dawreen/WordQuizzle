import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;

/**
 * Classe che riceve i messaggi UDP dal server.
 * Viene usata per svolgere le partite, il suo compito è mostrarre le parole da tradurre.
 */
public class BackgroundReceiverUDP extends SwingWorker<String, String> {
    private ClientGUI gui;
    private DatagramSocket socket;
    private DatagramPacket lastPacket;

    public BackgroundReceiverUDP(ClientGUI gui, DatagramSocket socket) {
        this.gui = gui;
        this.socket = socket;
    }

    @Override
    protected String doInBackground() {
        System.out.println("UDP started!");
        Timestamp startTime = null;
        Timestamp finishTime;

        byte[] buffer = new byte[65507];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                if (startTime == null) {
                    startTime = new Timestamp(System.currentTimeMillis());
                }
                //noinspection CharsetObjectCanBeUsed
                String s = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                System.out.println("parola ricevuta: " + s);
                if (s.equals("FINE")) { // ultimo messaggio che indica che le parole da tradurre sono finite
                    finishTime = new Timestamp(System.currentTimeMillis());
                    long time = finishTime.getTime() - startTime.getTime();
                    gui.sendTime(time);
                    return null;
                }
                if(s.equals("FINEtimeout")) { // messaggio di timeOut della partita
                    gui.sendTime(UDPGameServer.T2);
                    return null;
                }
                // udpLabel è dove vengono mostrate le parole sulla GUI
                gui.udpLabel.setText(s);
                gui.udpLabel.setVisible(true);
            } catch (SocketTimeoutException ex) {
                /* nel caso non si sia avuta una risposta dal server
                 si invia nuovamente l'ultimo pacchetto. */
                try {
                    socket.send(lastPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setLastMessage(DatagramPacket packet) {
        this.lastPacket = packet;
    }

    /**
     * Quando la partita termina (si riceve la parola FINE) si riporta la GUI allo stato "normale".
     */
    public void done() {
        this.gui.toNormal();
    }
}