import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextArea;

public class Anillo extends Thread{
	
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	
	private JTextArea text_area_chat_algortimos_1;
	
	public Anillo(String host, int port, JTextArea text_are_chat_algoritmo_1) {
		try {
			this.socket = new MulticastSocket(port);
			this.group = InetAddress.getByName(host);
			this.socket.joinGroup(group);
			this.text_area_chat_algortimos_1 = text_are_chat_algoritmo_1;
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
	}

	public void run() {
		Set<String> connectedNodes = new HashSet<>();

        try{
        	/*
            InetAddress group = InetAddress.getByName(this.);
            socket.joinGroup(group);
            */

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("Waiting for nodes to join...");

            //while (true) {
                socket.receive(packet);
                String nodeAddress = packet.getAddress().getHostAddress();
                connectedNodes.add(nodeAddress);
                System.out.println("Node connected: " + nodeAddress);
                System.out.println("Total nodes connected: " + connectedNodes.size());
            //}
            text_area_chat_algortimos_1.append("Nodo: " + nodeAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
