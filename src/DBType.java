public enum DBType {
	PRIMARY("INTEGER PRIMARY KEY AUTOINCREMENT"), 
	INT("INTEGER DEFAULT 0"),
	INT_DEF("INTEGER DEFAULT -1"),
	FLOAT("FLOAT"), 
	TEXT("TEXT"), 
	NUMERIC("NUMERIC"), 
	TEXT_NOT_NULL("TEXT NOT NULL"),
    TEXT_DEFAULT_EMPTY("TEXT DEFAULT \"\"");

	private String name;

	DBType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}