package nvh.lightcycle.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Game {

	public static final int WIDTH = 32;
	public static final int HEIGHT = 18;
	public static final int TICK = 1000; //The speed of the light cycle
	
	public int field[][] = new int[WIDTH][HEIGHT];
	public ArrayList<Player> players = new ArrayList<>();
	private Random rnd = new Random();

	public ArrayList<Integer> solidsX = new ArrayList<>();
	public ArrayList<Integer> solidsY = new ArrayList<>();
	
	public int highscore = 0;
	public boolean deadPlayersBecomeSolids = false;
	
	
	public Game() {
	}

	public void update() {

		move();
		checkCollisions();
		checkSolids();
		updateField();
	}
	
	private void move() {
		for (Player p : players) {
			if (p == null) continue;
			p.move();
		}
	}
	
	private void checkCollisions() {
		
		outerLoop:
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p == null) continue;
			
			/* check if the following criteria is satisfied, the player will be dead:
			* - the size (x and y) of the player is bigger then W and H of the game screen.
			* - the segment of player is out of the game screen.
			*  */
			if (	Collections.max(p.segmentsX) >= WIDTH ||
					Collections.max(p.segmentsY) >= HEIGHT ||
					Collections.min(p.segmentsX) < 0 ||
					Collections.min(p.segmentsY) < 0) {
				
				// make the player DEAD:
				if (deadPlayersBecomeSolids) playerToSolids(p);
				players.set(i, null);
				continue;
				
			}
			
			// other players
			int pX = p.segmentsX.get(0);
			int pY = p.segmentsY.get(0);

			boolean pX_equal_qX = false;
			boolean pY_equal_qY = false;

			int length_of_q = 0;

			// set q to be the each other player other than p:
			for (Player q : players) {
				if (q == null) continue;
				// Check if the player hits ifself:
				if (p == q) {
					
					for (int d = 1; d < q.segmentsX.size(); d++) {
						if (q.segmentsX.get(d) == pX && q.segmentsY.get(d) == pY) {
							
							// make p dead:
							System.out.println("checkSolids p == q to make p dead.");
							if (deadPlayersBecomeSolids) playerToSolids(p);
							players.set(i, null);
							continue outerLoop;
						}
					}

				// in case player p hits another:
				}
				else if (q.segmentsX.contains(pX) && q.segmentsY.contains(pY)) {
					length_of_q = q.segmentsX.size();
					for (int d = 1; d < length_of_q; d++) {
						pX_equal_qX = false;
						pY_equal_qY = false;
						if (q.segmentsX.get(d) == pX) {
							pX_equal_qX = true;
						}
						if (q.segmentsY.get(d) == pY) {
							pY_equal_qY = true;
						}
						if (pX_equal_qX && pY_equal_qY ){
							System.out.println("q.segmentsY.get(d) == pY = " + q.segmentsY.get(d));
							System.out.println("q.segmentsX.get(d) == pX = " + q.segmentsX.get(d));
							System.out.println("checkSolids p != q to make p dead.");
							if (deadPlayersBecomeSolids) playerToSolids(p);
							players.set(i, null);
							continue outerLoop;
						}
					}


					
				}
				
			}
			
		}
		
	}
	
	private void checkSolids() {
		
		outerLoop:
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p == null) continue;
			
			for (int j = 0; j < solidsX.size(); j++) {
				
				if (p.segmentsX.get(0) == solidsX.get(j) && p.segmentsY.get(0) == solidsY.get(j)) {
						
					} else {

						// make p dead:
						System.out.println("checkSolids to make p dead.");
						if (deadPlayersBecomeSolids) playerToSolids(p);
						players.set(i, null);
						continue outerLoop;
						
					}
					
				}
				
			}
			
		}

	
	private void updateField() {
		
		field = new int[WIDTH][HEIGHT];

		// solids
		for (int i = 0; i < solidsX.size(); i++) {
			
			// safezone
			if (solidsX.get(i) < 3 && solidsY.get(i) < 3) {
				solidsX.remove(i);
				solidsY.remove(i);
				continue;
			}
			
			field[solidsX.get(i)][solidsY.get(i)] = -1;
		}
		
		// players
		for (Player p : players) {
			if (p == null) continue;

			// segments
			for (int i = 0; i < p.segmentsX.size(); i++) {
				field[p.segmentsX.get(i)][p.segmentsY.get(i)] = p.id;
			}

			// head
			field[p.segmentsX.get(0)][p.segmentsY.get(0)] = -p.id;
		}
	}
	
	private void playerToSolids(Player p) {
		
		for (int i = 0; i < p.segmentsX.size(); i++) {
			int x = p.segmentsX.get(i);
			int y = p.segmentsY.get(i);
			if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
				solidsX.add(x);
				solidsY.add(y);
			}
		}
		
	}
	
}
