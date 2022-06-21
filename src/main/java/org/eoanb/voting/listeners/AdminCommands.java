package org.eoanb.voting.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.eoanb.voting.VoteManager;
import org.eoanb.voting.handlers.BinaryVotingHandler;
import org.eoanb.voting.handlers.RankedVotingHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminCommands extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(AdminCommands.class);

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) return;

		String[] args = event.getMessage().getContentStripped().trim().split("\\s+");

		switch (args[0].toLowerCase()) {
			case "!holdvote":
				if (VoteManager.activeVote != null) {
					event.getChannel().sendMessage("Vote is ongoing; end current vote first.").queue();
					return;
				}

				if (args.length < 2) {
					event.getChannel().sendMessage("You need to provide a voting system to choose.").queue();
					return;
				}

				switch (args[1].toLowerCase()) {
					case "ranked":
						VoteManager.setActiveVote(new RankedVotingHandler());
						event.getChannel().sendMessage("New ranked vote will be held").queue();
						break;
					case "binary":
						if (args.length < 3) return;
						StringBuilder string = new StringBuilder();
						for (int i = 2; i < args.length; i++) string.append(args[i]).append(" ");

						VoteManager.setActiveVote(new BinaryVotingHandler(string.toString()));
						event.getChannel().sendMessage("New vote is being held with description: " + string).queue();
						break;
					default:
						event.getChannel().sendMessage("Voting system is incorrect; should be 'Ranked' or 'Binary'.").queue();
						return;
				}

				logger.info("Command to hold new vote executed.");
				break;
			case "!endvote":
				if (VoteManager.activeVote == null) {
					event.getChannel().sendMessage("No vote is ongoing.").queue();
					return;
				}

				event.getChannel().sendMessage("Vote ended.").queue();

				VoteManager.declareResults(event.getChannel());
				VoteManager.endActiveVote();

				logger.info("Command to end vote executed.");
				break;
		}
	}
}