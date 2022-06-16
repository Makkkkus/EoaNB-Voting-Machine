package org.eoanb.voting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandler {
	private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);

	public static String readFile(String path) {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (FileNotFoundException ex) {
			return "";
		} catch (IOException ex) {
			logger.error("Could not read string.");
			System.exit(1);
			return "";
		}

		return builder.toString();
	}

	public static void writeFile(String path, String content) {
		try {
			FileWriter fWriter = new FileWriter(path);
			fWriter.write(content);
			fWriter.close();
		} catch (IOException ex) {
			logger.error("Could not write string.");
			System.exit(1);
		}
	}
}
