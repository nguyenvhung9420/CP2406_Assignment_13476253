package nvh.lightcycle.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.lang.*;

import javax.swing.Timer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import nvh.lightcycle.Request;
import nvh.lightcycle.Response;

public class ServerHandler implements ActionListener {
	
	private LightcycleServer lightcycleServer;
	private Server server;
	private Game game;
	private ArrayList<Integer> deadIds = new ArrayList<>();
	private HashMap<Integer, Integer> clients = new HashMap<>();
	private ArrayList<String> banList = new ArrayList<>();
	public Timer timer;
	

	public ServerHandler(LightcycleServer lightcycleServer) {
		this.lightcycleServer = lightcycleServer;
	}
	
	public void start() {
		
		server = new Server(8192, 8192);
		server.start();
		try {
			
			server.bind(54000, 54001);
			lightcycleServer.buttonStart.setEnabled(false);
			lightcycleServer.buttonStop.setEnabled(true);
			log("Server started");
			
			Kryo kryo = server.getKryo();
			kryo.register(Request.class);
			kryo.register(Response.class);
			
			server.addListener(new Listener() {
				public void received(Connection connection, Object object) {
					if (object instanceof Request) {
						Request request = (Request)object;
						handleRequest(request.content, connection);
					}
				}
				@Override
				public void connected(Connection connection) {
					if (banList.contains(connection.getRemoteAddressTCP().getAddress().toString())) {
						log("Banned IP " + connection.getRemoteAddressTCP().getAddress() + " tried to connect");
						respond("ban;You were banned!", connection);
						connection.close();
					} else {
						log("Client " + connection.getID() + " (" + connection.getRemoteAddressTCP().getAddress() + ") connected");
					}
				}
				@Override
				public void disconnected(Connection connection) {
					log("Client " + connection.getID() + " disconnected");
					
					// remove active players
					if (clients.containsKey(connection.getID())) {
						int playerID = clients.get(connection.getID());
						game.players.set(playerID - 2, null);
						log("Player " + playerID + " removed");
						deadIds.add(playerID);
						clients.remove(connection.getID());
					}
					
				}
			});
			
			game = new Game();
			timer = new Timer(Game.TICK, this);
			timer.start();
			log("Game created");
			
		} catch (Exception e) {
			log(e.toString());
		}
		
	}
	
	public void stop() {
		
		server.stop();
		timer.stop();
		lightcycleServer.buttonStart.setEnabled(true);
		lightcycleServer.buttonStop.setEnabled(false);
		log("Server stopped");

	}
	
	private void log(String message) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        lightcycleServer.textLog.setText(lightcycleServer.textLog.getText() + sdf.format(new Date()) + " " + message + "\n");
        
	}
	
	public void handleCommand(String command) {

		/* --- COMMANDS ---
		 * These commands are used only for the server, not client:
		 *
		 * freeze;[id]
		 * slowdown;[id];[steps]
		 * ban;[ip]
		 * score;[id];[score]
		 * speed;[tick]
		 * gamemode;[mode]	0 normal	1 deadPlayersBecomeSolids
		 *
		 */
		
		System.out.println("Command: " + command);
		
		if (command.startsWith("freeze")) {
			
			Player player = game.players.get(Integer.valueOf(command.substring(7)) - 2);
			if (player == null) return;
			player.freeze = !player.freeze;
			log("Freezed player " + player.id);
			
		} else if (command.startsWith("slowdown")) {
			
			String temp[] = command.split(";");
			Player player = game.players.get(Integer.valueOf(temp[1]) - 2);
			if (player == null) return;
			player.steps = Integer.valueOf(temp[2]);
			log("Slowed player " + player.id + " down to " + player.steps + " steps");
			
		} else if (command.startsWith("ban")) {
			
			String ip = command.substring(4);
			banList.add(ip);
			for (Connection c : server.getConnections()) {
				if (c.getRemoteAddressTCP().getAddress().toString().equals(ip)) {
					respond("ban;You were banned!", c);
					c.close();
				}
			}
			log("Banned IP " + ip);
			
		} else if (command.startsWith("score")) {

			String temp[] = command.split(";");
			Player player = game.players.get(Integer.valueOf(temp[1]) - 2);
			if (player == null) return;
			player.score = Integer.valueOf(temp[2]);
			log("Set score of player " + player.id + " to " + player.score);
			
		} else if (command.startsWith("speed")) {
			
			int tick = Integer.valueOf(command.substring(6));
			timer.setDelay(tick);
			log("Changed game tick to " + tick);
			
		} else if (command.startsWith("gamemode")) {
			
			int mode = Integer.valueOf(command.substring(9));
			switch (mode) {
			
				case 1:
					game.deadPlayersBecomeSolids = true;
					break;
				
				default:	// 0
					game.deadPlayersBecomeSolids = false;
					mode = 0;
					break;
			
			}
			log("Switched gamemode to " + mode);
			
		}
		
	}
	
	private void handleRequest(String content, Connection connection) {
		
		System.out.println("Request: " + content);
		
		if (content.startsWith("ADD_USER")) {
			
			Player newPlayer = new Player(game.players.size() + 2, content.substring(9));
			game.players.add(newPlayer);
			game.update();
			respond("setID;" + newPlayer.id, connection);
			clients.put(connection.getID(), newPlayer.id);
			
			// update colors
			Response response = new Response();
			response.content = "colors;" + newPlayer.id + ":" + newPlayer.color;
			server.sendToAllTCP(response);
			
			log("Player " + newPlayer.id + " joined the game (Client " + connection.getID() + ")");
			
		} else if (content.startsWith("direction")) {
			
			String temp[] = content.split("_");
			Player player = game.players.get(Integer.valueOf(temp[1]) - 2);
			if (player == null) return;

			if (Math.abs((player.direction - Integer.valueOf(temp[3]))) != 2
				&& Math.abs((player.direction - Integer.valueOf(temp[3]))) != 0){
					player.direction = Integer.valueOf(temp[3]);
					player.score++;
			}

		} else if (content.startsWith("getHighscore")) {
			
			respond("highscore;" + game.highscore, connection);
			
		} else if (content.startsWith("getColors")) {
			
			String colors = "";
			for (Player p : game.players) {
				if (p == null) continue;
				colors += p.id + ":" + p.color + ";";

			}
			if (colors.length() == 0) return;
			colors = colors.substring(0, colors.length() - 1);
			respond("colors;" + colors, connection);

			//Debug:
			System.out.println("The current color is " + colors);
		}
		
	}
	
	private void respond(String content, Connection connection) {
		Response response = new Response();
		response.content = content;
		connection.sendTCP(response);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		// update
		game.update();
		
		// send to clients
		String raw = "";
		for (int y = 0; y < Game.HEIGHT; y++) {
			for (int x = 0; x < Game.WIDTH; x++) {
				raw += String.valueOf(game.field[x][y]) + ":";
			}
			raw = raw.substring(0, raw.length() - 1);
			raw += ";";
		}
		raw = raw.substring(0, raw.length() - 1);
		
		
		Response response = new Response();
		response.content = "update;" + raw;
		server.sendToAllTCP(response);
		
		
		// scores and dead updates
		for (int i = 0; i < game.players.size(); i++) {
			Player p = game.players.get(i);
			
			if (p != null && p.updateScore) {
				response = new Response();
				response.content = "score;" + p.id + ";" + p.score;
				server.sendToAllTCP(response);
				p.updateScore = false;
				
				if (p.score > game.highscore) {
					game.highscore = p.score;
					response = new Response();
					response.content = "highscore;" + game.highscore;
					server.sendToAllTCP(response);
					log("New highscore by player " + p.id + ": " + game.highscore);
				}
				
			} else if (p == null && !deadIds.contains(i + 2)) {
				System.out.println("This one is happening");
				deadIds.add(i + 2);
				response = new Response();
				response.content = "dead;" + (i + 2);
				server.sendToAllTCP(response);
				log("Player " + (i + 2) + " died");
			}
			
		}
		
	}

}
