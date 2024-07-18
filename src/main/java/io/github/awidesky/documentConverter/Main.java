package io.github.awidesky.documentConverter;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.function.Function;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.jodconverter.core.office.OfficeException;

import io.github.awidesky.documentConverter.IOPair.IO;
import io.github.awidesky.guiUtil.SwingDialogs;

public class Main {
	
	private static Function<File, IO> toIO = null;
	private static boolean failedFlag = false;
	private static String extension = ".pdf";

	private static int targets = 0;
	private static int cnt = 0;
	private static JLabel loadingStatus;
	private static JProgressBar progress;
	private static JFrame loadingFrame;
	
	public static void main(String[] args) {
		JFileChooser jfc = new JFileChooser();
		jfc.setMultiSelectionEnabled(true);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setDialogTitle("Choose document files");
		
		if(jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) { return; }
		
		File[] ins = jfc.getSelectedFiles();
		
		jfc.setCurrentDirectory(ins[0].getParentFile());
		jfc.setMultiSelectionEnabled(false);
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setDialogTitle("Choose directory to save pdfs!");
		
		if(jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) { return; }
		
		File saveDir = jfc.getSelectedFile();
		toIO = IOFactory.toExtension(saveDir, extension);
		
		targets = ins.length;
		
		SwingUtilities.invokeLater(Main::showProgress);
		
		Instant startTime = Instant.now();
		ConvertUtil cu = new ConvertUtil();
		Arrays.stream(ins).parallel().map(toIO).forEach(io ->{
			try {
				cu.convert(io);
			} catch (OfficeException e) {
				setFailedFlag();
				e.printStackTrace();
				SwingDialogs.error("Failed to convert " + io.getIn().getName(), "%e%", e, false);
			} finally {
				SwingUtilities.invokeLater(Main::updateUI);
			}
		});
		long milli = Duration.between(startTime, Instant.now()).toMillis();
		
		if (!failedFlag) {
			SwingDialogs.information("done!", "Task done in " + String.format("%d min %d.%03d sec",  milli / (60 * 1000), (milli / 1000) % 60, milli % 1000)
			+ "\nChanged files are in following folder :\n" + saveDir.getAbsolutePath(), true);
		}
		
		SwingUtilities.invokeLater(() -> {
			loadingFrame.setVisible(false);
			loadingFrame.dispose();
		});
	}
	
//	public static String getTargetExtention() {
//		return extention;
//	}

	private static void showProgress() {

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		loadingFrame = new JFrame();
		loadingFrame.setTitle("Progress...");
		loadingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loadingFrame.setSize(420, 100);
		loadingFrame.setLocation(dim.width/2-loadingFrame.getSize().width/2, dim.height/2-loadingFrame.getSize().height/2);
		loadingFrame.setLayout(null);
		loadingFrame.setResizable(false);
		
		loadingStatus = new JLabel(String.format("%0" + String.valueOf(targets).length() + "d/%" + String.valueOf(targets).length() + "d", 0, targets));
		loadingStatus.setBounds(14, 8, 370, 18);
		
		progress = new JProgressBar();
		progress.setStringPainted(true);
		progress.setBounds(15, 27, 370, 18);
		
		loadingFrame.add(loadingStatus);
		loadingFrame.add(progress);
		loadingFrame.setVisible(true);
		
	}
	private static void updateUI() {
		loadingStatus.setText(String.format("%0" + String.valueOf(targets).length() + "d/%" + String.valueOf(targets).length() + "d", ++cnt, targets));
		progress.setValue((int) (100.0 * cnt / targets));
	}

	private synchronized static void setFailedFlag() {
		failedFlag = true;
	}
}
