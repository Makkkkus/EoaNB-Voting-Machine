package org.eoanb.voting.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.eoanb.voting.RankedVoter;

import java.io.File;
import java.io.IOException;

public class RankedVotingDatabase implements Database {
	@Override
	public void sendToDatabase(RankedVoter voter) {
		ObjectMapper mapper = new CBORMapper();
		File file = new File("test.json");

		try {
			mapper.writeValue(file, voter);
		} catch (IOException ignored) {

		}
	}

	@Override
	public Object getFromDatabase(String key) {
		File file = new File("test.json");

		ObjectMapper mapper = new CBORMapper();

		try {
			return mapper.readValue(file, RankedVoter.class);
		} catch (IOException ignored) {

		}

		return null;
	}
}
