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

	public ResultSet getColumn(String table, String column) {
		try {
			Statement st = connection.createStatement();

			return st.executeQuery("SELECT " + column + " FROM " + table);
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
			logger.error("Error loading column {} in table {}.", column, table);

			return null;
		}
	}

	public void insertColumn(String table, Object[] data) {

	}

	public void createTable(String name, HashMap<String, DataType> args) {
		try {
			StringBuilder builder = new StringBuilder();

			builder.append("CREATE TABLE ").append(name).append(" (");

			for (String arg : args.keySet()) {
				builder.append(arg)
					.append(" ")
					.append(convertDataTypeToText(args.get(arg), 0))
					.append(",");
			}

			builder.deleteCharAt(builder.length());

			builder.append(" )");

			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(builder.toString());

			rs.next();

			st.close();
			rs.close();
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
			logger.error("Could not create table with name '{}'", name);
		}
	}

	public String convertDataTypeToText(DataType dataType, int size) {
		switch (dataType) {
			case STRING:
				return "text";
			case INTEGER:
				return "int";
			default:
				return "";
		}
	}

	public enum DataType {
		STRING,
		INTEGER
	}
}
