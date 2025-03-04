package io.github.awidesky.documentConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import io.github.awidesky.projectPath.UserDataPath;

public class Main {
	public static final String VERSION = "v1.2";
	
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

		property = (Files.lines(prop.toPath())
				.filter(s -> !s.startsWith("#"))
				.map(s -> s.split("="))
				.peek(arr -> {
					if(arr.length == 2) System.out.println("Invalide argument : " + Arrays.stream(arr).collect(Collectors.joining("=")));
				})
				.filter(arr -> arr.length == 2)
				.collect(Collectors.toMap(arr -> arr[0].strip(), arr -> arr[1].strip())));
	}
	
	public static Map<String, String> getProperty() {
		return property;
	}
	public static String getProperty(String key, String defaultValue) {
		return property.getOrDefault(key, defaultValue);
	}
}
