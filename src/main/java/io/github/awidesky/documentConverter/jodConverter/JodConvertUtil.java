package io.github.awidesky.documentConverter.jodConverter;

import java.io.File;
import java.util.stream.IntStream;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;

import io.github.awidesky.documentConverter.ConvertUtil;
import io.github.awidesky.projectPath.UserDataPath;

public class JodConvertUtil implements ConvertUtil {
	private OfficeManager officeManager;
	private org.jodconverter.core.DocumentConverter converter;
	private static final int portStart = 50000;
	private String officeHome;

	public JodConvertUtil() {
		this(null);
	}
	public JodConvertUtil(String officeHome) {
		this.officeHome = officeHome;
	}
	
	@Override
	public void setup(int process) throws OfficeException {
		if(officeManager != null && officeManager.isRunning()) officeManager.stop();
		officeManager = LocalOfficeManager.builder()
				.portNumbers(IntStream.range(portStart, portStart + process).toArray())
				.officeHome(officeHome)
				.templateProfileDir(getProfileDir())
				.build();
		converter = LocalConverter.builder().officeManager(officeManager).build();
	}
	
	private String getProfileDir() {
		String ret = UserDataPath.getWindowsAppdataRoamingFolder("LibreOffice", "4");
		return (new File(ret).exists()) ? ret : null;
	}
	
	@Override
	public void start() throws OfficeException {
		officeManager.start();
	}

	@Override
	public boolean convert(IO io) throws OfficeException {
		converter.convert(io.getIn()).to(io.getOut()).execute();
		return true; //if failed, an OfficeException will be thrown anyway.
	}

	@Override
	public void close() throws OfficeException {
		officeManager.stop();
	}
}
