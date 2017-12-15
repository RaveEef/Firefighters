package fireFighters_MAS;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import org.stringtemplate.v4.compiler.STParser.ifstat_return;

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
enum TransmissionMethod {
	Radio, // Send a message with the radio (local)
	Satellite // Send a message with a satellite (global)
}

/**
 * A class describing the Firefighter agent and its behavior This is a "stupid"
 * implementation of the firefighter you should focus on improving it by
 * utilizing the Multi-Agent Systems concepts learned in class
 *
 * @author Kirill Tumanov, 2015-2017
 */
public class Firefighter {
	// Local variables definition
	private Context<Object> context; // Context in which the firefighter is placed
	private Grid<Object> grid; // Grid in which the firefighter is projected
	private int lifePoints; // Amount of damage the firefighter can still take from the fire, before
							// extinction
	private int strength; // Amount of damage the firefighter can deal to the fire
	private Velocity velocity; // Vector describing the firefighter movement's heading and speed
	private int sightRange; // Number of cells defining how far the firefighter can see around
	private int bounty; // Bounty units the firefighter has
	// Local variables initialization
	private boolean newInfo; // Flag if the firefighter has a new info to communicate to peers
	private Knowledge knowledge; // A knowledge the firefighter has
	ISchedulableAction stepSchedule; // Action scheduled for the step method
	ISchedulableAction removeSchedule; // Action scheduled for the remove method
	int id; // An ID of the firefighter
	private FirefighterCharacter character; // The character of the firefighter defining its behavior //TODO This should be
						// implemented
	private Parameters params;

	private int sat_counter;
	private int radio_counter;
	private int w_checks; // weather checks
	private int tot_visited; // totally not overlapping visited tiles
	private int ext_f_count; // extinguished fires counter
	private int max_ticks; // max ticks before comm
	private int msg_len; // total msg cost
	private int stepCounter; // count the number of calls for the step method
	private int helpRange;
	private int radioRange;
	private double helpDirection;
	private GridPoint helpPos;
	private int taskReqCounter;
	private int taskAccCounter;
	private int taskCompCounter;
	private int helpID;

	private Firefighter leader; // could be null in case you are a leader or you don't have a leader
	private Firefighter coLeader = null; // could be null in case you are not a leader or you are a leader and you don't have a co-leader
	private int numberOfTeams; // total number of teams
	private int numberOfLeaders; // total number of leaders
	private int team; // either team1 or team2 -  there is a special case: team0 (meaning that you are a leader overseeing 2 teams) - leaders and co-leaders also belong to a team
	private boolean isLeader; // boolean indicating whether you are a leader
	private boolean isCoLeader = false; // boolean indicating whether you are a leader

	private Random rnd;
	ArrayList<Firefighter> receivers;

	private FirefighterState internalState;

	private int BOUNTY_MIN_BEFORE_ASKING;
	private int BOUNTY_MIN_FOR_SENDING;
	private int BOUNTY_SEND_AMOUNT;
	private int trustThold;
	private int max_bounty_reqs;

	private ArrayList<Double> tot_bounty_req; //total requests done for each ff
	private ArrayList<Double> succ_bounty_req; //total successfull requests for each ff
	private ArrayList<Double> inc_bounty_req; //total requests recevied from each ff
	private ArrayList<Double> trust;
	private ArrayList<Double> trust_mul;  //trust multiplier, increases when
	private int bounty_earned;
	private int bounty_at_end;
	private int bounty_spend_comm;
	private int bounty_transfered_2peer;


	/**
	 * Custom constructor
	 *
	 * @param context
	 *            - context to which the firefighter is added
	 * @param grid
	 *            - grid to which the firefighter is added
	 * @param id
	 *            - an id of the firefighter
	 */
	public Firefighter(Context<Object> context, Grid<Object> grid, int id, int team, Firefighter leader, boolean isLeader) {

		params = RunEnvironment.getInstance().getParameters();
		// Initialize local variables
		this.context = context;
		this.grid = grid;
		this.id = id;
		this.team = team;
		if (leader != null) {
			this.leader = leader;
		}
		this.isLeader = isLeader;
		
		this.numberOfTeams = params.getInteger("firefighter_num_teams");
		this.numberOfLeaders = params.getInteger("firefighter_num_leaders");
		lifePoints = params.getInteger("firefighter_life");
		strength = params.getInteger("firefighter_strength");
		sightRange = params.getInteger("firefighter_sight_range");
		bounty = params.getInteger("firefighter_initial_bounty");
		double initialSpeed = params.getDouble("firefighter_initial_speed");
		double initialSpeedDeviation = params.getDouble("firefighter_initial_speed_deviation");
		velocity = new Velocity(RandomHelper.nextDoubleFromTo(initialSpeed - initialSpeedDeviation,
				initialSpeed + initialSpeedDeviation), RandomHelper.nextDoubleFromTo(0, 360));
		knowledge = new Knowledge(); // No knowledge yet
		newInfo = false; // No new info yet

		sat_counter = 0;
		radio_counter = 0;
		w_checks = 0;
		tot_visited = 0;
		msg_len = 0;
		ext_f_count = 0;
		stepCounter = 0;
		max_ticks = 20;
		helpRange = params.getInteger("firefighter_radio_range")*2;
		radioRange = params.getInteger("firefighter_radio_range");


		helpDirection = -1;
		helpPos = new GridPoint();
		helpID = -1;

		taskReqCounter = 0;
		taskAccCounter = 0;
		taskCompCounter = 0;

		internalState = FirefighterState.EXPLORING; //change internal state according to task

		BOUNTY_MIN_BEFORE_ASKING = bounty/10;
		BOUNTY_MIN_FOR_SENDING = bounty/5;
		BOUNTY_SEND_AMOUNT = bounty/100;
		trustThold = 5; //threshold for trusting a firefighter


		rnd = new Random();
		rnd.setSeed(params.getInteger("randomSeed"));
		receivers = new ArrayList<>();

		bounty_earned = 0;
		bounty_at_end = 0;
		bounty_spend_comm = 0;
		bounty_transfered_2peer = 0;
		max_bounty_reqs = 5;
		//trust array for each firefighter and multiplier taking into account memory


		this.tot_bounty_req =new ArrayList<>();
		this.succ_bounty_req =new ArrayList<>();
		this.inc_bounty_req = new ArrayList<>();
		this.trust = new ArrayList<>();
		this.trust_mul = new ArrayList<>();

		int firefighterCount = params.getInteger("firefighter_amount");


		//initialize trust and multiplier to default values depending on character
		for (int i=0; i < firefighterCount; i++) {
				tot_bounty_req.add(0.0);
				succ_bounty_req.add(0.0);
				inc_bounty_req.add(0.0);
				trust_mul.add(1.0);
				trust.add(10.0);
		}


		// Schedule methods
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double current_tick = schedule.getTickCount();
		ScheduleParameters sch_params = ScheduleParameters.createRepeating(current_tick + 1,
				Math.round(1 / velocity.speed));
		stepSchedule = schedule.schedule(sch_params, this, "step");
	}

	// first ff which is in the correct team and is not dead will be the co-leader
	public void pickCoLeader() {

		Iterable<Object> objects = grid.getObjects();

		for (Object obj : objects) {
			if (obj.getClass() == Firefighter.class) {
				Firefighter ff = (Firefighter) obj;
				if (ff.getLifePoints() > 0 && ff.getId() != this.id && !ff.getIsCoLeader() && !ff.getIsLeader() && ((ff.getTeam() == this.team) || (this.team == 0))) {
					ff.setIsCoLeader(true);
					coLeader = ff;
					break;
				}
			}
		}
	}

	protected void setCharacter(double rnd_character) {

		if (rnd_character < params.getDouble("firefighter_proportion_cooperative")) {
			this.character = FirefighterCharacter.COOPERATIVE;
		} else if (rnd_character < (params.getDouble("firefighter_proportion_cooperative") +
				params.getDouble("firefighter_proportion_selfish"))){
			this.character = FirefighterCharacter.SELFISH;
		} else {
			this.character = FirefighterCharacter.DESTRUCTIVE;
		}

	}

	/** A step method of the firefighter */
	@ScheduledMethod(shuffle = false) // Prevent call order shuffling


	public void step() {

		if (Tools.isLastTick()) {
			bounty_at_end = getBounty();
		}

		if (!Tools.isAtTick(stepSchedule.getNextTime())) {
			return;
		} // Execute only at the specified ticks

		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		//double current_tick = schedule.getTickCount();


		if (!context.contains(this)) {
			return;
		} // Safety

		stepCounter++;
		GridPoint myPos = grid.getLocation(this);
		receivers.clear();

		// Info acquisition part (takes no time)
		checkAroundForFire(sightRange);


		if (checkSurroundedByFire()){ // If caught by fire, die
			decreaseLifePoints(lifePoints);
		} else if (stepCounter % Math.round((0.1 - velocity.speed) * 1000) == 0 && internalState.canCheckWeather()) {
			w_checks++;
			checkWeather();
		} else if (knowledge.getFire(myPos)) { // If firefighter knows that he is standing in the fire
			runOutOfFire();
		} else if (internalState == FirefighterState.HELPING) {
			if (Tools.getDistance(this.getPos(), helpPos) < sightRange) {
				double result[] = findDirection2NearestFire();
				double distance = result[1];

				//if i am in help area and there is no fire decrease trust
				if (distance > sightRange) {
					decreaseTrust(helpID);
					internalState = FirefighterState.EXPLORING;
				}
				else {
				internalState = FirefighterState.EXTINGUISHING;
				taskCompCounter++;
				moveOrExtinguish();
				}
			} else {
				moveToHelp();
			}
		} else {
			moveOrExtinguish();
		}


		//if (lifePoints > 0 && !initialized) {
		//	knowledge.addPosition(myPos, -1);
		//	initialized = true;
		//}


		/*if (knowledge.getAllFirefighters().size() != RunEnvironment.getInstance().getParameters()
				.getInteger("firefighter_amount")) {
			for (Object o : grid.getObjects()) {
				if (o.getClass() == Firefighter.class) {
					Firefighter ff = (Firefighter) o;
					ff.knowledge.addFirefighter(myPos);
					knowledge.addFirefighter(ff.getPos());
				}
			}
		}

		radioMessage();*/
	}


	/** Movement routine of a firefighter */
	private void moveOrExtinguish() {
		double result[] = findDirection2NearestFire();
		double directionToFire = result[0];
		double distance = result[1];
		GridPoint myPos = grid.getLocation(this);
		GridPoint firePos = Tools.dirToCoord(directionToFire, myPos);
		GridPoint sightPos = Tools.dirToCoord(velocity.direction, myPos);
		Fire fire = (Fire) Tools.getObjectOfTypeAt(grid, Fire.class, firePos);

		if (distance == 1 && fire != null) // If fire is exactly at the extinguishingDistance
		{

			if (firePos.equals(sightPos)) {
				if (internalState != FirefighterState.EXTINGUISHING) {
					internalState = FirefighterState.EXTINGUISHING;
					//if destructive send colleagues to random position
					if (character == FirefighterCharacter.DESTRUCTIVE) {
						HelpMultiCast(Tools.getRandomPosWithinBounds(grid));
					}
					else {
						HelpMultiCast(firePos);
					}
				}
				extinguishFire(directionToFire);
			} // Extinguish the fire in the direction of heading
			else {
				velocity.direction = directionToFire;
				if (character == FirefighterCharacter.DESTRUCTIVE) {
					HelpMultiCast(Tools.getRandomPosWithinBounds(grid));
				}
				else {
					HelpMultiCast(firePos);
				}
			} // Turn to fire
		}

		else if (distance > 1 && fire != null) {
			if (character == FirefighterCharacter.DESTRUCTIVE) {
				HelpMultiCast(Tools.getRandomPosWithinBounds(grid));
			}
			else {
				HelpMultiCast(firePos);
			}
			tryToMove(directionToFire);
		} // If fire is more than extinguishingDistance away
		else // Otherwise explore randomly
		{
			// Ask all firefighters within some range for their map, update your own map and
			// make a decision
			//askAround();
			internalState = FirefighterState.EXPLORING;
			ArrayList<GridPoint> best_moves = best_move();
			GridPoint oldPos = getPos();

			if (best_moves.size() == 0) {
				velocity.direction = RandomHelper.nextDoubleFromTo(0, 360);
				tryToMove(velocity.direction);
			} else if (best_moves.size() == 1) {
				// int move_result = move(best_moves.get(0));
				velocity.direction = Tools.getAngle(oldPos, best_moves.get(0));
				boolean move_result = tryToMove(velocity.direction);
				if (!move_result) {
					velocity.direction = RandomHelper.nextDoubleFromTo(0, 360);
					tryToMove(velocity.direction);
				}
			} else {
				while (best_moves.size() > 0) {
					int random_move = rnd.nextInt(best_moves.size());
					velocity.direction = Tools.getAngle(oldPos, best_moves.get(random_move));
					boolean move_result = tryToMove(velocity.direction);

					if (move_result) {
						break;
					} else {
						best_moves.remove(random_move);
					}
				}
				if (best_moves.size() == 0) {
					velocity.direction = RandomHelper.nextDoubleFromTo(0, 360);
					tryToMove(velocity.direction);
				}
			}
		}
	}

	private void moveToHelp() {
		GridPoint myPos = grid.getLocation(this);

		for (int i = 0; i < 8; i++) {
			GridPoint newPos = Tools.dirToCoord(helpDirection + (i % 2 == 0 ? -i * 45 : i * 45), myPos);

			if (move(newPos) == 0) {
				return;
			}
		}
	}

	/**
	 * Given a possible movement direction, generate a set of others, and try to
	 * move in one of them
	 *
	 * @param pDir
	 *            - direction to try to move to
	 * @return 0 - movement failed, 1 - movement succeeded
	 */
	private boolean tryToMove(double pDir) {
		GridPoint myPos = grid.getLocation(this);

		for (int i = 0; i < 8; i++) {
			GridPoint newPos = Tools.dirToCoord(pDir + (i % 2 == 0 ? -i * 45 : i * 45), myPos);

			if (move(newPos) == 0) {
				return true;
			}
		}

		return false;
	}

	/** Firefighter's reaction on being in the fire */
	private void runOutOfFire() {
		if (!decreaseLifePoints(1)) {
			return;
		} // Burn, and see if still moving

		Velocity knownWindVelocity = knowledge.getWindVelocity();
		double directionUpwind = RandomHelper.nextDoubleFromTo(0, 360);

		if (knownWindVelocity != null) {
			directionUpwind = knownWindVelocity.direction + 180;
		}

		tryToMove(directionUpwind); // Try to move in an upwind direction to escape from fire
	}

	/**
	 * Decrease the lifePoints of the firefighter by a given amount
	 *
	 * @param amount
	 *            - an amount to decrease by
	 * @return 1 if still active, 0 - otherwise
	 */

	private boolean isTrusted(int id) {
		if (trust.get(id) > trustThold) {
			return true;
		}
		return false;
	}

	private void decreaseTrust(int id) {

		//System.out.println("Decreasing Trust of "+id);

		trust.set(id, (trust.get(id)-trust_mul.get(id)*1));
		trust_mul.set(id, trust_mul.get(id)+1);
	}

	//check if a ff is sharing or not bounty with you depending on how often he gave you bounty
	private boolean isSharingBounty(int id) {

		if (succ_bounty_req.get(id)/tot_bounty_req.get(id) > 0.5) {
			return true;
		}
		else if (succ_bounty_req.get(id) != 0){
			decreaseTrust(id);
			return false;}

		return true;

	}

	private ArrayList<Firefighter> getLivingFirefightersOfTeam() {

		ArrayList<Firefighter> team = new ArrayList<>();
		Iterable<Object> objects = grid.getObjects();

		for (Object obj : objects) {
			if (obj.getClass() == Firefighter.class) {
				Firefighter ff = (Firefighter) obj;
				if (ff.getLifePoints() > 0 && (ff.getTeam() == 0 || ff.getTeam() == this.team)) {
					team.add(ff);
				}
			}
		}

		return team;
	}

	private boolean decreaseLifePoints(int amount) {
		lifePoints -= amount;

		if (lifePoints <= 0) {
			if (isCoLeader) {
				leader.pickCoLeader();
			}
			if (isLeader) {
				coLeader.setIsLeader(true);
				coLeader.setIsCoLeader(false);
				coLeader.pickCoLeader();
				coLeader.setLeader(null);
				for (Firefighter ff : getLivingFirefightersOfTeam()) {
					ff.setLeader(coLeader);
				}
			}
			ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			double current_tick = schedule.getTickCount();
			ScheduleParameters removeParams = ScheduleParameters.createOneTime(current_tick + 0.000001);
			removeSchedule = schedule.schedule(removeParams, this, "remove");
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
	 *
	 * @param sightRange
	 *            - number of cells from the given position to search in
	 */
	private void checkAroundForFire(int sightRange) {
		GridPoint myPos = grid.getLocation(this);

		for (int i = -sightRange; i <= sightRange; i++) {
			for (int j = -sightRange; j <= sightRange; j++) {
				checkCell(new GridPoint(myPos.getX() + i, myPos.getY() + j));
			}
		}
	}

	/**
	 * Check if a firefighter is surrounded by fire Note: this method assumes that
	 * the firefighter knowledge about the surrounding was updated already, so
	 * checkCell(...) is not called
	 *
	 * @return 1 if surrounded, 0 if not
	 */
	private boolean checkSurroundedByFire() {
		GridPoint myPos = grid.getLocation(this);

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (!(i == 0 && j == 0)) // Do not check the point at which standing
				{
					GridPoint pos = new GridPoint(myPos.getX() + i, myPos.getY() + j);

					if (!knowledge.getFire(pos)) {
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Extinguish fire in a given direction
	 *
	 * @param directionToFire
	 *            - a direction to extinguish fire in
	 */
	private void extinguishFire(double directionToFire) {
		GridPoint myPos = grid.getLocation(this);
		GridPoint firePos = Tools.dirToCoord(directionToFire, myPos);
		Fire fire = (Fire) Tools.getObjectOfTypeAt(grid, Fire.class, firePos);

		if (fire != null) {
			if (!fire.decreaseLifetime(strength)) // If the fire was extinguished
			{
				adaptBounty(params.getInteger("firefighter_fire_reward_bounty"), false);
				bounty_earned += params.getInteger("firefighter_fire_reward_bounty");
				ext_f_count++;
				knowledge.removeFire(firePos);
				internalState = FirefighterState.EXPLORING;
			}
		}
	}

	/**
	 * Move to a given position
	 *
	 * @param newPos
	 *            - a position to move to
	 * @return -1 - if move was unsuccessful, 0 - if move was successful, 1 - if
	 *         couldn't move because another firefighter already took this place
	 */
	private int move(GridPoint newPos) {
		if (Tools.isWithinBorders(newPos, grid)) {

			checkCell(newPos);

			boolean hasFire = knowledge.getFire(newPos);
			boolean hasFirefighter = knowledge.getFirefighter(newPos);

			if (!hasFire && !hasFirefighter) // Make sure that the cell is not on fire, and is not occupied by another
												// firefighter and not visited earlier
			{

				if (!knowledge.getTeamVisited().contains(newPos)) {
					tot_visited++;
				}
				knowledge.addWentTo(newPos);
				knowledge.addTVisited(newPos);

				knowledge.addPosition(newPos, (int) stepSchedule.getNextTime() + 1);
				// Sending movement message to all FF in the current sight for which he now moves out of sight

				Message message = new Message();
				message.setContent(newPos.getX() + "," + newPos.getY() + "," + (stepSchedule.getNextTime() + 1));
				if (receivers.size() > 0) {
					for (Firefighter receiver : receivers) {
						if (receiver != null && Tools.getDistance(newPos, receiver.getPos()) > sightRange) {
							if (getBounty() >= message.getCost() && message.getContent().length() > 0) {
								adaptBounty(-(message.getCost()), true);
								bounty_spend_comm += message.getCost();
								receiver.receivePositionUpdate(message);
								msg_len += message.getCost();
								radio_counter++;

							}
						}
					}
				}

				grid.moveTo(this, newPos.toIntArray(null));
				//radioMessage();

				return 0;
			} else if (hasFirefighter) {
				// sendRadio(newPos);
				return 1;
			}
		}

		return -1;
	}

	public Boolean checkFF(GridPoint pos) {
		boolean hasFirefighter = (Tools.getObjectOfTypeAt(grid, Firefighter.class, pos) != null);

		if (hasFirefighter) {
			knowledge.addFirefighter(pos);

			return true;
		} else {
			knowledge.removeFirefighter(pos);
			return false;
		}


	}

	/**
	 * Method used to find the direction and distance to the nearest fire
	 *
	 * @return a tuple (direction, distance) to the nearest fire
	 */
	private double[] findDirection2NearestFire() {
		GridPoint myPos = grid.getLocation(this);
		int minDist = Integer.MAX_VALUE;
		double direction = -1;

		for (GridPoint p : knowledge.getAllFire()) // For all the fires in the firefighter's knowledge
		{
			int dist = Tools.getDistance(myPos, p);
			// Determine if the fire is closest. If so, update distance and direction
			// accordingly
			if (dist < minDist) {
				minDist = dist;
				direction = Tools.getAngle(myPos, p);
			}
		}

		double[] result = { direction, (minDist == Integer.MAX_VALUE ? -1 : minDist) };

		return result;
	}

	/**
	 * Check if a given position contains a fire object, update own knowledge
	 *
	 * @param pos
	 *            - position to check
	 */
	public void checkCell(GridPoint pos) {
		boolean hasFirefighter = (Tools.getObjectOfTypeAt(grid, Firefighter.class, pos) != null);
		boolean hasFire = (Tools.getObjectOfTypeAt(grid, Fire.class, pos) != null);
		boolean hasForest = (Tools.getObjectOfTypeAt(grid, Forest.class, pos) != null);

		if (hasFire) {
			knowledge.addFire(pos);
		} else {
			knowledge.removeFire(pos);
		}

		if (hasFirefighter) {
			if (knowledge.addFirefighter(pos)) {
				knowledge.addPosition(pos, (int)stepSchedule.getNextTime());
				if(Tools.getDistance(pos, getPos()) == sightRange) {
					receivers.add((Firefighter)Tools.getObjectOfTypeAt(grid, Firefighter.class, pos));
				}
			}
		} else {
			knowledge.removeFirefighter(pos);
		}

		if (hasForest) {
			knowledge.addForest(pos);
		} else {
			knowledge.removeForest(pos);
		}

		newInfo = true;
	}

	public void HelpMultiCast(GridPoint firePos) {

		GridPoint myPos = grid.getLocation(this);
		Message message = new Message();
		message.setContent(Integer.toString(firePos.getX()) + "," + Integer.toString(firePos.getY())+","+id);
		int messageCost = message.getCost();
		int satelliteCostMultiplier = params.getInteger("firefighter_satellite_cost_multiplier");

		int fireDist;
		int dist;
		int c = 0;

		//if i am not leader or there is no leader I send help request to leader
		if (!isLeader || (numberOfLeaders == 0)) {
			Firefighter recipient = leader;

			//this sometimes throws exceptions so try...
			try {
				dist = Tools.getDistance(this.getPos(), recipient.getPos());
				}
			catch (Exception e) {
				dist = 1000;
			}

			if (recipient != null && recipient.getLifePoints() > 0) {

				if (dist <= radioRange && getBounty() >= messageCost) { //???
					recipient.receiveHelpMC(message); // Deliver message
					adaptBounty(-messageCost, true); // Pay for the message
					radio_counter++;
				} else {
					messageCost = messageCost * satelliteCostMultiplier;
					if (getBounty() >= messageCost) {
						recipient.receiveHelpMC(message); // Deliver message
						adaptBounty(-messageCost, true); // Pay for the message
						sat_counter++;
					}
				}

			}
		}

		//If i am leader or there is no leader I send help requests around
		else {
			for (int i = 0; i < params.getInteger("firefighter_amount"); i++) {

				Iterable<Object> objects = grid.getObjects();
				Firefighter recipient = null;

				for (Object obj : objects) {
					if (obj.getClass() == Firefighter.class) {
						Firefighter ff = (Firefighter) obj;
						//only send notification to the people in the team
						if (ff.getId() == i && (ff.getId() != id) &&(ff.getTeam() == this.getTeam())) {
							recipient = ff;
						}

					}
				}

				if (recipient != null && recipient.getLifePoints() > 0 && isTrusted(recipient.getId())) // First of all, if the recipient is there at all
				{

					try {
						dist = Tools.getDistance(this.getPos(), recipient.getPos());
						fireDist = Tools.getDistance(firePos, recipient.getPos());
					} catch (Exception e) {
						fireDist = 10000;
						dist = 10000;
					}

					if (fireDist <= helpRange && dist <= radioRange) {
						if (getBounty() >= messageCost) {
							recipient.receiveHelpMC(message); // Deliver message
							adaptBounty(-messageCost, true); // Pay for the message

							bounty_spend_comm += messageCost;
							msg_len += messageCost;
							radio_counter++;
							taskReqCounter++;
							c++;
						}

					} else if (fireDist <= helpRange) {
						int globalMessageCost = messageCost * satelliteCostMultiplier; // A cost to send a message through
						// the satellite

						if (getBounty() >= globalMessageCost) {
							recipient.receiveHelpMC(message); // send / receive message
							adaptBounty(-globalMessageCost, true); // Pay for the message

							bounty_spend_comm += globalMessageCost;
							msg_len += messageCost;
							sat_counter++;
							taskReqCounter++;
							c++;

						}
					}

				}
			}
		}
	}

	public void receiveHelpMC(Message message) {
		String[] msg = message.getContent().split(",");
		helpPos = new GridPoint(Integer.parseInt(msg[0]), Integer.parseInt(msg[1]));
		helpID = Integer.parseInt(msg[2]);

		if (!isLeader || (numberOfLeaders == 0))  {
			//System.out.print("[INFO] Received help req, not leader at "+helpPos+'\n');
			//can help if not extinguishing or inhelp and if ff is trusted
			if (internalState != FirefighterState.EXTINGUISHING && internalState != FirefighterState.HELPING && isTrusted(helpID)) {
				internalState = FirefighterState.HELPING;
				helpDirection = getHelpDirection(helpPos);
				taskAccCounter++;
			}
		}

		else {
			//System.out.print("[INFO] Received help req, leader at "+helpPos+'\n');
			HelpMultiCast(helpPos);


		}


	}

	public void receivePositionUpdate(Message message) {
		String[] coords = message.getContent().split(",");
		GridPoint newPos = new GridPoint(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
		knowledge.addFirefighter(newPos);
		knowledge.addPosition(newPos, (int)(stepSchedule.getNextTime() + 1));
	}

	private double getHelpDirection(GridPoint firePos) {

		GridPoint myPos = grid.getLocation(this);
		return Tools.getAngle(myPos, firePos);

	}


	/**
	 * not used at the moment if transmissionMethod == radio, only ff in range will
	 * be reached if transmissionMethod == satellite, try with radio, fallback to
	 * satellite
	 */
	public void bCast(ArrayList<KnowledgeType> messageContents, TransmissionMethod transmissionMethod) {

		Message message = new Message();
		message.setContent(this.knowledge.getMessageContent(messageContents));
		int messageCost = message.getCost();
		int satelliteCostMultiplier = params.getInteger("firefighter_satellite_cost_multiplier");

		Iterable<Object> objects = grid.getObjects();

		for (Object obj : objects) {
			if (obj.getClass() == Firefighter.class) {
				Firefighter ff = (Firefighter) obj;
				if (ff.getLifePoints() > 0) {

					int dist;
					try {
						dist = Tools.getDistance(this.getPos(), ff.getPos());
					} catch (Exception e) {
						dist = 10000;
					}

					boolean messageIsFinallySent = false;

					// RADIO
					if (transmissionMethod == TransmissionMethod.Radio) {
						if (dist <= radioRange && getBounty() >= messageCost) {
							messageIsFinallySent = true;
							radio_counter++;
						}
					}

					// SATELLITE
					else {
						if (dist <= radioRange && getBounty() >= messageCost) { //???
							messageIsFinallySent = true;
							radio_counter++;
						} else {
							messageCost = messageCost * satelliteCostMultiplier;
							if (getBounty() >= messageCost) {
								messageIsFinallySent = true;
								sat_counter++;
							}
						}
					}

					if (messageIsFinallySent) {
						ff.recieveMessage(message);
						adaptBounty(-messageCost, true);
						msg_len += messageCost;
					}
				}

			}
		}
	}

	/**
	 * Receive message
	 *
	 * @param message
	 *            a message
	 */
	public void recieveMessage(Message message) {
		// received a bounty request
		// received declined bounty request
		if (!message.getContent().isEmpty()) {
			if (message.getContent().contains("!")) {
//				if (character.canGiveBounty()) {
					int ffID = Integer.parseInt(message.getContent().substring(1, message.getContent().length()));

					//check if he sent too many requests dont accept and decrease trust
					inc_bounty_req.set(ffID, inc_bounty_req.get(ffID)+1);
					boolean trust_accept = true;
					if (inc_bounty_req.get(ffID) > max_bounty_reqs) {
						decreaseTrust(ffID);
						trust_accept = false;
					}
					if (trust.get(ffID) < trustThold) {
						trust_accept = false;
					}

					Iterable<Object> objects = grid.getObjects();
					for (Object obj : objects) {
						if (obj.getClass() == Firefighter.class) {
							Firefighter ff = (Firefighter) obj;
							if (ff.getId() == ffID) { // found id of correct ff to send bounty
								Message m = new Message();
								int multiplier = 5;
								if ((bounty > BOUNTY_MIN_FOR_SENDING) && trust_accept && character.canGiveBounty()) {
									m.setContent("$" + multiplier);
								} else {
									multiplier = 0;
									m.setContent("");
								}
								//System.out.println("from:" + getId() + " to:" + ff.getId() + " empty msg:" + m.getContent().isEmpty() + " costs:" + m.getCost());
								ff.recieveMessage(m);
								//System.out.println("Bounty before:" + bounty);
								adaptBounty(-m.getCost(), false);
								bounty_spend_comm += m.getCost();

								adaptBounty(-(multiplier * BOUNTY_SEND_AMOUNT), false);
								bounty_transfered_2peer += (multiplier * BOUNTY_SEND_AMOUNT);

								//System.out.println("Bounty after:" + bounty);
								msg_len += m.getCost();
								return;
							}
						}
//					}
				}
			} else if (message.getContent().contains("$")) {			// received bounty
				int value = Integer.valueOf(message.getContent().replaceAll( "[^\\d]", "" ));
				//System.out.println(value);
				adaptBounty(value * BOUNTY_SEND_AMOUNT, false);
			} else {	// regular knowledge message
				Knowledge receivedKnowledge = new Knowledge();
				receivedKnowledge.convertFromString(message.getContent());
				knowledge.addKnowledge(receivedKnowledge);
			}
		}
	}

/*	public void radioMessage() {

		Parameters params = RunEnvironment.getInstance().getParameters();
		int radioRange = params.getInteger("firefighter_radio_range");
		Message message = new Message();
		message.setContent(knowledge.getMessageContent(KnowledgeType.POS_UPDATE));

		for (GridPoint p : knowledge.getAllFirefighters()) {

			if (Math.abs(p.getX() - getPos().getX()) <= Math.min(radioRange, 2 * sightRange)
					&& Math.abs(p.getY() - getPos().getY()) <= Math.min(radioRange, 2 * sightRange)) {
				Firefighter receiver = (Firefighter) Tools.getObjectOfTypeAt(grid, Firefighter.class, p);
				if (receiver != this && receiver != null) {
					if (getBounty() >= message.getCost()) { //check for bounty?
						radio_counter++;
						msg_len += message.getContent().length();
						adaptBounty(-message.getCost(), true);
						receiver.knowledge.convertFromString(message.getContent());
					}
				}

			}
		}
	}*/

	/** Check current weather conditions */
	private void checkWeather() {
		if (context != null) {
			if (context.getObjects(Wind.class).size() > 0) {
				knowledge.setWindVelocity(((Wind) context.getObjects(Wind.class).get(0)).getWindDirection());
			}

			IndexedIterable<Object> rains = context.getObjects(Rain.class);

			for (Object o : rains) {
				knowledge.addRain(grid.getLocation(o));
			}

		}
	}

	public void adaptBounty(int change, boolean checkForLowBounty) {
		bounty += change;

		if (checkForLowBounty) {
			checkForLowBounty();
		}
	}

	private void makeBountyRequest() {

		int maxRequests = 5;
		int requestNumber = 1;
		Iterable<Object> objects = grid.getObjects();
		int myTeam = this.getTeam();
		List<Firefighter> firefightersNotInMyTeam = new ArrayList<>();
		boolean requestSuccessfull = false; //false
		boolean competitive_environment = true;

		for (Object obj : objects) {
			if (obj.getClass() == Firefighter.class) {
				Firefighter ff = (Firefighter) obj;

				int dist;
				try {
					dist = Tools.getDistance(this.getPos(), ff.getPos());
				} catch (Exception e) {
					dist = 10000;
				}

				if (ff.getLifePoints() > 0 && ff.getId() != this.getId() && requestNumber <= maxRequests) {
					if(myTeam == ff.getTeam() || ff.getTeam() == 0 || myTeam == 0) {
						if (dist <= radioRange) {
							if (isTrusted(ff.getId()) && isSharingBounty(ff.getId())) {
								tot_bounty_req.set(ff.getId(), tot_bounty_req.get(ff.getId())+1);

								int oldBounty = bounty;
								Message message = new Message();
								message.setContent("!" + id);
								if (getBounty() >= message.getCost()) {
									ff.recieveMessage(message);
									adaptBounty(-message.getCost(), false);
									bounty_spend_comm += message.getCost();

									msg_len += message.getCost();
									// request was successful
									if (bounty > oldBounty) {
										succ_bounty_req.set(ff.getId(), succ_bounty_req.get(ff.getId())+1);
										requestSuccessfull = true;
										break;
									}
									requestNumber++;
								} else {
									break;
								}

							}
						}
					} else {
						firefightersNotInMyTeam.add(ff);
					}
				}
			}
		}
		// if there are no firefighters inside my team who could give me bounty ask another team in case of collaborating teams
		if (!requestSuccessfull && !competitive_environment) {
			for( Firefighter ff : firefightersNotInMyTeam) {
				int dist;
				try {
					dist = Tools.getDistance(this.getPos(), ff.getPos());
				} catch (Exception e) {
					dist = 10000;
				}

				if (ff.getLifePoints() > 0 && ff.getId() != this.getId() && requestNumber <= maxRequests && dist <= radioRange) {
					if (isTrusted(ff.getId()) && isSharingBounty(ff.getId())) {
						tot_bounty_req.set(ff.getId(), tot_bounty_req.get(ff.getId())+1);

						int oldBounty = bounty;
						Message message = new Message();
						message.setContent("!" + id);
						if (getBounty() >= message.getCost()) {
							ff.recieveMessage(message);
							adaptBounty(-message.getCost(), false);
							bounty_spend_comm += message.getCost();

							msg_len += message.getCost();
							// request was successful
							if (bounty > oldBounty) {
								succ_bounty_req.set(ff.getId(), succ_bounty_req.get(ff.getId())+1);
								break;
							}
							requestNumber++;
						} else {
							break;
						}

					}
				}
			}
		}
	}

	private void checkForLowBounty() {
		//if destructive sometimes make random bounty reqs
		if (character.canAskForBountyHigherThanThreshhold()) {
			makeBountyRequest();
		}
		else if (BOUNTY_MIN_BEFORE_ASKING > bounty) {
			makeBountyRequest();
		}
	}

/*	public void increaseRadioCounter() {
		this.radio_counter += 1;
	}

	public void askAround() {

		int askRange = Math.min(sightRange + 1,
				RunEnvironment.getInstance().getParameters().getInteger("firefighter_radio_range"));
		for (int x = -askRange; x <= askRange; x++) {
			for (int y = -askRange; y <= askRange; y++) {
				if (x == 0 && y == 0) {
					continue;
				} else {
					GridPoint cellPos = new GridPoint(getPos().getX() + x, getPos().getY() + y);
					if (Tools.isWithinBorders(cellPos, grid)) {
						if (knowledge.getFirefighter(cellPos)) {
							Firefighter ff = (Firefighter) Tools.getObjectOfTypeAt(grid, Firefighter.class, cellPos);
							if (ff != null) {
								Message message = new Message();

								message.setContent(ff.knowledge.getMessageContent(KnowledgeType.POS_UPDATE));

								if (message.getContent().length() > 0 && ff.getBounty() >= message.getCost()) {
									ff.adaptBounty(-message.getCost(), true); //check for bounty?
									ff.increaseRadioCounter();

									this.knowledge.convertFromString(message.getContent());

									 * for (Entry<GridPoint, Integer> e : ff.knowledge.getSharedMap().entrySet()) {
									 * this.knowledge.add2sharedMap(e.getKey(), e.getValue()); }

								}
							}
						}
					}
				}
			}
		}
	}*/

	public int updateValue(GridPoint newPos) {
		int oldMap = 0;
		int newMap = 0;
		GridPoint currentPos = getPos();

		// [sightRange - 1, sightRange + 1] --> include all cells for which the sight
		// value could be effected by a movement
		for (int i = -sightRange - 1; i <= sightRange + 1; i++) {
			for (int j = -sightRange - 1; j <= sightRange + 1; j++) {
				GridPoint cellPos = new GridPoint(currentPos.getX() + i, currentPos.getY() + j);
				int cellValue = 0;
				if (Tools.isWithinBorders(cellPos, grid)) {
					if (knowledge.getFire(cellPos)) {
						return Integer.MAX_VALUE;
					} else if (knowledge.getSharedMap().containsKey(cellPos)) {
						cellValue = knowledge.getSharedMap().get(cellPos);
						//if (knowledge.getVisit(cellPos) > -1)
						//	cellValue += knowledge.getVisit(cellPos);
					} else {
						cellValue = -1;
					}

					oldMap += cellValue;

					if (Math.abs(newPos.getX() - cellPos.getX()) <= sightRange
							&& Math.abs(newPos.getY() - cellPos.getY()) <= sightRange) {
						if ((int) stepSchedule.getNextTime() >= cellValue) {
							cellValue = (int) stepSchedule.getNextTime();
						}
					}
					newMap += cellValue;
				}
			}
		}
		return (newMap - oldMap);
	}

	public ArrayList<GridPoint> best_move(){
		ArrayList<GridPoint> returnArray = new ArrayList<>();
		int max_value = Integer.MIN_VALUE;

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0) {
					continue;
				}
				GridPoint pos = new GridPoint(getPos().getX() + i, getPos().getY() + j);
				if (Tools.isWithinBorders(pos, grid)) {
					int map_diff = updateValue(pos);
					if (map_diff == Integer.MAX_VALUE) {
						double[] closestFire = findDirection2NearestFire();
						returnArray.clear();
						returnArray.add(Tools.dirToCoord(closestFire[0], getPos()));
						return returnArray;
					}
					if (map_diff > max_value) {
						max_value = map_diff;
						returnArray.clear();
						returnArray.add(pos);
					} else if (map_diff == max_value) {
						returnArray.add(pos);
					}
				}
			}
		}
		if (returnArray.size() > 2) {
			return returnArray;
		}
		return returnArray;
	}
		/*if (max_value == Integer.MAX_VALUE && returnArray.size() > 1) {

			double[] closestFire = findDirection2NearestFire();
			GridPoint posFire = Tools.dirToCoord(closestFire[0], getPos());

			for (GridPoint p : returnArray) {

				if (Tools.getAngle(getPos(), p) == closestFire[0]) {
					posFire = p;
					break;
				}
				System.out.println("(" + p.getX() + ", " + p.getY() + ")");
			}
			returnArray.clear();
			returnArray.add(posFire);
			System.out.println("");
		}
		return returnArray;
	}*/

	/** The method for removal of the object */
	@ScheduledMethod(shuffle = false) // Prevent call order shuffling
	public void remove() {
		if (!Tools.isLastTick()) {
			context.remove(this);
		} // Do not include this in the executeEndActions()
	}

	/// Local getters & setters
	/**
	 * Get current amount of lifePoints the firefighter has
	 *
	 * @return the amount of lifePoints
	 */
	public int getLifePoints() {
		return lifePoints;
	}

	/**
	 * Get current amount of strength the firefighter has
	 *
	 * @return the amount of strength
	 */
	public int getStrength() {
		return strength;
	}

	/**
	 * Get current amount of bounty the firefighter has
	 *
	 * @return the amount of bounty
	 */
	public int getBounty() {
		return bounty;
	}

	/**
	 * Get speed of the firefighter
	 *
	 * @return - the speed
	 */
	public double getSpeed() {
		return velocity.speed;
	}

	/**
	 * Get heading of the firefighter
	 *
	 * @return - the direction
	 */
	public double getHeading() {
		return velocity.direction;
	}

	/**
	 * Get ID of the firefighter
	 *
	 * @return - the ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get position of the firefighter
	 *
	 * @return - the position
	 */
	public GridPoint getPos() {
		return grid.getLocation(this);
	}

	public int getExtFC() {
		return ext_f_count;
	}

	public int getTotMsg() {
		return (radio_counter + sat_counter);
	}

	public int getRadioCounter() {
		return radio_counter;
	}

	public double getRadioPercentage() {
		if (getTotMsg() == 0) {
			return 100;
		}
		return 100*(double)radio_counter/(double)getTotMsg();
	}

	public int getBountyEarned() { return bounty_earned; }

	public int getBountyAtEnd() { return bounty_at_end; }

	public int getBountyOnComm() { return bounty_spend_comm; }

	public int getBounty2Peer() { return bounty_transfered_2peer; }

	public int getMsgTotLen() { return msg_len; }

	public int getTeam() {
		return team;
	}

	public boolean getIsCoLeader() {
		return this.isCoLeader;
	}

	public boolean getIsLeader() {
		return this.isLeader;
	}

	public void setIsLeader(boolean leader) {
		this.isLeader = leader;
	}

	public void setIsCoLeader(boolean coLeader) {
		this.isCoLeader = coLeader;
	}

	public void setLeader(Firefighter leader) {
		this.leader = leader;
	}

	public void setCoLeader(Firefighter coLeader) {
		this.coLeader = coLeader;
	}

	public ArrayList<Double> getTrust(){return trust;}

	public String getCharacter() {
		if (character == FirefighterCharacter.DESTRUCTIVE) {
			return "Destructive";
		}

		if (character == FirefighterCharacter.SELFISH) {
			return "Selfish";
		}

		return "Cooperative";


	}

	/*
	 * public int getTotVisited() { return tot_visited; }
	 * public int getWChecks() { return w_checks; }
	 * public int getMsgTotLen() { return msg_len; }
	 * public int getSuccFireUpd() { return knowledge.getSuccFireUpdate(); }
	 * public int getSuccForestUpd() { return knowledge.getSuccForestUpdate(); }
	 * public int getSatCounter() { return sat_counter; }
	 * public int getLocLen() { return knowledge.getTeamVisited().size(); }
	 * public float getMsgLenAvg() {
		if (sat_counter + radio_counter != 0) {
			return msg_len / (sat_counter + radio_counter);
		}

		return 0;
		}
	 * public int getTaskReqCounter() {
		return taskReqCounter;
		}
	 * public int getTaskAccCounter() {
			return taskAccCounter;
		}
	 * public int getTaskCompCounter() {
			return taskCompCounter;
		}
	*/

}
