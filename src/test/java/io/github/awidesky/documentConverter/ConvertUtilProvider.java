package io.github.awidesky.documentConverter;

import java.util.List;

import io.github.awidesky.documentConverter.jodConverter.JodConvertUtil;

public class ConvertUtilProvider {
	public static List<ConvertUtil> convertUtils() {
		return List.of(
				new SimpleConvertUtil(),
				new JodConvertUtil()
				);
	}
}
