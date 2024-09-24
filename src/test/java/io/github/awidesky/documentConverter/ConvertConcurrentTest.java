package io.github.awidesky.documentConverter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.awidesky.documentConverter.jodConverter.IO;

class ConvertConcurrentTest {
	public static final long MAXPPTINPUTFILES = 10;
	
	static List<File> in;
	final static int processNum = Runtime.getRuntime().availableProcessors();

	@BeforeAll
	static void setUp() {
		Utils.clearPDFFiles();
		in = Arrays.stream(TestResourcePath.getResource("samples/ms_office").listFiles())
				.toList();
	}
	
	@AfterAll
	static void close() {
		//System.out.println(); System.out.println(); System.out.println();
		Utils.clearPDFFiles();
	}

	@ParameterizedTest
	@MethodSource("io.github.awidesky.documentConverter.ConvertUtilProvider#convertUtils")
	void bulkTest(ConvertUtil dc) throws Exception {
		dc.setup(processNum);
		dc.start();
		List<IO> ios = in.stream().map(IO::new).toList();
		ios.forEach(io -> io.setOut(new File(io.getOut().getParent(), "Sequential_" + io.getOut().getName())));
		dc.convert(ios.get(0)); //Test conversion to warm up
		Instant startTime = Instant.now();
		ios.stream().forEach(io -> {
			//System.out.println("\t" + io.toString());
			try {
				dc.convert(io);
			} catch (Exception e) {
				e.printStackTrace();
				fail("failed to convert while converting " + io.toString());
			}
		});
		System.out.println(dc.getClass().getName());
		System.out.println("Process : " + processNum);
		System.out.println("Sequential convert : " + Duration.between(startTime, Instant.now()).toMillis() + "ms");
		
		List<File> first = ios.stream().map(IO::getOut).toList();
		
		ios.forEach(io -> io.setOut(new File(io.getOut().getParent(), io.getOut().getName().replace("Sequential_", "Parallel_"))));
		
		startTime = Instant.now();
		ios.parallelStream().forEach(io -> {
			try {
				dc.convert(io);
			} catch (Exception e) {
				e.printStackTrace();
				fail("failed to convert while converting " + io.toString());
			}
		});
		System.out.println("Parallel   convert : " + Duration.between(startTime, Instant.now()).toMillis() + "ms");
		dc.close();
		
		Iterator<File> f1 = first.iterator();
		Iterator<File> f2 = ios.stream().map(IO::getOut).toList().iterator();
		while(f1.hasNext()) {
			assertTrue(Utils.comparePDF(f1.next(), f2.next()));
		}
	}

}
