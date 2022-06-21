package org.eoanb.voting.handlers;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.eoanb.voting.util.FileHandler;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

public class BinaryVotingHandler implements VotingHandler {
	private static final Logger logger = LoggerFactory.getLogger(BinaryVotingHandler.class);

	public static final String BINARY_VOTE_ID = "binary_vote";
	private static final String votersFile = "binary_voters.json";
	private static final String votesFile = "binary_votes.json";

	private final String[] options = { "Approve", "Disapprove" };
	private final String voteDescription;

	private final HashSet<String> voters = new HashSet<>();

	{
		String json = new JSONArray().toString();

		try {
			json = FileHandler.readFile(votersFile);
		} catch (IOException ignored) { }

		JSONArray jsonArray = new JSONArray(json);

		for (Object id : jsonArray) {
			if (id instanceof String) {
				voters.add((String) id);
			}
		}
	}

	public BinaryVotingHandler(String description) {
		voteDescription = description;
	}

	@Override
	public void startVote(String id, PrivateChannel channel) {
		if (voters.contains(id)) {
			channel.sendMessage("You have already voted.").queue();
			return;
		}

		ArrayList<SelectOption> selectCandidates = new ArrayList<>();
		for (String option : options) {
			selectCandidates.add(SelectOption.of(option, option.toLowerCase(Locale.ROOT)));
		}

		channel.sendMessage(new MessageBuilder()
			.setContent("Insert your choice into this form for the vote about:\n" + voteDescription)
			.setActionRows(ActionRow.of(new SelectMenuImpl(
				BINARY_VOTE_ID,
				"Please choose your selection.",
				1,
				1,
				false,
				selectCandidates)))
			.build()).queue();
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

	public void saveResult(String id, PrivateChannel channel, String vote) {
		// Read votes
		String json = new JSONArray().toString();
		try {
			json = FileHandler.readFile(votesFile);
		} catch (IOException ignored) { }

		// Save vote
		JSONArray votesJson = new JSONArray(json);
		votesJson.put(vote);

		try {
			FileHandler.writeFile(votesFile, votesJson.toString(4));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// Get all voters
		try {
			json = FileHandler.readFile(votersFile);
		} catch (IOException ignored) { }

		// Save voter
		JSONArray jsonArray = new JSONArray(json);
		jsonArray.put(id);

		try {
			FileHandler.writeFile(votersFile, jsonArray.toString(4));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		voters.add(id);

		channel.sendMessage("Successfully chosen vote: " + vote).queue();
	}
}
