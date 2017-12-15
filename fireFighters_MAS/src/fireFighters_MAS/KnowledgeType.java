package fireFighters_MAS;

public enum KnowledgeType {
	FIRE ("0"),
	FIREFIGHTER ("1"),
	FOREST ("2"),
	RAIN ("3"),
	TVISITED ("4"),
	POS_UPDATE ("5");

	KnowledgeType (String type) {
		this.type = type;
	}

	private String type;

	public String toString() {
		return this.type;
	}

	public static KnowledgeType getKnowledgeType(String type) {
		for (KnowledgeType kType : KnowledgeType.values()) {
			if (kType.toString().equals(type)) {
				return kType;
			}
		}
		return null;
	}
}
