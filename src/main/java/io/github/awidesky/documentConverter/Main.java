package io.github.awidesky.documentConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

public class Main {
	public static final String VERSION = "v1.1";
	
	private static final MainFrame mf = new MainFrame();
	
	public static void main(String[] args) {
		System.out.println("DocumentConverter " + VERSION);
		try { readProperty(); }
		catch(IOException e) {
			System.err.println("\nCannot read property file!");
			e.printStackTrace();
			System.out.println();
		}
		SwingUtilities.invokeLater(mf::init);
	}
	
	public static void readProperty() throws IOException {
		File prop = new File("docconvProp.txt");
		if(!prop.exists()) prop.createNewFile();

		mf.property(Files.lines(prop.toPath())
				.map(s -> s.split(":"))
				.collect(Collectors.toMap(s -> s[0].strip(), s -> s[1].strip())));
	}
}
