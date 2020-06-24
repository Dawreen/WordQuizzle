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
    private int UDP_PORT;

    public static String HOST = "localhost";

    private String username;

    private BackgroundReceiverTCP receiverTCP;
    private BackgroundReceiverUDP receiverUDP;
    private DatagramSocket socketUDP;
    private InetAddress addressUDP;

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
    private JButton testButton;
    private JPanel friendPanel;
    private JTextField amicoTextField;
    private JButton addFriendButton;
    protected JLabel resultFriendLabel;
    protected JList friendList;
    protected JScrollPane allFriendScrollPanel;
    private JButton randomButton1;
    private JButton GameButton;
    private JPanel gamePanel;
    private JButton sfidaButton;
    private JTextField player2TextField;

    protected JLabel statusSfidaLabel;

    protected JLabel udpLabel;
    private JButton sendUDPbotton;
    private JPanel infoPanel;
    private JButton rifiutaButton;
    private JButton accettaButton;

    private JTextField transaltionTextField;
    private JButton sendTradButton;

    private String sfidante = null;

    /**
     * Costruttore dell'intefaccia grafica.
     */
    public ClientGUI () {

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

        testButton.addActionListener(e -> sendTCP("TEST TCP"));

        addFriendButton.addActionListener(e -> {
            String amico = this.amicoTextField.getText();
            aggiungi_amico(amico);
            // TODO: 24/06/2020 non puoi aggiungere te stesso agli amici
        });

        sfidaButton.addActionListener(e -> {
                String player2 = player2TextField.getText();
                sfida(player2);
        });
        sendUDPbotton.addActionListener(e -> sendUDP("TEST UDP"));

        accettaButton.addActionListener(e -> {
            sendTCP("accetta_" + this.sfidante);
        });
        rifiutaButton.addActionListener(e -> {
            sendTCP("rifiuta_" + this.sfidante);
            rifiutaGUI();
        });
        sendTradButton.addActionListener(e -> {
            String trad = transaltionTextField.getText();
            sendUDP(this.username + "_" + this.indexToSend + "_" + trad);
            this.indexToSend++;
        });
    } // fine metodo costruttore ClientGUI

    /**
     * invia un messagio sulla connessione TCP.
     * @param msg stringa che si vuole inviare
     */
    private void sendTCP(String msg) {
        try {
            this.output.writeUTF(msg);
        } catch (IOException | NullPointerException e) {
            this.resutlLabel.setText("Errore di connessione!");
            this.resutlLabel.setVisible(true);
            // e.printStackTrace();
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
            this.resutlLabel.setText("Errore di connessione!");
            this.resutlLabel.setVisible(true);
            // e.printStackTrace();
        }
        // inizializza il ricevitore TCP, è fatto qui per ragione di scoping legato al
        // eventDispatcher di swing
        this.receiverTCP = new BackgroundReceiverTCP(this, this.input);
        this.receiverTCP.execute();
    }

    protected void startUDPconnection() {
        String hostname = "localhost";
        // if server is not on localhost change hostname...
        try {
            InetAddress ia = InetAddress.getByName(hostname);

            this.socketUDP = new DatagramSocket();
            this.addressUDP = ia;

        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }

        this.receiverUDP = new BackgroundReceiverUDP(this, this.socketUDP);
        this.receiverUDP.execute();
    }
    private void sendUDP(String msg) {
        try {
            //noinspection CharsetObjectCanBeUsed
            byte[] data = msg.getBytes("UTF-8");
            DatagramPacket output
                    = new DatagramPacket(data, data.length, this.addressUDP, this.UDP_PORT);
            this.socketUDP.send(output);
        } catch (IOException ex) {
            ex.printStackTrace();
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

    /**
     * Login di un utente già registrato per accedere al servizio.
     * Il server risponde con un codice che può indicare l'avvenuto login, oppure
     * un possibile errore: login già effetuato, password errata.
     * @param username username con il quale loggarsi
     * @param password password dell'account
     */
    private void login(String username, char[] password) {
        if (username.isBlank()) {
            this.resutlLabel.setText("No username!");
            resutlLabel.setVisible(true);
        } else if (password.length == 0){
            this.resutlLabel.setText("No password!");
            this.resutlLabel.setVisible(true);
        } else {
            sendTCP("login_" + username + "_" + Arrays.toString(password));
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
            receiverTCP.shutdown();
        }
    }

    /**
     * Aggiunge un utente alla cerchia degli amici.
     * Il server risponde con un codice di successo o comunicando il tipo di errore che si è verificato.
     * @param amico stringa con l'id dell'utente da aggiungere agli amici.
     */
    private void aggiungi_amico(String amico) {
        if (amico != null) {
            if (!amico.isBlank())
                sendTCP("aggiungiamico_" + amico);
        }
    }

    /**
     * Richiesta al server per visualizzare la lista degli amici.
     * Il server restituisce un oggetto JSON che rappresenta la lista degli amici.
     */
    protected void lista_amici() {
        // TODO: 24/06/2020 aggiornare quando si viene aggiunti da altri
        sendTCP("listaamici");
    }

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
    private void sfida(String player) {
        // TODO: 24/06/2020 username can't be blank or null
        if (player != null) {
            if (!player.isBlank()) {
                sendTCP("sfida_" + player);
                this.statusSfidaLabel.setText("in attesa...");
                this.statusSfidaLabel.setVisible(true);
                sfidaGUI();
            }
        }
    }

    // TODO: 20/06/2020 mostra_punteggio
    /* mostra_punteggio(nickUtente):​ il server restituisce il punteggio di nickUtente (chiamato
       “punteggio utente”) totalizzato in base ai punteggi partita ottenuti in tutte le sfide che ha
       effettuato. */

    // TODO: 20/06/2020 mostra_classifica
    /* mostra_classifica(nickUtente):​ Il server restituisce in formato JSON la classifica calcolata in
       base ai punteggi utente ottenuti da nickUtente e dai suoi amici. */

    public static void main(String[] args) {

            JFrame frame = new ClientGUI();
            frame.setVisible(true);

    } //fine main

    /**
     * Metodo RMI per la registrazione.
     * Il server risponde con un codice di successo se la registrazione è avvenuta o con un messaggio di
     * errore in caso il username sia già in uso o la password sia vuoto.
     * @param name stringa del nome che verà usata per identificare l'utente
     * @param password password che permetterà l'accesso al login
     */
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

        lista_amici();

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

    /**
     * Passaggio al pannello iniziale del login.
     */
    private void logoutGUI() {
        this.accessPanel.setVisible(true);

        this.usernameDisplayLabel.setText("");
        this.usernameDisplayLabel.setVisible(false);
        this.activityPannel.setVisible(false);
        this.setSize(360, 200);
        this.setLocationRelativeTo(null);
    }

    private void sfidaGUI() {
        // TODO: 24/06/2020 won't logout on close (if sfidato is blank...)
        this.player2TextField.setText("");
    }

    public void sfidatoGUI(String sfidante) {
        this.sfidante = sfidante;
        this.statusSfidaLabel.setText("vuoi giocare con " + sfidante + "?");
        this.statusSfidaLabel.setVisible(true);
        this.accettaButton.setVisible(true);
        this.rifiutaButton.setVisible(true);

        this.friendPanel.setVisible(false);
        this.infoPanel.setVisible(false);
        this.sfidaButton.setVisible(false);
        this.player2TextField.setVisible(false);
        this.player2TextField.setText("");
    }

    public void rifiutaGUI() {
        this.sfidante = null;
        this.statusSfidaLabel.setText("");
        this.statusSfidaLabel.setVisible(false);
        this.accettaButton.setVisible(false);
        this.rifiutaButton.setVisible(false);

        this.friendPanel.setVisible(true);
        this.infoPanel.setVisible(true);
        this.sfidaButton.setVisible(true);
        this.player2TextField.setVisible(true);
        this.player2TextField.setText("");
    }
    public void toNormal() {

    }
    private int indexToSend;
    public void accettaGUI(String player, String strPORT) {
        this.statusSfidaLabel.setVisible(true);
        this.accettaButton.setVisible(true);
        this.rifiutaButton.setVisible(true);

        this.friendPanel.setVisible(false);
        this.infoPanel.setVisible(false);
        this.sfidaButton.setVisible(false);
        this.player2TextField.setVisible(false);
        this.player2TextField.setText("");

        this.statusSfidaLabel.setText("inizia la sfida con " + player + " sulla porta = " + strPORT);

        this.accettaButton.setVisible(false);
        this.rifiutaButton.setVisible(false);

        this.UDP_PORT = Integer.parseInt(strPORT);

        startUDPconnection();

        this.indexToSend = 0;
        sendUDP(this.username + "_" + indexToSend + "_randomWord");
        this.indexToSend++;
        // TODO: 24/06/2020 partita

        this.transaltionTextField.setVisible(true);
        this.sendTradButton.setVisible(true);

        // TODO: 24/06/2020 label words to translate
        // TODO: 24/06/2020 textfield translation
        // TODO: 24/06/2020 button send translation
    }
}
