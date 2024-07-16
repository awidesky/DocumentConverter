package io.github.awidesky.documentConverter.IOPair;

import java.io.File;

public class IO {
	private final File in;
	private final File out;
	
	public IO(File in) {
		this(in, ".pdf");
	}
	
	public IO(File in, String newExtension) {
		this(in, in.getParentFile(), in.getName().substring(0, in.getName().lastIndexOf(".")) + newExtension);
	}

	public IO(File in, File outdir, String outName) {
		this(in, new File(outdir, outName));
	}

	public IO(File in, File out) {
		this.in = in;
		this.out = out;
	}

	public File getIn() {
		return in;
	}

	public File getOut() {
		return out;
	}
	
}
