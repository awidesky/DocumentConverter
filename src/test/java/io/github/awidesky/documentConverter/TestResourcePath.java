package io.github.awidesky.documentConverter;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class TestResourcePath {

	public static File getResource(String name) {
		File ret = null;
		try {
			ret = new File(URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource(".").getFile(), "UTF-8"), name);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return ret;
	}
}
