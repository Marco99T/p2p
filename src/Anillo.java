
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JTextArea;

public class Anillo implements Runnable{
	
	private MulticastSocket socket;
	private InetAddress group;
	private boolean coordinador=false;
    private boolean elector_lock=false;
	private int port;
	private int ID;
	private int votos;
	private String IP;
	
	private JTextArea text_area_chat_algortimos_1;
	
	public Anillo(String host, int port, JTextArea text_are_chat_algoritmo_1) {
		try {
			this.port = port;
			this.socket = new MulticastSocket(port);
			this.group = InetAddress.getByName(host);
			this.socket.joinGroup(group);
			this.text_area_chat_algortimos_1 = text_are_chat_algoritmo_1;
			this.ID = Integer.parseInt(InetAddress.getLocalHost().getHostAddress().substring(5).replace(".", ""));
			this.IP = String.valueOf(InetAddress.getLocalHost().getHostAddress());
			this.votos = 0;
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
	}
	
    
	public void send_message() {

		if(!elector_lock && !coordinador){
        	message_to_select();
        	String  message = "Se envia mensaje de eleccion con id:" + this.ID + " \n";
            text_area_chat_algortimos_1.append(message);
        }
	}

	public void run() {
		DatagramPacket pack_ = new DatagramPacket(new byte[1024], 1024);
		try {
			this.socket.receive(pack_);
			String address = String.valueOf(pack_.getAddress().getHostAddress());
			
			if(address.equals(this.IP)){
				//Validamos que es los que se esta recibiendo
				ArrayList <String> data = get_data_from_datagrampacket(pack_.getData());
				String message_cadidate = data.get(0);
				int id_candidate = Integer.parseInt(data.get(1));
				
				System.out.println(message_cadidate);
				
				switch(message_cadidate) {
					case "Coordinador":
						this.elector_lock = false;
	                    //System.out.println("El coordinador actual es: "+id_rec+" soy: "+id);
	                    String message = "";
	                    message = "El coordinador actual es: " + 5 + "\n Soy: " + this.ID + "\n";
	                    text_area_chat_algortimos_1.append(message);
						break;
					case "Eleccion":
						if(this.votos >3){
	                        this.coordinador = true;
	                        String status = "";
	                        status = "Votos: " + this.votos + " en :" + this.ID + " \n Coordinador: " + this.coordinador + "\n";
	                        text_area_chat_algortimos_1.append(status);
	                    }
	                    if(this.ID > id_candidate){
	                        elector_lock=true;
	                        text_area_chat_algortimos_1.append("Soy candidato(" + this.ID +") \n");
	                    }
	                    else{
	                        votos++;
	                        System.out.println(id_candidate);
	                    }
						break;
					case "a":
						break;
					default:
						break;
				}
			}
			else {
				System.out.println("Esto reciviendo datos en mi misma pc");
			}
		}catch (IOException e) {
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
	
	private ArrayList<String> get_data_from_datagrampacket(byte []cad){
        ArrayList<String> lista= new ArrayList<String>();
        String message = "";
        String id_nodo  ="";
        String cadena=new String(cad);
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
	
	private void message_from_coordinator(){
        byte buffer []= ("Coordinador (" + this.ID + ")").getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        try {
            socket.send(packet);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
