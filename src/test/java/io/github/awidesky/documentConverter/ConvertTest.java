package io.github.awidesky.documentConverter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.awidesky.documentConverter.jodConverter.IO;

class ConvertTest {

	static List<File> in;
	static List<IO> ios;

	@BeforeAll
	static void setup() {
		Utils.clearOutput();
		in = Arrays.stream(TestResourcePath.getResource("samples/ms_office").listFiles())
				.toList();
		ios = in.stream().map(Utils::toIO).toList();
	}
	
	@AfterAll
	static void close() {
		//System.out.println(); System.out.println(); System.out.println();
		Utils.clearOutput();
	}

	@ParameterizedTest
	@MethodSource("io.github.awidesky.documentConverter.ConvertUtilProvider#convertUtils")
	void convertTest(ConvertUtil dc) throws Exception {
		dc.setup(1);
		dc.start();
		dc.convert(ios);
		dc.close();
	}

	@ParameterizedTest
	@MethodSource("io.github.awidesky.documentConverter.ConvertUtilProvider#convertUtils")
	void duplicateTest(ConvertUtil dc) throws Exception {
		dc.setup(1);
		dc.start();
		for (IO io : ios) {
			IO io1 = new IO(io.getIn(), Utils.outDir(), IO.changeExtension(io.getIn(), "_1_.pdf"));
			IO io2 = new IO(io.getIn(), Utils.outDir(), IO.changeExtension(io.getIn(), "_2_.pdf"));

			// System.out.println("\t" + io1.toString());
			dc.convert(io1);
			// System.out.println("\t" + io2.toString());
			dc.convert(io2);

			assertTrue(Utils.comparePDF(io1.getOut(), io2.getOut()));
		}
		dc.close();
	}
	
	@Test
	void testTrickyFiles() {
		//.equals("DOCX_TestPage.docx")).filter(f -> !f.getName().equals("Extlst-test.pptx")
	}
	
}
