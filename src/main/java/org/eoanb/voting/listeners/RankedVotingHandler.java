package org.eoanb.voting.listeners;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.eoanb.voting.FileHandler;
import org.eoanb.voting.RankedVoter;
import org.eoanb.voting.VoteStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RankedVotingHandler extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(RankedVotingHandler.class);
	private static final String ID_PREFIX = "rpvoting_";

	public static final HashMap<String, RankedVoter> voters = new HashMap<>();
	public static String[] candidates = { "Cary", "Mandy", "Randy" };

	// TODO: Load voters from database.

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

			event.getAuthor().openPrivateChannel().queue(channel -> {
				if (!channel.canTalk()) logger.error("Can't send DM to {}", channel.getName());

				String id = event.getAuthor().getId();

				RankedVoter voter = voters.getOrDefault(id, new RankedVoter());

				if (voter.hasVoted()) {
					channel.sendMessage("You have already voted, overriding last vote.").queue();
				}

				// Send first select menu.
				sendSelectMenu(0, null, channel);

				voters.put(id, voter);
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

					String json = FileHandler.readFile("C:/test.json");
					JSONObject jsonObject = new JSONObject(json);

					jsonObject.put(id, new JSONArray(voter.getVotes()));

					FileHandler.writeFile("C:/test.json", jsonObject.toString(4));

					voter.clear();
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
