import com.google.gson.Gson;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Classe che riceve i messaggi TCP dal server.
 */
public class BackgroundReceiverTCP extends SwingWorker<String, String> {
    private final ClientGUI gui;
    private Socket socket;
    private DataInputStream input;

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
            // switch dei possibili messaggi
            switch (msgSplit[0]) {
                case "loginerr": // errore durante login
                    loginERR(msgSplit[1]);
                    break;
                case "loginok":
                    gui.loginGUI(msgSplit[1]);
                    break;
                case "amicoerr": // errore nell'aggiungere un amico
                    amicoERR(msgSplit[1]);
                    break;
                case "amicook":
                    gui.resultFriendLabel.setText("Amico aggiunto con successo!");
                    gui.resultFriendLabel.setVisible(true);
                    gui.timerDeleteLabel(gui.resultFriendLabel, 5);
                    gui.lista_amici();
                    break;
                case "aggiunto": // sei stato aggiunto agli amici da un altro utente
                    gui.resultFriendLabel.setText("Sei diventato amico di " + msgSplit[1]);
                    gui.resultFriendLabel.setVisible(true);
                    gui.timerDeleteLabel(gui.resultFriendLabel, 5);
                    gui.lista_amici();
                    break;
                case "listaamici":
                    lista_amici(msgSplit[1]);
                    break;

                case "sfidaerr": // errore durante sfida
                    sfidaERR(msgSplit[1]);
                    break;
                case "sfidato":
                    sfidato(msgSplit[1]);
                    break;
                case "accetta": // hai accettato una sfida
                    accetta(msgSplit[1], msgSplit[2]);
                    break;
                case "rifiuta": // hai rifiutato una sfida
                    gui.statusSfidaLabel.setText("Hai rifiutato la sfida!");
                    gui.statusSfidaLabel.setVisible(true);
                    gui.timerDeleteLabel(gui.statusSfidaLabel, 5);
                    gui.toNormal();
                    break;
                case "accettato": // l'altro giocatore ha accettato
                    accettato(msgSplit[1], msgSplit[2]);
                    break;
                case "rifiutato": // l'altro giocatore ha rifiutato
                    rifiutato(msgSplit[1]);
                    break;
                case "vincitore": // hai vinto l'utima parita
                    // TODO: 25/06/2020 aggiorna classifica
                    gui.statusSfidaLabel.setText("Hai vinto!");
                    gui.statusSfidaLabel.setVisible(true);
                    gui.timerDeleteLabel(gui.statusSfidaLabel, 5);
                    break;
                case "perdente": // hai perso l'ultima partita
                    // TODO: 25/06/2020 aggiorna classifica
                    gui.statusSfidaLabel.setText("Hai perso.");
                    gui.statusSfidaLabel.setVisible(true);
                    gui.timerDeleteLabel(gui.statusSfidaLabel, 5);
                    break;
                case "pareggio": // l'ultima partita è finita in pareggio
                    gui.statusSfidaLabel.setText("Finita in pareggio.");
                    gui.statusSfidaLabel.setVisible(true);
                    gui.timerDeleteLabel(gui.statusSfidaLabel, 5);
                    break;
                case "punteggio":
                    gui.resultFriendLabel.setText(msgSplit[1] + " ha " + msgSplit[2] + " punti.");
                    gui.resultFriendLabel.setVisible(true);
                    gui.timerDeleteLabel(gui.resultFriendLabel, 5);
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
        if (err.equals("0")) {
            // l'amico che si cerca di aggiungere non esiste
            gui.resultFriendLabel.setText("L'utente non esiste!");
        }
        if (err.equals("1")) {
            // la realzione di amicizia esiste già
            gui.resultFriendLabel.setText("Già presente tra gli amici.");
        }
        gui.resultFriendLabel.setVisible(true);
        gui.timerDeleteLabel(gui.resultFriendLabel, 5);
    }

    /**
     * comunica le ragioni per cui una sfida non è stata possibile
     * @param err tipo errore
     */
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
        if (err.equals("4")) {
            // l'utente è impegnato in un'altra partita
            gui.statusSfidaLabel.setText("Utente è in un'altra partita.");
            gui.statusSfidaLabel.setVisible(true);
        }
        gui.timerDeleteLabel(gui.statusSfidaLabel, 5);
    }

    /**
     * Comunica l'arrivo di una sfida da un altro giocatore
     * @param username stringa che indica l'id di che ti ha sfidato
     */
    private void sfidato(String username) {
        System.out.println("sfidato da " + username);
        this.gui.sfidatoGUI(username);
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

    /**
     * Conferma di accettazione della sfida
     * @param sfidante id di colui con il quale farai la sfida
     * @param strPORT stringa della porta alla quale connettersi per iniziare la sfida
     */
    private void accetta(String sfidante, String strPORT) {
        this.gui.statusSfidaLabel.setText("Hai accettato la sfida di " + sfidante);
        this.gui.statusSfidaLabel.setVisible(true);
        this.gui.accettaGUI(sfidante, strPORT);
    }

    /**
     * Colui che hai sfidato ha accettato la tua sfida
     * @param sfidato id del giocatore che ha accettato la tua sfida
     * @param strPORT porta alla quale connettersi per iniziare la sfida
     */
    private void accettato(String sfidato, String strPORT) {
        this.gui.statusSfidaLabel.setText(sfidato + " ha accettato la sfida!");
        this.gui.statusSfidaLabel.setVisible(true);
        this.gui.accettaGUI(sfidato, strPORT);
    }

    /**
     * il giocatore che hai sfidato ha rifiutato la sfida
     * @param sfidato id di chi avevi sfidato
     */
    private void rifiutato(String sfidato) {
        this.gui.statusSfidaLabel.setText(sfidato + " ha rifiutato la sfida!");
        this.gui.statusSfidaLabel.setVisible(true);
        gui.timerDeleteLabel(gui.statusSfidaLabel, 5);
        this.gui.toNormal();
    }

    /**
     * metodo per eseguire lo shutdown del worker
     */
    public void shutdown() {
        this.shutdown = true;
    }
}
