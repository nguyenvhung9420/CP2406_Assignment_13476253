package nvh.lightcycle.client;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LightcycleClient extends JFrame {

	private static final long serialVersionUID = 1L;
	
	public ClientHandler handler;
	private JPanel contentPane;
	public Board board;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LightcycleClient frame = new LightcycleClient();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public LightcycleClient() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				handler.stop();
			}
		});
		setTitle("Light Cycle Player");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		board = new Board();
		contentPane.add(board, BorderLayout.CENTER);
		pack();
		
		handler = new ClientHandler(this);
		handler.start();
		
	}

}
