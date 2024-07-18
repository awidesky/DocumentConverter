package io.github.awidesky.documentConverter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.jodconverter.core.office.OfficeException;

import io.github.awidesky.documentConverter.IOPair.IO;
import io.github.awidesky.guiUtil.SwingDialogs;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -479357695189581321L;
	private static final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

	private final JLabel format = new JLabel("Format : ");
	private final JComboBox<String> cb_format = new JComboBox<>(new String[] {".pdf", ".odt", ".docx", "pptx", ".ods", ".xlsx", "odp", ".txt", ".rtf"});
	private final JCheckBox ck_keep = new JCheckBox("keep original extension in filename");
	private final JFileChooser jfc = new JFileChooser() {
		private static final long serialVersionUID = 1838574539723650634L;

		@Override
		protected JDialog createDialog(Component parent) throws HeadlessException {
			JDialog d = super.createDialog(parent);
			d.setModalityType(ModalityType.DOCUMENT_MODAL);
			d.setAlwaysOnTop(true);
			return d;
		}
	};
	
	public MainFrame() {
		setTitle("DocumentConverter " + Main.VERSION);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(500, 500);
		setLayout(new BorderLayout(5, 5));
		JPanel f = new JPanel();
		f.add(format);
		f.add(Box.createHorizontalStrut(10));
		f.add(cb_format);
		f.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		add(f, BorderLayout.CENTER);
		
		JPanel k = new JPanel();
		k.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		k.add(ck_keep);
		add(k, BorderLayout.SOUTH);
		pack();
		setLocation(dim.width / 2 - getSize().width - jfc.getPreferredSize().width / 2, dim.height / 2 - getSize().height / 2);
		setVisible(true);
		start();
		dispose();
	}
	
	

	private Function<File, IO> toIO = null;
	private boolean failedFlag = false;

	private int targets = 0;
	private int cnt = 0;
	private JLabel loadingStatus;
	private JProgressBar progress;
	private JFrame loadingFrame;
	
	public void start() {
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
		
		targets = ins.length;
		
		
		cb_format.setEnabled(false);
		ck_keep.setEnabled(false);
		
		File saveDir = jfc.getSelectedFile();
		toIO = IOFactory.toExtension(saveDir, ck_keep.isSelected(), cb_format.getSelectedItem().toString());
		
		
		SwingUtilities.invokeLater(this::showProgress);
		
		Instant startTime = Instant.now();
		ConvertUtil cu = new ConvertUtil();
		try {
			cu.start();
			Arrays.stream(ins).map(toIO).forEach(io -> {
				SwingUtilities.invokeLater(() -> updateUI(io.getIn(), io.getOut()));
				try {
					cu.convert(io);
				} catch (OfficeException e) {
					setFailedFlag();
					e.printStackTrace();
					SwingDialogs.error("Failed to convert " + io.getIn().getName(), "%e%", e, false);
				} finally {
				}
			});
			cu.close();
		} catch (OfficeException e) {
			setFailedFlag();
			e.printStackTrace();
			SwingDialogs.error("Failed to run converter", "%e%", e, false);
		}
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

	private void showProgress() {

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
	private void updateUI(File in, File out) {
		loadingStatus.setText(String.format("%0" + String.valueOf(targets).length() + "d/%" + String.valueOf(targets).length() + "d    ", ++cnt, targets)
				+ in.getName() + " -> " + out.getName());
		progress.setValue((int) (100.0 * cnt / targets));
	}

	private synchronized void setFailedFlag() {
		failedFlag = true;
	}
}
