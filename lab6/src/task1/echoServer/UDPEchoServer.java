package task1.echoServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPEchoServer extends UDPServer {

    public final static int DEFAULT_PORT = 7;

    public UDPEchoServer() {
        super(DEFAULT_PORT);
    }

    @Override
    public void respond(DatagramSocket socket, DatagramPacket request) throws IOException {
        String receivedMessage = new String(request.getData(), 0, request.getLength(), "UTF-8");
        System.out.println("Отримано повідомлення від " + request.getAddress() + ":" + request.getPort() + ": " + receivedMessage);
        DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());
        socket.send(reply);
    }

    public static void main(String[] args) {
        UDPServer server = new UDPEchoServer();
        Thread t = new Thread(server);
        t.start();

        System.out.println("Echo-server запущено на порті " + DEFAULT_PORT);

    }
}