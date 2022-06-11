package org.eoanb.voting.listeners;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.eoanb.voting.database.Database;
import org.eoanb.voting.database.RankedVotingDatabase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class RankedVotingHandler extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(RankedVotingHandler.class);
	private static final String ID_PREFIX = "rpvoting_";
	private static final HashMap<String, ArrayList<String>> candidatePreferences = new HashMap<>();

	public static String[] candidates = { "Cary", "Mandy", "Randy" };

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

		// Only people who can vote should be able to vote
		//if (!event.getChannel().equals(event.getJDA().getGuildChannelById(Main.VOTING_CHANNEL))) return;
        String message = event.getMessage().getContentStripped();

		// Check if the command is to vote.
        if (message.equalsIgnoreCase("!vote")) {
			logger.info("Received command to vote by {}", event.getAuthor().getName());
			event.getChannel().sendMessage("Received voting request; check DMs.").queue();
			handleVoting(event.getAuthor());
        }
    }

	private void handleVoting(User user) {
		// Asynchronous method of sending a direct message with the vote.
		user.openPrivateChannel().queue(channel -> {
			if (!channel.canTalk()) logger.error("Can't send DM to {}", channel.getName());

			askCandidate(0, null, channel);
		});
	}

	private void askCandidate(int candidateID, @Nullable ArrayList<String> ignoredCandidates, PrivateChannel channel) {

		ArrayList<SelectOption> selectCandidates = new ArrayList<>();

		// Add blank vote.
		if (candidateID > 0) {
			selectCandidates.add(SelectOption.of("Blank/None", "blank"));
		}

		for (String candidate : candidates) {
			if (ignoredCandidates != null && ignoredCandidates.contains(candidate)) continue;

			selectCandidates.add(SelectOption.of(candidate, candidate));
		}

		channel.sendMessage(new MessageBuilder()
			.setContent("Insert your choice into this form.")
			.setActionRows(ActionRow.of(new SelectMenuImpl(
				ID_PREFIX + "candidate" + candidateID,
				"Candidate " + (candidateID + 1),
				1,
				1,
				false,
				selectCandidates)))
			.build()).queue();
	}

	@Override
	public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
		String selectMenuID = event.getComponentId();

		if (selectMenuID.startsWith(ID_PREFIX + "candidate")) {
			// Get user id (to store data).
			String id = event.getUser().getId();

			// TODO Check if user has voted...
			Database db = new RankedVotingDatabase();

			// Get the saved preferences or create a new preference.
			ArrayList<String> preferences = candidatePreferences.getOrDefault(id, new ArrayList<>());

			// Loop through the different candidates and get the index of the selected entry.

			// Get which vote we are on.
			int currentVote;
			try {
				currentVote = Integer.parseInt(selectMenuID.substring(selectMenuID.length() - 1));
			} catch (NumberFormatException e) {
				logger.error("Failed to parse what vote we are on. Resetting votes for {}", event.getUser().getName());

				candidatePreferences.remove(id);
				return;
			}

			preferences.add(currentVote, event.getSelectedOptions().get(0).getValue());

			candidatePreferences.put(id, preferences);

			if (preferences.size() < candidates.length) {
				// Ask for next candidate selection.
				askCandidate(currentVote + 1, preferences, event.getPrivateChannel());
			} else {
				if (preferences.size() == candidates.length) {
					// If we are done voting and should apply those votes.
					StringBuilder message = new StringBuilder("Successfully voted for:");

					for (String preference : preferences) {
						message.append(" ");
						message.append(preference);
					}

					message.append(".");
					event.reply(message.toString()).queue();

					// TODO: Add votes to database.
				}

				candidatePreferences.remove(id);
			}
		}
	}
}
