package nvh.lightcycle.client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.InetAddress;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import nvh.lightcycle.Request;
import nvh.lightcycle.Response;
import nvh.lightcycle.server.Game;

public class ClientHandler implements KeyListener {
	
	private LightcycleClient lightcycleClient;
	Client client = new Client(8192, 8192);
	public int id = 0;
	public int score = 0;
	public int highscore = 0;
	public boolean ingame = false;
	public Timer reconnectionTimer;
	

	public ClientHandler(LightcycleClient lightcycleClient) {
		this.lightcycleClient = lightcycleClient;
	}
	
	public void start() {
		
		client.start();
		try {

			Kryo kryo = client.getKryo();
			kryo.register(Request.class);
			kryo.register(Response.class);

			//In case want to input manually the IP host:
			//InetAddress address = client.discoverHost(54001, 5000);
			InetAddress address = InetAddress.getByName(JOptionPane.showInputDialog("Enter server IP"));
			
			client.connect(5000, address, 54000, 54001);
			
			client.addListener(new Listener() {
				public void received(Connection connection, Object object) {
					if (object instanceof Response) {
						Response response = (Response)object;
						handleResponse(response.content, connection);
					}
				}

				@Override
				public void disconnected(Connection connection) {
					System.out.println("Connection lost!");

					// try to reconnect
					lightcycleClient.board.text = "Connection lost! Trying to reconnect...";
					lightcycleClient.board.repaint();
					ingame = false;
					reconnectionTimer = new Timer(3000, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								client.reconnect(5000);
								reconnectionTimer.stop();

								lightcycleClient.board.id = 9999;
								request("getHighscore");
								request("getColors");
								
							} catch (IOException e1) {
								System.out.println(e1.toString());
							}
						}
					});
					reconnectionTimer.setRepeats(true);
					reconnectionTimer.start();
					
				}
			});
			
			lightcycleClient.addKeyListener(this);
			
			request("getHighscore");
			request("getColors");
			
		} catch (Exception e) {
			System.out.println(e.toString());
			
			if (e.toString().contains("host cannot be null.")) {
				
				int result = JOptionPane.showOptionDialog(null, "No server found! Start server first, then click 'Retry'.", "Error", 
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, 
						new String[]{"Retry", "Cancel"}, "Retry");
				
				if (result == 0) {
					start();
				} else {
					System.exit(1);
				}
				
			}
			
		}
		
	}
	
	public void stop() {
		
		client.close();
		client.stop();

	}
	
	private void handleResponse(String content, Connection connection) {

		if (!content.startsWith("update")) System.out.println("Response: " + content);
		
		
		if (content.startsWith("setID")) {
			
			id = Integer.valueOf(content.substring(6));
			lightcycleClient.board.id = id;
			score = 0;
			lightcycleClient.board.text = "Score: " + score + " | Highscore: " + highscore;
			ingame = true;
			
		} else if (content.startsWith("update")) {
			
			String raw = content.substring(7);
			String tempY[] = raw.split(";");
			int field[][] = new int[Game.WIDTH][Game.HEIGHT];
			
			for (int y = 0; y < Game.HEIGHT; y++) {
				String tempX[] = tempY[y].split(":");
				for (int x = 0; x < Game.WIDTH; x++) {
					field[x][y] = Integer.valueOf(tempX[x]);
				}
			}
			
			// display start point
			if (!ingame) {
				field[0][0] = -9999;
			}
			
			lightcycleClient.board.field = field;
			lightcycleClient.board.repaint();
			
		} else if (content.startsWith("score")) {
			
			String temp[] = content.split(";");
			if (Integer.valueOf(temp[1]) == id) {
				score = Integer.valueOf(temp[2]);
				lightcycleClient.board.text = "Score: " + score + " | Highscore: " + highscore;
				lightcycleClient.board.repaint();
			}
			
		} else if (content.startsWith("highscore")) {
			
			highscore = Integer.valueOf(content.substring(10));
			if (ingame) {
				lightcycleClient.board.text = "Score: " + score + " | Highscore: " + highscore;
			} else {
				lightcycleClient.board.text = "Press SPACE to play. | Highscore: " + highscore;
			}
			lightcycleClient.board.repaint();
			
		} else if (content.startsWith("dead")) {
			
			String temp[] = content.split(";");
			if (Integer.valueOf(temp[1]) == id) {
				lightcycleClient.board.text = "GAME OVER! Press SPACE to replay. | Highscore: " + highscore;
				lightcycleClient.board.id = 9999;
				lightcycleClient.board.repaint();
				ingame = false;
				System.out.println("GAME OVER");
			}
			
		} else if (content.startsWith("ban")) {
			
			lightcycleClient.board.text = content.substring(4);
			lightcycleClient.board.repaint();
			ingame = false;
			
		} else if (content.startsWith("colors")) {
		
			for (String s : content.substring(7).split(";")) {
				String temp[] = s.split(":");
				lightcycleClient.board.otherColor.put(Integer.valueOf(temp[0]), Color.decode(temp[1]));
			}
				
		}
		
	}
	
	private void request(String content) {
		Request request = new Request();
		request.content = content;
		client.sendTCP(request);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		
		int key = e.getKeyCode();
		int direction = -1;
		
		if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
			direction = 0;
			
		} else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
			direction = 1;
			
		} else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
			direction = 2;
			
		} else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
			direction = 3;

		} else if (key == KeyEvent.VK_ENTER ) {
			direction = 4;

		} else if (key == KeyEvent.VK_SPACE && !ingame) {
			
			// wanna play
			request("ADD_USER;#" + Integer.toHexString(lightcycleClient.board.myColor.getRGB()).substring(2));
			
		}
		
		if (direction != -1 && ingame) {
			request("direction:USER_" + String.valueOf(id) + "_TURNS_" + String.valueOf(direction));
			if (direction == 0){
				System.out.println("USER " + id + " TURNS RIGHT");
			}else if (direction == 1){
				System.out.println("USER " + id + " GOES UP");
			} else if (direction == 2){
				System.out.println("USER " + id + " TURNS LEFT");
			} else if (direction == 3){
				System.out.println("USER " + id + " GOES DOWN");
			} else if (direction == 4){
				System.out.println("USER " + id + " TURNS NOTHING");
			}
		}
		
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

}
