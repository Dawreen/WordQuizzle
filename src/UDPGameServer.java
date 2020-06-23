import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPGameServer extends UDPServer{
    public final static int DEFAUL_PORT = 7;

    private String[] words;

    public UDPGameServer(int port, String[] words) {
        super(port);
        this.words = words;
    }

    @Override
    public void respond(DatagramSocket socket, DatagramPacket packet) throws IOException {
        System.out.println("UDP reponding");
        //noinspection CharsetObjectCanBeUsed
        String s = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
        System.out.println("UDP client says: " + s);
        DatagramPacket out = new DatagramPacket(packet.getData(), packet.getLength(),
                packet.getAddress(), packet.getPort());
        socket.send(out);

        for (String each : this.words) {
            //noinspection CharsetObjectCanBeUsed
            byte[] data = each.getBytes("UTF-8");
            DatagramPacket outword = new DatagramPacket(data, data.length,
                    packet.getAddress(), packet.getPort());
            socket.send(outword);
        }
    }
}
