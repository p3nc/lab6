package task1.echoClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class UDPEchoClient {

    public final static int PORT = 7;

    public static void main(String[] args) {
        String hostname = "localhost";
        if (args.length > 0) {
            hostname = args[0];
        }

        try {
            InetAddress ia = InetAddress.getByName(hostname);
            DatagramSocket socket = new DatagramSocket();
            Thread receiver = new ReceiverThread(socket);
            receiver.start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                showMenu();
                int choice = scanner.nextInt();
                scanner.nextLine(); // Очистка буфера

                switch (choice) {
                    case 1:
                        System.out.print("Введіть повідомлення: ");
                        String message = scanner.nextLine();
                        sendMessage(socket, ia, PORT, message);
                        break;
                    case 2:
                        System.out.println("Завершення роботи клієнта.");
                        socket.close();
                        return;
                    default:
                        System.out.println("Невірний вибір. Спробуйте ще раз.");
                }
            }
        } catch (UnknownHostException ex) {
            System.err.println(ex);
        } catch (SocketException ex) {
            System.err.println(ex);
        }
    }

    private static void showMenu() {
        System.out.println("\nМеню:");
        System.out.println("1. Відправити повідомлення");
        System.out.println("2. Вихід");
        System.out.print("Виберіть дію: \n");
    }

    private static void sendMessage(DatagramSocket socket, InetAddress server, int port, String message) {
        try {
            byte[] data = message.getBytes("UTF-8");
            DatagramPacket output = new DatagramPacket(data, data.length, server, port);
            socket.send(output);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
