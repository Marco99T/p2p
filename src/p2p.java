import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class p2p extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private Set<InetAddress> peers = new HashSet<>();
    private int puerto;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					p2p frame = new p2p();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public p2p() {
		this.puerto = 10546;
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

}
