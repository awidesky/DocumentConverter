package io.github.awidesky.documentConverter;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
	
	public Future<Void> submitConvertsAll(List<IO> ios) {
		List<Future<Void>> list = ios.stream().map(this::submitConvert).toList();
		return new Future<Void>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return list.stream().allMatch(f -> f.cancel(mayInterruptIfRunning));
			}

			@Override
			public boolean isCancelled() {
				return list.stream().allMatch(Future::isCancelled);
			}

			@Override
			public boolean isDone() {
				return list.stream().allMatch(Future::isDone);
			}

			@Override
			public Void get() throws InterruptedException, ExecutionException {
				Exception ex = list.stream().map(f -> {
					Exception e = null;
					try {
						f.get();
					} catch (InterruptedException | ExecutionException e1) {
						// TODO log Exception
						e = e1;
					}
					return e;
				}).filter(Objects::nonNull).toList().get(0);
				if(ex != null) {
					if(ex instanceof InterruptedException ie) throw ie;
					else if(ex instanceof ExecutionException ee) throw ee;
					else throw new ExecutionException(ex);
				}
				return null;
			}

			@Override
			public Void get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				Exception ex = list.stream().map(f -> {
					Exception e = null;
					try {
						f.get(timeout, unit);
					} catch (InterruptedException | ExecutionException | TimeoutException e1) {
						// TODO log Exception
						e = e1;
					}
					return e;
				}).filter(Objects::nonNull).toList().get(0);
				if(ex != null) {
					if(ex instanceof InterruptedException ie) throw ie;
					else if(ex instanceof ExecutionException ee) throw ee;
					else if(ex instanceof TimeoutException te) throw te;
					else throw new ExecutionException(ex);
				}
				return null;
			}
			
		};
	}
	
}
