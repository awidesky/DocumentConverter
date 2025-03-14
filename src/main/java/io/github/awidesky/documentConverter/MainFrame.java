package io.github.awidesky.documentConverter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import io.github.awidesky.documentConverter.jodConverter.JodConvertManager;
import io.github.awidesky.guiUtil.SwingDialogs;
import io.github.awidesky.projectPath.JarPath;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -479357695189581321L;
	private static final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

	private final JLabel format = new JLabel("Format : ");
	private final JComboBox<String> cb_format = new JComboBox<>(new String[] {".pdf", ".odt", ".docx", ".pptx", ".ods", ".xlsx", ".odp", ".txt", ".rtf"});
	private final JCheckBox ck_keep = new JCheckBox("keep original extension in filename");
	private final JCheckBox ck_simpleImple = new JCheckBox("use simple soffice command");
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
	
	public void init() {
		setTitle("DocumentConverter " + Main.VERSION);
		try {
			File iconFile = new File(JarPath.getProjectPath(MainFrame.class, "icon") + File.separator + "icon" + File.separator + "docconv.png");
			if(iconFile.exists()) {
				BufferedImage ICON = ImageIO.read(iconFile);
				setIconImage(ICON);
				if(Taskbar.isTaskbarSupported()) Taskbar.getTaskbar().setIconImage(ICON);
			}
		} catch (IOException e) {
			SwingDialogs.error("Unable to find icon.png", "%e%", e, false);
			e.printStackTrace();
		}
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(500, 500);
		setLayout(new BorderLayout(5, 5));
		JPanel f = new JPanel();
		
		f.add(format);
		f.add(Box.createHorizontalStrut(10));
		cb_format.setEditable(true);
		f.add(cb_format);
		f.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		add(f, BorderLayout.CENTER);
		
		JPanel k = new JPanel();
		k.setLayout(new BoxLayout(k, BoxLayout.Y_AXIS));
		k.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		k.add(ck_keep);
		k.add(ck_simpleImple);
		add(k, BorderLayout.SOUTH);
		pack();
		setLocation(dim.width / 2 - getSize().width - jfc.getPreferredSize().width / 2, dim.height / 2 - getSize().height / 2);
		setVisible(true);
		start();
	}
	
	
	private ConvertManager converter;
	private boolean failedFlag = false;

	private int targets = 0;
	private int cnt = 0;
	private JLabel loadingStatus;
	private JProgressBar progress;
	private JFrame loadingFrame;
	
	public void start() {
		if(!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(this::start);
			return;
		}
		
		jfc.setMultiSelectionEnabled(true);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setDialogTitle("Choose document files");
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("All documents", 
				"docx", "doc", "hwp", "hwpx", "odt", "ott", "sxw", "wpd", "txt", "rtf", "html", "xlsx", "xls", 
				"csv", "tsv", "ods", "ots", "sxc" , "pptx", "ppt", "odp", "otp", "sxi", "docx", "doc", "xlsx", 
				"xls", "pptx", "ppt", "odt", "ott", "sxw", "ods", "ots", "sxc", "odp", "otp", "sxi", "hwp", "hwpx"));
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Text documents", 
				"docx", "doc", "hwp", "hwpx", "odt", "ott", "sxw", "wpd", "txt", "rtf", "html"));
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Spreadsheets",
				"xlsx", "xls", "csv", "tsv", "ods", "ots", "sxc"));
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Presentations", 
				"pptx", "ppt", "odp", "otp", "sxi"));
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Microsoft office",
				"docx", "doc", "xlsx", "xls", "pptx", "ppt"));
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("OpenDocument", 
				"odt", "ott", "sxw", "ods", "ots", "sxc", "odp", "otp", "sxi"));
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("아래한글 문서", "hwp", "hwpx"));
		jfc.setCurrentDirectory(new File(Main.getProperty("openDir", "")));
		
		
		List<File> ins = new ArrayList<>();
		while(jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			ins.addAll(Arrays.asList(jfc.getSelectedFiles()));
		}
		if(ins.isEmpty()) return;
		
		jfc.setCurrentDirectory(ins.get(0).getParentFile());
		jfc.setMultiSelectionEnabled(false);
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setDialogTitle("Choose directory to save pdfs!");
		jfc.resetChoosableFileFilters();
		
		if(jfc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) { return; }

		cb_format.setEnabled(false);
		ck_keep.setEnabled(false);
		if(ck_simpleImple.isSelected()) {
			converter = new SimpleConvertManager();
		} else {
			converter = new JodConvertManager();
		}
		targets = ins.size();
		
		File saveDir = jfc.getSelectedFile();
		while(!saveDir.isDirectory()) saveDir = saveDir.getParentFile();

		showProgress();
		
		converter.setup(saveDir, ck_keep.isSelected(), cb_format.getSelectedItem().toString());
		
		String savePath = saveDir.getAbsolutePath();
		Thread work = new Thread(() -> {
			Instant startTime = Instant.now();
			failedFlag = !converter.convert(ins, Main.getProperty(), this::updateUI);
			long milli = Duration.between(startTime, Instant.now()).toMillis();

			if (!failedFlag) {
				SwingDialogs.information("done!", "Task done in " + String.format("%d min %d.%03d sec",  milli / (60 * 1000), (milli / 1000) % 60, milli % 1000)
				+ "\nChanged files are in following folder :\n" + savePath, true);
			}

			if(loadingFrame != null) {
				loadingFrame.setVisible(false);
				loadingFrame.dispose();
			}
			dispose();
			Stream.of(Window.getWindows()).forEach(Window::dispose);
			System.exit(0);
		});
		work.setDaemon(true);
		work.start();
	}
	

	private void showProgress() {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		loadingFrame = new JFrame();
		loadingFrame.setTitle("Progress...");
		loadingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loadingFrame.setSize(420, 100);
		loadingFrame.setLocation(dim.width/2-loadingFrame.getSize().width/2, dim.height/2-loadingFrame.getSize().height/2);
		//loadingFrame.setLayout(null);
		loadingFrame.setResizable(true);
		
		loadingStatus = new JLabel(String.format("Initializing... - %0" + String.valueOf(targets).length() + "d/%" + String.valueOf(targets).length() + "d", 0, targets));
		
		progress = new JProgressBar();
		progress.setStringPainted(true);
		progress.setValue(0);
		
		JPanel p = new JPanel();
		p.setLayout(null);
		p.add(loadingStatus);
		p.add(progress);
		loadingFrame.add(p);

		progress.setBounds(15, 40, 390, 18);
		loadingStatus.setBounds(14, 8, 370, 18);
		
		loadingFrame.setVisible(true);
	}
	private synchronized void updateUI(File in, File out) {
		if(!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> updateUI(in, out));
			return;
		}
		loadingStatus.setText(String.format("%0" + String.valueOf(targets).length() + "d/%" + String.valueOf(targets).length() + "d    ", ++cnt, targets)
				+ in.getName() + " -> " + out.getName());
		progress.setValue((int) (100.0 * cnt / targets));
	}

}
