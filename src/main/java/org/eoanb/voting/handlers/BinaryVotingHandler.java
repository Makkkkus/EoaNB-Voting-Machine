package org.eoanb.voting.handlers;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.eoanb.voting.Main;
import org.eoanb.voting.VoteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class BinaryVotingHandler implements VotingHandler {
	private static final Logger logger = LoggerFactory.getLogger(BinaryVotingHandler.class);

	public static final String BINARY_VOTE_ID = "binary_vote";

	private final String[] options = { "Approve", "Disapprove" };
	private final String voteDescription;
	private final String voteName;

	public BinaryVotingHandler(int id, String description) {
		this.voteDescription = description;

		Random random = new Random();
		String generatedString = random.ints(97, 123)
			.limit(5)
			.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			.toString();

		this.voteName = generatedString + "_binary";

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
		// Check if we have voted already.
		try {
			Statement st = Main.db.getConnection().createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM " + voteName + " WHERE userID='" + id + "'");
			if (rs.next()) {
				channel.sendMessage("You have already voted.").queue();
				return;
			}
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}

		// Vote has started to add the current voter to the table with all the active voters.
		try {
			Statement st = Main.db.getConnection().createStatement();
			st.execute("INSERT INTO active_voters VALUES ('" + id + "', '" + voteID + "')");
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}

		// Get a list of SelectOptions with the alternatives presented.
		ArrayList<SelectOption> selectCandidates = new ArrayList<>();
		for (String option : options) {
			selectCandidates.add(SelectOption.of(option, option.toLowerCase(Locale.ROOT)));
		}

		// Create message and send it.
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
	public void finish(ComponentInteraction event, String id, String vote) {
		// Insert vote into table.
		try {
			Statement st = Main.db.getConnection().createStatement();
			st.execute("INSERT INTO " + voteName + " VALUES ('" + id + "', '" + vote + "')");
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

		event.getHook().sendMessage("Successfully chosen vote: " + vote).queue();
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
		return "Binary vote with statement: " + voteDescription;
	}
}
