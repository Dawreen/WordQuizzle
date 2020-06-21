import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class ClientGUI extends JFrame {
    //enables connections
    private static final String SERVER_ADDRESS = "127.0.0.1";
    //TCP connection
    private static final int SERVER_PORT = 34522;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    //UDP connection
    private static final int UDP_PORT = 7;
    private DatagramSocket socketUDP;
    private InetAddress address;



    public static String HOST = "localhost";
    private JPanel mainPanel;
    private JPanel accessPanel;
    private JLabel usernameLabel;
    private JTextField usernameTextField;
    private JPasswordField passwordField;
    private JLabel passwordLabel;
    private JButton registrationButton;
    private JButton loginButton;
    private JLabel resutlLabel;

    public ClientGUI (DatagramSocket socketUDP, InetAddress address) {

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
        this.socketUDP = socketUDP;
        this.address = address;

        //threads that receive and decode messages from server
        BackgroundReceiverTCP receiverTCP = new BackgroundReceiverTCP(this, input);
        receiverTCP.execute();
        BackgroundReceiverUDP receiverUDP = new BackgroundReceiverUDP(this, socketUDP);
        receiverUDP.execute();

        sendUDP("test UDP");
        registrationButton.addActionListener(e -> {
            String username = usernameTextField.getText();
            char[] password = passwordField.getPassword();

            registration(username, password);
        });
        loginButton.addActionListener(e -> {
            String username = usernameTextField.getText();
            char[] password = passwordField.getPassword();
            startTCPconnection();
            login(username, password);
            System.out.println("BUTTON - is socket close? " + socket.isClosed());
        });
        System.out.println("HERE");
    }

    private void sendTCP(String msg) {
        System.out.println("SENDTCP - is socket close? " + this.socket.isClosed());
        try {
            this.output.writeUTF(msg);
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

    private void startTCPconnection() {
        try {
            this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void closeTCPConnection() {
        try {
            this.socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // TODO: 20/06/2020 login
    private void login(String username, char[] password) {
        sendTCP("login_" + username + "_" + Arrays.toString(password));
    }
    // TODO: 20/06/2020 logout
    // TODO: 20/06/2020 aggiungi_amico
    // TODO: 20/06/2020 lista_amici
    // TODO: 20/06/2020 sfida
    // TODO: 20/06/2020 mostra_punteggio
    // TODO: 20/06/2020 mostra_classifica

    public static void main(String[] args) throws IOException {


            InetAddress address = InetAddress.getByName(SERVER_ADDRESS);
            DatagramSocket socketUDP = new DatagramSocket();
            socketUDP.connect(address, UDP_PORT);

            JFrame frame = new ClientGUI(socketUDP, address);
            frame.setVisible(true);

    }

    private void registration(String name, char[] password) {
        this.resutlLabel.setVisible(true);
        if (name.isBlank() || password.length == 0) {
            this.resutlLabel.setText("i campi username e password non possono essere vuoti!");
            return;
        }
        try {
            RegIntWQ stub;
            Remote RemoteObj;
            Registry registry = LocateRegistry.getRegistry(HOST);
            RemoteObj = registry.lookup("Registration");
            stub = (RegIntWQ) RemoteObj;
            boolean response = stub.registration(name, password);
            if (response) {
                this.resutlLabel.setText("Registrazione avvenuta con successo!");
            } else {
                this.resutlLabel.setText("Registrazione fallita!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // TODO: 20/06/2020 timer on resultLabel
    }
}
