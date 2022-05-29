package org.eoanb.voting.listeners;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import net.dv8tion.jda.internal.requests.restaction.MessageActionImpl;
import org.eoanb.voting.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class VoteListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(VoteListener.class);

	public static String[] candidates = { "Cary", "Mandy", "Randy" };
	public static int[] votes = { 0, 0, 0 };

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

		// Only people who can vote should be able to vote
		//if (!event.getChannel().equals(event.getJDA().getGuildChannelById(Main.VOTING_CHANNEL))) return;
        String message = event.getMessage().getContentStripped();

		// Check if the command is to vote.
        if (message.equals("!vote")) {
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

	private void askCandidate(int candidateID, @Nullable ArrayList<Integer> ignoredCandidates, PrivateChannel channel) {

		ArrayList<SelectOption> selectCandidates = new ArrayList<>();
		for (int i = 0; i < candidates.length; i++) {
			if (ignoredCandidates != null && ignoredCandidates.contains(i)) continue;

			selectCandidates.add(SelectOption.of(candidates[i], candidates[i]));
		}

		channel.sendMessage(new MessageBuilder()
			.setContent("Insert your choices into this form.")
			.setActionRows(ActionRow.of(new SelectMenuImpl(
				"candidate" + candidateID,
				"Candidate " + (candidateID + 1),
				1,
				1,
				false,
				selectCandidates)))
			.build()).queue();
	}

	HashMap<String, HashMap<Integer, String>> candidatePreferences = new HashMap<>();
	@Override
	public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
		// Check if user already has voted...

		String id = event.getUser().getId();

		// Get the saved preferences or create a new preference.
		HashMap<Integer, String> preferences = new HashMap<>();
		if (candidatePreferences.containsKey(id)) preferences = candidatePreferences.get(id);

		if (event.getComponentId().startsWith("candidate")) {
			int currentSelection = 0;
			for (int i = 0; i < candidates.length; i++) {
				if (event.getComponentId().equals("candidate" + i)) {
					currentSelection = i;
					break;
				}
			}

			preferences.put(currentSelection, event.getSelectedOptions().get(0).getValue());

			candidatePreferences.put(id, preferences);

			// If we are done voting, and should now apply those votes.
			if (preferences.size() != candidates.length) {
				askCandidate(currentSelection + 1, new ArrayList<>(preferences.keySet()), event.getPrivateChannel());
			} else {
				StringBuilder message = new StringBuilder("Successfully voted for:");

				for (int i = 0; i < preferences.size(); i++) {
					message.append(" ");
					message.append(preferences.get(i));
				}

				message.append(".");
				event.reply(message.toString()).queue();

				// Do some stuff to put votes in database or something idk
			}
		}
	}
}
