package fireFighters_MAS;

import java.util.ArrayList;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ActionQueue;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.collections.IndexedIterable;
// Enumerator listing the available message transmission methods
enum TransmissionMethod
{
	Radio,		// Send a message with the radio (local)
	Satellite	// Send a message with a satellite (global)
}
/**
 * A class describing the Firefighter agent and its behavior
 * This is a "stupid" implementation of the firefighter
 * you should focus on improving it by utilizing the
 * Multi-Agent Systems concepts learned in class
 * @author Kirill Tumanov, 2015-2017
 */
public class Firefighter
{
	// Local variables definition
	private Context<Object> context; // Context in which the firefighter is placed
	private Grid<Object> grid; // Grid in which the firefighter is projected
	private int lifePoints; // Amount of damage the firefighter can still take from the fire, before extinction
	private int strength; // Amount of damage the firefighter can deal to the fire
	private Velocity velocity; // Vector describing the firefighter movement's heading and speed
	private int sightRange; // Number of cells defining how far the firefighter can see around
	private int bounty; // Bounty units the firefighter has
	// Local variables initialization
	private boolean newInfo; // Flag if the firefighter has a new info to communicate to peers
	private Knowledge knowledge; // A knowledge the firefighter has
	ISchedulableAction stepSchedule;	// Action scheduled for the step method
	ISchedulableAction removeSchedule;	// Action scheduled for the remove method
	int id; // An ID of the firefighter
	double character; // The character of the firefighter defining its behavior //TODO This should be implemented
	/**
	 * Custom constructor
	 * @param context - context to which the firefighter is added
	 * @param grid - grid to which the firefighter is added
	 * @param id - an id of the firefighter
	 */
	public Firefighter(Context<Object> context, Grid<Object> grid, int id)
	{
		Parameters params = RunEnvironment.getInstance().getParameters();
		// Initialize local variables
		this.context = context;
		this.grid = grid;
		this.id = id;
		lifePoints = params.getInteger("firefighter_life");
		strength = params.getInteger("firefighter_strength");
		sightRange = params.getInteger("firefighter_sight_range");
		bounty = params.getInteger("firefighter_initial_bounty");
		double initialSpeed = params.getDouble("firefighter_initial_speed");
		double initialSpeedDeviation = params.getDouble("firefighter_initial_speed_deviation");
		velocity = new Velocity(RandomHelper.nextDoubleFromTo(initialSpeed - initialSpeedDeviation, initialSpeed + initialSpeedDeviation),
				RandomHelper.nextDoubleFromTo(0, 360));
		knowledge = new Knowledge(); // No knowledge yet
		newInfo = false; // No new info yet
		// Schedule methods
	    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	    double current_tick = schedule.getTickCount();
	    ScheduleParameters sch_params = ScheduleParameters.createRepeating(current_tick + 1, Math.round(1 / velocity.speed));
	    stepSchedule = schedule.schedule(sch_params, this, "step");
	}
	/** A step method of the firefighter */
	@ScheduledMethod(shuffle=false) // Prevent call order shuffling
	public void step()
	{
	    if (!Tools.isAtTick(stepSchedule.getNextTime())) { return; } // Execute only at the specified ticks
		
		if (!context.contains(this)) { return; } // Safety
		
		GridPoint myPos = grid.getLocation(this);
		// Info acquisition part (takes no time)
		checkAroundForFire(sightRange);
		
		if (checkSurroundedByFire()) // If caught by fire, die
		{
			decreaseLifePoints(lifePoints);
			return;
		}		
		// Action part (takes one step)
		boolean checkWeather = RandomHelper.nextDouble() < 0.05; // TODO This decision should be made in a meaningful way
		
		if (checkWeather) { checkWeather(); }
		else if (knowledge.getFire(myPos)) { runOutOfFire(); } // If firefighter knows that he is standing in the fire
		else { moveOrExtinguish(); }
		// Communication part (takes no time)
		if (newInfo) // If some new info of interest was obtained, send it around
		{
			int communicationDecision = 0; // TODO This decision should be made in a meaningful way
			
			if (communicationDecision == 0) { sendMessage(TransmissionMethod.Radio,knowledge.getAllFirefighters()); }
			else if (communicationDecision == 1) { sendMessage(TransmissionMethod.Satellite,knowledge.getAllFirefighters()); }
		}
	}
	/** Movement routine of a firefighter */
	private void moveOrExtinguish()
	{
		double result[] = findDirection2NearestFire();
		double directionToFire = result[0];
		double distance = result[1];
		
		if (distance == 1) // If fire is exactly at the extinguishingDistance
		{
			GridPoint myPos = grid.getLocation(this);
			GridPoint firePos = Tools.dirToCoord(directionToFire, myPos);
			GridPoint sightPos = Tools.dirToCoord(velocity.direction, myPos);
			
			if (firePos == sightPos) { extinguishFire(directionToFire); } // Extinguish the fire in the direction of heading
			else { velocity.direction = directionToFire; } // Turn to fire
		}
		else if (distance > 1) { tryToMove(directionToFire); } // If fire is more than extinguishingDistance away
		else // Otherwise explore randomly
		{
			velocity.direction = RandomHelper.nextDoubleFromTo(0,360);
			tryToMove(velocity.direction);
		}
	}
	/**
	 * Given a possible movement direction, generate a set of others, and try to move in one of them
	 * @param pDir - direction to try to move to
	 * @return 0 - movement failed, 1 - movement succeeded
	 */
	private boolean tryToMove(double pDir)
	{
		GridPoint myPos = grid.getLocation(this);
		
		for (int i = 0; i < 8; i++)
		{
			GridPoint newPos = Tools.dirToCoord(pDir + (i%2 == 0 ? -i*45 : i*45), myPos);
			
			if (move(newPos) == 0) { return true; }
		}
		
		return false;
	}
	/** Firefighter's reaction on being in the fire */
	private void runOutOfFire()
	{
		if (!decreaseLifePoints(1)) { return; } // Burn, and see if still moving
		
		Velocity knownWindVelocity = knowledge.getWindVelocity();
		double directionUpwind = RandomHelper.nextDoubleFromTo(0, 360);
		
		if (knownWindVelocity != null) { directionUpwind = knownWindVelocity.direction + 180; }
		
		tryToMove(directionUpwind); // Try to move in an upwind direction to escape from fire
	}
	/**
	 * Decrease the lifePoints of the firefighter by a given amount
	 * @param amount - an amount to decrease by
	 * @return 1 if still active, 0 - otherwise
	 */
	private boolean decreaseLifePoints(int amount)
	{
		lifePoints -= amount;
		
		if (lifePoints <= 0)
		{
			ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			double current_tick = schedule.getTickCount();
			ScheduleParameters removeParams = ScheduleParameters.createOneTime(current_tick + 0.000001);
			removeSchedule = schedule.schedule(removeParams,this,"remove");
	    	// Schedule method calls to the future
		    stepSchedule.reschedule(new ActionQueue());
		    // Remove the future method calls
		    schedule.removeAction(stepSchedule);
		    return false;
		}
		
		return true;
	}
	/**
	 * Check a NxN area around the firefighter for fires (N = 2*sightRange + 1)
	 * @param sightRange - number of cells from the given position to search in
	 */
	private void checkAroundForFire(int sightRange)
	{
		GridPoint myPos = grid.getLocation(this);
		
		for (int i = -sightRange; i <= sightRange; i++)
		{
			for (int j = -sightRange; j <= sightRange; j++) { checkCell(new GridPoint(myPos.getX() + i, myPos.getY() + j)); }
		}
	}
	/**
	 * Check if a firefighter is surrounded by fire
	 * Note: this method assumes that the firefighter knowledge about the surrounding was updated already,
	 * so checkCell(...) is not called
	 * @return 1 if surrounded, 0 if not
	 */
	private boolean checkSurroundedByFire()
	{
		GridPoint myPos = grid.getLocation(this);
		
		for (int i = -1; i <= 1; i++)
		{
			for (int j = -1; j <= 1; j++)
			{
				if (!(i == 0 && j == 0)) // Do not check the point at which standing
				{
					GridPoint pos = new GridPoint(myPos.getX() + i, myPos.getY() + j);

					if (!knowledge.getFire(pos)) { return false; }
				}
			}
		}
		
		return true;
	}
	/**
	 * Extinguish fire in a given direction
	 * @param directionToFire - a direction to extinguish fire in
	 */
	private void extinguishFire(double directionToFire)
	{
		GridPoint myPos = grid.getLocation(this);
		GridPoint firePos = Tools.dirToCoord(directionToFire, myPos);
		Fire fire = (Fire) Tools.getObjectOfTypeAt(grid, Fire.class, firePos);
			
		if (fire != null)
		{			
			if (!fire.decreaseLifetime(strength)) // If the fire was extinguished
			{
				Parameters params = RunEnvironment.getInstance().getParameters();
				bounty += params.getInteger("firefighter_fire_reward_bounty");
			}
		}
	}
	/**
	 * Move to a given position
	 * @param newPos - a position to move to
	 * @return -1 - if move was unsuccessful, 0 - if move was successful, 1 - if couldn't move because another firefighter already took this place
	 */
	private int move(GridPoint newPos)
	{
		if (Tools.isWithinBorders(newPos, grid))
		{
			checkCell(newPos);
			
			boolean hasFire = knowledge.getFire(newPos);
			boolean hasFirefighter = knowledge.getFirefighter(newPos);
			
			if (!hasFire && !hasFirefighter) // Make sure that the cell is not on fire, and is not occupied by another firefighter
			{
				grid.moveTo(this, newPos.toIntArray(null));
				return 0;
			}
			else if (hasFirefighter) { return 1; }
		}
		
		return -1;
	}
	/**
	 * Method used to find the direction and distance to the nearest fire
	 * @return a tuple (direction, distance) to the nearest fire
	 */
	private double[] findDirection2NearestFire()
	{
		GridPoint myPos = grid.getLocation(this);
		int minDist = Integer.MAX_VALUE;
		double direction = -1;
		
		for (GridPoint p : knowledge.getAllFire()) // For all the fires in the firefighter's knowledge
		{
			int dist = Tools.getDistance(myPos, p);
			// Determine if the fire is closest. If so, update distance and direction accordingly
			if (dist < minDist)
			{
				minDist = dist;
				direction = Tools.getAngle(myPos, p);
			}
		}
		
		double[] result = { direction, (minDist == Integer.MAX_VALUE ? -1 : minDist) };
		
		return result;
	}
	/**
	 * Check if a given position contains a fire object, update own knowledge
	 * @param pos - position to check
	 */
	public void checkCell(GridPoint pos)
	{
		boolean hasFirefighter = (Tools.getObjectOfTypeAt(grid, Firefighter.class, pos) != null);
		boolean hasFire = (Tools.getObjectOfTypeAt(grid, Fire.class, pos) != null);

		if (hasFire) { knowledge.addFire(pos); }
		else { knowledge.removeFire(pos); }

		if (hasFirefighter) { knowledge.addFirefighter(pos); }
		else { knowledge.removeFirefighter(pos); }
		
		newInfo = true;
	}
	/**
	 * Send a message to a given set of recipients using a given communication method
	 * @param transmissionMethod - a method to used for message sending
	 * @param recipientLocations - a list of locations of firefighters to send the message to
	 */
	public void sendMessage(TransmissionMethod transmissionMethod, ArrayList<GridPoint> recipientLocations)
	{
		Parameters params = RunEnvironment.getInstance().getParameters();
		GridPoint myPos = grid.getLocation(this);
		Message message = new Message();
		message.setContent(this.knowledge.convert2String());
		int messageCost = message.getCost();
		int radioRange = params.getInteger("firefighter_radio_range");
		int satelliteCostMultiplier = params.getInteger("firefighter_satellite_cost_multiplier");
		
		for (GridPoint recipientLocation : recipientLocations)
		{
			Firefighter recipient = (Firefighter)Tools.getObjectOfTypeAt(grid, Firefighter.class, recipientLocation);
			
			if (recipient != null) // First of all, if the recipient is there at all
			{
				if (transmissionMethod == TransmissionMethod.Radio && Tools.getDistance(myPos,recipientLocation) <= radioRange) // If using the radio, and the recipient is within range
				{
					if (getBounty() >= messageCost)
					{
						recipient.recieveMessage(message); // Deliver message
						bounty -= messageCost; // Pay for the message
					}
				}
				else if (transmissionMethod == TransmissionMethod.Satellite)
				{
					double globalMessageCost = messageCost * satelliteCostMultiplier; // A cost to send a message through the satellite
					
					if (getBounty() >= globalMessageCost)
					{
						recipient.recieveMessage(message); // Deliver message
						bounty -= globalMessageCost; // Pay for the message
					}
				}
			}
		}
		
		newInfo = false; // All the new information was sent, over now
	}
	/**
	 * Receive message
	 * @param message - a message
	 */
	public void recieveMessage(Message message)
	{
		Knowledge receivedKnowledge = new Knowledge();
		receivedKnowledge.convertFromString(message.getContent());
		knowledge.updateFromKnowledge(receivedKnowledge);
	}
	/** Check current weather conditions */
	private void checkWeather()
	{
		if (context != null)
		{
			if (context.getObjects(Wind.class).size() > 0) { knowledge.setWindVelocity(((Wind) context.getObjects(Wind.class).get(0)).getWindDirection()); }
			
			IndexedIterable<Object> rains = context.getObjects(Rain.class);
			
			for (Object o : rains) { knowledge.addRain(grid.getLocation(o)); }
		}
	}
	/** The method for removal of the object */
	@ScheduledMethod(shuffle=false) // Prevent call order shuffling
	public void remove()
	{
	    if (!Tools.isLastTick()) { context.remove(this); } // Do not include this in the executeEndActions()
	}
	/// Local getters & setters
	/**
	 * Get current amount of lifePoints the firefighter has
	 * @return the amount of lifePoints
	 */
	public int getLifePoints() { return lifePoints; }
	/**
	 * Get current amount of strength the firefighter has
	 * @return the amount of strength
	 */
	public int getStrength() { return strength; }
	/**
	 * Get current amount of bounty the firefighter has
	 * @return the amount of bounty
	 */
	public int getBounty() { return bounty; }
	/**
	 * Get speed of the firefighter
	 * @return - the speed
	 */
	public double getSpeed() { return velocity.speed; }
	/**
	 * Get heading of the firefighter
	 * @return - the direction
	 */
	public double getHeading() { return velocity.direction; }
	/**
	 * Get ID of the firefighter
	 * @return - the ID
	 */
	public double getId() { return id; }
	/**
	 * Get position of the firefighter
	 * @return - the position
	 */
	public String getPos() { return grid.getLocation(this).toString(); }
}
