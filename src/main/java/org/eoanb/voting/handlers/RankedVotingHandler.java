package org.eoanb.voting.handlers;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.eoanb.voting.util.RankedVoter;
import org.eoanb.voting.util.VoteStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RankedVotingHandler implements VotingHandler {
	private static final Logger logger = LoggerFactory.getLogger(RankedVotingHandler.class);

	public static final String RANKED_VOTE_PREFIX = "rpvoting_";

	public final String[] candidates;

	// TODO: Replace these with a database.
	private final HashMap<String, @Nullable RankedVoter> voters = new HashMap<>();
	private final HashSet<ArrayList<String>> votes = new HashSet<>();

	public RankedVotingHandler(String[] candidates) {
		this.candidates = candidates;
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
	public void cleanupVote() {
	}

	@Override
	public void save() {

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
		VoteStatus status = voter.vote(this, currentVote, voteString);

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

				logger.info("User with id \"{}\" finished voting.", id);

				// Save.
				voters.put(id, null);
				votes.add(voter.getVotes());
				break;
			case NEXT_VOTE:
				sendSelectMenu(currentVote + 1, voter.getVotes(), channel);
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
