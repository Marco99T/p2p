import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import javax.swing.JTextArea;

public class Manager_Files 	extends Thread{
	private MulticastSocket socket;
	private InetAddress host;
	private int port;
	
	private JTextArea chat_files;

	
	public Manager_Files(String host, int port, JTextArea chat_files){
		this.chat_files = chat_files;
		try {
			this.socket = new MulticastSocket(port);
			this.host = InetAddress.getByName(host);
			this.port = port;
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	
	@SuppressWarnings("resource")
	public boolean send_file(String path, String name) {
		/*
		//Ruta del archivo
		File file = new File(path);
		

        // Abrir flujo de entrada para leer el archivo
        FileInputStream file_input = null;
		try {
			file_input = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		// Tamaño del buffer para leer el archivo
        int buffer_size = 1024;
        byte[] buffer = new byte[buffer_size];

        int bytes_read;
        long file_size = file.length();

        // Envío del tamaño del archivo
        String file_size_string = String.valueOf(file_size);
        DatagramPacket file_size_packet = new DatagramPacket(file_size_string.getBytes(), file_size_string.getBytes().length, this.host, this.port);
        try {
			this.socket.send(file_size_packet);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

        // Envío del archivo en paquetes
        try {
			while ((bytes_read = file_input.read(buffer)) != -1) {
				DatagramPacket packet = new DatagramPacket(buffer, bytes_read, host, port);
			    this.socket.send(packet);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
        

        // Cierre del flujo de entrada y el socket
        
        try {
	        file_input.close();
	        //this.socket.close();
        }	catch(IOException e) {
        	System.out.println(e.getMessage());
        }
        */
		
		
		
		try {
			
            // Leer el archivo que deseas enviar
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                DatagramPacket packet = new DatagramPacket(buffer, bytesRead, host, port);
                socket.send(packet);
            }

            System.out.println("Archivo enviado correctamente.");

            // Cerrar el socket
            //this.socket.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
	}
	
	public void run () {
		/*
		long file_size = 0;
        byte[] buffer = new byte[1024];
        
		//Unirme a grupo multicast
		try {
			this.socket.joinGroup(host);
			
			// Recibir el tamaño del archivo
	        DatagramPacket file_size_packet = new DatagramPacket(buffer, buffer.length);
	        try {
				this.socket.receive(file_size_packet);
				String file_size_string = new String(file_size_packet.getData()).trim();
				if (!file_size_string.isEmpty()) {
					file_size = Long.parseLong(file_size_string);
				}
				else {
					System.out.println("No hay datos");
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
	        

	        // Flujo de salida para escribir el archivo
	        FileOutputStream file_output = null;
			try {
				String ruta = "C:" + File.separator;
				String 	directory = "P2P" + File.separator;
				File directory_ = new File(ruta, directory);
				
				// Verificar si la carpeta existe
		        if (!directory_.exists()) {
		        	// Intentar crear la carpeta
		            boolean carpetaCreada = directory_.mkdir();
		            if (carpetaCreada) {
		                System.out.println("La carpeta ha sido creada con éxito.");
		            } else {
		                System.out.println("Error al crear la carpeta.");
		            }
		        } 
				
				file_output = new FileOutputStream(ruta + directory + "testing.png");
			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
			}
			System.out.println("Archivo recibido");
	        // Recepción de paquetes y escritura en el archivo
			
	        while (file_size > 0) {
	            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	            try {
	            	synchronized(this) {
						socket.receive(packet);
						file_output.write(packet.getData(), 0, packet.getLength());
			            file_size -= packet.getLength();
	            	}
				} catch (IOException e) {
					//System.out.println("Error" + e.getMessage());
				}
	        }

	        // Cierre del flujo de salida y el socket
	        try {
				//file_output.close();
				socket.leaveGroup(host);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
	        //socket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
        */
		
		//FALTA SOLUCIONAR PROBLEMA PARA RECIBIR VARIOS ARCHIVOS
		
		try {
			MulticastSocket socket_serve = new MulticastSocket(this.port);
            socket_serve.joinGroup(host);
            String ruta = "C:" + File.separator;
			String 	directory = "P2P" + File.separator;
			
			//Validar directorio		
			File directory_ = new File(ruta, directory);
	        if (!directory_.exists()) {
	            boolean carpetaCreada = directory_.mkdir();
	            if (carpetaCreada) {
	                System.out.println("La carpeta ha sido creada con éxito.");
	            } else {
	                System.out.println("Error al crear la carpeta.");   
	            }
	        }
			
	        // Crear un buffer para recibir los datos
            byte[] buffer = new byte[1024];

            // Crear un paquete para recibir los datos
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Crear el archivo de salida
            FileOutputStream fileOutputStream = new FileOutputStream(ruta + directory + "archivo_recibido.jpg");

            // Recibir y escribir los datos hasta que se reciba el archivo completo
            while (true) {
            	synchronized(this) {
	                socket_serve.receive(packet);
	                fileOutputStream.write(packet.getData(), 0, packet.getLength());
	                if (packet.getLength() < buffer.length) {
	                    break; // Se ha recibido el archivo completo
	                }
            	}
                System.out.println("dentro..");
            }
            fileOutputStream.close();
            System.out.println("Archivo recibido y guardado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
		chat_files.append("Recibibo" + "\n");
    }

}
