package org.eoanb.voting.handlers;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

public class BinaryVotingHandler implements VotingHandler {
	private static final Logger logger = LoggerFactory.getLogger(BinaryVotingHandler.class);

	public static final String BINARY_VOTE_ID = "binary_vote";

	private final String[] options = { "Approve", "Disapprove" };
	private final String voteDescription;

	// TODO: Replace these with a database.
	private final HashSet<String> voters = new HashSet<>();
	private final HashSet<String> votes = new HashSet<>();

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
	public void cleanupVote() {
	}

	@Override
	public void save() {

	}

	public void finalise(String id, PrivateChannel channel, String vote) {
		voters.add(id);
		votes.add(vote);

		channel.sendMessage("Successfully chosen vote: " + vote).queue();
	}
}
