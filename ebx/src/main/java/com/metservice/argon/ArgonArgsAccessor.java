/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author roach
 */
public class ArgonArgsAccessor {

	private String msgAmbiguous() {
		return "Multiple values specified for '" + tagName + "'..." + UArgon.msgComma(m_zptqtwValues);
	}

	private String msgMissingRequired() {
		return "Missing required value for '" + tagName + "'";
	}

	private Date parseDateFromTX(String qtwValue)
			throws ArgonArgsException {
		try {
			return DateFactory.newDateFromTX(qtwValue);
		} catch (final ArgonFormatException ex) {
			final String m = "Value of '" + tagName + "' is not a valid T6/7/8 time..." + ex.getMessage();
			throw new ArgonArgsException(m);
		}
	}

	private Elapsed parseElapsed(String qtwValue)
			throws ArgonArgsException {
		try {
			return ElapsedFactory.newElapsed(qtwValue);
		} catch (final ArgonFormatException ex) {
			final String m = "Value of '" + tagName + "' is not a valid elapsed interval..." + ex.getMessage();
			throw new ArgonArgsException(m);
		}
	}

	private int parseInteger(String qtwValue)
			throws ArgonArgsException {
		try {
			return Integer.parseInt(qtwValue);
		} catch (final NumberFormatException ex) {
			final String m = "Value of '" + tagName + "' is not a valid integer";
			throw new ArgonArgsException(m);
		}
	}

	private Pattern parsePattern(String qtwValue)
			throws ArgonArgsException {
		try {
			return Pattern.compile(qtwValue);
		} catch (final PatternSyntaxException ex) {
			final String m = "Value of '" + tagName + "' is not a valid regular expression..." + ex.getMessage();
			throw new ArgonArgsException(m);
		}
	}

	public Date dateFromTXValue()
			throws ArgonArgsException {
		return parseDateFromTX(qtwValue());
	}

	public Elapsed elapsedValue()
			throws ArgonArgsException {
		return parseElapsed(qtwValue());
	}

	public Date getDateFromTXValue()
			throws ArgonArgsException {
		final String oqtwValue = oqtwValue();
		return oqtwValue == null ? null : parseDateFromTX(oqtwValue);
	}

	public Elapsed getElapsedValue()
			throws ArgonArgsException {
		final String oqtwValue = oqtwValue();
		return oqtwValue == null ? null : parseElapsed(oqtwValue);
	}

	public Integer getIntegerValue()
			throws ArgonArgsException {
		final String oqtwValue = oqtwValue();
		return oqtwValue == null ? null : parseInteger(oqtwValue);
	}

	public Pattern getPatternValue()
			throws ArgonArgsException {
		final String oqtwValue = oqtwValue();
		return oqtwValue == null ? null : parsePattern(oqtwValue);
	}

	public int integerValue()
			throws ArgonArgsException {
		return parseInteger(qtwValue());
	}

	public int integerValue(int defaultValue)
			throws ArgonArgsException {
		final Integer oValue = getIntegerValue();
		return oValue == null ? defaultValue : oValue.intValue();
	}

	public String oqtwValue()
			throws ArgonArgsException {
		if (m_zptqtwValues.length == 0) return null;
		if (m_zptqtwValues.length > 1) throw new ArgonArgsException(msgAmbiguous());
		return m_zptqtwValues[0];
	}

	public Pattern patternValue()
			throws ArgonArgsException {
		return parsePattern(qtwValue());
	}

	public String qtwValue()
			throws ArgonArgsException {
		if (m_zptqtwValues.length == 0) throw new ArgonArgsException(msgMissingRequired());
		if (m_zptqtwValues.length > 1) throw new ArgonArgsException(msgAmbiguous());
		return m_zptqtwValues[0];
	}

	public String qtwValue(String qtwDefault)
			throws ArgonArgsException {
		if (qtwDefault == null || qtwDefault.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String oqtwValue = oqtwValue();
		return oqtwValue == null ? qtwDefault : oqtwValue;
	}

	@Override
	public String toString() {
		return tagName + ":" + UArgon.msgComma(m_zptqtwValues);
	}

	public String[] xptqtwValues()
			throws ArgonArgsException {
		if (m_zptqtwValues.length == 0) throw new ArgonArgsException(msgMissingRequired());
		return m_zptqtwValues;
	}

	public String[] zptqtwValues() {
		return m_zptqtwValues;
	}

	ArgonArgsAccessor(String tagName, List<String> zlqtwValues) {
		if (tagName == null || tagName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (zlqtwValues == null) throw new IllegalArgumentException("object is null");
		this.tagName = tagName;
		m_zptqtwValues = zlqtwValues.toArray(new String[zlqtwValues.size()]);
	}

	ArgonArgsAccessor(String tagName, String[] zptqtwValues) {
		if (tagName == null || tagName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (zptqtwValues == null) throw new IllegalArgumentException("object is null");
		this.tagName = tagName;
		m_zptqtwValues = zptqtwValues;
	}

	public final String tagName;
	private final String[] m_zptqtwValues;
}
