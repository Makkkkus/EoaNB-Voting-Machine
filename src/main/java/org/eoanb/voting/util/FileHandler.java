package org.eoanb.voting.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class FileHandler {
	private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);

	public static final String STORE_DIRECTORY = System.getProperty("user.home").concat("/eoanbvotes/");

	static {
		// Create files
		File dir = new File(STORE_DIRECTORY);
		if (dir.mkdirs()) {
			logger.info("Created votes directory at: {}", STORE_DIRECTORY);
		}

	}

	public static String readFile(String filename) throws IOException {
		StringBuilder total = new StringBuilder();

		try (BufferedReader br = new BufferedReader(new FileReader(STORE_DIRECTORY + "/" + filename))) {
			String curLine;
			while ((curLine = br.readLine()) != null)
				total.append(curLine).append("\n");
		}

		return total.toString();
	}

	public static void writeFile(String filename, String data) throws IOException {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(STORE_DIRECTORY + "/" + filename))) {
			bufferedWriter.write(data);
		}
	}
}