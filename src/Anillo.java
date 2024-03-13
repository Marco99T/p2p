import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextArea;

public class Anillo implements Runnable{
	
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	private  int ID;
	private int votos;
	
	private JTextArea text_area_chat_algortimos_1;
	
	public Anillo(String host, int port, JTextArea text_are_chat_algoritmo_1) {
		try {
			this.port = port;
			this.socket = new MulticastSocket(port);
			this.group = InetAddress.getByName(host);
			this.socket.joinGroup(group);
			this.text_area_chat_algortimos_1 = text_are_chat_algoritmo_1;
			this.ID = Integer.parseInt(InetAddress.getLocalHost().getHostAddress().substring(8).replace(".", ""));
			this.votos = 0;
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
	}
	
    
	public void send() {

		try {
			InetAddress localHost = InetAddress.getLocalHost();
	        int id = Integer.parseInt(localHost.getHostAddress().substring(8).replace(".", ""));
	        
	        System.out.println("Mi dirección IP es: " + id);
	        
	     // Enviar solicitud de votos a todos los nodos en el grupo
	        String request = "VOTO: " + id;
	        DatagramPacket requestPacket = new DatagramPacket(request.getBytes(), request.getBytes().length, group, port);
	        socket.send(requestPacket);
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void run() {
		Set<String> connectedNodes = new HashSet<>();
		
		try {

            // Recibir respuestas de otros nodos
            byte[] responseData = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);
            socket.receive(responsePacket);
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());

            // Analizar respuestas y elegir al líder
            int leaderID = chooseLeader(response);

            // Si este nodo es el líder, realizar acciones de líder
            if (leaderID == ID) {
                performLeaderActions();
            }

            // Salir del grupo multicast
            socket.leaveGroup(group);
            socket.close();
        } catch (Exception e) {
            System.err.println("No se pudo determinar la dirección IP: " + e.getMessage());
        }

    }


	// Generar un identificador único para este nodo (para simplificar, se utiliza un valor aleatorio)
	private int generateUniqueID() {
	    return (int) (Math.random() * 1000);
	}
	
	// Analizar las respuestas de los nodos y elegir al líder basado en el identificador más alto
	private int chooseLeader(String response) {
	    // Aquí deberías analizar las respuestas de los nodos y determinar cuál tiene el identificador más alto
	    // Por simplicidad, supongamos que la respuesta contiene solo el ID del nodo líder
		String status = "";
        Integer id_nodo = Integer.valueOf(response.replace("\\D", ""));
        if (this.ID > id_nodo)	{
        	this.votos ++;
        	text_area_chat_algortimos_1.append("Soy candidato(" + this.ID + ") \n");
        	status = "true";
        }
        else if (this.ID < id_nodo){
        	status = "false";
        	DatagramPacket requestPacket = new DatagramPacket(status.getBytes(), status.getBytes().length, group, port);
	        try {
				socket.send(requestPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        
	    return 5;
	}
	
	// Acciones a realizar si este nodo es el líder
	private void performLeaderActions() {
	    System.out.println("¡Este nodo es el líder y realizará acciones de líder!");
	    // Aquí puedes agregar las acciones que debe realizar el líder
	}
}
