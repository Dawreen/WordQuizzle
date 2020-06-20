import javax.swing.*;
import java.net.DatagramSocket;

public class BackgroundReceiverUDP extends SwingWorker<String, String> {
    private ClientGUI gui;
    private DatagramSocket socket;

    public BackgroundReceiverUDP(ClientGUI gui, DatagramSocket socket) {
        this.gui = gui;
        this.socket = socket;
    }

    @Override
    protected String doInBackground() throws Exception {
        System.out.println("UDP working!");
        return null;
    }
}