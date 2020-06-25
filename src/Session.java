import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

/**
 * Classe che corrisponde ad un Client e permette la comunicazione con il server.
 */
public class Session implements Runnable{
    private final Server server; // istanza del server
    private final Socket socket; // connessione TCP

    private DataOutputStream output; // invio comunicazioni al client

    private User user; // istanza dell'utente

    private boolean shutdown = false;

    /**
     * Costruttore  della classe.
     * Inizialmente senza un utente (aggiunto al login)
     * @param socket che permette la comunicazione con il client
     * @param server istanza del server per accedere ai dati
     */
    public Session(Socket socket, Server server) {
        this.server = server;
        this.socket = socket;
        this.user = null;
    }

    @Override
    public void run() {
        System.out.println("Session working!");
        try (DataInputStream input = new DataInputStream(this.socket.getInputStream());
             DataOutputStream output = new DataOutputStream(this.socket.getOutputStream())) {

            this.output = output;

            String msg;
            String res;

            do { // ciclo while nel quale si ricevono comunicazioni dal server
                msg = input.readUTF();
                String[] msgSplit = msg.split("_");
                res = switch (msgSplit[0]) {
                    case "login" -> login(msgSplit[1], msgSplit[2]);
                    case "logout" -> logout();
                    case "aggiungiamico" -> aggiungi_amico(msgSplit[1]);
                    case "listaamici" -> lista_amici();
                    case "sfida" -> sfida(msgSplit[1]);

                    case "accetta" -> accetta(msgSplit[1]);
                    case "rifiuta" -> rifiuta(msgSplit[1]);

                    case "mostra_punteggio" -> mostra_punteggio();
                    case "mostra_classifica" -> mostra_classifica();

                    default -> msg;
                };
                output.writeUTF(res); // risposta da inviare al client
            } while (!this.shutdown);

        } catch (IOException ex) {
            //ex.printStackTrace();
            logout(); // in caso di disconnessione si fa il logout
            if (user != null) System.out.println(user.getId() + " si è disconnesso!");
        }

    } // fine metodo run

    /**
     * Metodo che effetua il login sul server di un utente che si è registrato in precedenza.
     * @param username stringa id dell'account con il quale si vuole fare il login
     * @param password stringa contenente la password corrispondente all'account
     * @return stringa che comunica la riuscita o meno del login.
     */
    private String login(String username, String password) {
        if (this.user != null) {
            return "loginerr_2"; // login gìà fatto in questa sessione
        } else {
            if (this.server.userInfo.checkRegistration(username)) {
                if (this.server.userInfo.checkOnline(username)) {
                    // login già effetuato in un'altra sessione
                    return "loginerr_2";
                } else {
                    if ((this.user = this.server.userInfo.checkPass(username, password)) == null) {
                        // password errata
                        return "loginerr_1";
                    } else {
                        // login con successo
                        this.server.userInfo.addOnline(username, this);

                        return "loginok_" + this.user.getId();
                    }
                }
            } else {
                // utente non registrato
                return "loginerr_3";
            }
        }
    } // fine login

    /**
     * Metodo che indica al server che un utente non è più online.
     * Dato che la connessione viene chiusa la session viene terminata.
     *
     * @return restituisce una stringa per coerenza con gli altri metodi,
     *         ma sul client la connessione viene chiusa al logout quindi il messaggio
     *         non arriva dal server.
     */
    private String logout() {
        if (user != null) {
            this.server.userInfo.removeOnline(this.user.getId()); // rimozione dell'utente dagli user online
            this.user = null;
            this.shutdown = true; // terminazione di session
        }
        return "logout";
    }

    /**
     * crea un arco non orientato tra 2 utenti (il loro username vengono salvati nelle
     * rispettive liste degli amici in caso di successo).
     * @param amico l'id utente di chi si vuole aggiungere agli amici
     * @return stringa che comunica l'esito dell'operazione
     */
    private String aggiungi_amico(String amico) {
        if (this.user != null) {
            // è necessario fare il login per aggiungere un amico
            if (this.user.getFriends().contains(amico)) {
                // la relazione di amicizia esiste già
                return "amicoerr_1";
            } else {
                // relazione di amicizia non ancora creata
                if (this.server.userInfo.checkRegistration(amico)) {
                    // l'utente che si vuole aggiungere esiste
                    this.server.userInfo.aggiungiAmicizia(this.user.getId(), amico);

                    if (this.server.userInfo.checkOnline(amico)) {
                        Session sessionAmico = this.server.userInfo.getSession(amico);
                        sessionAmico.aggiuntoDa(this.user.getId());
                    }
                    return "amicook";
                } else {
                    return "amicoerr_0";
                }
            }
        } else {
            return "loginerr_0"; // non entra mai qua
        }
    }

    /**
     * Metodo che notifica al client l'essere stato aggiunto da un altro utente
     * @param amico utente dal quale si è stati aggiunti
     */
    public void aggiuntoDa(String amico) {
        try {
            this.output.writeUTF("aggiunto_" + amico);
        } catch (IOException e) {
        }
    }

    /**
     * il server invia un oggetto JSON che rappresenta la lista degli amici.
     * @return stringa in formato JSON
     */
    public String lista_amici() {
        if (this.user != null) {
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            String[] friends = this.user.getFriends().toArray(
                    new String[this.user.getFriends().size()]);

            Gson gson = new Gson(); // codifica in formato JSON
            String gsonString = gson.toJson(friends);
            return "listaamici_" + gsonString;
        } else {
            return "loginerr_0";
        }
    }

    /**
     * Invio di sfida ad un altro utente.
     * @param player2 giocatore che si vuole sfidare
     * @return una stringa che comunica se è stato o meno possibile sfidare
     *         l'altro utente.
     * @throws IOException lanciata dal metodo richiestaSfida
     */
    private String sfida(String player2) throws IOException {
        if (this.user == null) return "loginerr_0";
        else {
            if (this.user.getFriends().contains(player2)) {
                if (this.server.userInfo.checkOnline(player2)) {
                    Session sessionP2 = this.server.userInfo.getSession(player2);
                    sessionP2.richiestaSfida(this.user.getId());

                    return "richiestaout";
                } else {
                    // l'utente che si vuole sfidare è offline
                    return "sfidaerr_2";
                }
            } else {
                // l'utente che si vuole sfidare non fa parte della cerchia degli amici
                return "sfidaerr_1";
            }
        }
    }
    // TODO: 20/06/2020 mostra_punteggio
    private String mostra_punteggio() {
        return "mostra_punteggio";
    }
    // TODO: 20/06/2020 mostra_classifica
    private String mostra_classifica() {
        return "mostra_classifica";
    }


    /**
     * Invio al client la richiesta di sfida da parte di un utente.
     * @param player1 stinga che indica l'id dello sfidante.
     * @throws IOException newl caso ci sia stato un errore nell'invio.
     */
    public void richiestaSfida(String player1) throws IOException {
        this.output.writeUTF("sfidato_" + player1);
    }

    /**
     * Comunica l'accetazione di una sfida da parte del client
     * @param player1 giocatore sfidante al quale comunicare l'avvenuta accettazione
     * @return stringa contente l'id dello sfidante e la porta sulla quale settare
     *         la connessione UDP della partita
     * @throws IOException lanciata dal metodo accettato()
     */
    private String accetta(String player1) throws IOException {
        Session sessionP1 = this.server.userInfo.getSession(player1);
        int port = (int)((Math.random())*((65535 - 1024) + 1)) + 1024;
        sessionP1.accettato(this.user.getId(), port);
        startGame(sessionP1, this, port);
        return "accetta_" + player1 + "_" + port;
    }

    /**
     * Il giocatore al quale si è inviata la richiesta di sfida (player2) ha accettato la sfida
     * @param player2 id dello sfidato
     * @param port porta sulla quale si dovrà settare la connessione UDP
     * @throws IOException in caso di errore di connessione del metodo write()
     */
    public void accettato(String player2, int port) throws IOException {
        this.output.writeUTF("accettato_" + player2 + "_" + port);
    }

    /**
     * Comunica il rifiuto di una sfida da parte del client.
     * @param player1 giocatore sfidante al quale comunicare l'avvenuto rifiuto
     * @return stringa di conferma dell'avvenuto rifiuto
     * @throws IOException lanciata dal metodo rifiutato()
     */
    private String rifiuta(String player1) throws IOException {
        Session sessionP1 = this.server.userInfo.getSession(player1);
        sessionP1.rifiutato(this.user.getId());
        return "rifiuta";
    }

    /**
     * Il giocatore al quale si è inviata la richiesta di sfida (player2) ha rifiutato la sfida.
     * @param player2 id dello sfidato
     * @throws IOException in caso di errore di connessione del metodo write()
     */
    public void rifiutato(String player2) throws IOException {
        this.output.writeUTF("rifiutato_" + player2);
    }

    /**
     * Avvio del lato server della partita
     * @param sessionP1 istanza della session del giocatore 1
     * @param sessionP2 istanza della session del giocatore 2
     * @param port porta sulla quale verrà settata la connessione UDP
     */
    public void startGame(Session sessionP1, Session sessionP2, int port) {
        UDPGameServer game = new UDPGameServer(sessionP1, sessionP2, port, this.server.dictionary.getWords(5));
        this.server.submitGame(game);
    }

    /**
     * Comunica al client di aver vinto la sfida.
     * Il metodo inoltre aggiorna il punteggio su file
     * @throws IOException in caso di errore di connessione del metodo write()
     */
    public void vincitore() throws IOException {
        this.output.writeUTF("vincitore");
        this.user.addPoints();
        this.server.userInfo.updateFile();
        // TODO: 25/06/2020 aggioranre punteggio tutti quelli online?
    }

    /**
     * Comunica al client di aver perso la sfida.
     * @throws IOException in caso di errore di connessione del metodo write()
     */
    public void perdente() throws IOException {
        this.output.writeUTF("perdente");
    }

    /**
     * Comunica al client che la partita è finita in pareggio.
     * @throws IOException in caso di errore di connessione del metodo write()
     */
    public void pareggio() throws IOException {
        this.output.writeUTF("pareggio");
    }

    /**
     * Getter dell'id utente.
     * @return L'id dell'utente con il quale si è fatto login nella session.
     */
    public String getUserID() {
        return this.user.getId();
    }
}
