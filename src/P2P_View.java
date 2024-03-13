import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Color;

public class P2P_View extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
    private JTextField text_send_message;
    private JTextArea text_area_chat;
    private JTextArea text_area_chat_algoritmos_1;
    private JTextField text_nickname;
    private JTextArea text_area_chat_files;
    
    
    private PeerMC peer;
    private Person person;
    private Manager_Files manager;
    private Anillo anillo;
    

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					P2P_View frame = new P2P_View();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public P2P_View() {
		setAutoRequestFocus(false);
		setTitle("P2P");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1258, 700);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel_algoritmo_1 = new JPanel();
		panel_algoritmo_1.setBackground(Color.LIGHT_GRAY);
		panel_algoritmo_1.setBounds(10, 74, 240, 580);
		contentPane.add(panel_algoritmo_1);
		panel_algoritmo_1.setLayout(null);
		
		text_area_chat_algoritmos_1 = new JTextArea();
		text_area_chat_algoritmos_1.setEditable(false);
		text_area_chat_algoritmos_1.setFont(new Font("Monospaced", Font.ITALIC, 12));
		
		JScrollPane scroll_chat_algoritmos_1 = new JScrollPane( text_area_chat_algoritmos_1);
		scroll_chat_algoritmos_1.setBounds(0, 60, 240, 520);
		panel_algoritmo_1.add(scroll_chat_algoritmos_1);
		
		
		JButton btn_bully = new JButton("Bully");
		btn_bully.setBounds(0, 0, 240, 30);
		panel_algoritmo_1.add(btn_bully);
		
		JButton btn_anillo = new JButton("Anillo");
		btn_anillo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		btn_anillo.setBounds(0, 30, 240, 30);
		panel_algoritmo_1.add(btn_anillo);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.LIGHT_GRAY);
		panel_1.setBounds(320, 74, 240, 580);
		contentPane.add(panel_1);
		
		JPanel panel_chat = new JPanel();
		panel_chat.setBounds(630, 74, 280, 580);
		contentPane.add(panel_chat);
		panel_chat.setLayout(null);
		
		text_area_chat = new JTextArea();
		text_area_chat.setEditable(false);
		text_area_chat.setFont(new Font("Monospaced", Font.ITALIC, 12));
		
		JScrollPane scroll_chat = new JScrollPane(text_area_chat);
		scroll_chat.setBounds(0, 0, 280, 520);
		panel_chat.add(scroll_chat);
		
		JPanel panel_send_message = new JPanel();
		panel_send_message.setBounds(0, 550, 280, 30);
		panel_chat.add(panel_send_message);
		panel_send_message.setLayout(new BorderLayout(0, 0));
		
		text_send_message = new JTextField();
		text_send_message.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send_message();
			}
		});
		text_send_message.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel_send_message.add(text_send_message, BorderLayout.CENTER);
		
		JButton btn_send_message = new JButton("Enviar");
		btn_send_message.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send_message();
			}
		});
		panel_send_message.add(btn_send_message, BorderLayout.EAST);
		
		JPanel panel_EOD = new JPanel();
		panel_EOD.setBackground(Color.LIGHT_GRAY);
		panel_EOD.setBounds(980, 74, 240, 580);
		contentPane.add(panel_EOD);
		panel_EOD.setLayout(null);
		
		JButton btn_upload_file = new JButton("Subir");
		btn_upload_file.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    
                    
                    boolean status = manager.send_file(selectedFile.getAbsolutePath(), fileChooser.getName());
                    if(status) {
                    	JOptionPane.showMessageDialog(null, "Enviado ");
                    }
                    else {
                    	JOptionPane.showMessageDialog(null, "Error al enviar archivo ");
                    }
                }
				
			}
		});
		btn_upload_file.setBounds(0, 550, 120, 30);
		panel_EOD.add(btn_upload_file);
		
		JButton btn_send_file = new JButton("Enviar");
		btn_send_file.setBounds(120, 550, 120, 30);
		panel_EOD.add(btn_send_file);
		
		text_area_chat_files = new JTextArea();
		text_area_chat_files.setEditable(false);
		
		JScrollPane scroll_chat_files = new JScrollPane(text_area_chat_files);
		scroll_chat_files.setBounds(0, 0, 240, 520);
		panel_EOD.add(scroll_chat_files);
		
		
		JLabel lblNewLabel = new JLabel("Algoritmos de eleccion");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(10, 10, 240, 45);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Algortimos de consorcio");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel_1.setBounds(320, 10, 240, 45);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Chat");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel_2.setBounds(630, 10, 280, 45);
		contentPane.add(lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel("Espacio de objetos distribuido");
		lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_3.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel_3.setBounds(980, 10, 240, 45);
		contentPane.add(lblNewLabel_3);
		
		JLabel lbl_nickname = new JLabel("NICKNAME");
		lbl_nickname.setFont(new Font("Arial", Font.BOLD, 12));
		lbl_nickname.setHorizontalAlignment(SwingConstants.CENTER);
		lbl_nickname.setBounds(0, 520, 110, 30);
		panel_chat.add(lbl_nickname);
		
		text_nickname = new JTextField();
		text_nickname.setFont(new Font("Arial", Font.ITALIC, 12));
		text_nickname.setBounds(110, 520, 170, 30);
		panel_chat.add(text_nickname);
		text_nickname.setColumns(10);
		
		start_objects("224.0.0.4", 5000);
		
	}
	
	
	private void start_objects(String host, int port) {
		peer = new PeerMC(host, port, text_area_chat);	
		new Thread(peer).start();
		manager = new Manager_Files(host, port+1000, text_area_chat_files);
		manager.start();
		//new Thread(manager).start();
		anillo = new Anillo(host, port, text_area_chat_algoritmos_1);
		anillo.start();
	}
	

	private void send_message() {
		person = new Person(text_nickname.getText(), text_send_message.getText());
		peer.send_message(person);
		text_send_message.setText("");
    }
	
	private void send_file() {
		
	}
}
