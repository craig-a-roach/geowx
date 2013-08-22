/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;


/**
 * @author roach
 */
public class ArgonCharsetFactory {

	public static Charset find(String qncName) {
		if (qncName == null || qncName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String zuctw = qncName.trim().toUpperCase();
		if (zuctw.length() == 0) return null;
		try {
			return Charset.forName(zuctw);
		} catch (final UnsupportedCharsetException exUC) {
			return null;
		}
	}

	public static Charset select(String qncName)
			throws ArgonApiException {
		final Charset oCharset = find(qncName);
		if (oCharset == null) {
			final String m = "Unsupported charset '" + qncName
					+ "'; commonly used options include UTF-8, US-ASCII and ISO-8859-1";
			throw new ArgonApiException(m);
		}
		return oCharset;
	}

	public static Charset selectDefaultUTF8(String ozncName)
			throws ArgonApiException {
		if (ozncName == null) return UTF8;
		final String zuctw = ozncName.trim().toUpperCase();
		if (zuctw.length() == 0) return UTF8;
		return select(zuctw);
	}

	public static final String CHARSET_NAME_ASCII = "US-ASCII";

	public static final String CHARSET_NAME_UTF8 = "UTF-8";

	public static final String CHARSET_NAME_ISO8859 = "ISO-8859-1";

	public static final Charset ASCII = Charset.forName(CHARSET_NAME_ASCII);

	public static final Charset UTF8 = Charset.forName(CHARSET_NAME_UTF8);

	public static final Charset ISO8859_1 = Charset.forName(CHARSET_NAME_ISO8859);

}
