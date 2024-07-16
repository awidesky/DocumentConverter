package io.github.awidesky.documentConverter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class Utils {

	public static String getHash(File f) {
		byte[] buffer= new byte[8192];
	    int count;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
			while ((count = bis.read(buffer)) > 0) {
				digest.update(buffer, 0, count);
			}
			bis.close();
			return HexFormat.of().formatHex(digest.digest());
		} catch (NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
}
