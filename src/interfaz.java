import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;

public class interfaz{

    private JFrame ventana;
    private JPanel panel;
    private JScrollPane scroll_chat;
    private JTextArea text_area_chat;
    private JLabel label_chat;
    private JButton button_send_message;
    private JTextArea text_area_read_message;
    
    public interfaz(){

        iniciar_componentes();
        ventana.setVisible(true);
    }

    private void iniciar_componentes(){
        ventana = new JFrame();
        ventana.setSize(900, 720);
        ventana.setTitle("Chat P2P");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

         // Creamos los contenedores para cada sección
        JPanel panelBotones1 = new JPanel();
        panelBotones1.add(new JButton("Botón 1"));
        panelBotones1.add(new JButton("Botón 2"));

        JPanel panelBotones2 = new JPanel();
        panelBotones2.add(new JButton("Botón 3"));
        panelBotones2.add(new JButton("Botón 4"));

        JPanel panelTexto = new JPanel();
        JTextArea textArea = new JTextArea(10, 30);
        JButton enviarButton = new JButton("Enviar");
        JTextField textField = new JTextField(20);
        panelTexto.add(new JScrollPane(textArea));
        panelTexto.add(textField);
        panelTexto.add(enviarButton);

        JPanel panelArchivos = new JPanel();
        panelArchivos.add(new JButton("Subir archivo"));
        panelArchivos.add(new JButton("Descargar archivo"));

        // Agregamos los contenedores al frame utilizando BorderLayout
        ventana.getContentPane().setLayout(new BorderLayout());
        ventana.getContentPane().add(panelBotones1, BorderLayout.NORTH);
        ventana.getContentPane().add(panelBotones2, BorderLayout.WEST);
        ventana.getContentPane().add(panelTexto, BorderLayout.CENTER);
        ventana.getContentPane().add(panelArchivos, BorderLayout.SOUTH);
    }
}

class principal{
    public static void main(String [] args){
        interfaz p2p = new interfaz();
    }
}
