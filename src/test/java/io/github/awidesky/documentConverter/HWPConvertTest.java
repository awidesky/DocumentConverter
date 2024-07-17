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

/***
 * https://github.com/ebandal/H2Orestart
 */
class HWPConvertTest {

	File f;
	File o;
	ConvertUtil dc;

	@BeforeEach
	void setup() throws OfficeException, IOException {
		f = TestResourcePath.getResource("samples/sample.hwp");
		Arrays.stream(f.getParentFile().listFiles()).filter(f -> f.getName().endsWith(".pdf")).forEach(File::delete);
		o = new File(f.getParentFile(), f.getName() + ".pdf");
		dc = new ConvertUtil();
		dc.start();
	}
	
	@AfterEach
	void close() throws OfficeException {
		dc.close();
		Arrays.stream(TestResourcePath.getResource("samples").listFiles()).filter(f -> f.getName().endsWith(".pdf")).forEach(File::delete);
	}
	
	@Test
	void convertTest() throws OfficeException, IOException {
		dc.convert(new IO(f, o));
		//Desktop.getDesktop().open(o);
	}
	
	@Test
	void duplicateTest() throws OfficeException, IOException {
		IO io1 = new IO(f, "_1_.pdf");
		IO io2 = new IO(f, "_2_.pdf");
		
		dc.convert(io1);
		dc.convert(io2);
		
		assertTrue(Utils.comparePDF(io1.getOut(), io2.getOut()));
	}
	
}
