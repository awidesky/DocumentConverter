package io.github.awidesky.documentConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.awidesky.documentConverter.IOPair.IO;

class ConvertTest {

	File f;
	File o;
	ConvertUtil dc;

	@BeforeEach
	void setup() throws OfficeException {
		f = TestResourcePath.getResource("pptx/sample.pptx");
		Arrays.stream(f.getParentFile().listFiles()).filter(f -> f.getName().endsWith(".pdf")).forEach(File::delete);
		o = new File(f.getParentFile(), f.getName() + ".pdf");
		dc = new ConvertUtil();
		dc.start();
	}
	
	@Test
	void convertTest() throws OfficeException, IOException {
		dc.convert(new IO(f, o));
		dc.close();
		Desktop.getDesktop().open(o);
	}
	
	@Test
	void duplicateTest() throws OfficeException, IOException {
		File o1 = new File("C:\\Users\\FVT01미래자동차01\\Downloads\\", f.getName() + "_1_.pdf");
		File o2 = new File("C:\\Users\\FVT01미래자동차01\\Downloads\\", f.getName() + "_2_.pdf");

		dc.convert(new IO(f, o1));
		dc.convert(new IO(f, o2));
		dc.close();
		
		assertEquals(Utils.getHash(o1), Utils.getHash(o2));
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
