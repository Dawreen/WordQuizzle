import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable{
    private Server server;
    private Socket socket;

    public Session(Socket socket, Server server) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Session working!");
        try (DataInputStream input = new DataInputStream(this.socket.getInputStream());
             DataOutputStream output = new DataOutputStream(this.socket.getOutputStream())) {

            String msg = input.readUTF();
            System.out.println("client says: " + msg);
            output.writeUTF(msg);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // TODO: 20/06/2020 registration
        // TODO: 20/06/2020 login
        // TODO: 20/06/2020 logout
        // TODO: 20/06/2020 aggiungi_amico
        // TODO: 20/06/2020 lista_amici
        // TODO: 20/06/2020 sfida
        // TODO: 20/06/2020 mostra_punteggio
        // TODO: 20/06/2020 mostra_classifica
    }
}
