import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.AccessController;
import java.security.Permission;

import javax.annotation.processing.FilerException;
import javax.swing.JTextArea;

public class Manager_Files 	extends Thread{
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	private String IP;
	
	private JTextArea text_area_chat_files;

	
	public Manager_Files(String address, int port, JTextArea chat_files){
		this.text_area_chat_files = chat_files;
		try {
			this.socket = new MulticastSocket(port);
			this.group = InetAddress.getByName(address);
			this.port = port;
			this.socket.joinGroup(group);
			this.IP = String.valueOf(InetAddress.getLocalHost().getHostAddress());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	
	public void send_file(String path, String name) {
		
		try {
            // Leer el archivo que deseas enviar
            File file = new File(path);
            String file_info = name + ";" + file.length();
            byte[] file_info_data = file_info.getBytes();
            int bytes_readed;
            byte[] buffer = new byte[1024];
            
            FileInputStream file_input_stream = new FileInputStream(file);
            DatagramPacket packet_info = new DatagramPacket(file_info_data, file_info_data.length, group, port);
            socket.send(packet_info);
            
            while ((bytes_readed = file_input_stream.read(buffer)) != -1) {
            	DatagramPacket packet_data = new DatagramPacket(buffer, bytes_readed, group, port);
                socket.send(packet_data);
            }
            text_area_chat_files.append("Achivo: " +name+ " enviado." + "\n");
            //this.socket.close();
            file_input_stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	//BUG OPENING THE FILE.
	@SuppressWarnings("resource")
	public void run () {
		try {
            
			String 	directory = File.separator + "files_recived" + File.separator;
			String directory_actually = System.getProperty("user.dir");
			
			//Validar directorio		
			File directory_ = new File(directory_actually, directory);
	        if (!directory_.exists()) {
	            boolean drectory_maked = directory_.mkdir();
	            if (!drectory_maked) {
	            	throw new FileNotFoundException("Error al crear el archivo");
	            }
	        }
			   
	        while(true) {
	            DatagramPacket packet_info = new DatagramPacket(new byte [1024], 1024, group, port);
	            socket.receive(packet_info);
	            String address = String.valueOf(packet_info.getAddress().getHostAddress());
				
				if(!address.equals(this.IP)){
	            
		            String file_info = new String(packet_info.getData(), 0, packet_info.getLength());
		            String[] file_info_array = file_info.split(";");
		            String file_name = file_info_array[0];
		            long file_size = Long.parseLong(file_info_array[1]);
		            
		            String rute = directory_actually + directory + file_name;
		            FileOutputStream file_output_stream = new FileOutputStream(rute.trim());
		            byte[] buffer = new byte[1024];
		            long total_bytes_received = 0;
		            
		            while (total_bytes_received < file_size) {
		                DatagramPacket packet_data = new DatagramPacket(buffer, buffer.length);
		                socket.receive(packet_data);
		                file_output_stream.write(packet_data.getData(), 0, packet_data.getLength());
		                total_bytes_received += packet_data.getLength();
		            }
		            
		            if (total_bytes_received == file_size) {
		                System.out.println("Se ha recibido el archivo correctamente: " + file_name);
		            } else {
		                throw new FilerException("Error al guardar el archivo");
		            }
		            
		            file_output_stream.close();
		            text_area_chat_files.append("Achivo: " + file_name+ " recivido." + "\n");
				}
	        }
            //socket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
