package org.eoanb.voting;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.eoanb.voting.listeners.HelpCommand;
import org.eoanb.voting.listeners.VoteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class Main {
	public static final String VOTING_CHANNEL = "971485915669229628";

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        JDA api = null;
        try {
            api = JDABuilder.createDefault(System.getenv().getOrDefault("EOANB_BOT_TOKEN", "")).build();
        } catch (LoginException e) {
			logger.error("Token is incorrect -- please submit a correct token. Terminating.");
            System.exit(1);
        }

        api.addEventListener(new VoteListener());
		api.addEventListener(new HelpCommand());
    }
}
