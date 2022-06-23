package org.eoanb.voting.handlers;

import net.dv8tion.jda.api.entities.PrivateChannel;

public interface VotingHandler {

	void startVote(String id, PrivateChannel channel);
	void cleanupVote();
	void save();
}
