import java.io.IOException;
import java.net.DatagramPacket;	
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;
import javax.swing.JTextArea;

public class Bully extends Thread{
	private MulticastSocket socket;
	private InetAddress group;
	
	private boolean coordinador=false;
    private boolean elector_lock=false;
	private int port;
	private int ID;
	private String IP;
    private boolean is_there_coordinador;
    private boolean status = false;
    private Timer timer_to_be_coordinador;
    private Timer timer_to_convert_in_coordinador;
	
	private JTextArea text_are_chat_algoritmo_bully;
	
	public Bully
    (
        String host, int port
        , JTextArea text_are_chat_algoritmo_bully, int ID, String IP
    ) 
    {

		timer_to_be_coordinador = new Timer();
        timer_to_convert_in_coordinador = new Timer();
		this.text_are_chat_algoritmo_bully = text_are_chat_algoritmo_bully;
		try {
            this.is_there_coordinador = false;
			this.port = port;
			this.socket = new MulticastSocket(port);
			this.group = InetAddress.getByName(host);
            this.ID = ID;
            this.IP = IP;
			this.socket.joinGroup(group);
            this.verify_leader();
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
                    System.out.println(data_from_packet);
                    
                    if(this.coordinador){
                        message_from_coordinator();
                    }
                    else {
                        switch(message_cadidate) {
                            case "Coordinador":
                            //With this i can knoew whos the coordidador
                                if(!is_there_coordinador){
                                    this.elector_lock = false;
                                    this.is_there_coordinador = true;
                                    String message = "El coordinador actual es: " + id_candidate + "\n";
                                    text_are_chat_algoritmo_bully.append(message);
                                    timer_to_be_coordinador.cancel();
                                }
                                break;
                            case "Eleccion":
                                if(this.ID > id_candidate && !this.elector_lock){
                                    this.elector_lock=true;
                                    text_are_chat_algoritmo_bully.append("Soy candidato(" + this.ID +") \n");
                                    message_to_select();
                                    message_to_change_status_of_nodo(address);
                                    
                                    //White a response, if there is not a response, then this node its convert in leader
                                    timer_to_convert_in_coordinador.scheduleAtFixedRate(new TimerTask() {
                                        public void run(){
                                            if(!status && !coordinador){
                                                text_are_chat_algoritmo_bully.append("Soy ahora el coordinador: " + ID +".\n");
                                                message_from_coordinator();
                                                coordinador = true;
                                                is_there_coordinador = true;
                                                elector_lock = false;
                                            }
                                        }
                                    }, 1500, 1000);
                                }
                                break;
                            case "Desconectado":
                                if(is_there_coordinador){
                                    this.is_there_coordinador = false;
                                    this.elector_lock = false;
                                    String message_to_nodes = "El coordinador: " + id_candidate + " se ha desconectado. \n";
                                    text_are_chat_algoritmo_bully.append(message_to_nodes);
                                }
                                break;
                            case "Leader":
                                message_no_leader();
                                break;
                            case "NoLeader":
                                text_are_chat_algoritmo_bully.append("No hay coordinador. \n");
                                timer_to_be_coordinador.cancel();
                                break;
                            case "Status":
                            this.status = true;
                                timer_to_convert_in_coordinador.cancel();
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
		}
        catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void message_from_coordinator(){
        byte buffer []= ("Coordinador " + this.ID + ".").getBytes();
        DatagramPacket packet = new DatagramPacket
        (
            buffer, buffer.length, 
            this.group, this.port
        );
        try {
            this.socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	private void message_to_select(){
        String message = "Eleccion " + this.ID + ".";
        DatagramPacket packet = new DatagramPacket
        (
            message.getBytes(), message.getBytes().length, 
            this.group, this.port
            );
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private ArrayList<String> get_data_from_datagrampacket(String cadena){
        ArrayList<String> lista= new ArrayList<String>();

        String message = "";
        String id_nodo = "";
        short id = 0;
        for(char c:cadena.toCharArray()){
            if(Character.isAlphabetic(c)){
                message += c;
            }
            if(Character.isDigit(c) && id < 4){
                id_nodo += c;
                id ++;
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
		String message = "Leader " + this.ID + ".";
        DatagramPacket packet = new DatagramPacket
        (
            message.getBytes(), message.getBytes().length, 
            this.group, this.port
        );
        try {
            this.socket.send(packet);
            timer_to_be_coordinador.scheduleAtFixedRate(new TimerTask() 
            {
                @Override
                public void run() {
                    if ((!status && elector_lock)
                        || (!is_there_coordinador && !coordinador)) 
                        {
                            text_are_chat_algoritmo_bully.append("Soy ahora el coordinador: " + ID +".\n");
                            message_from_coordinator();
                            coordinador = true;
                            elector_lock = false;
                            is_there_coordinador = true;
                        }
                }
            }, 1500, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void send_message() {
		if(!this.elector_lock && !this.coordinador 
            && !this.is_there_coordinador)
            {
                message_to_select();
                String  message = "Inicia proceso de seleccion. \n";
                text_are_chat_algoritmo_bully.append(message);
            }
            Timer convert_lider = new Timer();
            convert_lider.scheduleAtFixedRate(new TimerTask() {
                public void run(){
                    if(!status && !coordinador && !is_there_coordinador){
                        text_are_chat_algoritmo_bully.append("Soy ahora el coordinador: " + ID +".\n");
                        message_from_coordinator();
                        coordinador = true;
                        is_there_coordinador = true;
                        elector_lock = false;
                    }
                }
            }, 1500, 1200);
	}

    public void notify_disconeccted(){
        String message = "Desconectado " + this.ID + ".";
        DatagramPacket packet = new DatagramPacket
        (
            message.getBytes(), message.getBytes().length, 
            this.group, this.port
        );
        try {
            this.coordinador = false;
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void message_no_leader() {
    	String message = "NoLeader " + this.ID + ".";
        DatagramPacket packet = new DatagramPacket
        (
            message.getBytes(), message.getBytes().length, 
            this.group, this.port
        );
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void message_to_change_status_of_nodo(String address) {
    	String message = "Status " + this.ID + ".";
        try {
        	DatagramPacket packet = new DatagramPacket
            (
                message.getBytes(), message.getBytes().length, 
                InetAddress.getByName(address), this.port
            );
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
