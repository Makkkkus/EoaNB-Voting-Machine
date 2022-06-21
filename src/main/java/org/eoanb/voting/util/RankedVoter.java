package org.eoanb.voting.util;

import org.eoanb.voting.handlers.RankedVotingHandler;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;

public class RankedVoter {
	private final ArrayList<String> votes = new ArrayList<>();
	private int votesCounted = 0;

	public VoteStatus vote(int currentVote, String vote) {
		if (currentVote != votesCounted) return VoteStatus.FAILED_SILENT;
		votesCounted++;

		// No need to add blank votes to our votes.
		if (!vote.equals("blank")) {
			votes.add(currentVote, vote);
		}

		if (votesCounted < RankedVotingHandler.candidates.length) {
			// Ask for next vote.
			return VoteStatus.NEXT_VOTE;
		} else if (votesCounted == RankedVotingHandler.candidates.length) {
			{ // Save the votes.
				String json = new JSONArray().toString();

				try {
					json = FileHandler.readFile(RankedVotingHandler.votesFile);
				} catch (IOException ignored) { }

				JSONArray jsonArray = new JSONArray(json);
				jsonArray.put(new JSONArray(getVotes()));

				try {
					FileHandler.writeFile(RankedVotingHandler.votesFile, jsonArray.toString(4));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return VoteStatus.SUCCESS;
		} else {
			votes.clear();
			return VoteStatus.FAILED;
		}
	}

	public String[] getVotes() {
		return votes.toArray(new String[0]);
	}
}
