package io.github.awidesky.documentConverter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jodconverter.core.office.OfficeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.awidesky.documentConverter.jodConverter.ConvertUtil;
import io.github.awidesky.documentConverter.jodConverter.IO;

/***
 * https://github.com/ebandal/H2Orestart
 */
class HancomConvertTest {

	static List<File> in;
	static List<IO> ios;
	static ConvertUtil dc;

	@BeforeAll
	static void setup() throws OfficeException, IOException {
		Utils.clearPDFFiles();
		in = Arrays.stream(TestResourcePath.getResource("samples/hancom").listFiles()).toList();
		ios = in.stream().map(IO::new).toList();
		dc = new ConvertUtil(1);
		dc.start();
	}
	
	@AfterAll
	static void close() throws OfficeException {
		dc.close();
		//System.out.println(); System.out.println(); System.out.println();
		Utils.clearPDFFiles();
	}
	
	@Test
	void convertTest() throws OfficeException, IOException {
		for(IO io : ios) dc.convert(io);
	}
	
	@Test
	void duplicateTest() throws OfficeException, IOException {
		for (IO io : ios) {
			IO io1 = new IO(io.getIn(), "_1_.pdf");
			IO io2 = new IO(io.getIn(), "_2_.pdf");

			// System.out.println("\t" + io1.toString());
			dc.convert(io1);
			// System.out.println("\t" + io2.toString());
			dc.convert(io2);
			assertTrue(Utils.comparePDF(io1.getOut(), io2.getOut()));
		}
	}
	
}
