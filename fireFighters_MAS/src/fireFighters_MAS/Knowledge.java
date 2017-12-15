package fireFighters_MAS;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import repast.simphony.space.grid.GridPoint;
/**
 * A class describing the firefighter's knowledge of the world
 * It contains the relevant methods for knowledge manipulation
 * @author Kirill Tumanov, 2015-2017
 */
public class Knowledge
{
	// Local variables declaration
	private LinkedHashMap<GridPoint, Boolean> fireKnowledge;		// A hash with locations, and corresponding flags of fire presence in the knowledge
	private LinkedHashMap<GridPoint, Boolean> forestKnowledge;		// A hash with locations, and corresponding flags of forest presence in the knowledge
	private LinkedHashMap<GridPoint, Boolean> firefighterKnowledge;	// A hash with locations, and corresponding flags of firefighter presence in the knowledge
	private LinkedHashMap<GridPoint, Boolean> rainKnowledge;		// A hash with locations, and corresponding flags of rain presence in the knowledge
	private Velocity windVelocity;	// A knowledge about the wind velocity
	/** Custom constructor */
	public Knowledge()
	{
		// Initialize local variables
		this.fireKnowledge = new LinkedHashMap<GridPoint, Boolean>();
		this.forestKnowledge = new LinkedHashMap<GridPoint, Boolean>();
		this.firefighterKnowledge = new LinkedHashMap<GridPoint, Boolean>();
		this.rainKnowledge = new LinkedHashMap<GridPoint, Boolean>();
		this.windVelocity = null;
	}
	/**
	 * Get positions of all the fire objects from the current knowledge
	 * @return a set of positions of all the fire objects
	 */
	public ArrayList<GridPoint> getAllFire()
	{
		ArrayList<GridPoint> returnArray = new ArrayList<>();
		
		if (fireKnowledge != null)
		{
			for (GridPoint p : fireKnowledge.keySet())
			{
				if (fireKnowledge.get(p) != null) { returnArray.add(p); }
			}
		}
		
		return returnArray;
	}
	/**
	 * Get all the forest objects from the current knowledge
	 * @return  a set of positions of all the forest objects
	 */
	public ArrayList<GridPoint> getAllForest()
	{
		ArrayList<GridPoint> returnArray = new ArrayList<>();
		
		if (forestKnowledge != null)
		{
			for (GridPoint p : forestKnowledge.keySet())
			{
				if (forestKnowledge.get(p) != null) { returnArray.add(p); }
			}
		}
		
		return returnArray;
	}
	/**
	 * Get all the firefighter objects from the current knowledge
	 * @return  a set of positions of all the firefighter objects
	 */
	public ArrayList<GridPoint> getAllFirefighters()
	{
		ArrayList<GridPoint> returnArray = new ArrayList<>();
		
		if (firefighterKnowledge != null)
		{
			for (GridPoint p : firefighterKnowledge.keySet())
			{
				if (firefighterKnowledge.get(p) != null) { returnArray.add(p); }
			}
		}
		
		return returnArray;
	}
	/**
	 * Get all the rain objects from the current knowledge
	 * @return  a set of positions of all the rain objects
	 */
	public ArrayList<GridPoint> getAllRain()
	{
		ArrayList<GridPoint> returnArray = new ArrayList<>();
		
		if (rainKnowledge != null)
		{
			for (GridPoint p : rainKnowledge.keySet())
			{
				if (rainKnowledge.get(p) != null) { returnArray.add(p); }
			}
		}
		
		return returnArray;
	}
	/**
	 * Add a position of a fire to the current knowledge
	 * @param pos - position to put a fire to
	 * @return 0 - if this fire is already known, 1 - if the fire was unknown and was added to the knowledge
	 */
	public boolean addFire(GridPoint pos)
	{
		for (GridPoint p : fireKnowledge.keySet())
		{
			if (pos.equals(p)) { return false; }
		}
		
		fireKnowledge.put(pos, true);
		return true;
	}
	/**
	 * Add a forest object to a given position in a current knowledge
	 * @param pos - position at which the forest object should be added
	 * @return 0 - if this forest is already known, 1 - if the forest was unknown and was added to the knowledge
	 */
	public boolean addForest(GridPoint pos)
	{
		for (GridPoint p : forestKnowledge.keySet())
		{
			if (pos.equals(p)) { return false; }
		}
		
		forestKnowledge.put(pos, true);
		return true;
	}
	/**
	 * Add a firefighter object to a given position in a current knowledge
	 * @param pos - position at which the firefighter object should be added
	 * @return 0 - if this firefighter is already known, 1 - if the firefighter was unknown and was added to the knowledge
	 */
	public boolean addFirefighter(GridPoint pos)
	{
		for (GridPoint p : firefighterKnowledge.keySet())
		{
			if (pos.equals(p)) { return false; }
		}
		
		firefighterKnowledge.put(pos, true);
		return true;
	}
	/**
	 * Add a rain object to a given position in a current knowledge
	 * @param pos - position at which the rain object should be added
	 * @return 0 - if this rain is already known, 1 - if the rain was unknown and was added to the knowledge
	 */
	public boolean addRain(GridPoint pos)
	{
		for (GridPoint p : rainKnowledge.keySet())
		{
			if (pos.equals(p)) { return false; }
		}
		
		rainKnowledge.put(pos, true);
		return true;
	}
	/**
	 * Remove fire at a given position from a current knowledge
	 * @param pos - position from which the fire object should be removed
	 */
	public void removeFire(GridPoint pos)
	{
		for (GridPoint p : fireKnowledge.keySet())
		{
			if (pos.equals(p))
			{
				fireKnowledge.put(p,null);
				return;
			}
		}
	}
	/**
	 * Remove forest at a given position from a current knowledge
	 * @param pos - position from which the forest object should be removed
	 */
	public void removeForest(GridPoint pos)
	{
		for (GridPoint p : forestKnowledge.keySet())
		{
			if (pos.equals(p))
			{
				forestKnowledge.put(p,null);
				return;
			}
		}
	}
	/**
	 * Remove firefighter at a given position from a current knowledge
	 * @param pos - position from which the firefighter object should be removed
	 */
	public void removeFirefighter(GridPoint pos)
	{
		for (GridPoint p : firefighterKnowledge.keySet())
		{
			if (pos.equals(p))
			{
				firefighterKnowledge.put(p,null);
				return;
			}
		}
	}
	/**
	 * Remove rain at a given position from a current knowledge
	 * @param pos - position from which the rain object should be removed
	 */
	public void removeRain(GridPoint pos)
	{
		for (GridPoint p : rainKnowledge.keySet())
		{
			if (pos.equals(p))
			{
				rainKnowledge.put(p,null);
				return;
			}
		}
	}
	/**
	 * Encode the knowledge to a string
	 * @return the string representation of the knowledge
	 */
	public String convert2String()
	{
		//TODO Form a string out of the knowledge at hand - Add own code
		String str = "";
		
		for (GridPoint pos : getAllFire()) { str += "Fire " + pos.getX() + " " + pos.getY() + ";"; }
		
		return str;
	}
	/**
	 * Decode knowledge from the string message
	 * @param str - a string representation of knowledge
	 */
	public void convertFromString(String str)
	{
		//TODO Parse the string to extract the knowledge - Add own code
		String[] arr1 = str.split(";");
		
		for (int i = 0; i < arr1.length; i++)
		{
			String[] arr2 = arr1[i].split(" ");
			
			if (arr2[0].equals("Fire")) { addFire(new GridPoint(Integer.parseInt(arr2[1]),Integer.parseInt(arr2[2]))); }
		}
	}
	/**
	 * Update the knowledge of the firefighter by taking info from a given knowledge
	 * @param k - knowledge to update from
	 */
	public void updateFromKnowledge(Knowledge k)
	{
		for (GridPoint pos : k.getAllFire()) { addFire(pos); }
		for (GridPoint pos : k.getAllForest()) { addForest(pos); }
		for (GridPoint pos : k.getAllFirefighters()) { addFirefighter(pos); }
		for (GridPoint pos : k.getAllRain()) { addRain(pos); }
	}
	// Local getters
	/**
	 * Get knowledge about a presence of fire in a given cell
	 * @param p - a cell to check
	 * @return 1 if the object exists in the knowledge, 0 - if not
	 */
	public boolean getFire(GridPoint p)
	{
		if (fireKnowledge.get(p) == null) { return false; }
		
		return fireKnowledge.get(p);
	}
	/**
	 * Get knowledge about a presence of forest in a given cell
	 * @param p - a cell to check
	 * @return 1 if the object exists in the knowledge, 0 - if not
	 */
	public boolean getForest(GridPoint p)
	{
		if (forestKnowledge.get(p) == null) { return false; }
		
		return forestKnowledge.get(p);
	}
	/**
	 * Get knowledge about a presence of firefighter in a given cell
	 * @param p - a cell to check
	 * @return 1 if the object exists in the knowledge, 0 - if not
	 */
	public boolean getFirefighter(GridPoint p)
	{
		if (firefighterKnowledge.get(p) == null) { return false; }
		
		return firefighterKnowledge.get(p);
	}
	/**
	 * Get knowledge about a presence of rain in a given cell
	 * @param p - a cell to check
	 * @return 1 if the object exists in the knowledge, 0 - if not
	 */
	public boolean getRain(GridPoint p)
	{
		if (rainKnowledge.get(p) == null) { return false; }
		
		return rainKnowledge.get(p);
	}
	/**
	 * Get wind velocity
	 * @return - the velocity
	 */
	public Velocity getWindVelocity() { return windVelocity; }
	// Local setters
	/**
	 * Set wind velocity
	 * @param vel - the velocity
	 */
	public void setWindVelocity(Velocity vel) { windVelocity = vel; }
}
