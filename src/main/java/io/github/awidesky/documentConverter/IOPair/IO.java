package io.github.awidesky.documentConverter.IOPair;

import java.io.File;
import java.util.Objects;

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
		this(in, new File(Objects.requireNonNullElse(outdir, in.getParentFile()), outName));
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
	
	@Override
	public String toString() {
		return "IO from : " + in.getName() + " to : " + out.getName();
	}
	
	public String toStringWithAbsolutePath() {
		return "IO from : " + in.getAbsolutePath() + " to : " + out.getAbsolutePath();
	}
	
}
