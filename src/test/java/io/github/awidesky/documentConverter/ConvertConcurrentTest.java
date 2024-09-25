package io.github.awidesky.documentConverter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.awidesky.documentConverter.jodConverter.IO;
import io.github.awidesky.documentConverter.jodConverter.JodConvertUtil;

class ConvertConcurrentTest {
	public static final long MAXPPTINPUTFILES = 10;
	
	static List<File> in;
	final static int processNum = Runtime.getRuntime().availableProcessors();

	@BeforeAll
	static void setUp() {
		Utils.clearOutput();
		in = Arrays.stream(TestResourcePath.getResource("samples/ms_office").listFiles())
				.filter(f -> f.getName().endsWith(".docx") || 
							 f.getName().endsWith(".xlsx") ||
						 	 f.getName().endsWith(".pptx"))
				.toList();
	}
	
	@AfterAll
	static void close() {
		//System.out.println(); System.out.println(); System.out.println();
		Utils.clearOutput();
	}

	@Test
	void bulkTest_JodConvertUtil() throws Exception {
		ConvertUtil dc = new JodConvertUtil();
		dc.setup(processNum);
		dc.start();
		List<IO> ios = in.stream().map(Utils::toIO).toList();
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
		StringWriter sw = new StringWriter();
		sw.append(dc.getClass().getName()).append("\n");
		sw.append("Process : " + processNum).append("\n");
		sw.append("Sequential convert : " + Duration.between(startTime, Instant.now()).toMillis() + "ms").append("\n");
		
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
		sw.append("Parallel   convert : " + Duration.between(startTime, Instant.now()).toMillis() + "ms");
		System.out.println(sw.toString());
		dc.close();
		
		Iterator<File> f1 = first.iterator();
		Iterator<File> f2 = ios.stream().map(IO::getOut).toList().iterator();
		while(f1.hasNext()) {
			assertTrue(Utils.comparePDF(f1.next(), f2.next()));
		}
	}
	
	@Test
	void bulkTest_SimpleConvertUtil() throws Exception {
		ConvertUtil dc = new SimpleConvertUtil();
		dc.setup(1);
		dc.start();
		List<IO> ios = in.stream().map(Utils::toIO).toList();
		ios.forEach(io -> io.setOut(new File(io.getOut().getParent(), "Sequential_" + io.getOut().getName())));
		dc.convert(ios.get(0)); //Test conversion to warm up
		Instant startTime = Instant.now();
		ios.forEach(io -> {
			//System.out.println("\t" + io.toString());
			try {
				dc.convert(io);
			} catch (Exception e) {
				e.printStackTrace();
				fail("failed to convert while converting " + io.toString());
			}
		});
		dc.close();
		StringWriter sw = new StringWriter();
		sw.append(dc.getClass().getName()).append("\n");
		sw.append("Process : " + processNum).append("\n");
		sw.append("Sequential convert : " + Duration.between(startTime, Instant.now()).toMillis() + "ms").append("\n");
		
		List<File> first = ios.stream().map(IO::getOut).toList();
		
		ios.forEach(io -> io.setOut(new File(io.getOut().getParent(), io.getOut().getName().replace("Sequential_", "Parallel_"))));
		
		dc.setup(processNum);
		dc.start();
		startTime = Instant.now();
		try {
			dc.convert(ios);
		} catch (Exception e) {
			e.printStackTrace();
			fail("failed to convert");
		}
		sw.append("Parallel   convert : " + Duration.between(startTime, Instant.now()).toMillis() + "ms");
		System.out.println(sw.toString());
		
		Iterator<File> f1 = first.iterator();
		Iterator<File> f2 = ios.stream().map(IO::getOut).toList().iterator();
		while(f1.hasNext()) {
			assertTrue(Utils.comparePDF(f1.next(), f2.next()));
		}
	}

}
