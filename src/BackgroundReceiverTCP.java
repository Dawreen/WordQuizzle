import com.google.gson.Gson;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Classe che riceve i messaggi TCP dal server.
 */
public class BackgroundReceiverTCP extends SwingWorker<String, String> {
    private final ClientGUI gui;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private boolean shutdown = false;

    public BackgroundReceiverTCP(ClientGUI gui, DataInputStream input) {
        this.gui = gui;
        this.input = input;
    }

    @Override
    protected String doInBackground() throws IOException {
        System.out.println("TCP receiver started!");

        do {
            String msg = this.input.readUTF();
            System.out.println("TCP server says: " + msg);
            String[] msgSplit = msg.split("_");
            switch (msgSplit[0]) {
                case "loginerr":
                    loginERR(msgSplit[1]);
                    break;
                case "loginok":
                    gui.loginGUI(msgSplit[1], msgSplit[2]);
                    break;
                case "amicoerr":
                    amicoERR(msgSplit[1]);
                    break;
                case "amicook":
                    gui.resultFriendLabel.setText("Amico aggiunto con successo!");
                    gui.resultFriendLabel.setVisible(true);
                    gui.lista_amici();
                    break;
                case "listaamici":
                    lista_amici(msgSplit[1]);
                    break;

                case "sfidaerr":
                    sfidaERR(msgSplit[1]);
                    break;
                case "sfidato":
                    sfidato(msgSplit[1]);
                    break;
                case "richiestaout":
                    // TODO: 24/06/2020 inizio timer T1
                    attesaRisposta();
                    break;
                case "accetta": // hai accettato una sfida
                    // TODO: 24/06/2020 inizia sfida
                    accetta(msgSplit[1]);
                    break;
                case "rifiuta": // hai rifiutato una sfida
                    // TODO: 24/06/2020 hai rifiutato con successo
                    rifiutato(msgSplit[1]);
                    break;
                case "accettato": // l'altro giocatore ha accettato
                    // TODO: 24/06/2020 inizia game con msgSplit[1]
                    accettato(msgSplit[1]);
                    break;
                case "rifiutato": // l'altro giocatore ha rifiutato
                    // TODO: 24/06/2020 lo sfidato ha rifiutato
                    rifiutato(msgSplit[1]);
                    break;
            }
        } while (!shutdown);
        return null;
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

    private void sfidaERR(String err) {
        System.out.println("in error sfida");
        if (err.equals("1")) {
            // l'utente sfidato non fa parte della cerchia degli amici
            gui.statusSfidaLabel.setText("Utente non è tra amici!");
            gui.statusSfidaLabel.setVisible(true);
        }
        if (err.equals("2")) {
            // l'utente che si vuole sfidare è offline
            gui.statusSfidaLabel.setText("Utente offline!");
            gui.statusSfidaLabel.setVisible(true);
        }
        if (err.equals("3")) {
            // l'utente ha rifiutato la sfida
            gui.statusSfidaLabel.setText("Utente ha rifiutato la sfida!");
            gui.statusSfidaLabel.setVisible(true);
        }
    }
    private void sfidato(String username) {
        System.out.println("sfidato da " + username);
        this.gui.statusSfidaLabel.setText("sfidato da " + username);
        this.gui.statusSfidaLabel.setVisible(true);
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

    /**
     * Decodifica una stringa in formato JSON contetente la lista degli amici in arrivo dal server
     * @param allAmici stringa da decodificare.
     */
    private void lista_amici(String allAmici) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        String[] arrFriends = new Gson().fromJson(allAmici, String[].class);
        for (String each : arrFriends) {
            listModel.addElement(each);
        }
        gui.friendList = new JList();
        gui.friendList.setModel(listModel);
        gui.allFriendScrollPanel.setViewportView(gui.friendList);
    }

    private void attesaRisposta() {
        this.gui.statusSfidaLabel.setText("in attesa di risposta...");
        this.gui.statusSfidaLabel.setVisible(true);
        // TODO: 24/06/2020 start T1 timer
    }
    private void accetta(String sfidante) {
        this.gui.statusSfidaLabel.setText("Hai accettato la sfida di " + sfidante);
        this.gui.statusSfidaLabel.setVisible(true);
    }
    private void accettato(String sfidato) {
        this.gui.statusSfidaLabel.setText(sfidato + " ha accettato la sfida!");
        this.gui.statusSfidaLabel.setVisible(true);
    }
    private void rifiuta(String sfidante) {
        this.gui.statusSfidaLabel.setText("Hai rifiutato la sfida di " + sfidante);
        this.gui.statusSfidaLabel.setVisible(true);
    }
    private void rifiutato(String sfidato) {
        this.gui.statusSfidaLabel.setText(sfidato + " ha rifiutato la sfida!");
        this.gui.statusSfidaLabel.setVisible(true);
    }

    public void shutdown() {
        this.shutdown = true;
    }
}
