
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JTextArea;

public class Anillo extends Thread {
    private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private int ID;
    private String IP;
    private boolean lider = false;
    private int liderID = -1;
    private JTextArea textArea;
    private List<Integer> vecinos = new ArrayList<>();

    public Anillo(String host, int port, JTextArea textArea) {
        try {
            this.port = port;
            this.textArea = textArea;
            this.socket = new MulticastSocket(port);
            this.group = InetAddress.getByName(host); // Dirección de grupo multicast
            this.socket.joinGroup(group);
            this.ID = Integer.parseInt(InetAddress.getLocalHost().getHostAddress().substring(6).replace(".", ""));
			this.IP = String.valueOf(InetAddress.getLocalHost().getHostAddress());
            mostrarMensaje("Nodo iniciado. ID: " + ID);
        } catch (IOException e) {
            mostrarMensaje("Error al iniciar el nodo: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                procesarMensaje(message);
            } catch (IOException e) {
                mostrarMensaje("Error al recibir datos: " + e.getMessage());
            }
        }
    }

    public void iniciarElección() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!lider && liderID == -1) {
                    enviarMensaje("ELECCIÓN " + ID);
                    mostrarMensaje("Soy candidato para liderazgo.");
                }
            }
        }, 0, 5000); // Verificar cada 5 segundos
    }

    private void procesarMensaje(String message) {
        String[] parts = message.split(" ");
        String messageType = parts[0];
        int senderID = Integer.parseInt(parts[1]);

        switch (messageType) {
            case "ELECCIÓN":
                if (senderID > ID) {
                    enviarMensaje("ELECCIÓN " + senderID);
                    mostrarMensaje("Soy candidato para liderazgo.");
                } else if (senderID < ID) {
                    // Ignorar elecciones de nodos con ID más bajo
                } else {
                    // Tengo el mismo ID, soy el líder
                    lider = true;
                    liderID = ID;
                    mostrarMensaje("¡Soy el líder! ID: " + liderID);
                    enviarMensaje("LIDER " + liderID);
                }
                break;
            case "LIDER":
                int newLiderID = Integer.parseInt(parts[1]);
                liderID = newLiderID;
                mostrarMensaje("Nuevo líder elegido. ID: " + liderID);
                break;
            default:
                mostrarMensaje("Mensaje no reconocido: " + message);
                break;
        }
    }

    private void enviarMensaje(String message) {
        try {
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, group, port);
            socket.send(packet);
        } catch (IOException e) {
            mostrarMensaje("Error al enviar mensaje: " + e.getMessage());
        }
    }

    private int obtenerID() {
        // Lógica para obtener un ID único para este nodo
        return 1; // Por ahora, un ID fijo
    }

    private void mostrarMensaje(String message) {
    	textArea.append(message + "\n");
        System.out.println(message);
    }
}
