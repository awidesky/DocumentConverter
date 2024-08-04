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
		f = TestResourcePath.getResource("samples/harvard.pptx");
		o = new File(f.getParentFile(), f.getName() + ".pdf");
		dc = new ConvertUtil(2);
		dc.start();
	}
	
	@AfterEach
	void close() throws OfficeException {
		dc.close();
		System.out.println(); System.out.println(); System.out.println();
		Arrays.stream(TestResourcePath.getResource("samples").listFiles()).filter(f -> f.getName().endsWith(".pdf")).parallel().forEach(File::delete);
	}
	
	@Test
	void convertTest() throws OfficeException, IOException {
		dc.convert(new IO(f, o));
	}
	
	@Test
	void duplicateTest() throws OfficeException, IOException {
		IO io1 = new IO(f, "_1_.pdf");
		IO io2 = new IO(f, "_2_.pdf");
		
		System.out.println("\t" + io1.toString());
		dc.convert(io1);
		System.out.println("\t" + io2.toString());
		dc.convert(io2);
		
		assertTrue(Utils.comparePDF(io1.getOut(), io2.getOut()));
	}
	
}
