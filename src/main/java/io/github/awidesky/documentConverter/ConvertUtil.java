package io.github.awidesky.documentConverter;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;

import io.github.awidesky.documentConverter.IOPair.IO;

public class ConvertUtil {
	private final OfficeManager officeManager;
	private final org.jodconverter.core.DocumentConverter converter;

	public ConvertUtil() {
		this("C:\\Users\\FVT01미래자동차01\\Downloads\\LibreOfficePortable\\App\\libreoffice"); //TODO : proper path and configure
	}
	public ConvertUtil(String officeHome) {
		officeManager = LocalOfficeManager.builder().templateProfileDir("C:\\Users\\FVT01미래자동차01\\Downloads\\LibreOfficePortable\\Data\\settings").officeHome(officeHome).build();
		converter = LocalConverter.builder().officeManager(officeManager).build();
	}
	
	public void start() throws OfficeException {
		officeManager.start();
	}

	public void convert(List<IO> ios) throws OfficeException {
		for(IO io : ios) convert(io);
	}

	public void convert(IO io) throws OfficeException {
		converter.convert(io.getIn()).to(io.getOut()).execute();
	}
	
	public ConvertExecutor convertExecutor(ExecutorService threadpool) {
		return new ConvertExecutor(this, threadpool);
	}

	public void close() throws OfficeException {
		officeManager.stop();
	}
}
