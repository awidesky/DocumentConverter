package io.github.awidesky.documentConverter;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import de.redsix.pdfcompare.CompareResultImpl;
import de.redsix.pdfcompare.PdfComparator;
import de.redsix.pdfcompare.RenderingException;
import io.github.awidesky.documentConverter.jodConverter.IO;

public class Utils {
	public static boolean comparePDF(File f1, File f2) {
		try {
			CompareResultImpl diff = new PdfComparator<CompareResultImpl>(f1, f2).compare();
			boolean ret = diff.isEqual();
			if(!ret) {
				System.out.println();
				System.out.println("diff 1 : " + f1.getName());
				System.out.println("diff 2 : " + f2.getName());
				Desktop.getDesktop().open(f1);
				Desktop.getDesktop().open(f2);
				File d = new File(f1.getParent(), "diff_of-" + f1.getName() + "_" + f2.getName() +".pdf");
				diff.writeTo(new FileOutputStream(d));
				if(d.exists()) {
					Desktop.getDesktop().open(d);
				} else {
					System.out.println("Somehow diff file " + d.getAbsolutePath() + " does not exist!");
					System.out.println("Diff JSON : ");
					System.out.println(diff.getDifferencesJson().replace("\n", "\n\t"));
				}
			}
			return ret;
		} catch (RenderingException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private static final File outdir = TestResourcePath.getResource("output");
	public static File outDir() {
		return outdir;
	}
	
	public static void clearOutput() {
		if(!outdir.exists()) outdir.mkdirs();
		
		Arrays.stream(outdir.listFiles())
			.parallel()
			.forEach(File::delete);
	}
	
	public static IO toIO(File f) {
		return new IO(f, outdir, IO.changeExtension(f, ".pdf"));
	}
}
