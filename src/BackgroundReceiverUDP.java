import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class BackgroundReceiverUDP extends SwingWorker<String, String> {
    private ClientGUI gui;
    private DatagramSocket socket;

    public BackgroundReceiverUDP(ClientGUI gui, DatagramSocket socket) {
        this.gui = gui;
        this.socket = socket;
    }

    @Override
    protected String doInBackground() {
        System.out.println("UDP working!");

        byte[] buffer = new byte[65507];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                //noinspection CharsetObjectCanBeUsed
                String s = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                System.out.println("UDP server says: " + s);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}