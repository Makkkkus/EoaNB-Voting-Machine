package org.eoanb.voting.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;

public class DatabaseHandler {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseHandler.class);

	private Connection connection;

	public void init() {
		String url = "jdbc:postgresql://localhost:5432/eoanbDB";
		String user = "eoanb";
		String password = "admin";

		// Connect to database.
		try {
			connection = DriverManager.getConnection(url, user, password);
		} catch (SQLTimeoutException ex) {
			logger.error("Connecting to database timed out; terminating.");

			System.exit(-1);
			return;
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
			logger.error("Could not connect to database; terminating.");

			System.exit(-1);
			return;
		}

		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery("SELECT VERSION()");

			if (rs.next()) {
				logger.info(rs.getString(1));
			}
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}

	public Connection getConnection() {
		return connection;
	}
}
