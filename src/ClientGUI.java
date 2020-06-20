import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientGUI extends JFrame {
    //enables connections
    private static final String SERVER_ADDRESS = "127.0.0.1";
    //TCP connection
    private static final int SERVER_PORT = 34522;
    private DataInputStream input;
    private DataOutputStream output;
    //UDP connection
    private static final int UDP_PORT = 7;
    private DatagramSocket socketUDP;
    private InetAddress address;



    public static String HOST = "localhost";
    private JPanel mainPanel;

    public ClientGUI (DataInputStream input, DataOutputStream output,
                      DatagramSocket socketUDP, InetAddress address) {

        //creating the JFrame
        super("Word Quizzle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //initializing connections
        this.input = input;
        this.output = output;
        this.socketUDP = socketUDP;
        this.address = address;

        //threads that receive and decode messages from server
        BackgroundReceiverTCP receiverTCP = new BackgroundReceiverTCP(this, input);
        receiverTCP.execute();
        BackgroundReceiverUDP receiverUDP = new BackgroundReceiverUDP(this, socketUDP);
        receiverUDP.execute();

        sendTCP("test");
        sendUDP("test UDP");

        // TODO: 20/06/2020 registration
        // TODO: 20/06/2020 login
        // TODO: 20/06/2020 logout
        // TODO: 20/06/2020 aggiungi_amico
        // TODO: 20/06/2020 lista_amici
        // TODO: 20/06/2020 sfida
        // TODO: 20/06/2020 mostra_punteggio
        // TODO: 20/06/2020 mostra_classifica
    }

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

            InetAddress address = InetAddress.getByName(SERVER_ADDRESS);
            DatagramSocket socketUDP = new DatagramSocket();
            socketUDP.connect(address, UDP_PORT);

            JFrame frame = new ClientGUI(input, output, socketUDP, address);
            frame.setVisible(true);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendTCP(String msg) {
        try {
            output.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendUDP(String msg) {
        try {
            //noinspection CharsetObjectCanBeUsed
            byte[] data = msg.getBytes("UTF-8");
            DatagramPacket output = new DatagramPacket(data, data.length, address, UDP_PORT);
            socketUDP.send(output);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void registration(String name, String password) {
        if (name.isBlank() || password.isBlank()) {
// TODO: 20/06/2020 if name and password blank
            return;
        }
        try {
            RegIntWQ stub;
            Remote RemoteObj;
            Registry registry = LocateRegistry.getRegistry(HOST);
            RemoteObj = registry.lookup("Registration");
            stub = (RegIntWQ) RemoteObj;
            boolean response = stub.registration(name, password);
            //noinspection StatementWithEmptyBody
            if (response) {
// TODO: 20/06/2020 if registration TRUE
            } else {
// TODO: 20/06/2020 if registration FALSE
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
