package org.eoanb.voting;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.eoanb.voting.listeners.HelpCommand;
import org.eoanb.voting.listeners.SelectMenuListener;
import org.eoanb.voting.listeners.VoteCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class Main {
	public static final String VOTING_CHANNEL = "980078789243580456";

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        JDA api = null;
        try {
			logger.info("Token is {}", System.getenv().getOrDefault("EOANB_BOT_TOKEN", ""));
            api = JDABuilder.createDefault(System.getenv().getOrDefault("EOANB_BOT_TOKEN", "")).build();
        } catch (LoginException e) {
			logger.error("Token is incorrect -- please submit a correct token. Terminating.");
            System.exit(1);
        }

        api.addEventListener(new VoteCommand());
		api.addEventListener(new HelpCommand());
		api.addEventListener(new SelectMenuListener());
    }
}
