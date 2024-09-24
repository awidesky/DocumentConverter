package io.github.awidesky.documentConverter;

import java.util.List;

import io.github.awidesky.documentConverter.jodConverter.IO;

public interface ConvertUtil {
	public void setup(int process) throws Exception;
	public void start() throws Exception;
	public boolean convert(IO io) throws Exception;
	public default boolean convert(IO... ios) throws Exception {
		boolean ret = true;
		for(IO io : ios)
			if(!convert(io)) ret = false;
		
		return ret;
	}
	public default boolean convert(List<IO> ios) throws Exception {
		boolean ret = true;
		for(IO io : ios)
			if(!convert(io)) ret = false;
		
		return ret;
	}
	public void close() throws Exception;
}
