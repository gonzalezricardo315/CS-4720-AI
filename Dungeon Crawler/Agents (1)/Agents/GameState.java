import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;


public class GameState implements Comparable<GameState>{
	
	private static int numNodesExplored = 0;
	private static int numNodesCreated = 0;

	public static final char WALL = Screen.S_WALL;
	public static final char GROUND = Screen.S_GROUND;
	public static final char GOLD = Screen.S_GOLD;
	public static final char ELIXER = Screen.S_ELIXER;
	public static final char ENEMY = Screen.S_ENEMY;
	public static final char PLAYER = Screen.S_PLAYER;
	public static final char PLAYER_AND_GOLD = Screen.S_PLAYER_AND_GOLD;
	public static final char PLAYER_AND_MISSING_GOLD = Screen.S_PLAYER_AND_MISSING_GOLD;
	public static final char MISSING_GOLD = Screen.S_MISSING_GOLD;

	
	
	//Probably faster to compare characters (==) than strings (.equals())
	private final char[][] map;

	//x and y are the locations of the character on the map
	private final int x;
	private final int y;
	
	//Distance is the current distance the character traveled to get to this location, not how far remaining
	private final int currentDistance;
	
	//Parent is the GameState that generated this one
	private GameState parent;
	
	//Action will ultimately be the action we take - Note: Do not create "new" ones, just use the existing static ones
	private AgentAction action;

	//For now, we will say that two states are the same if they have the same string representation
	//Currently build like "x y mapCharactersHere"
	private final String stringRepresentationOfState; //We may need to do lazy instantiation on this, if it is slow
	
	//This constructor assumes that there is an 'S' somewhere on the map and the game is just starting
	//i.e., no parent node, and no distance
	public GameState(char[][] map) {
		numNodesCreated++; //One more node created
		
		parent = null;
		currentDistance = 0;

		action = null;
		
		int newX = -1;
		int newY = -1;
				
		this.map = new char[map.length][map[0].length];
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[i].length; j++) {
				//Copy things over
				this.map[i][j] = map[i][j];

				//Patch if necessary
				if(this.map[i][j] == MISSING_GOLD) {
					s.append(GROUND); //Keep the missing gold, but put ground in our state
				}
				else if(this.map[i][j] == PLAYER_AND_MISSING_GOLD) {
					newX = i;
					newY = j;
					s.append(PLAYER); //Keep the player and missing gold, but put Player in our state
				}
				else if(this.map[i][j] == PLAYER) {
					newX = i;
					newY = j;
					this.map[i][j] = GROUND;
					s.append(this.map[i][j]);
				}
				else if(this.map[i][j] == PLAYER_AND_GOLD) {
					newX = i;
					newY = j;
					this.map[i][j] = GOLD;
					s.append(this.map[i][j]);
				} else {
					s.append(this.map[i][j]);
				}
			}
		}
		
		x = newX;
		y = newY;
		stringRepresentationOfState = x + " " + y + " " + s;
	}

	//This constructor assumes that it is being created as a child from the existing s
	public GameState(GameState s, int newX, int newY) {
		numNodesCreated++; //One more node created

		parent = s;
		currentDistance = s.currentDistance+1;

		action = null;

		this.x = newX;
		this.y = newY;
		this.map = new char[s.map.length][s.map[0].length];
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < s.map.length; i++) {
			for(int j = 0; j < s.map[i].length; j++) {
				this.map[i][j] = s.map[i][j];
				//Patch if necessary
				if(this.map[i][j] == MISSING_GOLD) {
					sb.append(GROUND); //Keep the missing gold, but put ground in our state
				} else {
					sb.append(map[i][j]);
				}
			}
		}
		stringRepresentationOfState = x + " " + y + " " + sb;
	}

	@Override
	public String toString() {
		//All one line, so that we can use the string as a "hash" to quickly determine if things are already being searched
		return stringRepresentationOfState;
	}
	
	//These 2 methods make sure the HashSet works correctly
	@Override
	public boolean equals(final Object o)
	{
	    if (o instanceof GameState)
	    {
	        return stringRepresentationOfState.equals(((GameState)o).stringRepresentationOfState);
	    }
	    return false;
	}

	@Override
	public int hashCode()
	{
	    return stringRepresentationOfState.hashCode();
	}

	@Override
	public int compareTo(GameState o) {
		//TODO - you can leave this for now, but when we are implementing a priority queue, we will likely have to change this
		//To compare estimated distance from goal
		return stringRepresentationOfState.compareTo(o.stringRepresentationOfState);
	}

	
	public void printMaze() {
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[i].length; j++) {
				if(i == x && j == y) {
					if(map[i][j] == GOLD) {
						System.out.print(PLAYER_AND_GOLD);
					}
					else {
						System.out.print(PLAYER);
					}
				}
				else {
					System.out.print(map[i][j]);
				}
			}
			System.out.println();
		}
	}

	public Queue<AgentAction> getAllActions(){
		if(parent == null) {
			Queue<AgentAction> moves = new LinkedList<AgentAction>();
			if(action != null) {
				moves.add(action);
			}
			return moves;
		}
		else {
			Queue<AgentAction> moves = parent.getAllActions();
			moves.add(action);
			return moves;
		}
	}
	
	public void setAction(AgentAction a) {
		action = a;
	}
	
	public boolean isGoalState() {
		//TODO
		return true;
	}
	
	public GameState[] getNextStates() {
		//TODO
		return null;
	}
	
	public static Queue<AgentAction> search(char[][] problem){
		
		//TODO, change this next line as we introduce different types of search
		return breadthFirstSearch(problem);
	}
	
	private static Queue<AgentAction> breadthFirstSearch(char[][] problem){
		//Some static variables so that we can determine how "hard" problems are
		numNodesExplored = 0;
		numNodesCreated = 0;
		
		GameState node = new GameState(problem); //Essentially the second line of the book's BFS
		if(node.isGoalState()) { //Essentially the start of line 3 of the book's BFS
			node.setAction(AgentAction.declareVictory); //We don't have to do anything
			return node.getAllActions(); //Just the single thing, but this is an example for later
		}
		
		//Create the frontier queue, and reached hash
		Queue<GameState> frontier = new LinkedList<GameState>();
		HashSet<GameState> reached = new HashSet<GameState>();
		
		//Add the first node to the hash
		reached.add(node);

		//TODO - create the rest of the BFS function
		
		node = frontier.poll(); //retrieve and remove
		
		numNodesExplored++; //we are now ready to "explore" the starting node
		
		
		

		//Print this at the end, so we know how "hard" the problem was
		System.out.println("Number of nodes explored = " + numNodesExplored);
		System.out.println("Number of nodes created = " + numNodesCreated);

		//return the goalNode.getAllActions() if you find a goal node
		return null; 

	}
	

}
