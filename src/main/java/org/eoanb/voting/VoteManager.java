package org.eoanb.voting;

import net.dv8tion.jda.api.entities.MessageChannel;
import org.eoanb.voting.handlers.VotingHandler;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoteManager {
	private static final Logger logger = LoggerFactory.getLogger(VoteManager.class);

	public static final String VOTING_CHANNEL = "980078789243580456";

	@Nullable
	public static VotingHandler activeVote = null;

	public static void initVote() {
		logger.info("Initialising voting system...");
	}

	public static void setActiveVote(VotingHandler system) {
		if (activeVote != null) {
			activeVote.cleanupVote();
		}

		activeVote = system;

		logger.info("Successfully changed voting system.");
	}

	public static void declareResults(MessageChannel channel) {
		if (activeVote != null) {
			// TODO: MAKE INTO OBJECT
			JSONArray json = new JSONArray(activeVote.getResults());

			channel.sendMessage(json.toString()).queue();
		}
	}

	public static void endActiveVote() {
		if (activeVote != null) {
			activeVote.cleanupVote();

			activeVote = null;
			logger.info("Successfully ended vote.");
		}
	}
}
