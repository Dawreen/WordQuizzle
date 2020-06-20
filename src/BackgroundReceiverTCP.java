import javax.swing.*;
import java.io.DataInputStream;

public class BackgroundReceiverTCP extends SwingWorker<String, String> {
    private ClientGUI gui;
    private DataInputStream input;

    public BackgroundReceiverTCP(ClientGUI gui, DataInputStream input) {
        this.gui = gui;
        this.input = input;
    }

    @Override
    protected String doInBackground() throws Exception {
        System.out.println("TCP working!");
        while(true) {
            String msg = input.readUTF();
            System.out.println("server says: " + msg);
        }
    }
}
