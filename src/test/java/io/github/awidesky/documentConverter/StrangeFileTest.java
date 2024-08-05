package io.github.awidesky.documentConverter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.jodconverter.core.office.OfficeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.awidesky.documentConverter.IOPair.IO;

class StrangeFileTest {

	static ConvertUtil dc;

	@BeforeAll
	static void setup() throws OfficeException {
		dc = new ConvertUtil(1);
		dc.start();
	}
	
	@AfterAll
	static void close() throws OfficeException {
		dc.close();
		Arrays.stream(TestResourcePath.getResource("samples").listFiles()).parallel()
			.filter(f -> f.getName().endsWith(".pdf")).forEach(File::delete);
	}

	/*
	 * *** Terminating app due to uncaught exception
	 * 'NSInternalInconsistencyException', reason: 'API misuse: modification of a
	 * menu's items on a non-main thread when the menu is part of the main menu.
	 * Main menu contents may only be modified from the main thread.' First throw
	 */
	@Test
	void API_Misuse() throws OfficeException {
		File sample = TestResourcePath.getResource("samples");
		IO io = new IO(new File(sample, "DOCX_TestPage.docx"), ".pdf");
		System.out.println("\t" + io.toString());
		dc.convert(io);
	}
	
	@Test
	void diff() throws OfficeException {
		File sample = TestResourcePath.getResource("samples");
		File in = new File(sample, "Extlst-test.pptx");
		IO io1 = new IO(in, "_1_.pdf");
		IO io2 = new IO(in, "_2_.pdf");

		System.out.println("\t" + io1.toString());
		dc.convert(io1);
		System.out.println("\t" + io2.toString());
		dc.convert(io2);

		assertTrue(Utils.comparePDF(io1.getOut(), io2.getOut()));
	}

}
