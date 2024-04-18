import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Block {
    private int index;
    private long timestamp;
    private String previousHash;
    private String hash;
    private List<Transaction> transactions;
    private int nonce;
    private int dificultad;

    //generacion del bloque
    public Block(int index, long timestamp, String previousHash, List<Transaction> transactions, int dificultad) {
        this.index = index;
        this.timestamp = timestamp;
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.dificultad = dificultad;
        this.nonce = 0;
        this.hash = calculateHash();
    }
    public String calculateHash() {
        StringBuilder data = new StringBuilder();
        data.append(index)
                .append(timestamp)
                .append(previousHash)
                .append(nonce)
                .append(transactionsToString());

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        byte[] hashBytes = digest.digest(data.toString().getBytes());
        return Base64.getEncoder().encodeToString(hashBytes);
    }
    private String transactionsToString() {
        StringBuilder builder = new StringBuilder();
        for (Transaction tx : transactions) {
            builder.append(tx.toString());
        }
        return builder.toString();
    }
    //resolucion de la prueba de trabajo
    public void mineBlock(){
        String target = new String(new char[dificultad]).replace('\0', '0');
        while (!hash.substring(0, dificultad).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block mined: " + hash);
    }
    //validacion del bloque
    public boolean isValidBlock(Block block, Block previousBlock, int difficulty){
        for (Transaction transaction : block.getTransactions()) {
            if (!transaction.isValid()) {
                return false;
            }
        }

        // Verificar integridad del bloque anterior
        if (!block.getPreviousHash().equals(previousBlock.getHash())) {
            return false;
        }

        // Verificar prueba de trabajo (PoW)
        String target = new String(new char[difficulty]).replace('\0', '0');
        String hash = block.calculateHash();
        if (!hash.substring(0, difficulty).equals(target)) {
            return false;
        }

        // Si todas las condiciones se cumplen, el bloque es válido
        return true;
    }
    //Consenso y propagacion de bloques
    public void handleRecivedBlock(Block block){

    }

    //getters y setters
    public String getHash(){
        return hash;
    }
    public String getPreviousHash(){
        return previousHash;
    }

}
