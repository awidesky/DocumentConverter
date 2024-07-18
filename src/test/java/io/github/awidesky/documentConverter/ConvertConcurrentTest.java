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
		Arrays.stream(TestResourcePath.getResource("samples").listFiles()).filter(f -> f.getName().endsWith(".pdf")).parallel().forEach(File::delete);
	}
	
	@Test
	void bulkTest() throws OfficeException, InterruptedException, ExecutionException {
		List<IO> ios = in.stream().map(IO::new).toList();
		Instant startTime = Instant.now();
		ios.stream().forEach(io -> {
			try {
				dc.convert(io);
			} catch (OfficeException e) {
				e.printStackTrace();
				fail("failed to convert!");
			}
		});
		System.out.println("Sequential convert : " + Duration.between(startTime, Instant.now()).toMillis() + "ms");
		
		List<File> first = ios.stream().map(IO::getOut).toList();
		
		ios.forEach(io -> io.setOut(new File(io.getOut().getParent(), "(1)" + io.getOut().getName())));
		
		startTime = Instant.now();
		ios.parallelStream().forEach(io -> {
			try {
				dc.convert(io);
			} catch (OfficeException e) {
				e.printStackTrace();
				fail("failed to convert!");
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
