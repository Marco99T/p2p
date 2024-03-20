import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import javax.swing.JTextArea;

public class Bully implements Runnable{
	private MulticastSocket socket;
	private InetAddress group;
	
	private boolean coordinador=false;
    private boolean elector_lock=false;
	private int port;
	private int ID;
	private int votos;
	private String IP;
	//private int id_candidate;
	
	private JTextArea text_area_chat_algortimos_1;
	
	public Bully(String host, int port, JTextArea text_are_chat_algoritmo_1) {
		try {
			this.port = port;
			this.socket = new MulticastSocket(port);
			this.group = InetAddress.getByName(host);
			this.socket.joinGroup(group);
			this.text_area_chat_algortimos_1 = text_are_chat_algoritmo_1;
			this.ID = Integer.parseInt(InetAddress.getLocalHost().getHostAddress().substring(5).replace(".", ""));
			this.IP = String.valueOf(InetAddress.getLocalHost().getHostAddress());
			//this.id_candidate = -1;
			System.out.println(this.ID + "//" + this.IP);
			this.votos = 0;
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
	}


	protected void verify_leader() {
		String message = "Leader " + this.ID;
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, group, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
	}
	
	public void send_message() {
		if(!elector_lock && !coordinador){

            //System.out.println("Se envia mensaje eleccion: "+id);
        	message_to_select();
        	String  message = "Se envia mensaje de eleccion con id:" + this.ID + " \n";
            text_area_chat_algortimos_1.append(message);
        }
	}
	
	public void run(){
		DatagramPacket pack_ = new DatagramPacket(new byte[1024], 1024);
		try {
			this.socket.receive(pack_);
			String address = String.valueOf(pack_.getAddress().getHostAddress());
			
			if(!address.equals(this.IP)){
				//Validamos que es los que se esta recibiendo
				String data_from_packet = new String(pack_.getData());
				ArrayList <String> data = get_data_from_datagrampacket(data_from_packet);
				String message_cadidate = data.get(0);
				int id_candidate = Integer.parseInt(data.get(1));
				
				if(coordinador){
                    message_from_coordinator();
                    String message = "Soy el coordinado (" + this.ID + ") \n";
                    text_area_chat_algortimos_1.append(message);
                }
				else {
					switch(message_cadidate) {
						case "Coordinador":
							this.elector_lock = false;
					        String message = "El coordinador actual es: " + id_candidate + "\n";
					        text_area_chat_algortimos_1.append(message);
							break;
						case "Eleccion":
					        if(this.ID > id_candidate){
					            elector_lock=true;
					            text_area_chat_algortimos_1.append("Soy candidato(" + this.ID +") \n");
					            message_to_select();
					        }
							break;
						case "Desconectado":
							String message_to_nodes = "El coordinador: " + id_candidate + " se ha desconectado. \n";
					        text_area_chat_algortimos_1.append(message_to_nodes);
							break;
						case "Leader":
							String message_to_ip = "";
							if (this.coordinador) {
								message_to_ip = "Coordinador " + this.ID;
							}
					        else {
					        	message_to_ip = "NoLeader " + this.ID;
							}
							try {
						        DatagramPacket packet_to_ip = new DatagramPacket(message_to_ip.getBytes(), message_to_ip.getBytes().length, InetAddress.getByName(address), port);
					            socket.send(packet_to_ip);
					        } catch (IOException e) {
					            System.out.println(e.getMessage());
					        }
							break;
						case "NoLeader":
							text_area_chat_algortimos_1.append("No hay coordinador. \n");
							break;
						default:
							break;
					}
				}
			}
			else {
			}
		}catch (IOException e) {
			System.out.println(e.getMessage());
		}
			
	}

	
	
	private void message_from_coordinator(){
        byte buffer []= ("Coordinador (" + this.ID + ")").getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        try {
            socket.send(packet);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
	
	private void message_to_select(){
        String message = "Eleccion " + this.ID;
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, group, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
	}
	
	private ArrayList<String> get_data_from_datagrampacket(String cadena){
        ArrayList<String> lista= new ArrayList<String>();
        String message = "";
        String id_nodo  ="";
        for(char c:cadena.toCharArray()){
            if(Character.isAlphabetic(c)){
                message += c;
            }
            if(Character.isDigit(c)){
                id_nodo += c;
            }
        }
        lista.add(message);
        lista.add(id_nodo);
        return lista;
    }
	
	public boolean notify_nodes_there_is_no_leader() {
		if (this.coordinador) {
			byte buffer []= ("Desconectado (" + this.ID + ")").getBytes();
	        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
	        try {
	            socket.send(packet);
	        } catch (Exception e) {
	            System.out.println(e.getMessage());
	        }
	        return true;
		}
		return false;
	}
	
	private void notify_i_am_leader() {
		if(this.coordinador) {
			message_from_coordinator();
		}
	}

}
