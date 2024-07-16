package io.github.awidesky.documentConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.jodconverter.core.office.OfficeException;
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

	@Test
	void bulkTest() throws OfficeException, InterruptedException, ExecutionException {
		List<IO> ios = in.stream().map(IO::new).toList();
		dc.convert(ios);
		Map<File, String> map = ios.stream()
				.map(IO::getOut)
				.map(ConvertConcurrentTest::getFileAndHash)
				.filter(fah -> fah.hash != null)
				.collect(Collectors.toMap(FileAndHash::getFile, FileAndHash::getHash));
		
		//ios.stream().map(IO::getOut).forEach(File::delete);
		ios.forEach(io -> io.setOut(new File(io.getOut().getParent() + File.separator + "out", io.getOut().getName())));
		
		ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		ConvertExecutor ce = dc.convertExecutor(pool);
		ce.submitConvertsAll(ios).get();
		pool.shutdown();
		
		ios.stream()
			.forEach(io -> {
				File f = io.getOut();
				String h = map.get(f);
				String h2 = Utils.getHash(f);
				assertEquals(h, h2, f.getAbsolutePath() + " must be " + h + " but " + h2);
				//if(h == null || h.equals(h2)) return null;
				//return new FileAndHash(f, h, h2);
			});
			//.filter(Objects::nonNull)
			//.map(fah -> fah.file + " must be " + fah.hash + " but " + fah.hash2)
			//.toList();

		/*
		if(!list.isEmpty()) {
			fail(list.stream().collect(Collectors.joining("\n")));
		}
		*/
	}
	
	private static class FileAndHash {
		private File file;
		private String hash;
		public FileAndHash(File f, String h) {
			file = f;
			hash = h;
		}
		public File getFile() {
			return file;
		}
		public String getHash() {
			return hash;
		}
	}

	private static FileAndHash getFileAndHash(File f) {
		return new FileAndHash(f, Utils.getHash(f));
	}
}
