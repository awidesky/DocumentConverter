package io.github.awidesky.documentConverter.jodConverter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jodconverter.core.office.OfficeException;

import io.github.awidesky.documentConverter.ConvertManager;
import io.github.awidesky.guiUtil.SwingDialogs;

public class JodConvertManager implements ConvertManager {

	private Function<File, IO> toIO = null;
	@Override
	public void setup(File outdir, boolean keepOriginalExtension, String format) {
		toIO = IOFactory.toExtension(outdir, keepOriginalExtension, format);		
	}
	@Override
	public boolean convert(List<File> inputs, Map<String, String> property, BiConsumer<File, File> updateUI) {
		AtomicBoolean ret = new AtomicBoolean(true);
		int processNum = 4;
		try {
			String s = property.get("sofficeProcess");
			if(s != null) processNum = Integer.parseInt(s);
		} catch(NumberFormatException e) {
			System.err.println(e.getMessage());
		}
		JodConvertUtil cu = new JodConvertUtil();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				cu.close();
			} catch (OfficeException e) {
				System.out.println("\n");
				System.err.println("Shutdown hook Exception :");
				e.printStackTrace();
			}
		}));
		try {
			cu.setup(Math.min(processNum, inputs.size()));
			cu.start();
			inputs.stream().parallel().map(toIO).forEach(io -> {
				updateUI.accept(io.getIn(), io.getOut());
				try {
					cu.convert(io);
				} catch (OfficeException e) {
					ret.set(false);
					e.printStackTrace();
					SwingDialogs.error("Failed to convert " + io.getIn().getName(), "%e%", e, false);
				}
			});
			
			cu.close();
		} catch (OfficeException e) {
			ret.set(false);
			e.printStackTrace();
			SwingDialogs.error("Failed to run converter", "%e%", e, false);
		}
		return ret.get();
	}

}
