package io.github.awidesky.documentConverter;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Simple conversion mode that just do
 * <code>
 * soffice --headless --convert-to "format" --outdir "outdir" "files"
 * </code>
 */
public class SimpleConvertManager implements ConvertManager {

	private File outdir;
	private String format;
	
	@Override
	public void setup(File outdir, boolean keepOriginalExtension, String format) {
		this.outdir = outdir;
		this.format = format;
		if(keepOriginalExtension) {
			System.out.println("[Warning] Cannot keep original extension in simple implementation mode");
			System.out.println("[Warning] since it uses command : soffice --headless --convert-to \"format\" --outdir \"outdir\" \"files\"");
			System.out.println("[Warning] keepOriginalExtension=" + keepOriginalExtension + " will be ignored.");
		}
	}

	@Override
	public boolean convert(List<File> inputs, Map<String, String> property, BiConsumer<File, File> updateUI) {
		int processNum = 4;
		try {
			String s = property.get("sofficeProcess");
			if(s != null) processNum = Integer.parseInt(s);
		} catch(NumberFormatException e) {
			System.err.println(e.getMessage());
		}
		
		ArrayList<List<File>> lists = new ArrayList<>(processNum);
		int portion = inputs.size() / processNum;
		for(int i = 0; i < processNum; i++) {
			lists.add(inputs.subList(0 + portion * i, (i == processNum - 1) ? inputs.size() : (portion * (i + 1))));
		}
		final Pattern fileNamePtr = Pattern.compile("convert (.*) as a .* -> (.*) using filter : .*");
		ExecutorService threadpool = Executors.newFixedThreadPool(processNum);
		boolean ret =  IntStream.range(0, processNum).mapToObj(i -> 
			threadpool.submit(() -> {
				ProcessBuilder pb = new ProcessBuilder("soffice", "--headless", "--convert-to", "\"" + format + "\"", "--outdir", "\"" + outdir.getAbsolutePath() + "\"",
						lists.get(i).stream().map(s ->  "\"" + s + "\"").collect(Collectors.joining(", "))
						);
				System.out.println("[Debug] soffice command : " + pb.command().stream().collect(Collectors.joining(" ")));
				Process p = pb.start();
				try(BufferedReader br = p.inputReader()) {
					String s = br.readLine();
					Matcher m = fileNamePtr.matcher(s);
					if(m.find()) {
						updateUI.accept(new File(m.group(1)), new File(m.group(2)));
					}
					System.out.printf("[Process %2d stdout] : %s\n", i, s);
				}
				try(BufferedReader br = p.errorReader()) {
					System.err.printf("[Process %2d stderr] : %s\n", i, br.readLine());
				}
				int err = p.waitFor();
				System.out.printf("[Process %2d finished with error code : %d]", i, err);
				return err;
			})).map(NonThrowFuture::new).map(NonThrowFuture::get).allMatch(i -> i == 0);
		
		try {
			threadpool.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			threadpool.shutdownNow();
		}
		return ret;
	}

}

class NonThrowFuture {
	
	private final Future<Integer> f;

	public NonThrowFuture(Future<Integer> f) {
		this.f = f;
	}
	
	public int get() {
		try {
			return f.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return -1;
		}
	}
}