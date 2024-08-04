package io.github.awidesky.documentConverter;

import java.io.File;
import java.util.stream.IntStream;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;

import io.github.awidesky.documentConverter.IOPair.IO;
import io.github.awidesky.projectPath.UserDataPath;

public class ConvertUtil {
	private final OfficeManager officeManager;
	private final org.jodconverter.core.DocumentConverter converter;
	private static final int portStart = 50000;

	public ConvertUtil(int officeProcess) {
		this(null, officeProcess);
	}
	public ConvertUtil(String officeHome, int officeProcess) {
		officeManager = LocalOfficeManager.builder()
				.portNumbers(IntStream.range(portStart, portStart + officeProcess).toArray())
				.officeHome(officeHome)
				.templateProfileDir(getProfileDir())
				.build();
		converter = LocalConverter.builder().officeManager(officeManager).build();
	}
	
	private String getProfileDir() {
		String ret = UserDataPath.getWindowsAppdataRoamingFolder("LibreOffice", "4");
		return (new File(ret).exists()) ? ret : null;
	}
	
	public void start() throws OfficeException {
		officeManager.start();
	}

	public void convert(IO io) throws OfficeException {
		converter.convert(io.getIn()).to(io.getOut()).execute();
	}

	public void close() throws OfficeException {
		officeManager.stop();
	}
}
