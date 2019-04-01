import java.io.IOException;
import java.net.SocketException;

public class NetworkPlayer implements Runnable {
    private boolean running;
    private Network network;
    private int port;

    //Initialise Network player:
    NetworkPlayer(int port) {
        this.port = port;
    }

    void start() {
        if (running) return;
        try {
            network = new Network(port);
            Thread fil = new Thread(this);
            fil.start();
        } catch (IOException e) {
            running = false;
        }
    }

    void stop() {
        if (!running) return;
        network.close();
        running = false;
    }

    @Override
    public void run() {
        while (running){
            try {
                String nachricht = network.receive();
                System.out.println("Received Buffer: " + nachricht);
            } catch (SocketException se){
                System.out.println("Closed");
                break;
            } catch (IOException e){
                System.err.println("Network Error: " + e);
                break;
            }
        }
        network.close();
    }
}
