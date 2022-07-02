package org.eoanb.voting.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.eoanb.voting.VoteManager;
import org.eoanb.voting.handlers.BinaryVotingHandler;
import org.eoanb.voting.handlers.RankedVotingHandler;
import org.eoanb.voting.handlers.VotingHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class AdminCommands extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(AdminCommands.class);

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) return;

		String[] args = event.getMessage().getContentStripped().trim().split("\\s+");

		switch (args[0].toLowerCase()) {
			case "!holdvote":
				// Check for command errors
				if (args.length < 2) {
					event.getChannel().sendMessage("Too few arguments; You need to provide a voting system to choose.").queue();
					return;
				}

				// Generate id.
				int id = VoteManager.generateUniqueID();

				switch (args[1].toLowerCase()) {
					case "ranked":
						// Error
						if (args.length < 3) {
							event.getChannel().sendMessage("Too few arguments; Ranked voting requires choices.").queue();
							return;
						}

						// Put candidates into a list.
						ArrayList<String> candidates = new ArrayList<>(Arrays.asList(args).subList(2, args.length));

						// Create vote
						VoteManager.startVote(id, new RankedVotingHandler(id, candidates.toArray(new String[0])));
						event.getChannel().sendMessage("New ranked vote will be held").queue();
						break;
					case "binary":
						// Error
						if (args.length < 3) {
							event.getChannel().sendMessage("Too few arguments; Binary voting requires a description.").queue();
							return;
						}

						// Combine the succeeding arguments into one string. (The description.)
						StringBuilder string = new StringBuilder();
						for (int i = 2; i < args.length; i++) string.append(args[i]).append(" ");

						// Create vote
						VoteManager.startVote(id, new BinaryVotingHandler(id, string.toString()));
						event.getChannel().sendMessage("New vote is being held with description: " + string).queue();
						break;
					default:
						event.getChannel().sendMessage("Voting system is incorrect; should be 'Ranked' or 'Binary'.").queue();
						return;
				}

				logger.info("Command to hold new vote executed.");
				break;
			case "!endvote":
				if (args.length < 2) {
					event.getChannel().sendMessage("You need to provide the id of the vote to end it.").queue();
					return;
				}

				int voteId;
				try {
					voteId = Integer.parseInt(args[1]);
				} catch (NumberFormatException ex) {
					logger.error(ex.getMessage());
					return;
				}

				VoteManager.postResults(voteId, event.getChannel());
				VoteManager.endVote(voteId);

				logger.info("Vote successfully ended.");
				break;
			case "!listvotes":
				assert VoteManager.getActiveVotes() != null;
				// Check if there are any active votes.
				if (VoteManager.getActiveVotes().isEmpty()) {
					event.getChannel().sendMessage("There are no active votes.").queue();
					return;
				}

				EmbedBuilder message = new EmbedBuilder();
				message.setTitle("Active votes:");

				for (int i : VoteManager.getActiveVotes().keySet()) {
					message.addField(Integer.toString(i), VoteManager.getActiveVotes().get(i).getDescription(), false);
				}

				event.getChannel().sendMessageEmbeds(message.build()).queue();
				break;
		}
	}
}
