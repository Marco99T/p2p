import java.util.Date;

public class Transaction {
    private String sender;
    private String recipient;
    private double amount;
    private Date timestamp;
    private String transactionHash;

    public Transaction(String sender, String recipient, double amount, Date timestamp) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.timestamp = timestamp;
        this.transactionHash = calculateHash();
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public double getAmount() {
        return amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    // Método para calcular el hash de la transacción
    private String calculateHash() {
        String data = sender + recipient + amount + timestamp.toString();
        // Implementa aquí la lógica para calcular el hash de la transacción
        return "hash_de_prueba"; // Debes implementar la lógica real
    }

    // Método para verificar la validez de la transacción
    public boolean isValid() {
        // Implementa la lógica para verificar la validez de la transacción
        // Por ejemplo, podrías comprobar si el hash de la transacción es válido
        return true; // En este ejemplo, siempre se considera válida
    }

    // Método toString para representar la transacción como una cadena
    @Override
    public String toString() {
        return "Transaction{" +
                "sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", transactionHash='" + transactionHash + '\'' +
                '}';
    }
}
