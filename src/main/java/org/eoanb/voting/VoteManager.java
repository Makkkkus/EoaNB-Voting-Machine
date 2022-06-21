package org.eoanb.voting;

import org.eoanb.voting.handlers.RankedVotingHandler;
import org.eoanb.voting.handlers.VotingHandler;

public class VoteManager {
	public static final String RANKED_VOTE_PREFIX = "rpvoting_";
	public static VotingHandler activeVote = new RankedVotingHandler();

	public static void initVote() {

	}
}
