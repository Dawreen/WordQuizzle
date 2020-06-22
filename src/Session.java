import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable{
    private Server server;
    private Socket socket;

    private User user;

    private boolean shutdown = false;

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
                    case "lista_amici" -> lista_amici();
                    case "sfida" -> sfida();
                    case "mostra_punteggio" -> mostra_punteggio();
                    case "mostra_classifica" -> mostra_classifica();
                    default -> msg;
                };
                System.out.println("res = " + res);
                output.writeUTF(res);
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
    // TODO: 20/06/2020 lista_amici
    private String lista_amici() {
        /*
        il server restituisce un oggetto JSON che rappresenta la lista degli amici.
         */
        return "lista_amici";
    }
    // TODO: 20/06/2020 sfida
    private String sfida() {
        return "sfida";
    }
    // TODO: 20/06/2020 mostra_punteggio
    private String mostra_punteggio() {
        return "mostra_punteggio";
    }
    // TODO: 20/06/2020 mostra_classifica
    private String mostra_classifica() {
        return "mostra_classifica";
    }
}
