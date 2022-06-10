package org.eoanb.voting.database;

public class PreferentialVoteDB implements IDatabase {
	private String id;

	public PreferentialVoteDB(String id) {
		this.id = id;
	}

	@Override
	public void sendToDatabase() {

	}
}
