import java.io.IOException;
import java.net.DatagramPacket;	
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;
import javax.swing.JTextArea;

public class Bully implements Runnable{
	private MulticastSocket socket;
	private InetAddress group;
	
	private boolean coordinador=false;
    private boolean elector_lock=false;
	private int port;
	private int ID;
	private String IP;
    private boolean is_there_coordinador;
    private boolean receivedData = false;
	
	private JTextArea text_are_chat_algoritmo_1;
	
	public Bully(String host, int port, JTextArea text_are_chat_algoritmo_1) {
		try {
            this.is_there_coordinador = false;
			this.port = port;
			this.socket = new MulticastSocket(port);
			this.group = InetAddress.getByName(host);
			this.socket.joinGroup(group);
			this.text_are_chat_algoritmo_1 = text_are_chat_algoritmo_1;
			this.ID = Integer.parseInt(InetAddress.getLocalHost().getHostAddress().substring(6).replace(".", ""));
			this.IP = String.valueOf(InetAddress.getLocalHost().getHostAddress());
            this.verify_leader();
			System.out.println(this.ID + "//" + this.IP);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	
	public void run() {
        Timer timer = new Timer();
		DatagramPacket pack_ = new DatagramPacket(new byte[1024], 1024);
		try {
            while (true) {
                
                this.socket.receive(pack_);
                String address = String.valueOf(pack_.getAddress().getHostAddress());
                
                if(!address.equals(this.IP)){
                    //Validamos que es los que se esta recibiendo
                    String data_from_packet = new String(pack_.getData());
                    ArrayList <String> data = get_data_from_datagrampacket(data_from_packet);
                    String message_cadidate = data.get(0);
                    int id_candidate = Integer.parseInt(data.get(1));
                    
                    if(this.coordinador){
                        message_from_coordinator();
                        //String message = "Soy el coordinador (" + this.ID + ") \n";
                        //text_are_chat_algoritmo_1.append(message);
                    }
                    else {
                        switch(message_cadidate) {
                            case "Coordinador":
                            //With this i can knoew whos the coordidador
                                if(!is_there_coordinador){
                                    this.elector_lock = false;
                                    this.is_there_coordinador = true;
                                    String message = "El coordinador actual es: " + id_candidate + "\n";
                                    text_are_chat_algoritmo_1.append(message);
                                }
                                break;
                            case "Eleccion":
                                if(this.ID > id_candidate && !this.elector_lock){
                                    this.elector_lock=true;
                                    text_are_chat_algoritmo_1.append("Soy candidato(" + this.ID +") \n");
                                    message_to_select();
                                }
                                break;
                            case "Desconectado":
                                if(is_there_coordinador){
                                    this.is_there_coordinador = false;
                                    this.elector_lock = false;
                                    String message_to_nodes = "El coordinador: " + id_candidate + " se ha desconectado. \n";
                                    text_are_chat_algoritmo_1.append(message_to_nodes);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    if(!this.is_there_coordinador && !this.elector_lock && !this.coordinador){
                        this.text_are_chat_algoritmo_1.append("No hay coordinador. \n");
                    }
                }
                
                else if(address.equals(this.IP) && this.elector_lock){  //Case when we want so select a leader
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (!coordinador) {
                                // Acción a realizar si no se han recibido datos dentro del intervalo de tiempo
                                text_are_chat_algoritmo_1.append("Soy ahora el coordinador...: " + ID +".\n");
                                message_from_coordinator();
                                coordinador = true; // Reiniciar el indicador de recepción de datos
                            }
                            //receivedData = false;
                        }
                    }, 7000, 3000);
                    if(coordinador){
                        timer.cancel();
                        text_are_chat_algoritmo_1.append("tempo cancelado. \n");
                    }
                }
                
                else{   //Case when start to search a leader
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (!coordinador) {
                                // Acción a realizar si no se han recibido datos dentro del intervalo de tiempo
                                text_are_chat_algoritmo_1.append("Soy ahora el coordinador: " + ID +".\n");
                                message_from_coordinator();
                                coordinador = true;// Reiniciar el indicador de recepción de datos
                            }
                        }
                    }, 7000, 1000);
                    if(this.coordinador){
                        timer.cancel();
                    }
                }
            }
		}
        catch (IOException e) {
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
        Timer timer = new Timer();
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
	}
	
	private ArrayList<String> get_data_from_datagrampacket(String cadena){
        ArrayList<String> lista= new ArrayList<String>();

        String message = "";
        String id_nodo = "";
        for(char c:cadena.toCharArray()){
            if(Character.isAlphabetic(c)){
                message += c;
            }
            if(Character.isDigit(c)){
                id_nodo += c;
            }
            if(c == '.'){
                break;
            }
        }
        lista.add(message);
        lista.add(id_nodo);
        return lista;
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
		if(!this.elector_lock && !this.coordinador && !this.is_there_coordinador){
        	message_to_select();
        	String  message = "Se envia mensaje de eleccion con id:" + this.ID + " \n";
            text_are_chat_algoritmo_1.append(message);
        }
	}

    public void notify_disconeccted(){
        String message = "Desconectado " + this.ID;
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, group, port);
        try {
            this.coordinador = false;
            socket.send(packet);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
