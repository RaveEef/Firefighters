package fireFighters_MAS;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
////NOTE ---> DO NOT MODIFY THIS FILE <---
/**
 * A class describing the Wind agent and its behavior
 * @author Kirill Tumanov, 2015-2017
 */
public class Wind
{
	// Local variables declaration
	private Velocity velocity; // Vector describing the wind movement's heading and speed
	ISchedulableAction stepSchedule; // Action scheduled for the step method
	/** Custom constructor */
	public Wind()
	{
		Parameters params = RunEnvironment.getInstance().getParameters();
		// Initialize local variables
		double initialSpeed = params.getDouble("wind_initial_speed");
		double initialSpeedDeviation = params.getDouble("wind_speed_deviation");
		velocity = new Velocity(RandomHelper.nextDoubleFromTo(initialSpeed - initialSpeedDeviation, initialSpeed + initialSpeedDeviation),
				RandomHelper.nextDoubleFromTo(0, 360));
		double windDirectionChangeSpeed = params.getDouble("wind_direction_change_speed");
		// Schedule methods
	    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	    double current_tick = schedule.getTickCount();
	    ScheduleParameters sch_params = ScheduleParameters.createRepeating(current_tick + 1, Math.round(1 / windDirectionChangeSpeed), 1);
	    stepSchedule = schedule.schedule(sch_params, this, "changeWindDirection");
	}
	/** The function to change the wind direction */
	@ScheduledMethod(shuffle=false) // Prevent call order shuffling
	public void changeWindDirection()
	{
		Parameters params = RunEnvironment.getInstance().getParameters();
		double directionVariation = params.getDouble("wind_direction_variation");
		double speedDeviation = params.getDouble("wind_speed_deviation");
		
		velocity.direction += RandomHelper.nextDoubleFromTo(-directionVariation, directionVariation);
		velocity.speed += RandomHelper.nextDoubleFromTo(-speedDeviation, speedDeviation);
	}
	/**
	 * Get velocity of the wind
	 * @return - the velocity
	 */
	public Velocity getWindDirection() { return velocity; }
	/**
	 * Get speed of the wind
	 * @return - the speed
	 */
	public double getSpeed() { return velocity.speed; }
	/**
	 * Get direction of the wind
	 * @return - the direction
	 */
	public double getHeading() { return velocity.direction; }
}
