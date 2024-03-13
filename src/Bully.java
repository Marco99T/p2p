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
	private  int ID;
	private int votos;
	
	private JTextArea text_area_chat_algortimos_1;
	
	public Bully(String host, int port, JTextArea text_are_chat_algoritmo_1) {
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


	public void run() {
		try {
            DatagramPacket paquete;
            socket.setTimeToLive(0);
            while(true){
                if(coordinador){
                    //System.out.println("Soy el coordinador "+id);
                    message_from_coordinator();
                    String message = "";
                    message = "Soy el coordinado (" + this.ID + ") \n";
                    text_area_chat_algortimos_1.append(message);
                }
                else{
                	if(!elector_lock && !coordinador){

                        //System.out.println("Se envia mensaje eleccion: "+id);
                    	message_to_select();
                    	String  message = "";
                    	message = "Se envia mensaje de eleccion con id:" + this.ID + " \n";
                        text_area_chat_algortimos_1.append(message);
                    }
                    try {
                    	
                    	
                        byte buffer[]= new byte[20];
                        paquete=new DatagramPacket(buffer,buffer.length);
                        socket.receive(paquete);

                        ArrayList<String >data = get_data_from_datagrampacket(paquete.getData());

                        String message_nodo = String.valueOf(data.get(0));
                        int id_nodo = Integer.parseInt(data.get(1));

                        if(message_nodo.equalsIgnoreCase("Coordinador")){
                            elector_lock = false;
                            //System.out.println("El coordinador actual es: "+id_rec+" soy: "+id);
                            String message = "";
                            message = "El coordinador actual es: " + id_nodo + "\n Soy: " + this.ID + "\n";
                            text_area_chat_algortimos_1.append(message);

                        }
                        if(message_nodo.equalsIgnoreCase("Eleccion")){
                            if(this.votos >3){
                                this.coordinador = true;
                                //System.out.println("Count: "+count+" en: "+id+"Coordinador: "+Coordinador);
                                String status = "";
                                status = "Votos: " + this.votos + " en :" + this.ID + " \n Coordinador: " + this.coordinador + "\n";
                                text_area_chat_algortimos_1.append(status);
                            }
                            if(this.ID > id_nodo){
                                elector_lock=true;
                            }
                            if(this.ID <= id_nodo){
                                votos++;
                            }
                        }
                    } catch (Exception e) {
                    	System.out.println(e.getMessage());
                    }
                }
                Thread.sleep(3000);
            }
        } catch (Exception e) {
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

}
