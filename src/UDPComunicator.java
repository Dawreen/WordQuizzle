import java.io.IOException;
import java.net.*;

public class UDPComunicator implements Runnable {
    private DatagramSocket socket;
    private int port;
    private int bufferSize = 8192;

    private InetAddress address;
    private int sendPort;

    private volatile boolean shutdown = false;

    public UDPComunicator(DatagramSocket socket, int port, InetAddress ia, int sendPort) {
        this.socket = socket;
        this.port = port;
        this.address = ia;
        this.sendPort = sendPort;
    }
    @Override
    public void run() {
        byte[] buffer = new byte[bufferSize];
        try {
            while(true) {
                if (shutdown) return;
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(incoming);
                if (this.address == null) {
                    this.address = incoming.getAddress();
                    System.out.println("address == " + this.address.toString());
                }
                if (this.sendPort == 0) {
                    this.sendPort = incoming.getPort();
                    System.out.println("port in the udp == " + this.sendPort);
                }
                String received = new String(incoming.getData(), 0, incoming.getLength(), "UTF-8");
                System.out.println(received);
                // TODO: 23/06/2020 response
            } // end while
        } catch (SocketTimeoutException ex) {

        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
