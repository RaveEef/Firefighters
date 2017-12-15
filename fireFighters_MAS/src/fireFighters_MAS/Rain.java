package fireFighters_MAS;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
////NOTE ---> DO NOT MODIFY THIS FILE <---
/**
 * A class describing the Rain agent and its behavior
 * @author Kirill Tumanov, 2015-2017
 */
public class Rain
{
	// Local variables declaration
	private Context<Object> context; // Context in which the rain is placed
	private Grid<Object> grid; // Grid in which the rain is projected
	private int strength; // Amount of damage the rain can deal to the fire
	private double moveRandomness; // A percentage, by which the speed and direction of the rain cloud is altered at each step
	private double spawnProb; // A probability to spawn a new rain cloud next to itself
	private double vaporProb; // A probability to evaporate/disappear
	private Velocity velocity; // Vector describing the rain movement's heading and speed
	ISchedulableAction stepSchedule; // Action scheduled for the step method
	/**
	 * Custom constructor
	 * @param context - context to which the object is added
	 * @param grid - a grid to add the object to
	 */
	public Rain(Context<Object> context, Grid<Object> grid, GridPoint pos)
	{
		Parameters params = RunEnvironment.getInstance().getParameters();
		// Initialize local variables
		this.context = context;
		this.grid = grid;
		strength = params.getInteger("rain_strength");
		double initialSpeed = params.getDouble("rain_initial_speed");
		double initialSpeedDeviation = params.getDouble("rain_initial_speed_deviation");
		velocity = new Velocity(RandomHelper.nextDoubleFromTo(initialSpeed - initialSpeedDeviation, initialSpeed + initialSpeedDeviation),
				RandomHelper.nextDoubleFromTo(0, 360));
		moveRandomness = params.getDouble("rain_moveRandomness");
		spawnProb = params.getDouble("rain_spawnProb");
		vaporProb = params.getDouble("rain_vaporProb");
		// Add the object to the context
		if (!context.contains(this)) { context.add(this); }
		// Add the object to the grid
		if (pos != null) { grid.moveTo(this, pos.toIntArray(null)); }
		else { grid.moveTo(this,Tools.getRandomPosWithinBounds(grid).toIntArray(null)); }
		// Schedule methods
	    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	    double current_tick = schedule.getTickCount();
	    ScheduleParameters sch_params = ScheduleParameters.createRepeating(current_tick + 1, Math.round(1 / velocity.speed));
	    stepSchedule = schedule.schedule(sch_params, this, "step");
	}
	/** Rain step method */
	@ScheduledMethod(shuffle=false) // Prevent call order shuffling
	public void step()
	{
	    if (!Tools.isAtTick(stepSchedule.getNextTime())) { return; } // Execute only at the specified ticks
		
		if (!context.contains(this)) { return; } // Safety
		
		GridPoint myPos = grid.getLocation(this);
		// Extinguish any fire on the ground of the current location
		Fire fire = (Fire) Tools.getObjectOfTypeAt(grid, Fire.class, myPos);
		
		if (fire != null) { fire.decreaseLifetime(strength); }
		//
		spawn(); // Span new rain clouds around itself
		move(); // Move to a new place
		vapor(); // Disappear, if no water is left
		// Schedule a reschedule of the object according to the updated parameters
	    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	    double current_tick = schedule.getTickCount();
	    ScheduleParameters sch_params = ScheduleParameters.createOneTime(current_tick + 0.000001);
	    schedule.schedule(sch_params, this, "reschedule");
	}
	/** Rain movement function */
	private void move()
	{
		// Move the rain
		GridPoint myPos = grid.getLocation(this);
		GridPoint targetPos = Tools.dirToCoord(velocity.direction, myPos);
		
		if (Tools.isWithinBorders(targetPos, grid)) { grid.moveTo(this, targetPos.toIntArray(null)); }
		else
		{
			context.remove(this);
			return;
		}
		// Alter the rain velocity according to the wind velocity
		Velocity windDirectionVector = ((Wind) context.getObjects(Wind.class).get(0)).getWindDirection();		
		velocity = Tools.addVectors(velocity, windDirectionVector);
		//
		velocity.direction *= (RandomHelper.nextDouble() < 0.5) ? (1 + moveRandomness) : (1 - moveRandomness);
		velocity.speed *= (RandomHelper.nextDouble() < 0.5) ? (1 + moveRandomness) : (1 - moveRandomness);
		// Make sure the speed is within the limits
		Parameters params = RunEnvironment.getInstance().getParameters();
		double initialSpeed = params.getDouble("rain_initial_speed");
		double initialSpeedDeviation = params.getDouble("rain_initial_speed_deviation");
		velocity.speed = Math.max(velocity.speed, initialSpeed - initialSpeedDeviation);
		velocity.speed = Math.min(velocity.speed, initialSpeed + initialSpeedDeviation);
	}
	/** Rain spawn method */
	private void spawn()
	{
		if (RandomHelper.nextDouble() < spawnProb)
		{
			Velocity spawnVelocity = ((Wind) context.getObjects(Wind.class).get(0)).getWindDirection();			
			GridPoint myPos = grid.getLocation(this);
			GridPoint targetPos = Tools.dirToCoord(spawnVelocity.direction, myPos);
			
			if (Tools.isWithinBorders(targetPos, grid)) { new Rain(context, grid, targetPos); }
		}
	}
	/** Rain evaporation method */
	private void vapor()
	{
		if (RandomHelper.nextDouble() < vaporProb)
		{
			RunEnvironment.getInstance().getCurrentSchedule().removeAction(stepSchedule);
			context.remove(this);
		}
	}
	/** Reschedule the step method of the rain object */
	@ScheduledMethod(shuffle=false) // Prevent call order shuffling
	public void reschedule()
	{
		// Reschedule step calls for the object according to the updated parameters
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	    double next_tick = stepSchedule.getNextTime();
	    double current_tick = schedule.getTickCount();
		
		if (!schedule.removeAction(stepSchedule))
		{
			System.out.println("Couldn't remove the action from the queue. Scheduling another reschedule.");
			ScheduleParameters sch_params = ScheduleParameters.createOneTime(current_tick + 0.000001);
		    schedule.schedule(sch_params, this, "reschedule");
		    return;
		}
		
	    ScheduleParameters sch_params = ScheduleParameters.createRepeating(next_tick + 0.000001, Math.round(1 / velocity.speed));
	    stepSchedule = schedule.schedule(sch_params, this, "step");
	}
	/// Local getters & setters
	/**
	 * Get strength of the rain
	 * @return - the strength
	 */
	public int getStrength() { return strength; }
}
