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

		// Get the content of the message and put it in an array.
		String[] args = event.getMessage().getContentStripped().trim().split("\\s+");

		// Check if the command is to vote.
		if (args[0].equalsIgnoreCase("!vote")) {
			logger.info("Received command to vote by {}", event.getAuthor().getName());
			event.getChannel().sendMessage("Received voting request; check DMs.").queue();

			event.getAuthor().openPrivateChannel().queue(channel -> {
				assert VoteManager.getActiveVotes() != null;
				// Check if there are any active votes.
				if (VoteManager.getActiveVotes().isEmpty()) {
					event.getChannel().sendMessage("There are no active votes.").queue();
					return;
				}

				// Too few arguments error
				if (args.length < 2) {
					event.getChannel().sendMessage("Too few arguments; You need to provide a vote id.").queue();
					return;
				}

				String id = event.getAuthor().getId();

				int voteID;
				try {
					voteID = Integer.parseInt(args[1]);
				} catch (NumberFormatException ex) {
					logger.error(ex.getMessage());
					event.getChannel().sendMessage("Vote id must be a number.").queue();
					return;
				}

				if (!VoteManager.getActiveVotes().containsKey(voteID)) {
					event.getChannel().sendMessage("Vote isn't valid, use !listvotes to get a list of active votes.").queue();
					return;
				}

				VoteManager.getVoteFromVoteID(voteID).startVote(id, voteID, channel);
			});
		}
	}
}
