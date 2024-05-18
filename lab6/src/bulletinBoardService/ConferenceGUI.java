package bulletinBoardService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;

public class ConferenceGUI extends JFrame {

    private JTextField textFieldUserName;
    private JTextField textFieldMsg;
    private JTextArea textArea;
    private JButton btnConnect;
    private JButton btnSend;
    private JButton btnDisconnect;

    private Messanger messanger = null;
    private InetAddress addr = null;
    private int port = 3456;

    public ConferenceGUI() {
        super("Текстова конференція");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel panelTop = new JPanel(new FlowLayout());
        JLabel labelUserName = new JLabel("Ім'я користувача:");
        textFieldUserName = new JTextField(10);
        btnConnect = new JButton("З'єднати");
        btnDisconnect = new JButton("Від'єднати");
        btnDisconnect.setEnabled(false);
        panelTop.add(labelUserName);
        panelTop.add(textFieldUserName);
        panelTop.add(btnConnect);
        panelTop.add(btnDisconnect);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        JPanel panelBottom = new JPanel(new FlowLayout());
        textFieldMsg = new JTextField(20);
        btnSend = new JButton("Відправити");
        btnSend.setEnabled(false);
        panelBottom.add(textFieldMsg);
        panelBottom.add(btnSend);

        add(panelTop, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);

        // Обробники подій
        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });

        btnDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnect();
            }
        });

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                messanger.send();
            }
        });

        setVisible(true);
    }

    private void connect() {
        String name = textFieldUserName.getText();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введіть ім'я користувача!");
            return;
        }
        try {
            addr = InetAddress.getByName("224.0.0.1");
            // Створення проксі для UITasks
            UITasks ui = (UITasks) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{UITasks.class},
                    new EDTInvocationHandler(new UITasksImpl()));
            messanger = new MessangerImpl(addr, port, name, ui);
            messanger.start();
            btnConnect.setEnabled(false);
            btnDisconnect.setEnabled(true);
            btnSend.setEnabled(true);
            textFieldUserName.setEditable(false);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Помилка підключення!");
        }
    }

    private void disconnect() {
        if (messanger != null) {
            messanger.stop();
            messanger = null;
        }
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        btnSend.setEnabled(false);
        textFieldUserName.setEditable(true);
    }

    // Внутрішній клас для реалізації UITasks
    private class UITasksImpl implements UITasks {
        @Override
        public String getMessage() {
            String res = textFieldMsg.getText();
            textFieldMsg.setText("");
            return res;
        }

        @Override
        public void setText(String txt) {
            textArea.append(txt + "\n");
        }
    }

    // Клас обробника викликів для динамічного проксі
    public class EDTInvocationHandler implements InvocationHandler {
        private Object invocationResult = null;
        private UITasks ui;

        public EDTInvocationHandler(UITasks ui) {
            this.ui = ui;
        }

        @Override
        public Object invoke(Object proxy, final Method method, final Object[] args)
                throws Throwable {
            if (SwingUtilities.isEventDispatchThread()) {
                invocationResult = method.invoke(ui, args);
            } else {
                Runnable shell = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            invocationResult = method.invoke(ui, args);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                };
                SwingUtilities.invokeAndWait(shell);
            }
            return invocationResult;
        }
    }

    public static void main(String[] args) {
        new ConferenceGUI();
    }
}