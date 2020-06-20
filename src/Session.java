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
        // TODO: 20/06/2020 registration
        // TODO: 20/06/2020 login
        // TODO: 20/06/2020 logout
        // TODO: 20/06/2020 aggiungi_amico
        // TODO: 20/06/2020 lista_amici
        // TODO: 20/06/2020 sfida
        // TODO: 20/06/2020 mostra_punteggio
        // TODO: 20/06/2020 mostra_classifica
        while(true) {
            System.out.println("Session working!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
