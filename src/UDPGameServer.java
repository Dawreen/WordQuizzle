import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPGameServer extends UDPServer{
    public final static int DEFAUL_PORT = 7;

    private String[] words;

    public UDPGameServer() {
        super(DEFAUL_PORT);
    }

    public UDPGameServer(String[] words) {
        super(DEFAUL_PORT);
        this.words = words;
    }

    @Override
    public void respond(DatagramSocket socket, DatagramPacket packet) throws IOException {
        //noinspection CharsetObjectCanBeUsed
        String s = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
        System.out.println("UDP client says: " + s);
        DatagramPacket out = new DatagramPacket(packet.getData(), packet.getLength(),
                packet.getAddress(), packet.getPort());
        socket.send(out);

        for (String each : this.words) {
            byte[] data = each.getBytes("UTF-8");
            DatagramPacket outword = new DatagramPacket(data, data.length,
                    packet.getAddress(), packet.getPort());
            socket.send(outword);
        }
    }
}
