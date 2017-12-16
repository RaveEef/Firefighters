package fireFighters_MAS;

public enum FirefighterState {
	EXPLORING (true),
	HELPING (false),
	EXTINGUISHING (false),
	MOVING2FIRE (false);

	FirefighterState(boolean checkWeather) {
		this.checkWeather = checkWeather;
	}

	private boolean checkWeather;

	public boolean canCheckWeather() {
		return checkWeather;
	}
}
