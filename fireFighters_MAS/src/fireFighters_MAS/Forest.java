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
////NOTE ---> DO NOT MODIFY THIS FILE <---
/**
 * A class describing the Forest agent and its behavior
 * @author Kirill Tumanov, 2015-2017
 */
public class Forest
{
	// Local variables definition
	private Context<Object> context; // Context in which the forest is placed
	private Grid<Object> grid; // Grid in which the forest is projected
	private int health; // Amount of damage the forest can still take from the fire, before extinction
	ISchedulableAction igniteSchedule;	// Action scheduled for the ignite method
	ISchedulableAction removeSchedule;	// Action scheduled for the remove method
	/**
	 * Custom constructor
	 * @param context - context to which the forest is added
	 * @param grid - a grid object to add the forest object to
	 */
	public Forest(Context<Object> context, Grid<Object> grid)
	{
		Parameters params = RunEnvironment.getInstance().getParameters();
		// Initialize local variables
		this.context = context;
		this.grid = grid;
		health = params.getInteger("forest_life");
		int ignititionProbabilityInterval = params.getInteger("forest_ignition_prob");
		// Schedule methods
	    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	    ScheduleParameters sch_params = ScheduleParameters.createRepeating(RandomHelper.nextIntFromTo(0,ignititionProbabilityInterval),
	    		RandomHelper.nextIntFromTo(0,ignititionProbabilityInterval));
	    igniteSchedule = schedule.schedule(sch_params, this, "ignite");
	}
//	@ScheduledMethod(start = 10000, interval = 10000)
//	public void regrowth() { grow(); }
	/** Spawn new forest objects at random on the grid and around the forest on empty cells */
	public void grow()
	{
		GridPoint myPos = grid.getLocation(this);
		
		for (int i = 0; i < 4; i++)
		{
			Forest f = new Forest(context,grid);
			int x = myPos.getX(), y = myPos.getY();
			
			switch (i)
			{
				case 0:
					x++;
					break;
				case 1:
					x--;
					break;
				case 2:
					y++;
					break;
				case 3:
					y--;
					break;
			}
			
			if (Tools.isWithinBorders(new GridPoint(x,y), grid) && grid.getObjectAt(x, y) == null)
			{
				context.add(f);
				grid.moveTo(f, x, y);
			}
		}
		
		GridPoint randPos = Tools.getRandomPosWithinBounds(grid);
		
		if (Tools.isWithinBorders(randPos, grid) && grid.getObjectAt(randPos.toIntArray(null)) == null)
		{
			Forest fNew = new Forest(context,grid);
			context.add(fNew);
			grid.moveTo(fNew, randPos.toIntArray(null));
		}
	}
	/**
	 * Decrease health of the forest object by a given amount
	 * @param amount - amount of health loss
	 * @return 1 if still active, 0 - otherwise
	 */
	public boolean decreaseHealth(int amount)
	{
		health -= amount;
		
		if (health <= 0)
		{
			ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			double current_tick = schedule.getTickCount();
			ScheduleParameters removeParams = ScheduleParameters.createOneTime(current_tick + 0.000001);
			removeSchedule = schedule.schedule(removeParams,this,"remove");
	    	// Schedule method calls to the future
		    igniteSchedule.reschedule(new ActionQueue());
		    // Remove the future method calls
		    schedule.removeAction(igniteSchedule);
		    return false;
		}
		
		return true;
	}
	/** Ignite the forest cell, by adding a fire object to the cell */
	@ScheduledMethod(shuffle=false) // Prevent call order shuffling
	public void ignite()
	{
	    if (!Tools.isAtTick(igniteSchedule.getNextTime())) { return; } // Execute only at the specified ticks
		
		if (!context.contains(this)) { return; } // Safety check
		
		GridPoint myPos = grid.getLocation(this);
		
		if (Tools.getObjectOfTypeAt(grid, Fire.class, myPos) == null) // Ignite only non-burning cells
		{
			Fire fire = new Fire(context, grid, 0.0);
			context.add(fire);
			grid.moveTo(fire, myPos.toIntArray(null));
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
	 * Get the health of the forest
	 * @return - the health
	 */
	public int getHealth() { return health; }
}
