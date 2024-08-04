package io.github.awidesky.documentConverter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jodconverter.core.office.OfficeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.awidesky.documentConverter.IOPair.IO;

class ConvertConcurrentTest {
	public static final long MAXPPTINPUTFILES = 10;
	
	ConvertUtil dc;
	List<File> in;

	@BeforeEach
	void setUp() throws Exception {
		in = Arrays.stream(TestResourcePath.getResource("samples").listFiles()).toList();
		dc = new ConvertUtil(Runtime.getRuntime().availableProcessors());
		dc.start();
	}
	
	@AfterEach
	void close() throws OfficeException {
		dc.close();
		//System.out.println(); System.out.println(); System.out.println();
		Arrays.stream(TestResourcePath.getResource("samples").listFiles()).filter(f -> f.getName().endsWith(".pdf")).parallel().forEach(File::delete);
	}
	
	@Test
	void bulkTest() throws OfficeException, InterruptedException, ExecutionException {
		in = new ArrayList<File>(in.stream().filter(f -> !f.getName().equals("DOCX_TestPage.docx")).filter(f -> !f.getName().equals("Extlst-test.pptx")).toList());
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
