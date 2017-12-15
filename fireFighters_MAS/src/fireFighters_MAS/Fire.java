package fireFighters_MAS;

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
import java.util.*;
//// NOTE ---> DO NOT MODIFY THIS FILE <---
/**
 * A class describing the Fire agent and its behavior
 * @author Kirill Tumanov, 2015-2017
 */
public class Fire
{
	// Local variables definition
	private Context<Object> context; // Context in which the fire is placed
	private Grid<Object> grid; // Grid in which the fire is projected
	private int lifetime; // Number of steps the fire can take before extinction
	private int strength; // Burn strength, at which the fire damages other agents
	private Velocity velocity; // Vector describing the fire movement's heading and speed
	ISchedulableAction stepSchedule;	// Action scheduled for the step method
	ISchedulableAction removeSchedule;	// Action scheduled for the remove method
	/**
	 * Custom constructor
	 * @param context - context to which the fire is added
	 * @param grid - a grid to add the fire to
	 * @param speed - a speed to be assigned to the fire (use 0.0 to use the user specified initial speed)
	 */
	public Fire(Context<Object> context, Grid<Object> grid, double speed)
	{
		Parameters params = RunEnvironment.getInstance().getParameters();
		// Initialize local variables
		this.context = context;
		this.grid = grid;
		lifetime = params.getInteger("fire_lifetime");
		strength = params.getInteger("fire_strength");
		double initialSpeed = params.getDouble("fire_initial_speed");
		double initialSpeedDeviation = params.getDouble("fire_initial_speed_deviation");
		
		if (speed == 0.0)
		{
			velocity = new Velocity(RandomHelper.nextDoubleFromTo(initialSpeed - initialSpeedDeviation, initialSpeed + initialSpeedDeviation),
					RandomHelper.nextDoubleFromTo(0, 360));
		}
		else { velocity = new Velocity(speed, RandomHelper.nextDoubleFromTo(0, 360)); }
		// Schedule methods
	    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	    double current_tick = schedule.getTickCount();
	    ScheduleParameters stepSchParams = ScheduleParameters.createRepeating(current_tick + 1, Math.round(1 / velocity.speed));
	    stepSchedule = schedule.schedule(stepSchParams, this, "step");
	}
	/** A step method of the fire */
	@ScheduledMethod(shuffle=false) // Prevent call order shuffling
	public void step()
	{
	    if (!Tools.isAtTick(stepSchedule.getNextTime())) { return; } // Execute only at the specified ticks
	    
		if (!context.contains(this)) { return; } // Safety
		// Burn forest in the cell, if any
		GridPoint myPos = grid.getLocation(this);
		Forest f = (Forest) Tools.getObjectOfTypeAt(grid, Forest.class, myPos);
		
		if (f != null)
		{
			if (!f.decreaseHealth(strength)) // Burn the wood and vanish, if all the wood is burnt now
			{ 
				decreaseLifetime(lifetime);
				return;
			}
		}		
		// Spread to a new cell
		Velocity windDirection = ((Wind) context.getObjects(Wind.class).get(0)).getWindDirection();			
		spawnFire(Tools.addVectors(velocity, windDirection).direction);			
		decreaseLifetime(1); // Reduce lifetime for taking this step
	}
	/**
	 * Spawn a new fire in a given direction, form itself
	 * @param direction - direction to move in
	 */
	private void spawnFire(double direction)
	{
		GridPoint myPos = grid.getLocation(this);
		List<Double> probs = new ArrayList<Double>(); // Directional probabilities
		int granularity = 8;
		
		for (int i = 0; i < granularity; i++) { probs.add(1.0 / granularity); }
		
		for (int i = 0; i < probs.size(); i++)
		{
			GridPoint newPos = Tools.dirToCoord(i * (360 / granularity), myPos);
			
			if (!Tools.isWithinBorders(newPos, grid)) { continue; }
			
			boolean hasRain = Tools.getObjectOfTypeAt(grid, Rain.class, newPos) != null;
			
			probs.set(i, probs.get(i) * (hasRain ? 0.5 : 1) * (1 - (double)Math.abs(i * (360 / granularity) - direction) / 360));
			
			if (RandomHelper.nextDouble() < probs.get(i))
			{
				igniteTowards(newPos, hasRain);
				break;
			}
		}
	}
	/**
	 * Spawn the fire given a new position
	 * @param newPos - new position
	 * @param hasRain - new position has rain on it
	 */
	public void igniteTowards(GridPoint newPos, boolean hasRain)
	{
		if (Tools.isWithinBorders(newPos, grid))
		{
			boolean hasFire = Tools.getObjectOfTypeAt(grid, Fire.class, newPos) != null;
			boolean hasForest = Tools.getObjectOfTypeAt(grid, Forest.class, newPos) != null;
			
			if (hasForest && !hasFire)
			{
				Parameters params = RunEnvironment.getInstance().getParameters();
				double fireSpeedRainMultiplier = params.getDouble("fire_speed_rain_multiplier");
				double initialSpeed = params.getDouble("fire_initial_speed");
				double initialSpeedDeviation = params.getDouble("fire_initial_speed_deviation");
				double newFireSpeed = hasRain ? velocity.speed / fireSpeedRainMultiplier : velocity.speed * fireSpeedRainMultiplier;
				newFireSpeed = Math.max(newFireSpeed, initialSpeed - initialSpeedDeviation);
				newFireSpeed = Math.min(newFireSpeed, initialSpeed + initialSpeedDeviation);
				
				Fire newFire = new Fire(context, grid, newFireSpeed);
				context.add(newFire);
				grid.moveTo(newFire, newPos.getX(), newPos.getY());
			}
		}
	}
	/**
	 * Decrease the lifetime of the fire by a given amount
	 * @param amount - an amount to decrease by
	 * @return 1 if still active, 0 - otherwise
	 */
	public boolean decreaseLifetime(int amount)
	{
		lifetime -= amount;
		
		if (lifetime <= 0)
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
	/** The method for removal of the object */
	@ScheduledMethod(shuffle=false) // Prevent call order shuffling
	public void remove()
	{
	    if (!Tools.isLastTick()) { context.remove(this); } // Do not include this in the executeEndActions()
	}
	/// Local getters & setters
	/**
	 * Get lifetime of the fire
	 * @return - the lifetime
	 */
	public int getLifetime() { return lifetime; }
	/**
	 * Get strength of the fire
	 * @return - the strength
	 */
	public int getStrength() { return strength; }
}
