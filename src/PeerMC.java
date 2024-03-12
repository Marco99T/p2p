import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import javax.swing.JTextArea;

public class PeerMC implements Runnable{
	
    private MulticastSocket socket;
    private InetAddress host;
    private DatagramPacket packet;
    private int port;
    private JTextArea text_area_chat;
    
    public PeerMC(String host, int port, JTextArea text_area_chat){
    	
    	this.text_area_chat = text_area_chat;
        try {
            this.socket = new MulticastSocket(port);
            this.host = InetAddress.getByName(host);
            this.port = port;
        } 
        catch (UnknownHostException e) {
        	 System.out.println(e.getMessage());
        }
        catch (IOException e) {
        	System.out.println(e.getMessage());
        }
    }

    
    public void send_message(Person person){
    	
    	// Convertir el objeto a un array de bytes
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
		try {
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(person);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
        byte[] data = byteArrayOutputStream.toByteArray();
        
        // Crear el DatagramPacket con los datos y la dirección del destinatario
         this.packet = new DatagramPacket(data, data.length, host, port);

    	
    	//byte[] message = new byte[1024];
        //message = message_to_send.getBytes();
        //this.packet = new DatagramPacket(message, message.length, host, port);
        try {
            this.socket.send(packet);
            //text_area_chat.append("Yo: " + message);
            //System.out.println("Yo" + message);
        } 
        catch (IOException e) {
        	System.out.println(e.getMessage());
        }
    }

    public void run() {
        String message_recived = "";
        Person person = null;
        try {
            this.socket.joinGroup(host);
            while(true){
                this.packet = new DatagramPacket(new byte[1024] , 1024);
                synchronized(this){
                    this.socket.receive(packet);
                    
                    // Convertir los bytes recibidos de nuevo en un objeto Serializable
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    try {
                    	person = (Person) objectInputStream.readObject();
					} catch (ClassNotFoundException e) {
						System.out.println(e.getMessage());
					}
                }
                //message_recived = new String(packet.getData()) + "\n";
                message_recived = new String(person.getNickname() + ": " + person.getMessage()) + "\n";
                text_area_chat.append(message_recived);
                //System.out.println("Recibio: " + message_recived);
            }
        }
        catch (IOException e) {
        	System.out.println(e.getMessage());
        }
    }
}
