package io.github.awidesky.documentConverter;

import java.awt.Desktop;
import java.io.File;
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
				File d = new File(f1.getParent(), f1.getName() + " _ " + f2.getName() +".pdf");
				diff.writeTo(d.getAbsolutePath());
				Desktop.getDesktop().open(d);
			}
			return ret;
		} catch (RenderingException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
}
