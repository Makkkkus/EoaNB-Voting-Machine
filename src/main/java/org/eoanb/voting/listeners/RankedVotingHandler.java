package org.eoanb.voting.listeners;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.eoanb.voting.Main;
import org.eoanb.voting.util.FileHandler;
import org.eoanb.voting.util.RankedVoter;
import org.eoanb.voting.util.VoteStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RankedVotingHandler extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(RankedVotingHandler.class);
	private static final String ID_PREFIX = "rpvoting_";

	public static final HashMap<String, @Nullable RankedVoter> voters = new HashMap<>();
	public static String[] candidates = { "Cary", "Mandy", "Randy" };

	static {
		String json = new JSONArray().toString();

		try {
			json = FileHandler.readFile("voters.json");
		} catch (IOException ignored) { }

		JSONArray jsonArray = new JSONArray(json);

		for (Object id : jsonArray) {
			if (id instanceof String) {
				voters.put((String) id, null);
			}
		}
	}

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

				String id = event.getAuthor().getId();

				if (voters.containsKey(id)) {
					channel.sendMessage("You have already voted.").queue();
					return;
				}

				voters.put(id, new RankedVoter());

				// Send first select menu.
				sendSelectMenu(0, null, channel);

			});
        }
    }

	private void sendSelectMenu(int voteNumber, @Nullable ArrayList<String> ignoredCandidates, PrivateChannel channel) {
		ArrayList<SelectOption> selectCandidates = new ArrayList<>();

		// Add blank vote.
		if (voteNumber > 0) {
			selectCandidates.add(SelectOption.of("Blank/None", "blank"));
		}

		for (String candidate : candidates) {
			if (ignoredCandidates != null && ignoredCandidates.contains(candidate)) continue;

			selectCandidates.add(SelectOption.of(candidate, candidate));
		}

		channel.sendMessage(new MessageBuilder()
			.setContent("Insert your choice into this form.")
			.setActionRows(ActionRow.of(new SelectMenuImpl(
				ID_PREFIX + "candidate" + voteNumber,
				"Candidate " + (voteNumber + 1),
				1,
				1,
				false,
				selectCandidates)))
			.build()).queue();
	}

	@Override
	public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
		if (event.getComponentId().startsWith(ID_PREFIX + "candidate")) {
			// Get user id (to store data).
			String id = event.getUser().getId();

			// Get the voter.
			RankedVoter voter = voters.get(id);

			if (voter == null)
				return;

			// Get which vote we are on.
			int currentVote;
			try {
				currentVote = Integer.parseInt(event.getComponentId().substring(event.getComponentId().length() - 1));
			} catch (NumberFormatException e) {
				logger.error("Failed to parse what vote we are on. Resetting votes for {}", event.getUser().getName());

				voters.remove(id);
				return;
			}

			// Actually vote.
			VoteStatus status = voter.vote(currentVote, event.getSelectedOptions().get(0).getValue());

			// Do stuff according to the result of the vote.
			switch (status) {
				case SUCCESS:
					// If we are done voting and should apply those votes.
					StringBuilder message = new StringBuilder("Successfully voted for:");

					for (String preference : voter.getVotes()) {
						message.append(" ");
						message.append(preference);
					}

					message.append(".");
					event.reply(message.toString()).queue();

					{ // Add voter to database.
						String json = new JSONArray().toString();

						try {
							json = FileHandler.readFile("voters.json");
						} catch (IOException ignored) { }

						JSONArray jsonArray = new JSONArray(json);
						jsonArray.put(id);

						try {
							FileHandler.writeFile("voters.json", jsonArray.toString(4));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					voters.put(id, null);
					break;
				case NEXT_VOTE:
					sendSelectMenu(currentVote + 1, new ArrayList<>(Arrays.asList(voter.getVotes())), event.getPrivateChannel());
					break;
				case FAILED:
					event.reply("Error when voting. Please try again.").queue();
					break;
			}
		}
	}
}
