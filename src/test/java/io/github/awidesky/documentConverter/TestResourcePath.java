package io.github.awidesky.documentConverter;

import java.io.File;

public class TestResourcePath {

	public static File getResource(String name) {
		String path = "C:\\Users\\FVT01미래자동차01\\Downloads\\" + name;//URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource(name).getFile(), "UTF-8");
		return new File(path);
	}
}
