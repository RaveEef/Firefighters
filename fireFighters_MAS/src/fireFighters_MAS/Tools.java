package fireFighters_MAS;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
////NOTE ---> DO NOT MODIFY THIS FILE <---
/**
 * A class containing usefull Tools for methods of other agents
 * @author Kirill Tumanov, 2015-2017
 */
public class Tools
{
	/**
	 * Get coordinates of a grid cell located in a given direction from a given grid cell coordinates
	 * @param direction - direction to the other cell
	 * @param point - this grid cell
	 * @return set of coordinates of the other cell (X,Y)
	 */
	public final static GridPoint dirToCoord(double direction, GridPoint point)
	{
		int x = point.getX(), y = point.getY();
		
		if ((direction >= 337.5 && direction <= 360) || (direction >= 0 && direction < 22.5)) // North
		{
			y++;
		}
		else if (direction >= 22.5 && direction < 67.5) // North-East
		{
			x++;
			y++;
		}
		else if (direction >= 67.5 && direction < 112.5) // East
		{
			x++;
		}
		else if (direction >= 112.5 && direction < 157.5) // South-East
		{
			x++;
			y--;
		}
		else if (direction >= 157.5 && direction < 202.5) // South
		{
			y--;
		}
		else if (direction >= 202.5 && direction < 247.5) // South-West
		{
			x--;
			y--;
		}
		else if (direction >= 247.5 && direction < 292.5) // West
		{
			x--;
		}
		else if (direction >= 292.5 && direction < 337.5) // North-West
		{
			x--;
			y++;
		}
		
		return new GridPoint(x,y);
	}
	/**
	 * Check that given grid cell coordinates lay inside a given grid
	 * @param point - a point to check
	 * @param grid - a grid to check against
	 * @return 0 - if out of borders, 1 - if within borders
	 */
	public final static boolean isWithinBorders(GridPoint point, Grid<Object> grid)
	{
		return (point.getX() >= 0 && point.getY() >= 0 && point.getX() < grid.getDimensions().getWidth()
	            && point.getY() < grid.getDimensions().getHeight());
	}
	/**
	 * Find a resulting vector, given the two
	 * @param v1 - the first vector of the sum
	 * @param v2 - the second vector of the sum
	 * @return - a resulting/sum of the given two vectors
	 */
	public final static Velocity addVectors(Velocity v1, Velocity v2)
	{
		double angRad = Math.toRadians(180 - (v1.direction - v2.direction));
		double resSpeed = Math.sqrt(Math.pow(v1.speed,2) + Math.pow(v2.speed,2) - 2 * v1.speed * v2.speed * Math.cos(angRad));
		double directionDiff = Math.toDegrees(Math.asin(v2.speed * Math.sin(angRad) / resSpeed));
		double resDirection = v1.direction - directionDiff;
		
		Velocity v = new Velocity(resSpeed, resDirection);
		return v;
	}
	/**
	 * Get angle between two points
	 * @param p1 - first point
	 * @param p2 - second point
	 * @return angle in degrees between the points
	 */
	public final static double getAngle(GridPoint p1, GridPoint p2)
	{
		if (p1.equals(p2)) { return 0; }
		
	    double angle = Math.toDegrees(Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX()));
	    
	    angle = 90 - angle; // Make North the reference direction

	    if (angle < 0) { angle += 360; }

	    return angle;
	}
	/**
	 * Get distance between two points (Chebyshev Distance)
	 * @param p1 - first point
	 * @param p2 - second point
	 * @return distance in cells between the given points
	 */
	public final static int getDistance(GridPoint p1, GridPoint p2)
	{
		if (p1.equals(p2)) { return 0; }
		// Absolute linear distances
		int fXDistance = Math.abs(p2.getX() - p1.getX()), fYDistance = Math.abs(p2.getY() - p1.getY());
		//
		return (Math.max(fXDistance,fYDistance));
	}
	/**
	 * Get an object of the given type, at a given position from a given grid
	 * @param grid - grid to use
	 * @param type - object type to search for
	 * @param pos - position to search at
	 * @return - object - if found, null - if not
	 */
	public final static <T> Object getObjectOfTypeAt(Grid<Object> grid, T type, GridPoint pos)
	{
		if (Tools.isWithinBorders(pos, grid))
		{
			Iterable<Object> objects = grid.getObjectsAt(pos.getX(), pos.getY());
			
			for (Object obj : objects)
			{
				if (obj.getClass() == type) { return obj; }
			}
		}
		
		return null;
	}
	/**
	 * Get a point with random coordinates with the given grid
	 * @param grid - grid to use
	 * @return a random position on the grid
	 */
	public final static GridPoint getRandomPosWithinBounds(Grid<Object> grid)
	{
		int x = -1;
		int y = -1;
		GridPoint pos = new GridPoint(x,y);
		
		while (!Tools.isWithinBorders(pos, grid))
		{
			x = RandomHelper.nextIntFromTo(-1, (grid.getDimensions().getWidth() - 1));
			y = RandomHelper.nextIntFromTo(-1, (grid.getDimensions().getHeight() - 1));
			pos = new GridPoint(x,y);
		}
		
		return pos;		
	}
	/**
	 * Check if now at the last tick of the simulation
	 * @return 1 - if at the last tick, 0 - if not
	 */
	public final static boolean isLastTick()
	{
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	    double current_tick = schedule.getTickCount();
		Parameters params = RunEnvironment.getInstance().getParameters();
	    int endTick = params.getInteger("end_tick");
	    
	    if (endTick == current_tick) { return true; }
	    
	    return false;
	}
	/**
	 * Check if now at the specified tick of the simulation
	 * @param tick - a tick to compare the current one with
	 * @return 1 - if at the tick, 0 - if not
	 */
	public final static boolean isAtTick(double tick)
	{
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	    double current_tick = schedule.getTickCount();
	    
	    if (tick == current_tick) { return true; }
	    
	    return false;
	}
}
