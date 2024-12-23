package io.github.awidesky.documentConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import io.github.awidesky.projectPath.UserDataPath;

public class Main {
	public static final String VERSION = "v1.1";
	
	private static final MainFrame mf = new MainFrame();
	private static Map<String, String> property;
	static {
		try { readProperty(); }
		catch(IOException e) {
			System.err.println("\nCannot read property file!");
			e.printStackTrace();
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("DocumentConverter " + VERSION);
		SwingUtilities.invokeLater(mf::init);
	}
	
	private static void readProperty() throws IOException {
		File prop = new File(UserDataPath.appLocalFolder("awidesky", "DocumentConverter"), "docconvProp.txt");
		if (!prop.exists()) {
			prop.getParentFile().mkdirs();
			prop.createNewFile();
		}

		property = (Files.lines(prop.toPath()).map(s -> s.split("="))
				.collect(Collectors.toMap(s -> s[0].strip(), s -> s[1].strip())));
	}
	
	public static Map<String, String> getProperty() {
		return property;
	}
	public static String getProperty(String key, String defaultValue) {
		return property.getOrDefault(key, defaultValue);
	}
}
