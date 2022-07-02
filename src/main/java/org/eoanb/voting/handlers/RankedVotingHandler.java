package org.eoanb.voting.handlers;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.eoanb.voting.Main;
import org.eoanb.voting.util.RankedVoter;
import org.eoanb.voting.util.VoteStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class RankedVotingHandler implements VotingHandler {
	private static final Logger logger = LoggerFactory.getLogger(RankedVotingHandler.class);

	public static final String RANKED_VOTE_PREFIX = "rpvoting_";

	public final String[] candidates;
	private final String voteName;

	private final HashMap<String, @Nullable RankedVoter> voters = new HashMap<>();

	public RankedVotingHandler(int voteID, String[] candidates) {
		this.candidates = candidates;

		Random random = new Random();
		String generatedString = random.ints(97, 123)
			.limit(5)
			.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			.toString();

		this.voteName = generatedString + "_ranked";

		// Create table to store votes.
		try {
			Statement st = Main.db.getConnection().createStatement();
			st.execute("CREATE TABLE " + voteName + " (userID text, vote text)");
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}

	@Override
	public void startVote(String id, int voteID, PrivateChannel channel) {
		if (voters.containsKey(id)) {
			channel.sendMessage("You have already voted.").queue();
			return;
		}

		// Insert this voter into active_voters.
		try {
			Statement st = Main.db.getConnection().createStatement();
			st.execute("INSERT INTO active_voters VALUES (" + id + ", " + voteID + ")");
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}

		voters.put(id, new RankedVoter());

		// Send first select menu.
		ArrayList<SelectOption> selectCandidates = new ArrayList<>();
		for (String candidate : candidates) {
			selectCandidates.add(SelectOption.of(candidate, candidate));
		}

		channel.sendMessage(new MessageBuilder()
			.setContent("Insert your choice into this form.")
			.setActionRows(ActionRow.of(new SelectMenuImpl(
				RANKED_VOTE_PREFIX + "candidate" + 0,
				"Candidate " + 1,
				1,
				1,
				false,
				selectCandidates)))
			.build()).queue();
	}

	@Override
	public void finish(ComponentInteraction event, String id, String vote) {
		// If we are done voting and should apply those votes.
		StringBuilder message = new StringBuilder("Successfully voted for:");

		String rawVoteList = vote.substring(1, vote.length()-1);
		String[] votes = rawVoteList.split(",");

		for (String preference : votes) {
			message.append(" ");
			message.append(preference);
		}

		message.append(".");
		event.getHook().sendMessage(message.toString()).queue();

		logger.info("User with id \"{}\" finished voting.", id);

		// Delete voter.
		voters.remove(id);

		// Save votes.
		try {
			Statement st = Main.db.getConnection().createStatement();
			st.execute("INSERT INTO " + voteName + " VALUES ('" + id + "', '" + rawVoteList + "')");
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}

		// Remove from active voters.
		try {
			Statement st = Main.db.getConnection().createStatement();
			st.execute("DELETE FROM active_voters WHERE userID='" + id + "'");
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}

	@Override
	public void endVote() {
		// Delete table.
		try {
			Statement st = Main.db.getConnection().createStatement();
			st.execute("DROP TABLE " + voteName);
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}

	@Override
	public String getDescription() {
		return "Ranked vote with options" + Arrays.toString(candidates);
	}

	public void pollNextVote(ComponentInteraction event, String id, int currentVote, String voteString) {
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
				finish(event, id, voter.getVotes().toString());
				break;
			case NEXT_VOTE:
				sendSelectMenu(event, currentVote + 1, voter.getVotes());
				break;
			case FAILED:
				event.getHook().sendMessage("Error when voting. Please try again.").queue();
				break;
		}
	}

	private void sendSelectMenu(ComponentInteraction event, int voteNumber, ArrayList<String> ignoredCandidates) {
		ArrayList<SelectOption> selectCandidates = new ArrayList<>();

		// Add blank vote.
		if (voteNumber > 0) {
			selectCandidates.add(SelectOption.of("Blank/None", "blank"));
		}

		for (String candidate : candidates) {
			if (ignoredCandidates != null && ignoredCandidates.contains(candidate)) continue;

			selectCandidates.add(SelectOption.of(candidate, candidate));
		}

		event.getHook().sendMessage(new MessageBuilder()
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
