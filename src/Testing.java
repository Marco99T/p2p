import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class Testing {

    /*public void send_message_to_test(){
        try{
            MulticastSocket socket = new MulticastSocket(6500);
            InetAddress group = InetAddress.getByName("224.0.0.4");
            socket.joinGroup(group);

            String message = "Status 3821.";
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName("192.168.2.192"), 6500);
            socket.send(packet);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    
    public static void main(String [] args){
        Testing test = new Testing();
        test.send_message_to_test();
    }*/
    public static void main(String[] args) {
        // Crear algunas transacciones
        Transaction transaction1 = new Transaction("sender1", "recipient1", 10.0);
        Transaction transaction2 = new Transaction("sender2", "recipient2", 20.0);
        Transaction transaction3 = new Transaction("sender3", "recipient3", 30.0);

        // Agregar las transacciones a una lista
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);

        // Crear el bloque génesis
        Block genesisBlock = new Block(0, System.currentTimeMillis(), "0000000000000000", transactions, 3);
        Block previousBlock = genesisBlock;

        // Minar el bloque génesis
        genesisBlock.mineBlock();
        System.out.println("se mino el bloque genesis");

        // Agregar el bloque génesis a la cadena de bloques
        genesisBlock.handleRecivedBlock(genesisBlock);
        System.out.println("genesis se agrego a la cadena");
        System.out.println("empieza a minar");

        // Crear y minar los siguientes dos bloques
        for (int i = 1; i <= 2; i++) {
            Block block = new Block(i, System.currentTimeMillis(), previousBlock.getHash(), transactions, 3);
            block.mineBlock();
            block.handleRecivedBlock(block);
            previousBlock = block;
        }

        System.out.println("empieza la verificacion de validez");
        // Verificar la validez de los bloques
        for (int i = 1; i < Block.getBlockchain().size(); i++) {
            Block currentBlock = Block.getBlockchain().get(i);
            Block previousBlocka = Block.getBlockchain().get(i - 1);

            if (!currentBlock.isValidBlock(currentBlock, previousBlocka, 3)) {
                System.out.println("El bloque " + currentBlock.getIndex() + " no es válido.");
                return;
            }
        }
        System.out.println("Todos los bloques son válidos.");
    }

}

