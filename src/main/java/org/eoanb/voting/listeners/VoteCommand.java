package org.eoanb.voting.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.eoanb.voting.Main;
import org.eoanb.voting.handlers.RankedVotingHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoteCommand extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(VoteCommand.class);

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		// Don't listen to bots.
		if (event.getAuthor().isBot()) return;

		// Only people who can vote should be able to vote.
		if (!event.getChannel().equals(event.getJDA().getGuildChannelById(Main.VOTING_CHANNEL))) return;

		// Get the content of the message.
		String message = event.getMessage().getContentStripped();

		// Check if the command is to vote.
		if (message.equalsIgnoreCase("!vote")) {
			logger.info("Received command to vote by {}", event.getAuthor().getName());
			event.getChannel().sendMessage("Received voting request; check DMs.").queue();

			event.getAuthor().openPrivateChannel().queue(channel -> {
				if (!channel.canTalk()) logger.error("Can't send DM to {}", channel.getName());

				// TODO: Add more voting systems.
				RankedVotingHandler.startVote(event.getAuthor().getId(), channel);
			});
		}
	}
}
