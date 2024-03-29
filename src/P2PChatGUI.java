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

        discoverPeers();

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
            try (DatagramSocket socket = new DatagramSocket()) {
                byte[] sendData = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, peer, 12600);
                socket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void discoverPeers() {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(puerto);
                socket.setBroadcast(true);
                byte[] sendData = "P2PChatDiscovery".getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), puerto);
                socket.send(sendPacket);
                
                byte[] receiveData = new byte[1024];
                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    InetAddress senderAddress = receivePacket.getAddress();
                    if (!senderAddress.equals(socket.getLocalAddress())) {
                        peers.add(senderAddress);
                        chatArea.append("Peer discovered: " + senderAddress.getHostAddress() + "\n");
                        System.out.println("Peer discovered: " + senderAddress.getHostAddress());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new P2PChatGUI(new Scanner(System.in).nextInt()));
    }
}
