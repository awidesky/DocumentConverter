package io.github.awidesky.documentConverter;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.jodconverter.core.office.OfficeException;

import io.github.awidesky.documentConverter.IOPair.IO;

public class ConvertExecutor {

	private final ConvertUtil convertUtil;
	private final ExecutorService threadpool;
	
	public ConvertExecutor(ConvertUtil convertUtil, ExecutorService threadpool) {
		this.convertUtil = convertUtil;
		this.threadpool = threadpool;
	}

	public Future<Void> submitConvert(IO io) {
		return threadpool.submit(new Callable<Void>() {
			@Override
			public Void call() throws OfficeException {
				convertUtil.convert(io);
				return null;
			}
		});
	}

	public List<Future<Void>> submitConverts(List<IO> ios) {
		return ios.stream().map(this::submitConvert).toList();
	}
	
}
