package org.eoanb.voting.handlers;

import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

public interface VotingHandler {
	void startVote(String id, int voteID, PrivateChannel channel);
	void finish(ComponentInteraction event, String id, String vote);
	void endVote();
	String getDescription();
}
