package org.eoanb.voting;

import org.eoanb.voting.listeners.RankedVotingHandler;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class RankedVoter {
	private final ArrayList<String> votes = new ArrayList<>();
	private boolean voted = false;
	private int votesCounted = 0;

	public VoteStatus vote(int currentVote, String vote) {

		// No need to add blank votes to our votes.
		if (!vote.equals("blank")) {
			votes.add(currentVote, vote);
		}

		votesCounted++;

		if (votesCounted < RankedVotingHandler.candidates.length) {
			// Ask for next vote.
			return VoteStatus.NEXT_VOTE;
		} else if (votesCounted == RankedVotingHandler.candidates.length) {
			voted = true;

			return VoteStatus.SUCCESS;
		} else {
			votes.clear();
			return VoteStatus.FAILED;
		}
	}

	public void clear() {
		// Delete stored votes.
		votes.clear();
	}

	public boolean hasVoted() {
		return voted;
	}

	public String[] getVotes() {
		return votes.toArray(new String[0]);
	}
}
