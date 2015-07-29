package com.intuit.paymentsapi.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class PropertyUtil {

	private static ResourceBundle appProperties;
	static {
		appProperties = ResourceBundle.getBundle("application", Locale.US);
	}

	public static String getValue(String key, Object... args) {
		String value = appProperties.getString(key);
		if (args != null) {
			value = MessageFormat.format(value, args);
		}
		return value;
	}
	
	public static String getValue(String key) {
		String value = appProperties.getString(key);
		return value;
	}

}
