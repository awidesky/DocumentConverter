package io.github.awidesky.documentConverter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public interface ConvertManager {

	public void setup(File outdir, boolean keepOriginalExtension, String format);
	public boolean convert(List<File> inputs, Map<String, String> property, BiConsumer<File, File> updateUI);
	
}
