package org.eoanb.voting;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import org.eoanb.voting.handlers.VotingHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class VoteManager {
	private static final Logger logger = LoggerFactory.getLogger(VoteManager.class);

	public static final String VOTING_CHANNEL = "980078789243580456";

	@Nullable
	private static ArrayList<VotingHandler> activeVotes = new ArrayList<>();

	// TODO: Replace this with a database.
	private static HashMap<String, Integer> userActiveVotes = new HashMap<>();

	public static void initVotes() {
		logger.info("Initialising voting system...");
	}

	public static void startVote(int voteID, String id, PrivateChannel channel) {
		userActiveVotes.put(id, voteID);

		activeVotes.get(voteID).startVote(id, channel);
	}

	public static void setActiveVote(VotingHandler system) {
		activeVotes.add(system);

		logger.info("Successfully changed voting system.");
	}

	public static void declareResults(int voteId, MessageChannel channel) {

		channel.sendMessage("").queue();
	}

	public static void endActiveVote(int voteId) {
		activeVotes.get(voteId).cleanupVote();

		logger.info("Successfully ended vote.");
	}

	public static VotingHandler getActiveVoteFromUserID(String id) {
		return activeVotes.get(userActiveVotes.get(id));
	}

}
