package io.github.awidesky.documentConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.awidesky.documentConverter.jodConverter.IO;
import io.github.awidesky.guiUtil.SwingDialogs;

public class SimpleConvertUtil implements ConvertUtil {

	private static String sofficePath = Main.getProperty("sofficePath", ".");
	/*
	static {
		Process p;
		try {
			p = new ProcessBuilder("sh", "-c", (System.getProperty("os.name").toLowerCase().contains("windows") ?
					"where" : "where") + " soffice").start();
			try(BufferedReader br = p.inputReader()) {
				//sofficePath = br.readLine();
				br.lines().forEach(System.out::println);
			}
			try(BufferedReader br = p.errorReader()) {
				//sofficePath = br.readLine();
				br.lines().forEach(System.out::println);
			}
			System.out.println("sofficePath : " + sofficePath);
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	 */
	private ExecutorService threadpool;
	private int process;

	private String format;
	private File outdir;
	private Consumer<String> stdConsumer = s -> {};
	
	@Override
	public void setup(int process) {
		this.process = process;
	}
	
	@Override
	public void start() {
		threadpool = Executors.newFixedThreadPool(process);		
	}
	
	@Override
	public boolean convert(IO io) {
		boolean ret = false;
		getParams(io);
		try {
			ret = threadpool.submit(getJob(0, io.getOut().getParentFile(), getCommand(List.of(io.getIn())))).get() == 0;
			if(ret) renameOutFile(io);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			SwingDialogs.error("Convert task failed!", "%e%", e, true);
		}
		return ret;
	}

	@Override
	public boolean convert(IO... ios) {
		return convert(List.of(ios));
	}

	@Override
	public boolean convert(List<IO> ios) {
		getParams(ios.get(0));
		
		List<File> inputs = ios.stream().map(IO::getIn).toList();
		ArrayList<List<File>> lists = new ArrayList<>(process);
		int portion = ios.size() / process;
		for(int i = 0; i < process; i++) {
			lists.add(inputs.subList(0 + portion * i, (i == process - 1) ? inputs.size() : (portion * (i + 1))));
		}

		boolean success = IntStream.range(0, process)
				.filter(i -> !lists.get(i).isEmpty())
				.mapToObj(i -> threadpool.submit(getJob(i, ios.get(0).getOut().getParentFile(), getCommand(lists.get(i)))))
				.map(NonThrowFuture::new)
				.map(NonThrowFuture::get)
				.allMatch(i -> i == 0);
		
		ios.forEach(this::renameOutFile);
		
		return success;
	}

	@Override
	public void close() {
		try {
			threadpool.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			threadpool.shutdownNow();
		}
	}
	
	public void setOutdir(File out) {
		outdir = out;
	}
	
	private void getParams(IO io) {
		if(outdir == null) outdir = io.getOut().getParentFile();
		String name = io.getOut().getName();
		format = name.substring(name.lastIndexOf(".") + 1);
	}
	
	public void setStdConsumer(Consumer<String> stdConsumer) {
		this.stdConsumer  = stdConsumer;
	}

	private List<String> getCommand(List<File> files) {
		List<String> ret = new ArrayList<>(10);
		/*
		if(System.getProperty("os.name").toLowerCase().contains("windows")) {
			 ret.add("cmd");
			 ret.add("/c");
		} else {
			ret.add("sh");
			ret.add("-c");
		}
		List<String> ret1 = new ArrayList<>();
		ret1.addAll(List.of("soffice", "--headless", "--convert-to", "\"" + format + "\"",
				"--outdir", "\"" + outdir.getAbsolutePath() + "\""));
		files.stream().map(File::getAbsolutePath).map(s -> "\"" + s + "\"").forEach(ret1::add);
		
		ret.add(ret1.stream().collect(Collectors.joining(" ")));
		return ret;
		*/
		ret.addAll(List.of(sofficePath + "soffice", "--headless", "--convert-to", "\"" + format + "\"",
				"--outdir", "\"" + outdir.getAbsolutePath() + "\""));
		files.stream().map(File::getAbsolutePath).map(s -> "\"" + s + "\"").forEach(ret::add);
		if(System.getProperty("os.name").toLowerCase().contains("mac")) {
			ret = List.of("zsh", "-c", ret.stream().collect(Collectors.joining(" ")));
		}
		return ret;
	}
	
	private Callable<Integer> getJob(int i, File workingdir, List<String> command) {
		return () -> {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.directory(workingdir);
			System.out.println("[Debug] soffice command : " + pb.command().stream().collect(Collectors.joining(" ")));
			Process p = pb.start();
			try(BufferedReader br = p.inputReader()) {
				br.lines().forEach(s -> {
					stdConsumer.accept(s);
					System.out.printf("[Process %d stdout] : %s\n", i, s);
				});
			}
			StringWriter stderr = new StringWriter();
			try(BufferedReader br = p.errorReader()) {
				br.lines().forEach(s -> {
					stderr.append(s).append("\n");
					System.err.printf("[Process %d stderr] : %s\n", i, s);
				});
			}
			int err = p.waitFor();
			System.out.printf("[Process %d finished with error code : %d]\n", i, err);
			if(err != 0)
				SwingDialogs.error("Failed to convert", "Error code : " + err + "\n" + stderr.toString(), null, true);
			
			return err;
		};
	}

	private void renameOutFile(IO io) {
		String newFile = io.getIn().getName();
		newFile = newFile.substring(0, newFile.lastIndexOf(".")) + "." + format;
		File f = new File(outdir, newFile);
		System.out.println("newFile(" + f.exists() + ") : " + f.getAbsolutePath());//TODO
		if(f.exists()) 
			f.renameTo(io.getOut());
		System.out.println("renamedFile(" + io.getOut().exists() + ") : " + io.getOut().getAbsolutePath());//TODO		
	}
}
