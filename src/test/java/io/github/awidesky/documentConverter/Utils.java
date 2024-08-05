package io.github.awidesky.documentConverter;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.redsix.pdfcompare.CompareResultImpl;
import de.redsix.pdfcompare.PdfComparator;
import de.redsix.pdfcompare.RenderingException;

public class Utils {
	public static boolean comparePDF(File f1, File f2) {
		try {
			CompareResultImpl diff = new PdfComparator<CompareResultImpl>(f1, f2).compare();
			boolean ret = diff.isEqual();
			if(!ret) {
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
}
