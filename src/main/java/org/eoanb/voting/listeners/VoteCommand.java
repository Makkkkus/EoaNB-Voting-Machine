package org.eoanb.voting.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.eoanb.voting.Main;
import org.eoanb.voting.VoteManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;

public class VoteCommand extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(VoteCommand.class);

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		// Don't listen to bots.
		if (event.getAuthor().isBot()) return;

		// Only people who can vote should be able to vote.
		//if (!event.getChannel().equals(event.getJDA().getGuildChannelById(VoteManager.VOTING_CHANNEL))) return;

		// Get the content of the message.
		String message = event.getMessage().getContentStripped();

		// Check if the command is to vote.
		if (message.equalsIgnoreCase("!vote")) {
			logger.info("Received command to vote by {}", event.getAuthor().getName());
			event.getChannel().sendMessage("Received voting request; check DMs.").queue();

			event.getAuthor().openPrivateChannel().queue(channel -> {
				if (!channel.canTalk()) logger.error("Can't send DM to {}", channel.getName());

				String id = event.getAuthor().getId();

				assert VoteManager.getActiveVotes() != null;
				int voteID = VoteManager.getActiveVotes().keySet().toArray(new Integer[0])[0];

				assert VoteManager.getVoteFromVoteID(voteID) != null;
				VoteManager.getVoteFromVoteID(voteID).startVote(id, channel);
			});
		}
	}
}
