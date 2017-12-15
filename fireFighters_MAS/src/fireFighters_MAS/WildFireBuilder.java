package fireFighters_MAS;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.BouncyBorders;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
/**
 * A class used to build the simulation environment
 * @author Kirill Tumanov, 2015-2017
 */
public class WildFireBuilder implements ContextBuilder<Object>
{
	ISchedulableAction addRainSchedule; // Action scheduled for the addRain method
	
	@Override
	public Context<Object> build(Context<Object> context) // Build a context of the simulation
	{
		context.setId("fireFighters_MAS");
		context = initRandom(context);
		return context;
	}
	/**
	 * Initialize simulation with random positioning of firefighters
	 * @param context - a context to build
	 * @return - a built context
	 */
	public Context<Object> initRandom(Context<Object> context)
	{
		// Get access to the user accessible parameters
		Parameters params = RunEnvironment.getInstance().getParameters();
		// Create a grid for the simulation
		int gridXsize = params.getInteger("gridWidth");
		int gridYsize = params.getInteger("gridHeight");
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new BouncyBorders(), new SimpleGridAdder<Object>(), true, gridXsize, gridYsize));
		// Create firefighter instances, and add them to the context and to the grid in random locations
		int firefighterCount = params.getInteger("firefighter_amount");
		Random rnd_character = new Random();
		rnd_character.setSeed(params.getInteger("randomSeed"));
		
		// get information about teams and leaders
		int numberOfTeams = params.getInteger("firefighter_num_teams");
		int numberOfLeaders = params.getInteger("firefighter_num_leaders");
		
		Firefighter firstLeader = null;
		Firefighter secondLeader = null;
		
		// create leader firefighters
		for (int i = 0; i < numberOfLeaders; i++) 
		{
			int team;
			if (numberOfLeaders == 1 && numberOfTeams == 2)
				team = 0;
			else
				team = (i % numberOfTeams) + 1; 
			
			Firefighter f = new Firefighter(context, grid, i, team, null, true);
			if (team == 0 || team == 1) {
				firstLeader = f;
			} else {
				secondLeader = f;
			}
			
			f.setCharacter(rnd_character.nextDouble());
			context.add(f);
			grid.moveTo(f, Tools.getRandomPosWithinBounds(grid).toIntArray(null));
		}
		
		// create regular firefighters
		for (int i = numberOfLeaders; i < firefighterCount; i++) {
			int team = (i % numberOfTeams) + 1;
			Firefighter leader;
			if (team == 2 && numberOfLeaders == 2) {
				leader = secondLeader;
			} else {
				leader = firstLeader;
			}
			Firefighter f = new Firefighter(context, grid, i, team, leader, false);
			f.setCharacter(rnd_character.nextDouble());
			context.add(f);
			grid.moveTo(f, Tools.getRandomPosWithinBounds(grid).toIntArray(null));
		}
		
		if (firstLeader != null) {
			firstLeader.pickCoLeader();
		}
		if (secondLeader != null) {
			secondLeader.pickCoLeader();
		}
		
		// Create forest instances, and add them to the context and to the grid
		double forestProb = 1; // Probability to plant a forest on a grid cell
		
		for (int i = 0; i < gridXsize; i++)
		{
			for (int j = 0; j < gridYsize; j++)
			{
				if (RandomHelper.nextDouble() < forestProb)
				{
					Forest f = new Forest(context,grid);
					context.add(f);
					grid.moveTo(f, i, j);
				}
			}
		}
		// Add wind to the simulation
		context.add(new Wind());
		// Schedule methods
	    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	    double rainProb = params.getDouble("rain_generation_speed");
	    ScheduleParameters sch_params = ScheduleParameters.createRepeating(1, 1 / rainProb);
	    addRainSchedule = schedule.schedule(sch_params, this, "addRain", context, grid);
	    // Set the simulation termination tick
	    int endTick = params.getInteger("end_tick");
	    RunEnvironment.getInstance().endAt(endTick);
		return context;
	}
	/**
	 * Method called regularly to add a new rain to a random location
	 * @param context - context to add the rain to
	 * @param grid - grid to add the rain to
	 */
	public void addRain(Context<Object> context, Grid<Object> grid) { new Rain(context, grid, null); }
}
