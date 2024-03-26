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
    private boolean status = false;
    private Timer timer_to_be_coordinador;
	
	private JTextArea text_are_chat_algoritmo_1;
	
	public Bully(String host, int port, JTextArea text_are_chat_algoritmo_1) {
		timer_to_be_coordinador = new Timer();
		this.text_are_chat_algoritmo_1 = text_are_chat_algoritmo_1;
		try {
            this.is_there_coordinador = false;
			this.port = port;
			this.socket = new MulticastSocket(port);
			this.group = InetAddress.getByName(host);
			this.socket.joinGroup(group);
			this.ID = Integer.parseInt(InetAddress.getLocalHost().getHostAddress().substring(6).replace(".", ""));
			this.IP = String.valueOf(InetAddress.getLocalHost().getHostAddress());
            this.verify_leader();
			System.out.println(this.ID + "//" + this.IP);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	
	public void run() {
		DatagramPacket pack_ = new DatagramPacket(new byte[1024], 1024);
		try {
			//Case when start to search a leader
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
                    else if(!this.is_there_coordinador || !this.coordinador) {
                    	message_no_leader();
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
                                    message_to_change_status_of_nodo(address);
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
                            case "NoLeader":
                            	//timer_to_be_coordinador.cancel();
                            	break;
                            case "Status":
                            	this.status = true;
                            	break;
                            default:
                                break;
                        }
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
            timer_to_be_coordinador.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if ((!status && elector_lock) || (!is_there_coordinador && !coordinador)) {
                        // Acción a realizar si no se han recibido datos dentro del intervalo de tiempo
                        text_are_chat_algoritmo_1.append("Soy ahora el coordinador: " + ID +".\n");
                        message_from_coordinator();
                        coordinador = true;// Reiniciar el indicador de recepción de datos
                        elector_lock = false;
                        is_there_coordinador = true;
                    }
                }
            }, 5000, 3000);
            /*
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run(){
                    
                    if(!is_there_coordinador && !coordinador){
                        text_are_chat_algoritmo_1.append("No hay coordinador. \n");
                    }else{
                        this.cancel();
                    }
                }
            }, 2000, 3000);
            */
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
    
    private void message_no_leader() {
    	String message = "NoLeader " + this.ID;
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, group, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private void message_to_change_status_of_nodo(String address) {
    	String message = "Status " + this.ID;
        try {
        	DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(address), port);
            socket.send(packet);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
