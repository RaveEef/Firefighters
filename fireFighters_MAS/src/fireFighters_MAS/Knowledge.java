package fireFighters_MAS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.parameter.Parameters;

/**
 * A class describing the firefighter's knowledge of the world It contains the
 * relevant methods for knowledge manipulation
 *
 * @author Kirill Tumanov, 2015-2017
 */
public class Knowledge {
	// Local variables declaration
	private LinkedHashMap<GridPoint, Boolean> fireKnowledge; // A hash with locations, and corresponding flags of fire
																// presence in the knowledge
	private LinkedHashMap<GridPoint, Boolean> forestKnowledge; // A hash with locations, and corresponding flags of
																// forest presence in the knowledge
	private LinkedHashMap<GridPoint, Boolean> firefighterKnowledge; // A hash with locations, and corresponding flags of
																	// firefighter presence in the knowledge
	private LinkedHashMap<GridPoint, Boolean> rainKnowledge; // A hash with locations, and corresponding flags of rain
																// presence in the knowledge
	private Velocity windVelocity; // A knowledge about the wind velocity

	private LinkedHashMap<GridPoint, Boolean> fireKnowledgeNew; // A hash with locations, and corresponding flags of
																// fire presence in the knowledge
	private LinkedHashMap<GridPoint, Boolean> forestKnowledgeNew; // A hash with locations, and corresponding flags of
																	// forest presence in the knowledge
	private LinkedHashMap<GridPoint, Boolean> firefighterKnowledgeNew; // A hash with locations, and corresponding flags
																		// of firefighter presence in the knowledge
	private LinkedHashMap<GridPoint, Boolean> rainKnowledgeNew; // A hash with locations, and corresponding flags of
																// rain presence in the knowledge
	
	private LinkedHashMap<GridPoint, Boolean> fireMemory;
	private LinkedHashMap<GridPoint, Boolean> forestMemory;
	private LinkedHashMap<GridPoint, Boolean> firefighterMemory;
	private LinkedHashMap<GridPoint, Boolean> rainMemory;

	private ArrayList<GridPoint> wentTo;
	private Set<GridPoint> teamVisited; // A hash with locations, and corresponding flags of firefighter visit in the
	private Set<GridPoint> teamVisitedNew;// past

	private int succ_fire_update;
	private int succ_forest_update;

	private LinkedHashMap<GridPoint, Integer> sharedMap;

	/** Custom constructor */
	public Knowledge() {
		// Initialize local variables
		this.fireKnowledge = new LinkedHashMap<GridPoint, Boolean>();
		this.forestKnowledge = new LinkedHashMap<GridPoint, Boolean>();
		this.firefighterKnowledge = new LinkedHashMap<GridPoint, Boolean>();
		this.rainKnowledge = new LinkedHashMap<GridPoint, Boolean>();
		this.teamVisited = new HashSet<>();

		this.fireKnowledgeNew = new LinkedHashMap<GridPoint, Boolean>();
		this.forestKnowledgeNew = new LinkedHashMap<GridPoint, Boolean>();
		this.firefighterKnowledgeNew = new LinkedHashMap<GridPoint, Boolean>();
		this.rainKnowledgeNew = new LinkedHashMap<GridPoint, Boolean>();
		this.teamVisitedNew = new HashSet<>();

		this.fireMemory = new LinkedHashMap<GridPoint, Boolean>();
		this.forestMemory = new LinkedHashMap<GridPoint, Boolean>();
		this.firefighterMemory = new LinkedHashMap<GridPoint, Boolean>();
		this.rainMemory = new LinkedHashMap<GridPoint, Boolean>();
		
		this.sharedMap = new LinkedHashMap<GridPoint, Integer>();
		
		this.windVelocity = null;

		this.succ_fire_update = 0;
		this.succ_forest_update = 0;
		this.wentTo = new ArrayList<>();
		
		
	}

	public void addWentTo(GridPoint p) {
		wentTo.add(p);
	}

	/**
	 * Cloning the current knowledge to the memory to check if any new knowledge is gained
	 */
	@SuppressWarnings("unchecked")
	public void setMemory() {
		this.fireMemory = (LinkedHashMap<GridPoint, Boolean>)fireKnowledge.clone();
		this.forestMemory = (LinkedHashMap<GridPoint, Boolean>)forestKnowledge.clone();
		this.firefighterMemory = (LinkedHashMap<GridPoint, Boolean>)firefighterKnowledge.clone();
		this.rainMemory = (LinkedHashMap<GridPoint, Boolean>)rainKnowledge.clone();
	}
	
	/* 
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++			GETTING ENTIRE OR NEW KNOWLEDGE OF OBJECT		++
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	*/
	
	/**
	 * Get positions of all the fire objects from the current knowledge
	 *
	 * @return a set of positions of all the fire objects
	 */
	public ArrayList<GridPoint> getAllFire() {
		ArrayList<GridPoint> returnArray = new ArrayList<>();
		
		if (fireKnowledge != null) {
			for (GridPoint p : fireKnowledge.keySet()) {
				if (fireKnowledge.get(p) != null) {
					returnArray.add(p);
				}
			}
		}
		
		return returnArray;
	}
	
	/**
	 * Get positions of all the fire objects that were added or removed from the firefighter's knowledge
	 * (compared to what was in its memory) 
	 * 
	 * @return a set of positions of all recently changed fire objects 
	 */
	public ArrayList<GridPoint> getNewFires(){
		ArrayList<GridPoint> returnArray = new ArrayList<>();
		
		if (fireKnowledge != null) {
			for (GridPoint p : fireKnowledge.keySet()) {
				if (fireKnowledge.get(p) != null && fireMemory.get(p) == null) {	// New fire at p
					returnArray.add(p);
				}
			}
		}
		
		if (fireMemory != null) {
			for (GridPoint p : fireMemory.keySet()) {
				if (fireMemory.get(p) != null && fireKnowledge.get(p) == null) {	// Removed fire at p
					returnArray.add(p);
				}
			}
		}
		
		return returnArray;
	}
	
	/**
	 * Get all the forest objects from the current knowledge
	 *
	 * @return a set of positions of all the forest objects
	 */
	public ArrayList<GridPoint> getAllForest() {
		ArrayList<GridPoint> returnArray = new ArrayList<>();

		if (forestKnowledge != null) {
			for (GridPoint p : forestKnowledge.keySet()) {
				if (forestKnowledge.get(p) != null) {
					returnArray.add(p);
				}
			}
		}

		return returnArray;
	}
	
	/**
	 * Get positions of all the forest object that were added or removed from the firefighter's knowledge
	 * (compared to what was in its memory) 
	 * 
	 * @return a set of positions of all recently changed forest objects 
	 */
	public ArrayList<GridPoint> getNewForests(){
		ArrayList<GridPoint> returnArray = new ArrayList<>();
		
		if (forestKnowledge != null) {
			for (GridPoint p : forestKnowledge.keySet()) {
				if (forestKnowledge.get(p) != null && forestMemory.get(p) == null) {	// New forest at p
					returnArray.add(p);
				}
			}
		}
		
		if (forestMemory != null) {
			for (GridPoint p : forestMemory.keySet()) {
				if (forestMemory.get(p) != null && forestKnowledge.get(p) == null) {	// Removed forest at p
					returnArray.add(p);
				}
			}
		}
		
		return returnArray;
	}

	/**
	 * Get all the firefighter objects from the current knowledge
	 *
	 * @return a set of positions of all the firefighter objects
	 */
	public ArrayList<GridPoint> getAllFirefighters() {
		ArrayList<GridPoint> returnArray = new ArrayList<>();

		if (firefighterKnowledge != null) {
			for (GridPoint p : firefighterKnowledge.keySet()) {
				if (firefighterKnowledge.get(p) != null) {
					returnArray.add(p);
				}
			}
		}

		return returnArray;
	}
	
	/**
	 * Get positions of all firefighter object that were added or removed from the firefighter's knowledge
	 * (compared to what was in its memory) 
	 * 
	 * @return a set of positions of all recently changed firefighter objects 
	 */
	public ArrayList<GridPoint> getNewFirefighters(){
		ArrayList<GridPoint> returnArray = new ArrayList<>();
		
		if (firefighterKnowledge != null) {
			for (GridPoint p : firefighterKnowledge.keySet()) {
				if (firefighterKnowledge.get(p) != null && firefighterMemory.get(p) == null) {	// New firefighter at p
					returnArray.add(p);
				}
			}
		}
		
		if (firefighterMemory != null) {
			for (GridPoint p : firefighterMemory.keySet()) {
				if (firefighterMemory.get(p) != null && firefighterKnowledge.get(p) == null) {	// Removed firefighter at p
					returnArray.add(p);
				}
			}
		}
		
		return returnArray;
	}

	/**
	 * Get all the rain objects from the current knowledge
	 *
	 * @return a set of positions of all the rain objects
	 */
	public ArrayList<GridPoint> getAllRain() {
		ArrayList<GridPoint> returnArray = new ArrayList<>();

		if (rainKnowledge != null) {
			for (GridPoint p : rainKnowledge.keySet()) {
				if (rainKnowledge.get(p) != null) {
					returnArray.add(p);
				}
			}
		}

		return returnArray;
	}
	
	/**
	 * Get positions of all rain object that were added or removed from the firefighter's knowledge
	 * (compared to what was in its memory) 
	 * 
	 * @return a set of positions of all recently changed rain objects 
	 */
	public ArrayList<GridPoint> getNewRain(){
		ArrayList<GridPoint> returnArray = new ArrayList<>();
		
		if (rainKnowledge != null) {
			for (GridPoint p : rainKnowledge.keySet()) {
				if (rainKnowledge.get(p) != null && rainMemory.get(p) == null) {
					returnArray.add(p);
				}
			}
		}
		
		if (rainMemory != null) {
			for (GridPoint p : rainMemory.keySet()) {
				if (rainMemory.get(p) != null && rainKnowledge.get(p) == null) {
					returnArray.add(p);
				}
			}
		}
		
		return returnArray;
	}
	
	/**
	 * Get the shared map of the fire fighter
	 * @return the shared map of the fire fighter
	 */
	public LinkedHashMap<GridPoint, Integer> getSharedMap(){
		return sharedMap;
	}
	
	/* 
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++					ADDING TO KNOWLEDGE						++
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	*/
	
	/**
	 * Add a position of a fire to the current knowledge
	 *
	 * @param pos
	 *            - position to put a fire to
	 * @return 0 - if this fire is already known, 1 - if the fire was unknown and
	 *         was added to the knowledge
	 */
	public boolean addFire(GridPoint pos) {

		for (GridPoint p : fireKnowledge.keySet()) {
			if (pos.equals(p)) {
				return false;
			}
		}
		
		succ_fire_update++;
		fireKnowledge.put(pos, true);
		return true;
	}

	/**
	 * Add a forest object to a given position in a current knowledge
	 *
	 * @param pos
	 *            - position at which the forest object should be added
	 * @return 0 - if this forest is already known, 1 - if the forest was unknown
	 *         and was added to the knowledge
	 */
	public boolean addForest(GridPoint pos) {
		for (GridPoint p : forestKnowledge.keySet()) {
			if (pos.equals(p)) {
				return false;
			}
		}

		succ_forest_update++;
		forestKnowledge.put(pos, true);
		return true;
	}

	/**
	 * Add a firefighter object to a given position in a current knowledge
	 *
	 * @param pos
	 *            - position at which the firefighter object should be added
	 * @return 0 - if this firefighter is already known, 1 - if the firefighter was
	 *         unknown and was added to the knowledge
	 */
	public boolean addFirefighter(GridPoint pos) {
		for (GridPoint p : firefighterKnowledge.keySet()) {
			if (pos.equals(p)) {
				return false;
			}
		}

		firefighterKnowledge.put(pos, true);
		return true;
	}

	/**
	 * Add a rain object to a given position in a current knowledge
	 *
	 * @param pos
	 *            - position at which the rain object should be added
	 * @return 0 - if this rain is already known, 1 - if the rain was unknown and
	 *         was added to the knowledge
	 */
	public boolean addRain(GridPoint pos) {
		for (GridPoint p : rainKnowledge.keySet()) {
			if (pos.equals(p)) {
				return false;
			}
		}

		rainKnowledge.put(pos, true);
		return true;
	}

	// add record of a visited cell
	public void addTVisited(GridPoint pos) {
		teamVisitedNew.add(pos);
	}

	// Add the agent's sight to the shared map
	public void add2sharedMap(GridPoint pos, int tick) {

		if (sharedMap.containsKey(pos)) {
			if (sharedMap.get(pos) < tick) {
				sharedMap.put(pos, tick);
			}
		} else {
			sharedMap.put(pos, tick);
		}
	}

	public void addPosition(GridPoint pos, int tick) {

		Parameters params = RunEnvironment.getInstance().getParameters();
		int sightRange = params.getInteger("firefighter_sight_range");
		for (int x = -sightRange; x <= sightRange; x++) {
			for (int y = -sightRange; y <= sightRange; y++) {
				GridPoint p = new GridPoint(pos.getX() + x, pos.getY() + y);
				if (p.getX() >= 0 && p.getX() < params.getInteger("gridWidth") && p.getY() >= 0 && p.getY() < params.getInteger("gridHeight")) {
					add2sharedMap(p, tick);
				}
			}
		}
	}

	/* 
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++				REMOVING  FROM  KNOWLEDGE					++
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	*/
	
	/**
	 * Remove fire at a given position from a current knowledge
	 *
	 * @param pos
	 *            - position from which the fire object should be removed
	 */
	public void removeFire(GridPoint pos) {
		for (GridPoint p : fireKnowledge.keySet()) {
			if (pos.equals(p)) {
				succ_fire_update++;
				fireKnowledge.put(p, null);
				break;
			}
		}
	}
	
	/**
	 * Remove forest at a given position from a current knowledge
	 *
	 * @param pos
	 *            - position from which the forest object should be removed
	 */
	public void removeForest(GridPoint pos) {
		for (GridPoint p : forestKnowledge.keySet()) {
			if (pos.equals(p)) {
				succ_forest_update++;
				forestKnowledge.put(p, null);
				return;
			}
		}
	}

	/**
	 * Remove firefighter at a given position from a current knowledge
	 *
	 * @param pos
	 *            - position from which the firefighter object should be removed
	 */
	public void removeFirefighter(GridPoint pos) {
		
		for (GridPoint p : firefighterKnowledge.keySet()) {
			if (pos.equals(p)) {
				firefighterKnowledge.put(p, null);
				return;
			}
		}
	}

	/**
	 * Remove rain at a given position from a current knowledge
	 *
	 * @param pos
	 *            - position from which the rain object should be removed
	 */
	public void removeRain(GridPoint pos) {
		for (GridPoint p : rainKnowledge.keySet()) {
			if (pos.equals(p)) {
				rainKnowledge.put(p, null);
				return;
			}
		}
	}

	/* 
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++				KNOWLEDGE <--> STRING CONVERSION			++
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	*/
	
	/**
	 * Encode the knowledge to a string
	 *
	 * @return the string representation of the knowledge
	 */
	public StringBuilder convertNewKnowledge2String(KnowledgeType knowledgeType, StringBuilder sb) {

		sb.append(knowledgeType.toString()).append("@");
		if (knowledgeType == KnowledgeType.FIRE && getNewFires().size() > 0) {
			for (GridPoint pos : getNewFires()) {
				sb.append(pos.getX()).append(",").append(pos.getY()).append("@");
			}
			sb.append(":");
		}
		else if (knowledgeType == KnowledgeType.FOREST && getNewForests().size() > 0) {
			for (GridPoint pos : getNewForests()) {
				sb.append(pos.getX()).append(",").append(pos.getY()).append("@");
			}
			sb.append(":");
		}
		else if (knowledgeType == KnowledgeType.FIREFIGHTER && getNewFirefighters().size() > 0) {
			for (GridPoint pos : getNewFirefighters()) {
				sb.append(pos.getX()).append(",").append(pos.getY()).append("@");
			}
			sb.append(":");
		}
		else if (knowledgeType == KnowledgeType.RAIN && getNewRain().size() > 0) {
			for (GridPoint pos : getNewRain()) {
				sb.append(pos.getX()).append(",").append(pos.getY()).append("@");
			}
			sb.append(":");
		}
		else if (knowledgeType == KnowledgeType.TVISITED && getTeamVisited().size() > 0) {
			for (GridPoint pos : getTeamVisited()) {
				sb.append(pos.getX()).append(",").append(pos.getY()).append("@");
			}
			sb.append(":");
		}
		else if (knowledgeType == KnowledgeType.POS_UPDATE && sharedMap.size() > 0) {
			for (GridPoint pos : getAllFirefighters()) {
				if (sharedMap.containsKey(pos)) {
					sb.append(pos.getX()).append(",").append(pos.getY()).append(",").append(sharedMap.get(pos)).append("@");
				}
			}
			sb.append(":");
			/*for (Map.Entry<GridPoint, Integer> e : sharedMap.entrySet()) {
				sb.append(e.getKey().getX()).append(",").append(e.getKey().getY()).append(",").append(e.getValue()).append("@");
			}*/
		}

		return sb;
	}

	public String getMessageContent(Object messageContent) {
		StringBuilder sb = new StringBuilder();
		if (messageContent.getClass() == ArrayList.class) {
			ArrayList<KnowledgeType> messageContents = (ArrayList<KnowledgeType>)messageContent;

			if (messageContents.size() == 0) {
				messageContents.add(KnowledgeType.FIRE);
				messageContents.add(KnowledgeType.FOREST);
				messageContents.add(KnowledgeType.FIREFIGHTER);
				messageContents.add(KnowledgeType.RAIN);
				//messageContents.add(KnowledgeType.SHAREDMAP);
				messageContents.add(KnowledgeType.TVISITED);
			}
			for (KnowledgeType objectType : messageContents) {

				sb.append(convertNewKnowledge2String(objectType, sb));
			}
		} else {
			KnowledgeType messageType = (KnowledgeType)messageContent;
			sb.append(convertNewKnowledge2String(messageType, sb));
		}
		return sb.toString();
	}

	/**
	 * Decode knowledge from the string message
	 *
	 * @param str
	 *            - a string representation of knowledge
	 */
	public void convertFromString(String str) {

		String[] categories = str.split(":");
		KnowledgeType knowledgeType;

		for (int i = 0; i < categories.length; i++) {

			String[] positions = categories[i].split("@");

			knowledgeType = KnowledgeType.getKnowledgeType(positions[0]);

			if (knowledgeType != null) {
				for (int j = 1; j < positions.length; j++) {

					String[] coord = positions[j].split(",");
					int posX = Integer.parseInt(coord[0]);
					int posY = Integer.parseInt(coord[1]);
					GridPoint newGridPoint = new GridPoint(posX, posY);
					switch (knowledgeType) {
					case FIRE:
						addFire(newGridPoint);
						break;
					case FOREST:
						addForest(newGridPoint);
						break;
					case FIREFIGHTER:
						addFirefighter(newGridPoint);
						break;
					case RAIN:
						addRain(newGridPoint);
						break;
					case TVISITED:
						addTVisited(newGridPoint);
						break;
					case POS_UPDATE:
						addPosition(newGridPoint, Integer.parseInt(coord[2]));
						break;
					}
				}
			}
		}
	}

	/* 
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++					UPDATIG KNOWLEDGE						++
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	*/

	/**
	 * Add the new knowledge to the old one.
	 */
	public void addNew2OldKnowledge() {
		this.fireKnowledge.putAll(this.fireKnowledgeNew);
		this.forestKnowledge.putAll(this.forestKnowledgeNew);
		this.firefighterKnowledge.putAll(this.firefighterKnowledgeNew);
		this.rainKnowledge.putAll(this.rainKnowledgeNew);
		this.teamVisited.addAll(this.teamVisitedNew);

		this.fireKnowledgeNew.clear();
		this.forestKnowledgeNew.clear();
		this.firefighterKnowledgeNew.clear();
		this.rainKnowledgeNew.clear();
		this.teamVisitedNew.clear();
	}

	/**
	 * Update the knowledge of the firefighter by taking info from a given knowledge
	 *
	 * @param k
	 *            - knowledge to update from
	 */
	public void addKnowledge(Knowledge k) {

		for (GridPoint pos : k.getAllFire()) {addFire(pos);}
		for (GridPoint pos : k.getAllForest()) { addForest(pos); }
		for (GridPoint pos : k.getAllFirefighters()) { addFirefighter(pos); }
		for (GridPoint pos : k.getAllRain()) { addRain(pos); }
		//for (Map.Entry<GridPoint, Integer> e : k.getSharedMap().entrySet()) { add2sharedMap(e.getKey(), e.getValue()); }
		for (GridPoint pos : k.getTeamVisited()) { addTVisited(pos); }
	}

	/* 
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++						LOCAL GETTERS						++
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	*/
	
	/**
	 * Get knowledge about a presence of fire in a given cell
	 *
	 * @param p
	 *            - a cell to check
	 * @return 1 if the object exists in the knowledge, 0 - if not
	 */
	public boolean getFire(GridPoint p) {
		
		if (fireKnowledge.get(p) == null) {
			return false;
		}

		return fireKnowledge.get(p);
	}

	/**
	 * Get knowledge about a presence of forest in a given cell
	 *
	 * @param p
	 *            - a cell to check
	 * @return 1 if the object exists in the knowledge, 0 - if not
	 */
	public boolean getForest(GridPoint p) {
		
		if (forestKnowledge.get(p) == null) {
			return false;
		}
		
		return forestKnowledge.get(p);
	}

	/**
	 * Get knowledge about a presence of firefighter in a given cell
	 *
	 * @param p
	 *            - a cell to check
	 * @return 1 if the object exists in the knowledge, 0 - if not
	 */
	public boolean getFirefighter(GridPoint p) {
		
		if (firefighterKnowledge.get(p) == null) {
			return false;
		}

		return firefighterKnowledge.get(p);
	}

	/**
	 * Get knowledge about a presence of rain in a given cell
	 *
	 * @param p
	 *            - a cell to check
	 * @return 1 if the object exists in the knowledge, 0 - if not
	 */
	public boolean getRain(GridPoint p) {
		
		if (rainKnowledge.get(p) == null) {
			return false;
		}

		return rainKnowledge.get(p);
	}

	/**
	 * Get wind velocity
	 *
	 * @return - the velocity
	 */
	public Velocity getWindVelocity() {
		return windVelocity;
	}

	public int getSuccFireUpdate() {
		return succ_fire_update;
	}

	public int getSuccForestUpdate() {
		return succ_forest_update;
	}

	public Set<GridPoint> getTeamVisited() {
		return teamVisitedNew;
	}

	public ArrayList<GridPoint> getWentTo() {
		return wentTo;
	}
	
	/* 
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++						LOCAL SETTERS						++
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	*/
	
	/**
	 * Set wind velocity
	 *
	 * @param vel
	 *            - the velocity
	 */
	public void setWindVelocity(Velocity vel) {
		windVelocity = vel;
	}

}
