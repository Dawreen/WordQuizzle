import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable{
    private Server server;
    private Socket socket;

    private boolean shutdown = false;

    public Session(Socket socket, Server server) {
        this.server = server;
        this.socket = socket;
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
                    case "login" -> login(msgSplit[1], msgSplit[2].toCharArray());
                    case "logout" -> logout();
                    case "aggiungi_amico" -> aggiungi_amico();
                    case "lista_amici" -> lista_amici();
                    case "sfida" -> sfida();
                    case "mostra_punteggio" -> mostra_punteggio();
                    case "mostra_classifica" -> mostra_classifica();
                    default -> msg;
                };
                output.writeUTF(res);
            } while (this.shutdown);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    // TODO: 20/06/2020 registration
    // TODO: 20/06/2020 login
    private String login(String username, char[] password) {
        this.server.login(username, password);
        return "login";
    }
    // TODO: 20/06/2020 logout
    private String logout() {
        return "logout";
    }
    // TODO: 20/06/2020 aggiungi_amico
    private String aggiungi_amico() {
        return "aggiungi_amico";
    }
    // TODO: 20/06/2020 lista_amici
    private String lista_amici() {
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
