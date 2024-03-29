
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JTextArea;

public class Anillo extends Thread {
    private MulticastSocket socket;
    private InetAddress group;

    private int port;
    private int ID;
    private String IP;
    private boolean coordinador = false;
    private boolean status = false;
    private boolean is_there_coordinador;
    private boolean elector_lock = false;
    private JTextArea text_area_chat_algorithm_anillo;
    private List<String> nodos;

    private Timer timer_to_be_coordinador;
    private Timer timer_to_convert;

    public Anillo(String host, int port, JTextArea text_area_chat_algorithm_anillo, int ID, String IP) {
        try {
            this.port = port;
            this.socket = new MulticastSocket(port);
            this.group = InetAddress.getByName(host);
            this.socket.joinGroup(group);
            this.nodos = new ArrayList<String>();
            this.timer_to_be_coordinador = new Timer();
            this.timer_to_convert = new Timer();
            this.ID = ID;
            this.IP = IP;
            this.text_area_chat_algorithm_anillo = text_area_chat_algorithm_anillo;
            this.nodos.add(this.IP);
            verify_leader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        try {
            while(true){
                socket.receive(receivePacket);
                String address = String.valueOf(receivePacket.getAddress().getHostAddress());
                if(!address.equals(this.IP)){
                    String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    String[] parts = receivedData.split(":");

                    int receivedID = Integer.parseInt(parts[0]);
                    InetAddress nextNodeAddress = InetAddress.getByName(parts[1]);
                    String case_messagge = parts[2];
                    System.out.println(receivedID + ": " + nextNodeAddress + ": " + case_messagge);
                    if(!nodos.contains(parts[1])){
                        nodos.add(parts[1]);
                    }

                    if(this.coordinador){
                        message_from_coordinator();
                    }
                    else {
                        switch (case_messagge) {
                            case "Coordinador":
                                if(!is_there_coordinador){
                                    this.elector_lock = false;
                                    this.is_there_coordinador = true;
                                    String message = "El coordinador actual es: " + receivedID + "\n";
                                    text_area_chat_algorithm_anillo.append(message);
                                    timer_to_be_coordinador.cancel();
                                }
                                break;
                            case "Searching":
                                get_ip_and_id_of_this_nodo();
                                break;
                            case "Add":
                                if(!nodos.contains(parts[1])){
                                    nodos.add(parts[1]);
                                }
                                break;
                            case "Eleccion":
                                ArrayList<String> dataList = new ArrayList<>();
                                // Obtener el ArrayList de partes
                                String dataString = parts[3];
                                String[] dataParts = dataString.split(",");
                                Collections.addAll(dataList, dataParts);
                                String siguienteElemento = "";
                                System.out.println(dataList);

                                if (receivedID < this.ID) {
                                    // Forward the message to the next node
                                    text_area_chat_algorithm_anillo.append("Soy candidato ("+ this.ID +") \n");
                                    this.elector_lock = true;

                                    if(dataList.contains(this.IP)){
                                        int indice = dataList.indexOf(this.IP);
                                        // Verificar si el elemento encontrado no es el último de la lista
                                        if (indice < dataList.size() - 1) {
                                            // Obtener el siguiente elemento
                                            siguienteElemento = dataList.get(indice + 1);
                                            System.out.println("El siguiente elemento después de '" + this.IP + "' es: " + siguienteElemento);
                                        } else {
                                            siguienteElemento = dataList.get(0);
                                            System.out.println("'" + this.IP + "' es el último elemento de la lista, no hay un siguiente elemento.");
                                        }
                                    }

                                    String newdataString = convert_arraylist_to_arraystring(nodos);
                                    String forwardData = this.ID + ":" + this.IP + ":" + case_messagge + ":" + newdataString;
                                    DatagramPacket forwardPacket = new DatagramPacket(forwardData.getBytes(), forwardData.getBytes().length, InetAddress.getByName(siguienteElemento), this.port);
                                    socket.send(forwardPacket);
                                } else if (receivedID == this.ID && this.elector_lock) {
                                    // This node is the leader
                                    this.text_area_chat_algorithm_anillo.append("I am the leader with ID: " + ID + "\n");
                                    this.coordinador = true;
                                    this.is_there_coordinador = true;
                                    this.elector_lock = false;
                                } else {
                                    // This node is not the leader, pass it on
                                    text_area_chat_algorithm_anillo.append("No soy candidato. \n");
                                    if(dataList.contains(this.IP)){
                                        int indice = dataList.indexOf(this.IP);
                                        // Verificar si el elemento encontrado no es el último de la lista
                                        if (indice < dataList.size() - 1) {
                                            // Obtener el siguiente elemento
                                            siguienteElemento = dataList.get(indice + 1);
                                            System.out.println("El siguiente elemento después de '" + this.IP + "' es: " + siguienteElemento);
                                        } else {
                                            siguienteElemento = dataList.get(0);
                                            System.out.println("'" + this.IP + "' es el último elemento de la lista, no hay un siguiente elemento.");
                                        }
                                    }
                                    String newdataString = convert_arraylist_to_arraystring(nodos);
                                    String forwardData = receivedID + ":" + nextNodeAddress + ":" + case_messagge + ":" + newdataString;
                                    DatagramPacket forwardPacket = new DatagramPacket(forwardData.getBytes(), forwardData.getBytes().length, InetAddress.getByName(siguienteElemento), this.port);
                                    socket.send(forwardPacket);
                                }
                                break;
                            case "Leader":
                                message_no_leader();
                                break;
                            case "NoLeader":
                                text_area_chat_algorithm_anillo.append("No hay coordinador. \n");
                                timer_to_be_coordinador.cancel();
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void verify_leader() {
        byte[] sendData = (this.ID + ":" + this.IP + ":" + "Leader").getBytes();
		//String message = "Leader " + this.ID + ".";
        DatagramPacket packet = new DatagramPacket
        (
            sendData, sendData.length, 
            this.group, this.port
        );
        try {
            this.socket.send(packet);
            timer_to_be_coordinador.schedule(new TimerTask() 
            {
                @Override
                public void run() {
                    if ((!status && elector_lock)
                        || (!is_there_coordinador && !coordinador)){
                        text_area_chat_algorithm_anillo.append("Soy ahora el coordinador: " + ID +".\n");
                        message_from_coordinator();
                        coordinador = true;
                        elector_lock = false;
                        is_there_coordinador = true;
                    }
                    System.out.println(nodos + " :mostrnado nodos en very_leader");
                }
            }, 1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    public void startElection() {
        try {
            // Broadcast the election message to all nodes
            byte[] sendData = (ID + ":" + IP + ":" + "Searching").getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, group, port);
            socket.send(sendPacket);
            this.timer_to_convert.schedule(new TimerTask() {
                public void run(){
                    try {
                        // Convertir ArrayList a String
                        String dataString = convert_arraylist_to_arraystring(nodos);
                        byte[] sendData = (ID + ":" + IP + ":" + "Eleccion" + ":" + dataString).getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, group, port);
                        socket.send(sendPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void message_from_coordinator(){
        byte[] sendData = (this.ID + ":" + this.IP + ":" + "Coordinador").getBytes();
        //byte buffer []= ("Coordinador " + this.ID + ".").getBytes();
        DatagramPacket packet = new DatagramPacket
        (
            sendData, sendData.length, 
            this.group, this.port
        );
        try {
            this.socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void message_no_leader() {
        byte[] sendData = (this.ID + ":" + this.IP + ":" + "NoLeader").getBytes();
    	//String message = "NoLeader " + this.ID + ".";
        DatagramPacket packet = new DatagramPacket
        (
            sendData, sendData.length, 
            this.group, this.port
        );
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void get_ip_and_id_of_this_nodo() {
        byte[] sendData = (this.ID + ":" + this.IP + ":" + "Add").getBytes();
    	//String message = "NoLeader " + this.ID + ".";
        DatagramPacket packet = new DatagramPacket
        (
            sendData, sendData.length, 
            this.group, this.port
        );
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    private String convert_arraylist_to_arraystring(List<String> nodos2){
        StringBuilder dataBuilder = new StringBuilder();
        for (String data : nodos2) {
            dataBuilder.append(data).append(",");
        }
        //String dataString = dataBuilder.toString();
        return dataBuilder.toString();
    }
}
