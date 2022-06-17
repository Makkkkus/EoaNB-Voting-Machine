package org.eoanb.voting.database;

import org.eoanb.voting.util.RankedVoter;

public interface Database {
	void sendToDatabase(RankedVoter voter);
	Object getFromDatabase(String key);
}
