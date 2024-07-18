package io.github.awidesky.documentConverter;

import java.io.File;
import java.util.function.Function;

import io.github.awidesky.documentConverter.IOPair.IO;

public class IOFactory {
	
	public static Function<File, IO> toExtension(File outDir, boolean saveOriginalExtension, String extension) {
		return f ->  {
			String newName = saveOriginalExtension ? f.getName() : f.getName().substring(0, f.getName().lastIndexOf("."));
			return new IO(f, outDir, newName + extension);
		};
	}
	
	public static Function<File, IO> regexReplace(File outDir, String regex, String replacement, String extension) {
		return f -> {
			String name = f.getName().replaceAll(regex, replacement);
 		 	return new IO(f, outDir, name.substring(0, name.lastIndexOf(".")) + extension);
		};
	}
	
}
