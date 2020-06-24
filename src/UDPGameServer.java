import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UDPGameServer extends UDPServer{
    public final static int DEFAUL_PORT = 7;

    private String[] words;

    private String p1ID;
    private Session player1;
    private String p2ID;
    private Session player2;

    private String[] tradP1;
    private String[] tradP2;

    public UDPGameServer(Session player1, Session player2, int port, String[] words) {
        super(port);
        this.words = words;

        this.tradP1 = new String[words.length];
        this.tradP2 = new String[words.length];

        this.player1 = player1;
        this.p1ID = player1.getUserID();
        this.player2 = player2;
        this.p2ID = player2.getUserID();

        System.out.println("player1 = " + this.p1ID);
        System.out.println("player2 = " + this.p2ID);
    }

    @Override
    public void respond(DatagramSocket socket, DatagramPacket packet) throws IOException {
        //noinspection CharsetObjectCanBeUsed
        String s = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
        System.out.println("UDP client says: " + s);
        // s = player_num_traduzione
        // player = giocatore che ha inviato, num = index parola, traduzione se null blank
        String[] sSplit = s.split("_");
        String currentPlayer = sSplit[0];
        System.out.println("current player = " + currentPlayer);
        int index = Integer.parseInt(sSplit[1]);
        System.out.println("index = " + index);
        String trad = sSplit[2];
        System.out.println("traduzione = " + trad);
        if (index != 0) {
            if (currentPlayer.equals(p1ID)) {
                tradP1[index-1] = trad;
            }
            if (currentPlayer.equals(p2ID)) {
                tradP2[index-1] = trad;
            }
        }

        if (index < words.length) {
            byte[] data = words[index].getBytes("UTF-8");
            DatagramPacket out = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
            socket.send(out);
        } else {
            byte[] data = "FINE".getBytes();
            DatagramPacket out = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
            socket.send(out);
        }

        System.out.println("tradp1 = " + Arrays.toString(tradP1));
        System.out.println("tradp2 = " + Arrays.toString(tradP2));
    }
}
