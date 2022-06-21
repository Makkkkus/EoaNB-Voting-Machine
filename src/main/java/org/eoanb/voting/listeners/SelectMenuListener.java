package org.eoanb.voting.listeners;

import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.eoanb.voting.VoteManager;
import org.eoanb.voting.handlers.RankedVotingHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectMenuListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(SelectMenuListener.class);

	@Override
	public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {

		// For ranked voting.
		if (event.getComponentId().startsWith(VoteManager.RANKED_VOTE_PREFIX + "candidate") && VoteManager.activeVote instanceof RankedVotingHandler) {
			// Get which vote we are on.
			int currentVote = -1;
			try {
				currentVote = Integer.parseInt(event.getComponentId().substring(event.getComponentId().length() - 1));
			} catch (NumberFormatException e) {
				logger.error("Failed to parse what vote we are on. Resetting votes for {}", event.getUser().getName());
			}

			// Get user id (to store data).
			String id = event.getUser().getId();

			((RankedVotingHandler) VoteManager.activeVote).pollNextVote(id, event, currentVote);
		}
	}
}
