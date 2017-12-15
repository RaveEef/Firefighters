package fireFighters_MAS;

import java.util.Random;

import org.apache.commons.math3.analysis.function.Abs;
import org.stringtemplate.v4.compiler.STParser.ifstat_return;

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
		
		// [leader, leader]
		int[] leaders =  new int[numberOfLeaders];
		for (int i = 0; i < numberOfLeaders; i++) {
			
			int leader = rnd_character.nextInt(firefighterCount);
			if (i == 0)
				leaders[i] = leader;
			else {
				// So no double leaders, and for both teams a leader (even team: odd leader)
				while (leader == leaders[0] || (leader%2) == (leaders[0]%2)) {
					leader = rnd_character.nextInt(firefighterCount);
				}
				leaders[i] = leader;
			}
		}
		
		Firefighter[] leaderFF = new Firefighter[numberOfLeaders];
		for (int i = 0; i < numberOfLeaders; i++) {
			leaderFF[i] = new Firefighter(context, grid, leaders[i], (leaders[i]%2) + 1, null);
			leaderFF[i].setCharacter(rnd_character.nextDouble());
			context.add(leaderFF[i]);
			grid.moveTo(leaderFF[i], Tools.getRandomPosWithinBounds(grid).toIntArray(null));
		}

		nextFirefighter:
		for (int i = 0; i < firefighterCount; i++) {
			
			for (int j = 0; j < numberOfLeaders; j++) {
				if (i == leaders[j])
					continue nextFirefighter;
			}
			
			Firefighter ff;
			if (numberOfTeams == 1){
				if (numberOfLeaders == 0)
					ff = new Firefighter(context, grid, i, 1, null);
				else
					ff = new Firefighter(context, grid, i, 1, leaderFF[0]);
			} else {
				if (numberOfLeaders == 0)
					ff = new Firefighter(context, grid, i, (i%2) + 1, null);
				else if (numberOfLeaders == 1)
					ff = new Firefighter(context, grid, i, (i%2) + 1, leaderFF[0]);
				else
					ff = new Firefighter(context, grid, i, (i%2) + 1, leaderFF[(i+1)%2]);
			}
			ff.setCharacter(rnd_character.nextDouble());
			context.add(ff);
			grid.moveTo(ff, Tools.getRandomPosWithinBounds(grid).toIntArray(null));
		}
/*		Leader/Team issues resolved

- Firefighter with ID = 0 was always chosen; now depending on rndSeed.
- If Leader = 0, there was still a leader and coleader assigned.
- In the case of 2 leaders, 2 teams, both teams have a leader which      would have been in the same team if it was no leader.
- Coleader is in the same team as the leader'
*/		for (int i = 0; i < numberOfLeaders; i++) {
			leaderFF[i].pickCoLeader();
			System.out.println("leader " + leaderFF[i].getId() + " choose FF " + leaderFF[i].getCoLeaderId() + " as Coleader.");
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
