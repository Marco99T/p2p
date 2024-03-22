import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Testing {

    public void send_message_to_test(){
        try{
            MulticastSocket socket = new MulticastSocket(6500);
            InetAddress group = InetAddress.getByName("224.0.0.4");
            socket.joinGroup(group);

            String message = "Eleccion 3821.";
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, group, 6500);
            socket.send(packet);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    
    public static void main(String [] args){
        Testing test = new Testing();
        test.send_message_to_test();
    }
}

