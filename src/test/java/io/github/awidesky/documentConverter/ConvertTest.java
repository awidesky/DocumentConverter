package io.github.awidesky.documentConverter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.jodconverter.core.office.OfficeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
	
	@AfterEach
	void close() throws OfficeException {
		dc.close();
		Arrays.stream(TestResourcePath.getResource("pptx").listFiles()).filter(f -> !f.getName().endsWith(".pptx")).forEach(File::delete);
	}
	
	@Test
	void convertTest() throws OfficeException, IOException {
		dc.convert(new IO(f, o));
		//Desktop.getDesktop().open(o);
	}
	
	@Test
	void duplicateTest() throws OfficeException, IOException {
		File o1 = new File("C:\\Users\\FVT01미래자동차01\\Downloads\\", f.getName() + "_1_.pdf");
		File o2 = new File("C:\\Users\\FVT01미래자동차01\\Downloads\\", f.getName() + "_2_.pdf");

		dc.convert(new IO(f, o1));
		dc.convert(new IO(f, o2));
		
		assertTrue(Utils.comparePDF(o1, o2));
	}
	
}
