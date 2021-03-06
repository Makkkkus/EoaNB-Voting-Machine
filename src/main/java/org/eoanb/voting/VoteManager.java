package org.eoanb.voting;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import org.eoanb.voting.handlers.VotingHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Random;

public class VoteManager {
	private static final Logger logger = LoggerFactory.getLogger(VoteManager.class);

	public static final String VOTING_CHANNEL = "980078789243580456";

	@Nullable
	private static final HashMap<Integer, VotingHandler> activeVotes = new HashMap<>();

	public static void initVotes() {
		logger.info("Initialising voting system...");

		// Create new table.
		try {
			Statement st = Main.db.getConnection().createStatement();
			st.execute("CREATE TABLE active_voters (UserID text, CurrentVote int)");
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}

		// Clear table.
		try {
			Statement st = Main.db.getConnection().createStatement();
			st.execute("TRUNCATE TABLE active_voters");
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}

	public static void startVote(int voteID, VotingHandler system) {
		assert activeVotes != null;
		activeVotes.put(voteID, system);

		logger.info("Started new vote with id {}.", voteID);
	}

	public static void postResults(int voteId, MessageChannel channel) {
		assert activeVotes != null;

		channel.sendMessage("none").queue();
	}

	public static void endVote(int voteId) {
		assert activeVotes != null;

		// End vote
		activeVotes.get(voteId).endVote();

		// Remove vote from list.
		activeVotes.remove(voteId);

		logger.info("Successfully ended vote with id {}.", voteId);
	}

	public static VotingHandler getVoteFromUserID(String id) {
		try {
			// Get ResultSet with the current vote active by this user.
			Statement st = Main.db.getConnection().createStatement();
			ResultSet rs = st.executeQuery("SELECT CurrentVote FROM active_voters WHERE UserID='" + id + "'");

			// Check if row exists.
			if (rs.next()) {
				// Return the found result.
				assert activeVotes != null;
				return activeVotes.get(rs.getInt(1));
			}
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}

		return null;
	}

	public static VotingHandler getVoteFromVoteID(int id) {
		return activeVotes.get(id);
	}

	public static HashMap<Integer, VotingHandler> getActiveVotes() {
		return activeVotes;
	}

	public static int generateUniqueID() {
		Random random = new Random();
		int id = random.nextInt();

		assert activeVotes != null;

		// Check if id is available.
		int tries = 0;
		while (activeVotes.containsKey(id)) {
			tries++;
			id = random.nextInt();

			if (tries > 999) {
				logger.error("Could not find valid vote id.");
				System.exit(-1);
			}
		}

		return id;
	}
}
