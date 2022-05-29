package org.eoanb.voting.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelpCommand extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(HelpCommand.class);

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) return;

		String message = event.getMessage().getContentStripped();

		// Check if the command is to vote.
		if (message.equals("!help")) {
			event.getChannel().sendMessage("Vote by using the !vote command").queue();
		}
	}
}
