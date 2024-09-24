package io.github.awidesky.documentConverter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.awidesky.documentConverter.jodConverter.IO;

/**
 * Simple conversion mode that just do
 * <code>
 * soffice --headless --convert-to "format" --outdir "outdir" "files"
 * </code>
 */
public class SimpleConvertManager implements ConvertManager {

	private String format;
	private SimpleConvertUtil converter = new SimpleConvertUtil();
	
	@Override
	public void setup(File outdir, boolean keepOriginalExtension, String format) {
		converter.setOutdir(outdir);
		this.format = format;
		if(keepOriginalExtension) {
			System.out.println("[Warning] Cannot keep original extension in simple implementation mode");
			System.out.println("[Warning] since it uses command : soffice --headless --convert-to \"format\" --outdir \"outdir\" \"files\"");
			System.out.println("[Warning] keepOriginalExtension=" + keepOriginalExtension + " will be ignored.");
		}
	}

	@Override
	public boolean convert(List<File> inputs, Map<String, String> property, BiConsumer<File, File> updateUI) {
		int processNum = 4;
		try {
			String s = property.get("sofficeProcess");
			if(s != null) processNum = Integer.parseInt(s);
		} catch(NumberFormatException e) {
			System.err.println(e.getMessage());
		}
		
		converter.setup(processNum);
		final Pattern fileNamePtr = Pattern.compile("convert (.*) as a .* -> (.*) using filter : .*");
		converter.setStdConsumer(s -> {
			Matcher m = fileNamePtr.matcher(s);
			if(m.find()) {
				updateUI.accept(new File(m.group(1)), new File(m.group(2)));
			}
		});
		converter.start();
		
		return converter.convert(inputs.stream().map(f -> new IO(f, format)).toList());
	}

}

class NonThrowFuture {
	
	private final Future<Integer> f;

	public NonThrowFuture(Future<Integer> f) {
		this.f = f;
	}
	
	public int get() {
		try {
			return f.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return -1;
		}
	}
}