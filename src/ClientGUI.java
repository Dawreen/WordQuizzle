import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

    private String username;

    // componenti intefaccia grafica
    private JPanel mainPanel;
    private JPanel accessPanel;
    private JLabel usernameLabel;
    private JTextField usernameTextField;
    private JPasswordField passwordField;
    private JLabel passwordLabel;
    private JButton registrationButton;
    private JButton loginButton;

    // viene usata per mostrare il risulatato di registrazione e login (in caso di errore)
    protected JLabel resutlLabel;

    private JLabel usernameDisplayLabel;
    private JPanel activityPannel;
    private JPanel buttonsPanelFirst;
    private JButton logoutButton;
    private JButton randomButton;
    private JPanel friendPanel;
    private JTextField amicoTextField;
    private JButton addFriendButton;
    protected JLabel resultFriendLabel;

    /**
     * Costruttore dell'intefaccia grafica.
     * @param socketUDP DatagramSocket per far funzionare le connessioni UDP
     * @param address InetAddress per cominicazioni sulla connessione UDP
     */
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

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                logout();
            }
        });

        //initializing connections
        this.socketUDP = socketUDP;
        this.address = address;

        //threads that receive and decode messages from server
        BackgroundReceiverUDP receiverUDP = new BackgroundReceiverUDP(this, this.socketUDP);
        receiverUDP.execute();

        // invoca metodo RMI
        registrationButton.addActionListener(e -> {
            String username = usernameTextField.getText();
            char[] password = passwordField.getPassword();
            registration(username, password);
        });

        // invoca metodo con connessione TCP
        loginButton.addActionListener(e -> {
            String username = usernameTextField.getText();
            char[] password = passwordField.getPassword();
            startTCPconnection();
            login(username, password);
        });

        logoutButton.addActionListener(e -> logout());

        randomButton.addActionListener(e -> sendTCP("TEST!"));

        addFriendButton.addActionListener(e -> {
            String amico = this.amicoTextField.getText();
            aggiungi_amico(amico);
        });
    } // fine metodo costruttore ClientGUI

    /**
     * invia un messagio sulla connessione TCP.
     * @param msg stringa che si vuole inviare
     */
    private void sendTCP(String msg) {
        try {
            this.output.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * invia un messaggio sulla connessione UDP
     * @param msg stringa che si vuole inviare
     */
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

    /**
     * inizializza la connessione TCP, aprendo la socket.
     */
    private void startTCPconnection() {
        try {
            this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: 21/06/2020 set message if error
        }
    }

    /**
     * chiude la connessione TCP, chiudendo la socket
     */
    private void closeTCPConnection() {
        try {
            this.socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // TODO: 20/06/2020 login
    /*Login di un utente​ già registrato per accedere al servizio. Il server
    risponde con un codice che può indicare l’avvenuto login, oppure, se l’utente ha già effettuato la
    login o la password è errata, restituisce un messaggio d’errore. */
    private void login(String username, char[] password) {
        if (username.isBlank()) {
            this.resutlLabel.setText("No username!");
            resutlLabel.setVisible(true);
        } else if (password.length == 0){
            this.resutlLabel.setText("No password!");
            this.resutlLabel.setVisible(true);
        } else {
            sendTCP("login_" + username + "_" + Arrays.toString(password));
            // inizializza il ricevitore TCP, è fatto qui per ragione di scoping legato al
            // eventDispatcher di swing
            BackgroundReceiverTCP receiverTCP = new BackgroundReceiverTCP(this, this.input);
            receiverTCP.execute();
        }
    }

    /**
     * effettua il logout dell’utente dal servizio
     */
    private void logout() {
        if (this.username != null) {
            this.username = null;
            sendTCP("logout");
            closeTCPConnection();
            logoutGUI();
        }
    }

    // TODO: 20/06/2020 aggiungi_amico
    /* cerchia di amici di un utente. Viene creato un arco non orientato tra i due utenti (se A è amico
       di B, B è amico di A). Il Server risponde con un codice che indica l’avvenuta registrazione
       dell’amicizia oppure con un codice di errore, se il nickname del nodo destinazione/sorgente
       della richiesta non esiste, oppure se è stato richiesto di creare una relazione di amicizia già
       esistente. Non è necessario che il server richieda l’accettazione dell’amicizia da parte di nickAmico.*/
    private void aggiungi_amico(String amico) {
        if (amico != null) {
            if (!amico.isBlank())
                sendTCP("aggiungiamico_" + amico);
        }
    }

    // TODO: 20/06/2020 lista_amici
    /*utilizzata da​ un utente per visualizzare la lista dei propri amici, fornendo
    le proprie generalità. Il server restituisce un oggetto JSON che rappresenta la lista degli  amici.*/

    // TODO: 20/06/2020 sfida
    /* sfida(nickUtente, nickAmico): l’utente nickUtente intende sfidare l’utente di nome nickAmico. Il
       server controlla che nickAmico appartenga alla lista di amicizie di nickUtente, in caso negativo
       restituisce un codice di errore e l’operazione termina. In caso positivo, il server invia a
       nickAmico ​ una richiesta di accettazione della sfida e, solo dopo che la richiesta è stata
       accettata, la sfida può avere inizio (se la risposta non è stata ricevuta entro un intervallo di
       tempo T1 si considera la sfida come non accettata). La sfida riguarda la traduzione di una lista
       di parole italiane in parole inglesi, nel minimo tempo possibile. Il server sceglie, in modo casuale,
       K parole da un dizionario contenente N parole italiane da inviare successivamente, una alla volta,
       ai due sfidanti. La partita può durare al massimo un intervallo di tempo T2. Il server invia ai
       partecipanti la prima parola. Quando il giocatore invia la traduzione (giusta o sbagliata), il server
       invia la parola successiva a quel giocatore. Il gioco termina quando entrambi i giocatori hanno
       inviato le traduzioni alle K parole o quando scade il timer.  La correttezza della traduzione viene
       controllata dal server utilizzando un servizio esterno, come specificato nella sezione seguente.
       Ogni traduzione corretta assegna X punti al giocatore; ogni traduzione sbagliata assegna Y punti
       negativi; il giocatore con più punti vince la sfida ed ottiene Z punti extra. Per ogni risposta non
       inviata (a causa della scadenza del timer) si assegnano 0 punti. Il punteggio ottenuto da ciascun
       partecipante alla fine della partita viene chiamato punteggio partita.  I valori espressi come K,
       N, T1, T2, X, Y e Z sono a discrezione dello studente. */

    // TODO: 20/06/2020 mostra_punteggio
    /* mostra_punteggio(nickUtente):​ il server restituisce il punteggio di nickUtente (chiamato
       “punteggio utente”) totalizzato in base ai punteggi partita ottenuti in tutte le sfide che ha
       effettuato. */

    // TODO: 20/06/2020 mostra_classifica
    /* mostra_classifica(nickUtente):​ Il server restituisce in formato JSON la classifica calcolata in
       base ai punteggi utente ottenuti da nickUtente e dai suoi amici. */

    public static void main(String[] args) throws IOException {

            InetAddress address = InetAddress.getByName(SERVER_ADDRESS);
            DatagramSocket socketUDP = new DatagramSocket();
            socketUDP.connect(address, UDP_PORT);

            JFrame frame = new ClientGUI(socketUDP, address);
            frame.setVisible(true);

    } //fine main

    /**
     * metodo RMI per la registrazione
     * @param name stringa del nome che verà usata per identificare l'utente
     * @param password password che permetterà l'accesso al login
     */
    /*Registrazione di un utente ​ : per inserire un nuovo utente, il server mette a disposizione una
      operazione ​registra_utente(nickUtente,password). ​ Il server risponde con un codice che può
      indicare l’avvenuta registrazione, oppure, se il nickname è già presente, o se la password è
      vuota, restituisce un messaggio d’errore. Come specificato in seguito, le registrazioni sono tra
      le informazioni da persistere. */
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
            int response = stub.registration(name, Arrays.toString(password));
            if (response == 0) {
                this.resutlLabel.setText("Registrazione avvenuta con successo!");
            } else if (response == 2){
                this.resutlLabel.setText("Username già in uso!");
            } else { // response == 2
                this.resutlLabel.setText("Errore!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.resutlLabel.setText("Errore da server!");
        }
        // TODO: 20/06/2020 timer on resultLabel
    } // fine registration

    /**
     * Il metodo modifica la GUI in modo da passare ad un nuovo pannello
     * @param username stringa che contiene il username con il quale si è fatto login
     */
    public void loginGUI(String username) {
        this.username = username;

        this.usernameTextField.setText("");
        this.passwordField.setText("");
        this.accessPanel.setVisible(false);
        this.resutlLabel.setVisible(false);

        this.usernameDisplayLabel.setText(username);
        this.usernameDisplayLabel.setVisible(true);
        this.setSize(480, 480);
        this.setLocationRelativeTo(null);
        this.activityPannel.setVisible(true);
    }
    private void logoutGUI() {
        this.accessPanel.setVisible(true);

        this.usernameDisplayLabel.setText("");
        this.usernameDisplayLabel.setVisible(false);
        this.activityPannel.setVisible(false);
        this.setSize(360, 200);
        this.setLocationRelativeTo(null);
    }
}
