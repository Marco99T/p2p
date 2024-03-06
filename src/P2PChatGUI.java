import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class P2PChatGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private Set<InetAddress> peers = new HashSet<>();
    private int puerto;

    public P2PChatGUI(int puerto) {
        this.puerto = puerto;
        setTitle("P2P Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send");
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        discoverPeers(puerto);

        setVisible(true);
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            chatArea.append("Me: " + message + "\n");
            messageField.setText("");
            sendToPeers(message);
        }
    }

    private void sendToPeers(String message) {
        for (InetAddress peer : peers) {
            System.out.println(peer.getHostAddress());
            try (Socket socket = new Socket(peer, ++puerto);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void discoverPeers(int puerto) {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(puerto);
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    InetAddress senderAddress = packet.getAddress();
                    if (!senderAddress.equals(socket.getLocalAddress())) {
                        System.out.println(socket.getReuseAddress());
                        peers.add(senderAddress);
                        chatArea.append("Peer discovered: " + senderAddress.getHostAddress() + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int puerto = scan.nextInt();
        P2PChatGUI p2p = new P2PChatGUI(puerto);

        //SwingUtilities.invokeLater(P2PChatGUI::new);
    }
}
