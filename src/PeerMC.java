import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import javax.swing.JTextArea;

public class PeerMC implements Runnable{
	
    private MulticastSocket socket;
    private InetAddress group;
    private DatagramPacket packet;
    private int port;
    private JTextArea text_area_chat;
    
    public PeerMC(String host, int port, JTextArea text_area_chat){
    	
    	this.text_area_chat = text_area_chat;
        try {
            this.socket = new MulticastSocket(port);
            this.group = InetAddress.getByName(host);
            this.port = port;
            this.socket.joinGroup(group);
        } 
        catch (Exception e) {
        	 System.out.println(e.getMessage());
        }
    }

    
    public void send_message(Person person){
    	
    	// Convertir el objeto a un array de bytes
		try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(person);
            byte[] data = byteArrayOutputStream.toByteArray();
            this.packet = new DatagramPacket(data, data.length, group, port);
            this.socket.send(packet);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
    }

    public void run() {
        String message_recived = "";
        Person person = null;
        try {
            while(true){
                this.packet = new DatagramPacket(new byte[1024] , 1024);
                synchronized(this){
                    this.socket.receive(packet);
                    
                    // Convertir los bytes recibidos de nuevo en un objeto Serializable
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    person = (Person) objectInputStream.readObject();
                }
                message_recived = new String(person.getNickname() + ": " + person.getMessage()) + "\n";
                text_area_chat.append(message_recived);
            }
        }
        catch (Exception e) {
        	System.out.println(e.getMessage());
        }
    }
}
