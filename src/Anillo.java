
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JTextArea;

public class Anillo extends Thread {
    private DatagramSocket socket;
    private InetAddress group;
    private int port;
    private int ID;
    private String IP;
    private boolean lider = false;
    private int liderID = -1;
    private JTextArea textArea;
    private List<Integer> vecinos = new ArrayList<>();

    public Anillo(String host, int port, JTextArea textArea, int ID, String IP) {
        try {
            this.port = port;
            this.socket = new DatagramSocket(port);
            this.ID = ID;
            this.IP = IP;
            this.textArea = textArea;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        try {
            socket.receive(receivePacket);
            String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
            String[] parts = receivedData.split(":");

            int receivedID = Integer.parseInt(parts[0]);
            InetAddress nextNodeAddress = InetAddress.getByName(parts[1]);
            int nextNodePort = Integer.parseInt(parts[2]);

            if (receivedID < ID) {
                // Forward the message to the next node
                String forwardData = receivedID + ":" + IP + ":" + port;
                DatagramPacket forwardPacket = new DatagramPacket(forwardData.getBytes(), forwardData.getBytes().length, nextNodeAddress, nextNodePort);
                socket.send(forwardPacket);
            } else if (receivedID == ID) {
                // This node is the leader
                textArea.append("I am the leader with ID: " + ID + "\n");
            } else {
                // This node is not the leader, pass it on
                String forwardData = receivedData;
                DatagramPacket forwardPacket = new DatagramPacket(forwardData.getBytes(), forwardData.getBytes().length, nextNodeAddress, nextNodePort);
                socket.send(forwardPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startElection() {
        try {
            // Broadcast the election message to all nodes
            byte[] sendData = (ID + ":" + IP + ":" + port).getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), port);
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
