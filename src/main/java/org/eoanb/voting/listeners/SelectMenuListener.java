package org.eoanb.voting.listeners;

import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Modal;
import org.eoanb.voting.VoteManager;
import org.eoanb.voting.handlers.BinaryVotingHandler;
import org.eoanb.voting.handlers.RankedVotingHandler;
import org.eoanb.voting.handlers.VotingHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectMenuListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(SelectMenuListener.class);

	@Override
	public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
		// Get user id (to store data).
		String id = event.getUser().getId();

		// Get vote.
		VotingHandler vote = VoteManager.getVoteFromUserID(id);

		event.deferReply().queue();

		// For ranked voting.
		if (event.getComponentId().startsWith(RankedVotingHandler.RANKED_VOTE_PREFIX + "candidate") && vote instanceof RankedVotingHandler) {
			// Get an int with the value of the current vote. This is done by getting the last character in the id string, and converting it to an integer.
			int currentVote = -1;
			try {
				currentVote = Integer.parseInt(event.getComponentId().substring(event.getComponentId().length() - 1));
			} catch (NumberFormatException e) {
				logger.error("Failed to parse what vote we are on. Resetting votes for {}", event.getUser().getName());
			}

			// Get vote.
			String voteString = event.getSelectedOptions().get(0).getValue();

			// Send request to get next vote. (The handling of any possible errors is handled by this method.)
			((RankedVotingHandler) VoteManager.getVoteFromUserID(id)).pollNextVote(event, id, currentVote, voteString);
		} else if (event.getComponentId().equals(BinaryVotingHandler.BINARY_VOTE_ID) && vote instanceof BinaryVotingHandler) {

			// Get vote.
			String voteString = event.getSelectedOptions().get(0).getValue();

			vote.finish(event, id, voteString);
		}
	}
}
