package io.github.awidesky.documentConverter.IOPair;

import java.io.File;

// TODO : make IO interface, and subclass IO_File, IO_FileStream
public class IO {
	private File in;
	private File out;
	
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

	public void setIn(File in) {
		this.in = in;
	}

	public void setOut(File out) {
		this.out = out;
	}
	
}
