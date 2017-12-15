package fireFighters_MAS;
////NOTE ---> DO NOT MODIFY THIS FILE <---
/**
 * A class describing the Velocity of an agent
 * @author Kirill Tumanov, 2015-2017
 */
public class Velocity
{
	// Local variables declaration
	public double speed; // Speed at which the agent moves in the given direction
	public double direction; // Direction of the agent's heading
	/**
	 * Custom constructor
	 * @param speed - numerical value of the vector
	 * @param direction - direction of pointing
	 */
	public Velocity(double speed, double direction)
	{
		// Initialize local variables
		this.speed = speed; // In seconds - time it takes to move for 1 grid cell
		this.direction = direction; // In degrees - 0 is North
	}
}
