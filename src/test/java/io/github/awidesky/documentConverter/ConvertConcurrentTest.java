package io.github.awidesky.documentConverter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
		Arrays.stream(TestResourcePath.getResource("pptx").listFiles()).filter(f -> !f.getName().endsWith(".pptx")).forEach(File::delete);
		in = Arrays.stream(TestResourcePath.getResource("pptx").listFiles()).filter(f -> f.getName().endsWith(".pptx")).limit(MAXPPTINPUTFILES).toList();
		dc = new ConvertUtil();
		dc.start();
	}
	
	@AfterEach
	void close() throws OfficeException {
		dc.close();
		Arrays.stream(TestResourcePath.getResource("pptx").listFiles()).filter(f -> !f.getName().endsWith(".pptx")).forEach(File::delete);
	}
	
	@Test
	void bulkTest() throws OfficeException, InterruptedException, ExecutionException {
		List<IO> ios = in.stream().map(IO::new).toList();
		dc.convert(ios);
		List<File> first = ios.stream().map(IO::getOut).toList();
		
		ios.forEach(io -> io.setOut(new File(io.getOut().getParent(), "(1)" + io.getOut().getName())));
		
		ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		ConvertExecutor ce = dc.convertExecutor(pool);
		ce.submitConvertsAll(ios).get();
		pool.shutdown();
		
		Iterator<File> f1 = first.iterator();
		Iterator<File> f2 = ios.stream().map(IO::getOut).toList().iterator();
		while(f1.hasNext()) {
			assertTrue(Utils.comparePDF(f1.next(), f2.next()));
		}
	}

}
