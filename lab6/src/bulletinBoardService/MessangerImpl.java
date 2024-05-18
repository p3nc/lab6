package bulletinBoardService;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MessangerImpl implements Messanger {
    private UITasks ui = null;
    private MulticastSocket group = null;
    private InetAddress addr = null;
    private int port;
    private String name;

    private volatile boolean canceled = false;

    public MessangerImpl(InetAddress addr, int port, String name, UITasks ui) {
        this.name = name;
        this.ui = ui;
        this.addr = addr;
        this.port = port;
        try {
            group = new MulticastSocket(port);
            group.setTimeToLive(2);
            group.joinGroup(addr);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Помилка підключення!");
        }
    }

    @Override
    public void start() {
        Thread t = new Receiver();
        t.start();
    }

    @Override
    public void stop() {
        cancel();
        try {
            group.leaveGroup(addr);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Помилка від'єднання!\n" + e.getMessage());
        } finally {
            group.close();
        }
    }

    @Override
    public void send() {
        new Sender().start();
    }

    private class Sender extends Thread {
        public void run() {
            try {
                String msg = name + ": " + ui.getMessage();
                byte[] out = msg.getBytes();
                DatagramPacket pkt = new DatagramPacket(out, out.length, addr, port);
                group.send(pkt);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Помилка відправлення!\n" + e.getMessage());
            }
        }
    }

    private class Receiver extends Thread {
        public void run() {
            try {
                byte[] in = new byte[512];
                DatagramPacket pkt = new DatagramPacket(in, in.length);
                while (!isCanceled()) {
                    group.receive(pkt);
                    ui.setText(new String(pkt.getData(), 0, pkt.getLength()));
                }
            } catch (Exception e) {
                if (isCanceled()) {
                    JOptionPane.showMessageDialog(null, "З'єднання завершено.");
                } else {
                    JOptionPane.showMessageDialog(null, "Помилка прийому!\n" + e.getMessage());
                }
            }
        }
    }

    private synchronized boolean isCanceled() {
        return canceled;
    }

    public synchronized void cancel() {
        canceled = true;
    }
}