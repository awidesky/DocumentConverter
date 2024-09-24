package io.github.awidesky.documentConverter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.awidesky.documentConverter.jodConverter.IO;

/***
 * https://github.com/ebandal/H2Orestart
 */
class HancomConvertTest {

	static List<File> in;
	static List<IO> ios;

	@BeforeAll
	static void setup() {
		Utils.clearPDFFiles();
		in = Arrays.stream(TestResourcePath.getResource("samples/hancom").listFiles()).toList();
		ios = in.stream().map(IO::new).toList();
	}
	
	@AfterAll
	static void close() {
		//System.out.println(); System.out.println(); System.out.println();
		Utils.clearPDFFiles();
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
			IO io1 = new IO(io.getIn(), "_1_.pdf");
			IO io2 = new IO(io.getIn(), "_2_.pdf");

			// System.out.println("\t" + io1.toString());
			dc.convert(io1);
			// System.out.println("\t" + io2.toString());
			dc.convert(io2);
			assertTrue(Utils.comparePDF(io1.getOut(), io2.getOut()));
		}
		dc.close();
	}
	
}
