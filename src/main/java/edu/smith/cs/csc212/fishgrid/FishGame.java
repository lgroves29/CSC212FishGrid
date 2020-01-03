package edu.smith.cs.csc212.fishgrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class manages our model of gameplay: missing and found fish, etc.
 * @author jfoley
 *
 */
public class FishGame {
	/**
	 * This is the world in which the fish are missing. (It's mostly a List!).
	 */
	World world;
	/**
	 * The player (a Fish.COLORS[0]-colored fish) goes seeking their friends.
	 */
	Fish player;
	/**
	 * The home location.
	 */
	FishHome home;
	/**
	 * These are the missing fish!
	 */
	List<Fish> missing;
	
	/**
	 * These are fish we've found!
	 */
	List<Fish> found;
	
	List<Fish> homeList;
	
	Snail snail; 
	
	
	
	/**
	 * Number of steps!
	 */
	int stepsTaken;
	
	/**
	 * Score!
	 */
	int score;
	
	int rocks = 10;
	
	FallingRock fallingRock;
	
	/**
	 * Create a FishGame of a particular size.
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	public FishGame(int w, int h) {
		world = new World(w, h);
		
		missing = new ArrayList<Fish>();
		found = new ArrayList<Fish>();
		homeList = new ArrayList<Fish>();
		
		// Add a home!
		home = world.insertFishHome();
		
		fallingRock = world.insertFallingRockRandomly();
		
		for (int i=0; i<rocks; i++) {
			world.insertRockRandomly();
		}
		
		// Make the snail!
		
		// Make the player out of the 0th fish color.
		player = new Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);
		snail = world.insertSnailRandomly();
		snail.eyesOpen = true;
		fallingRock = new FallingRock(world);
		
		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < Fish.COLORS.length; ft++) {
			Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);

		}
	}
	
	
	/**
	 * How we tell if the game is over: if missingFishLeft() == 0.
	 * @return the size of the missing list.
	 */
	public int missingFishLeft() {
		return missing.size();
	}
	
	/**
	 * This method is how the Main app tells whether we're done.
	 * @return true if the player has won (or maybe lost?).
	 */
	public boolean gameOver() {
		// TODO(FishGrid) We want to bring the fish home before we win!
		
		return (found.isEmpty() && missing.isEmpty());
	}

	/**
	 * Update positions of everything (the user has just pressed a button).
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1;
				
		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();
		// The player is there, too, let's skip them.
		overlap.remove(this.player);
		
		if (this.player.getX() == home.getX() && this.player.getY() == home.getY()) {
			for (WorldObject wo : found) {
				homeList.add((Fish) wo);
			}	
		}
		
	
		// If we find a fish, remove it from missing.
		for (WorldObject wo : overlap) {
			// It is missing if it's in our missing list.
			if (missing.contains(wo)) {
				// Remove this fish from the missing list.
				missing.remove(wo);
				found.add((Fish) wo);
				// add to found instead! (So we see objectsFollow work!)
				//Increase score when you find a fish!
				if (((Fish) wo).color == 1) {
					score += 15;
				}
				if (((Fish) wo).color == 2) {
					score += 10;
				}
				if (((Fish) wo).color == 3) {
					score += 8;
				}
			}
		}
		
		for (int i = 1; i < found.size(); i ++) {
			found.get(i).bored += 1;
			if (found.get(i).bored >= 20 && found.get(i).lostAgain == true) {
					missing.add(found.get(i));	
				}	
			}
	
		for (Fish fish: missing) {
			if (found.contains(fish)){
				found.remove(fish);
				fish.bored = 0;
			}
			}
		
		
		wanderMissingFish();
		//When fish get added to "found" they will follow the player around.
		World.objectsFollow(player, found);
		// Step any world-objects that run themselves.
		world.stepAll();
		
		for (Fish fish : missing) {
			if (fish.getX() == home.getX() && fish.getY() == home.getY()) {
					homeList.add(fish);
						
			}
		}
		
		for (WorldObject wo : homeList) {
			missing.remove(wo);
			found.remove(wo);
			world.remove(wo);
		}
		
		//every time a fish gets home and added to the homeList
		// it disappears, whether is was brought there or wandered there itself
		
	}
	
	/**
	 * Call moveRandomly() on all of the missing fish to make them seem alive.
	 */
	private void wanderMissingFish() {
		Random rand = ThreadLocalRandom.current();
		for (Fish lost : missing) {
			// 30% of the time, lost fish move randomly.
			if (lost.fastScared = true) {
				if (rand.nextDouble()<.8) {
					lost.moveRandomly();
				}
			}
			else if (rand.nextDouble() < 0.3) {
				lost.moveRandomly();
				
			}
		}
	}

	/**
	 * This gets a click on the grid. We want it to destroy rocks that ruin the game.
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 */
	public void click(int x, int y) {
		// TODO(FishGrid) use this print to debug your World.canSwim changes!
		System.out.println("Clicked on: "+x+","+y+ " world.canSwim(player,...)="+world.canSwim(player, x, y));
		List<WorldObject> atPoint = world.find(x, y);
		// TODO(FishGrid) allow the user to click and remove rocks.
		for (WorldObject it : atPoint) {
			if (it instanceof Rock) {
				world.remove(it);
			}
			
		}

	}
	
}
