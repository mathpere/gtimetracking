package com.googlecode.gtimetracking.vo;

import org.springframework.util.StringUtils;

public class Utils {

	public final static String unnull(String input) {
		return unnull(input, "");
	}

	public final static String unnull(String input, String defaultValue) {
		if (StringUtils.hasLength(input)) {
			return StringUtils.trimWhitespace(input);
		} else {
			return defaultValue;
		}
	}
}