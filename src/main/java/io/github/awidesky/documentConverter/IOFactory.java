package io.github.awidesky.documentConverter;

import java.io.File;
import java.util.function.Function;

import io.github.awidesky.documentConverter.IOPair.IO;

public class IOFactory {
	
	public static Function<File, IO> toExtension(File outDir, String extension) {
		return f -> new IO(f, outDir, f.getName().substring(0, f.getName().lastIndexOf(".")) + extension);
	}
	
	public static Function<File, IO> saveOriginalExtension(File outDir, String extension) {
		return f -> new IO(f, outDir, f.getName() + extension);
	}
	
	public static Function<File, IO> regexReplace(File outDir, String regex, String replacement, String extension) {
		return f -> {
			String name = f.getName().replaceAll(regex, replacement);
 		 	return new IO(f, outDir, name.substring(0, name.lastIndexOf(".")) + extension);
		};
	}
	
}
