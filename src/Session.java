import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Collection;

public class Session implements Runnable{
    private Server server;
    private Socket socket;

    private DataInputStream input;
    private DataOutputStream output;

    private User user;

    private boolean shutdown = false;

    // stringa usata per accettare o rifiutare sfide
    private String msgOut = null;

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

            this.input = input;
            this.output = output;

            String msg;
            String res;

            do {
                msg = input.readUTF();
                System.out.println("client says: " + msg);
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
                System.out.println("res = " + res);
                output.writeUTF(res);
                if (this.msgOut != null) {
                    System.out.println("sending msg " + this.msgOut);
                    output.writeUTF(this.msgOut);
                    this.msgOut = null;
                }
            } while (!this.shutdown);

        } catch (IOException ex) {
            //ex.printStackTrace();
            logout();
            if (user != null) System.out.println(user.getId() + " si è disconnesso!");
            // TODO: 22/06/2020 close gracefuly
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
     * Nel caso l'utente non avesse mai fatto il login, il metodo non fa alcuna modifica.
     * @return restituisce una stringa per coerenza con gli altri metodi,
     *         ma sul client la connessione viene chiusa al logout quindi il messaggio
     *         non arriva dal server.
     */
    private String logout() {
        if (user != null) {
            this.server.userInfo.removeOnline(this.user.getId());
            this.user = null;
            this.msgOut = null;
            this.shutdown = true;
        }
        return "logout";
    }

    /**
     * crea un arco non orientato tra 2 utenti (il loro username vengono salvati nelle
     * rispettive liste degli amici in caso di successo.
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
     * il server invia un oggetto JSON che rappresenta la lista degli amici.
     * @return stringa in formato JSON
     */
    private String lista_amici() {
        if (this.user != null) {
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            String[] friends = this.user.getFriends().toArray(
                    new String[this.user.getFriends().size()]);

            Gson gson = new Gson();
            String gsonString = gson.toJson(friends);
            return "listaamici_" + gsonString;
        } else {
            return "loginerr_0";
        }
    }

    // TODO: 20/06/2020 sfida
    private String sfida(String username) throws IOException {
        if (this.user == null) return "loginerr_0";
        else {
            if (this.user.getFriends().contains(username)) {
                if (this.server.userInfo.checkOnline(username)) {
                    // TODO: 23/06/2020 invio richiesta sfida
                    Session sessionP2 = this.server.userInfo.getSession(username);
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
     * i metodi seguiti servono per accettare o rifiutare delle partite.
     * player1 è colui che sfida.
     * player2 è lo sfidato.
     * @param player1 stinga che indica l'id dello sfidante.
     */
    public void richiestaSfida(String player1) throws IOException {
        this.output.writeUTF("sfidato_" + player1);
    }
    private String accetta(String player1) throws IOException {
        Session sessionP1 = this.server.userInfo.getSession(player1);
        int port = (int)((Math.random())*((65535 - 1024) + 1)) + 1024;
        sessionP1.accettato(this.user.getId(), port);
        // TODO: 24/06/2020 start game player1 e player2
        startGame(sessionP1, this, port);
        return "accetta_" + player1 + "_" + port;
    }
    public void accettato(String player2, int port) throws IOException {
        this.output.writeUTF("accettato_" + player2 + "_" + port);
    }

    private String rifiuta(String player1) throws IOException {
        Session sessionP1 = this.server.userInfo.getSession(player1);
        sessionP1.rifiutato(this.user.getId());
        return "rifiuta";
    }
    public void rifiutato(String player2) throws IOException {
        this.output.writeUTF("rifiutato_" + player2);
    }

    public void startGame(Session sessionP1, Session sessionP2, int port) {
        UDPGameServer game = new UDPGameServer(sessionP1, sessionP2, port, this.server.dictionary.getWords(5));
        this.server.submitGame(game);

        // TODO: 24/06/2020 creare udpgameserver
        // TODO: 24/06/2020 avviare udpgameserver
        // TODO: 24/06/2020 comunicare ai giocatore la porta sulla quale connettersi
    }

    public String getUserID() {
        return this.user.getId();
    }
}
