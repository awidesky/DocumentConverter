package io.github.awidesky.documentConverter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jodconverter.core.office.OfficeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConvertConcurrentTest {
	public static final long MAXPPTINPUTFILES = 10;
	
	static ConvertUtil dc;
	static List<File> in;

	@BeforeAll
	static void setUp() throws Exception {
		Utils.clearPDFFiles();
		in = Arrays.stream(TestResourcePath.getResource("samples/ms_office").listFiles())
				.filter(f -> !f.getName().equals("DOCX_TestPage.docx"))
				.filter(f -> !f.getName().equals("Extlst-test.pptx"))
				.toList();
		dc = new ConvertUtil(Runtime.getRuntime().availableProcessors());
		dc.start();
	}
	
	@AfterAll
	static void close() throws OfficeException {
		dc.close();
		//System.out.println(); System.out.println(); System.out.println();
		Utils.clearPDFFiles();
	}
	
	@Test
	void bulkTest() throws OfficeException, InterruptedException, ExecutionException {
		List<IO> ios = in.stream().map(IO::new).toList();
		ios.forEach(io -> io.setOut(new File(io.getOut().getParent(), "Sequential_" + io.getOut().getName())));
		dc.convert(ios.get(0)); //Test conversion to warm up
		Instant startTime = Instant.now();
		ios.stream().forEach(io -> {
			//System.out.println("\t" + io.toString());
			try {
				dc.convert(io);
			} catch (OfficeException e) {
				e.printStackTrace();
				fail("failed to convert while converting " + io.toString());
			}
		});
		System.out.println("Sequential convert : " + Duration.between(startTime, Instant.now()).toMillis() + "ms");
		
		List<File> first = ios.stream().map(IO::getOut).toList();
		
		ios.forEach(io -> io.setOut(new File(io.getOut().getParent(), "Parallel_" + io.getOut().getName())));
		
		startTime = Instant.now();
		ios.parallelStream().forEach(io -> {
			//System.out.println("\t" + io.toString());
			try {
				dc.convert(io);
			} catch (OfficeException e) {
				e.printStackTrace();
				fail("failed to convert while converting " + io.toString());
			}
		});
		System.out.println("Parallel   convert : " + Duration.between(startTime, Instant.now()).toMillis() + "ms");
		
		Iterator<File> f1 = first.iterator();
		Iterator<File> f2 = ios.stream().map(IO::getOut).toList().iterator();
		while(f1.hasNext()) {
			assertTrue(Utils.comparePDF(f1.next(), f2.next()));
		}
	}

}
