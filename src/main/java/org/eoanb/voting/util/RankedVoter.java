package org.eoanb.voting.util;

import org.eoanb.voting.handlers.RankedVotingHandler;

import java.util.ArrayList;

public class RankedVoter {
	private final ArrayList<String> votes = new ArrayList<>();
	private int votesCounted = 0;

	public VoteStatus vote(RankedVotingHandler handler, int currentVote, String vote) {
		if (currentVote != votesCounted) return VoteStatus.FAILED_SILENT;
		votesCounted++;

		// No need to add blank votes to our votes.
		if (!vote.equals("blank")) {
			votes.add(currentVote, vote);
		}

		if (votesCounted < handler.candidates.length) {
			// Ask for next vote.
			return VoteStatus.NEXT_VOTE;
		} else if (votesCounted == handler.candidates.length) {
			return VoteStatus.SUCCESS;
		} else {
			votes.clear();
			return VoteStatus.FAILED;
		}
	}

	public ArrayList<String> getVotes() {
		return votes;
	}
}
