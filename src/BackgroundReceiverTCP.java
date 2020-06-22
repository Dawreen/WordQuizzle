import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Classe che riceve i messaggi TCP dal server.
 */
public class BackgroundReceiverTCP extends SwingWorker<String, String> {
    private ClientGUI gui;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    public BackgroundReceiverTCP(ClientGUI gui, DataInputStream input) {
        this.gui = gui;
        this.input = input;
    }

    @Override
    protected String doInBackground() throws IOException {
        System.out.println("TCP receiver working!");

        do {
            String msg = this.input.readUTF();
            System.out.println("TCP server says: " + msg);
            String[] msgSplit = msg.split("_");
            switch (msgSplit[0]) {
                case "loginerr":
                    loginERR(msgSplit[1]);
                    break;
                case "loginok":
                    gui.loginGUI(msgSplit[1]);
                    break;
                case "amicoerr":
                    amicoERR(msgSplit[1]);
                    break;
                case "amicook":
                    // TODO: 22/06/2020 amico aggiunto con successo
                    break;
            }
        } while (true);
    }

    /**
     * comunica il tipo di errore che si è verificato cercando di aggiungere un nuovo amico
     * @param err stringa che indica il codice di errore
     */
    private void amicoERR(String err) {
        System.out.println("in error amico");
        if (err.equals("0")) {
            // l'amico che si cerca di aggiungere non esiste
            gui.resultFriendLabel.setText("L'utente non esiste!");
        }
        if (err.equals("1")) {
            // la realzione di amicizia esiste già
            gui.resultFriendLabel.setText("Già presente tra gli amici.");
        }
        gui.resultFriendLabel.setVisible(true);
    }

    /**
     * Comunica il tipo di errore che si è verificato al momento del login
     * @param err stringa che indica il tipo di errore.
     */
    private void loginERR(String err) {
        System.out.println("in error login");
        if (err.equals("0")) {
            // quando si cerca di effetuare un'operazione senza aver fatto il login
            gui.resutlLabel.setText("Devi effettuare il login!");
        }
        if (err.equals("1")) {
            gui.resutlLabel.setText("Password login errata!");
        }
        if (err.equals("2")) {
            gui.resutlLabel.setText("Login già effetuato!");
        }
        if (err.equals("3")) {
            gui.resutlLabel.setText("Utente non registrato!");
        }
        gui.resutlLabel.setVisible(true);
    }
}
