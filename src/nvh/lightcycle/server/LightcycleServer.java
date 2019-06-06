package nvh.lightcycle.server;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import javax.swing.JButton;
import javax.swing.JTextPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class LightcycleServer extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private ServerHandler handler;
	private JPanel contentPane;
	public JButton buttonStart;
	public JButton buttonStop;
	public JTextPane textLog;
	private JTextField textCmd;


	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LightcycleServer frame = new LightcycleServer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * create the frame.
	 */
	public LightcycleServer() {
		setTitle("Player");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		// create and add the panel:
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);

		// Start button:
		buttonStart = new JButton("Start");
		buttonStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handler = new ServerHandler(LightcycleServer.this);
				handler.start();
			}
		});
		panel.add(buttonStart);

		// Stop button:
		buttonStop = new JButton("Stop");
		buttonStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handler.stop();
			}
		});
		buttonStop.setEnabled(false);
		panel.add(buttonStop);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		textLog = new JTextPane();
		textLog.setEnabled(false);
		textLog.setEditable(false);
		scrollPane.setViewportView(textLog);
		
		textCmd = new JTextField();
		textCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (handler != null) {
					handler.handleCommand(textCmd.getText());
				}
				textCmd.setText("");
			}
		});
		contentPane.add(textCmd, BorderLayout.SOUTH);
		textCmd.setColumns(10);
		
		DefaultCaret caret = (DefaultCaret)textLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
	}

}
