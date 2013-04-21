/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.metservice.argon.file.ArgonDirectoryManagement;
import com.metservice.argon.json.JsonDecoder;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class ArgonPropertiesAttribute {

	public File cndir()
			throws ArgonFormatException {
		try {
			return ArgonDirectoryManagement.cndir(qtwValue);
		} catch (final ArgonPermissionException ex) {
			throw new ArgonFormatException(invalid(ex));
		}
	}

	public File cndirReadable()
			throws ArgonFormatException {
		try {
			return ArgonDirectoryManagement.cndirEnsureReadable(qtwValue);
		} catch (final ArgonPermissionException ex) {
			throw new ArgonFormatException(invalid(ex));
		}
	}

	public File cndirUser()
			throws ArgonFormatException {
		try {
			return ArgonDirectoryManagement.cndirUser(qtwValue);
		} catch (final ArgonPermissionException ex) {
			throw new ArgonFormatException(invalid(ex));
		}
	}

	public File cndirUserReadable()
			throws ArgonFormatException {
		try {
			return ArgonDirectoryManagement.cndirEnsureUserReadable(qtwValue);
		} catch (final ArgonPermissionException ex) {
			throw new ArgonFormatException(invalid(ex));
		}
	}

	public File cndirUserWriteable()
			throws ArgonFormatException {
		try {
			return ArgonDirectoryManagement.cndirEnsureUserWriteable(qtwValue);
		} catch (final ArgonPermissionException ex) {
			throw new ArgonFormatException(invalid(ex));
		}
	}

	public File cndirWriteable()
			throws ArgonFormatException {
		try {
			return ArgonDirectoryManagement.cndirEnsureWriteable(qtwValue);
		} catch (final ArgonPermissionException ex) {
			throw new ArgonFormatException(invalid(ex));
		}
	}

	public int count()
			throws ArgonFormatException {
		final int len = qtwValue.length();
		try {
			int mul = 1;
			if (qtwValue.endsWith("K")) {
				mul = CArgon.K;
			} else if (qtwValue.endsWith("M")) {
				mul = CArgon.M;
			}
			final String zValue = qtwValue.substring(0, mul == 1 ? len : (len - 1));
			final String qValue = zValue.length() == 0 ? "1" : zValue;
			return Integer.parseInt(qValue) * mul;
		} catch (final NumberFormatException ex) {
			final String m = "Malformed '" + pname + "' value ''" + qtwValue + "'; expecting an integer";
			throw new ArgonFormatException(m);
		}
	}

	public int count(int lo, int hi)
			throws ArgonFormatException {
		final int c = count();
		if (c < lo) {
			final String m = "Value of '" + pname + "' is too small (" + c + ")... must be at least " + lo;
			throw new ArgonFormatException(m);
		}
		if (c > hi) {
			final String m = "Value of '" + pname + "' is too large (" + c + ")... must not exceed " + hi;
			throw new ArgonFormatException(m);
		}
		return c;
	}

	public boolean flag() {
		return Boolean.parseBoolean(qtwValue);
	}

	public String invalid(Throwable ex) {
		return "Invalid '" + pname + "' value '" + qtwValue + "'..." + ex.getMessage();
	}

	public JsonObject jsonObject()
			throws ArgonFormatException {
		try {
			return JsonDecoder.Default.decodeObject(qtwValue);
		} catch (final JsonSchemaException ex) {
			final String m = "Value of '" + pname + "' must be a JSON object";
			throw new ArgonFormatException(m);
		} catch (final ArgonFormatException ex) {
			final String m = "Value of '" + pname + "' must be a well-formed JSON object..." + ex.getMessage();
			throw new ArgonFormatException(m);
		}
	}

	public int ms()
			throws ArgonFormatException {
		final int len = qtwValue.length();
		try {
			int mul = 1;
			if (qtwValue.endsWith("s")) {
				mul = CArgon.SEC_TO_MS;
			} else if (qtwValue.endsWith("m")) {
				mul = CArgon.MIN_TO_MS;
			} else if (qtwValue.endsWith("h")) {
				mul = CArgon.HR_TO_MS;
			}
			final String zValue = qtwValue.substring(0, mul == 1 ? len : (len - 1));
			final String qValue = zValue.length() == 0 ? "1" : zValue;
			return Integer.parseInt(qValue) * mul;
		} catch (final NumberFormatException ex) {
			final String m = "Malformed '" + pname + "' value ''" + qtwValue + "'; expecting an integer";
			throw new ArgonFormatException(m);
		}
	}

	public int ms(int lo, int hi)
			throws ArgonFormatException {
		final int ms = ms();
		if (ms < lo) {
			final String m = "Value of '" + pname + "' is too short (" + ms + ")... must be at least " + lo;
			throw new ArgonFormatException(m);
		}
		if (ms > hi) {
			final String m = "Value of '" + pname + "' is too long (" + ms + ")... must not exceed " + hi;
			throw new ArgonFormatException(m);
		}
		return ms;
	}

	public Pattern opattern()
			throws ArgonFormatException {
		try {
			return qtwValue.equals("^") ? null : Pattern.compile(qtwValue);
		} catch (final PatternSyntaxException ex) {
			final String m = "Malformed '" + pname + "' value ''" + qtwValue + "'; expecting a valid regular expression  ("
					+ ex.getMessage() + ")";
			throw new ArgonFormatException(m);
		}
	}

	public int percent()
			throws ArgonFormatException {
		final int len = qtwValue.length();
		try {
			final String zValue = qtwValue.endsWith("%") ? qtwValue.substring(0, len - 1) : qtwValue;
			final String qValue = zValue.length() == 0 ? "100" : zValue;
			return Integer.parseInt(qValue);
		} catch (final NumberFormatException ex) {
			final String m = "Malformed '" + pname + "' value ''" + qtwValue + "'; expecting a percentage";
			throw new ArgonFormatException(m);
		}
	}

	public int percent(int lo, int hi)
			throws ArgonFormatException {
		final long p = percent();
		if (p < lo) {
			final String m = "Value of '" + pname + "' is too small (" + p + ")... must be at least " + lo;
			throw new ArgonFormatException(m);
		}
		if (p > hi) {
			final String m = "Value of '" + pname + "' is too large (" + p + ")... must not exceed " + hi;
			throw new ArgonFormatException(m);
		}
		return (int) p;
	}

	public int port()
			throws ArgonFormatException {
		return count(CArgon.LIMIT_PORT_USERLO, CArgon.LIMIT_PORT_HI);
	}

	@Override
	public String toString() {
		return pname + "=" + qtwValue;
	}

	public long ts()
			throws ArgonFormatException {
		try {
			return DateFactory.newTsFromT8(qtwValue);
		} catch (final ArgonFormatException ex) {
			final String m = "Malformed '" + pname + "' value ''" + qtwValue + "'; expecting a T8 timestamp ("
					+ ex.getMessage() + ")";
			throw new ArgonFormatException(m);
		}
	}

	public ArgonPropertiesAttribute(String pname, String qtwValue) {
		if (pname == null || pname.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qtwValue == null || qtwValue.length() == 0) throw new IllegalArgumentException("string is null or empty");
		this.pname = pname;
		this.qtwValue = qtwValue;
	}

	public final String pname;
	public final String qtwValue;
}
