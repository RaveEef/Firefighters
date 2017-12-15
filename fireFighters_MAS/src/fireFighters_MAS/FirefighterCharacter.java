package fireFighters_MAS;

public enum FirefighterCharacter {
	// boolean order as variables below
	SELFISH (false, false, false),
	DESTRUCTIVE (true, false, true),
	COOPERATIVE (true, true, false);

	FirefighterCharacter(boolean sendHelpRequests, boolean giveBounty, boolean askForBountyHigherThanLimit) {
		this.sendHelpRequests = sendHelpRequests;
		this.giveBounty = giveBounty;
		this.askForBountyHigherThanThreshhold = askForBountyHigherThanLimit;
	}

	// variables (boolean in character)
	private boolean sendHelpRequests;
	private boolean giveBounty;
	private boolean askForBountyHigherThanThreshhold;

	public boolean canSendHelpRequests() {
		return sendHelpRequests;
	}

	public boolean canGiveBounty() {
		return giveBounty;
	}

	public boolean canAskForBountyHigherThanThreshhold() {
		return askForBountyHigherThanThreshhold;
	}
}
