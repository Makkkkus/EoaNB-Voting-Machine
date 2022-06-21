package org.eoanb.voting.handlers;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.eoanb.voting.VoteManager;
import org.eoanb.voting.util.FileHandler;
import org.eoanb.voting.util.RankedVoter;
import org.eoanb.voting.util.VoteStatus;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RankedVotingHandler implements VotingHandler {
	private static final Logger logger = LoggerFactory.getLogger(RankedVotingHandler.class);

	public static final String RANKED_VOTE_PREFIX = "rpvoting_";
	public static final String votersFile = "ranked_voters.json";
	public static final String votesFile = "ranked_votes.json";

	private final HashMap<String, @Nullable RankedVoter> voters = new HashMap<>();
	public static String[] candidates = { "Cary", "Mandy", "Randy" };


	{
		String json = new JSONArray().toString();

		try {
			json = FileHandler.readFile(votersFile);
		} catch (IOException ignored) { }

		JSONArray jsonArray = new JSONArray(json);

		for (Object id : jsonArray) {
			if (id instanceof String) {
				voters.put((String) id, null);
			}
		}
	}

	@Override
	public void startVote(String id, PrivateChannel channel) {
		if (voters.containsKey(id)) {
			channel.sendMessage("You have already voted.").queue();
			return;
		}

		voters.put(id, new RankedVoter());

		// Send first select menu.
		sendSelectMenu(0, null, channel);
	}

	@Override
	public String getResults() {
		String json = new JSONArray().toString();
		try {
			json = FileHandler.readFile(votesFile);
		} catch (IOException ignored) { }

		return json;
	}

	@Override
	public void cleanupVote() {
		FileHandler.deleteFile(votesFile);
		FileHandler.deleteFile(votersFile);
	}

	public void pollNextVote(String id, PrivateChannel channel, int currentVote, String voteString) {
		// Get the voter.
		RankedVoter voter = voters.get(id);

		// If we already have voted, or we haven't started voting yet.
		if (voter == null)
			return;

		// If current vote is wrong.
		if (currentVote == -1) {
			voters.remove(id);
			return;
		}

		// Actually vote.
		VoteStatus status = voter.vote(currentVote, voteString);

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
				channel.sendMessage(message.toString()).queue();

				{ // Add voter to database.
					String json = new JSONArray().toString();

					try {
					json = FileHandler.readFile(votersFile);
					} catch (IOException ignored) { }

					JSONArray jsonArray = new JSONArray(json);
					jsonArray.put(id);

					try {
						FileHandler.writeFile(votersFile, jsonArray.toString(4));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				logger.info("User with id \"{}\" finished voting.", id);

				// Reserve an empty slot to confirm that this user has voted.
				voters.put(id, null);
				break;
			case NEXT_VOTE:
				sendSelectMenu(currentVote + 1, new ArrayList<>(Arrays.asList(voter.getVotes())), channel);
				break;
			case FAILED:
				channel.sendMessage("Error when voting. Please try again.").queue();
				break;
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
				RANKED_VOTE_PREFIX + "candidate" + voteNumber,
				"Candidate " + (voteNumber + 1),
				1,
				1,
				false,
				selectCandidates)))
			.build()).queue();
	}
}
