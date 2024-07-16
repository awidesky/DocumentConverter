package io.github.awidesky.documentConverter;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.awidesky.documentConverter.IOPair.IO;

class ConvertTest {

	File f = new File("C:\\Users\\FVT01미래자동차01\\Downloads\\sample.pptx");
	File o = new File("C:\\Users\\FVT01미래자동차01\\Downloads\\sample.pdf");
	ConvertUtil dc = new ConvertUtil();

	@BeforeEach
	void setup() throws OfficeException {
		dc.start();
	}
	
	@Test
	void test() throws OfficeException, IOException {
		dc.convert(new IO(f, o));
		dc.close();
		Desktop.getDesktop().open(o);
	}
	
	@Test
	@Disabled
	/**
	 * Example from {@linkplain https://github.com/jodconverter/jodconverter/wiki/Java-Library}
	 */
	void manualConvert() throws OfficeException, IOException {
		final LocalOfficeManager officeManager = LocalOfficeManager.builder().officeHome("C:\\Users\\FVT01미래자동차01\\Downloads\\LibreOfficePortable\\App\\libreoffice").build();
		try {

		    // Start an office process and connect to the started instance (on port 2002).
		    officeManager.start();

		    // Convert
		    LocalConverter.builder().officeManager(officeManager).build()
		             .convert(f)
		             .to(o)
		             .execute();

			Desktop.getDesktop().open(o);
		} finally {
		    // Stop the office process
		    OfficeUtils.stopQuietly(officeManager);
		}
	}

}
