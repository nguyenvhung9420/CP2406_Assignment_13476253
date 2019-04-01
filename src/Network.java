import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Network {

    private DatagramSocket socket;

    //For initialise a Network object
    Network(int port) throws IOException {
        socket = new DatagramSocket(port);
    }

    //Sending data:
    void send(String message, int port) throws IOException {
        InetAddress address = InetAddress.getLocalHost();
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), address, port);
        socket.send(packet);
    }

    //Receiving data:
    String receive() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(buffer).trim();
    }

    //Disconnecting:
    void close() {
        socket.close();
    }
}

