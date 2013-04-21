/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.metservice.argon.ArgonApiException;

/**
 * @author roach
 */
public class ArgonProbeFilter {

	private static final Pattern SPLITTER = Pattern.compile(",");
	private static final Pattern SPEC = Pattern.compile("(\\w+)[\\s]*=[\\s]*(.+)");
	private static final Pattern WILD = Pattern.compile(".*");

	private static boolean accept(Pattern oPattern, String qKeyword) {
		return oPattern != null && oPattern.matcher(qKeyword).matches();
	}

	public boolean fail(String qKeyword) {
		return accept(m_oFail, qKeyword);
	}

	public boolean info(String qKeyword) {
		return accept(m_oInfo, qKeyword);
	}

	public boolean live(String qKeyword) {
		return accept(m_oLive, qKeyword);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (m_oFail != null) {
			sb.append("fail=").append(m_oFail);
		}
		if (m_oWarn != null) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append("warn=").append(m_oWarn);
		}
		if (m_oInfo != null) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append("info=").append(m_oInfo);
		}
		if (m_oLive != null) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append("live=").append(m_oLive);
		}
		return sb.toString();
	}

	public boolean warn(String qKeyword) {
		return accept(m_oWarn, qKeyword);
	}

	public ArgonProbeFilter(boolean fail, boolean warn, boolean info, boolean live) {
		m_oFail = fail ? WILD : null;
		m_oWarn = warn ? WILD : null;
		m_oInfo = info ? WILD : null;
		m_oLive = live ? WILD : null;
	}

	public ArgonProbeFilter(String ozPattern) throws ArgonApiException {
		Pattern oFail = null;
		Pattern oWarn = null;
		Pattern oInfo = null;
		Pattern oLive = null;

		if (ozPattern != null && ozPattern.length() > 0) {
			final String[] zptSpecs = SPLITTER.split(ozPattern);
			for (int i = 0; i < zptSpecs.length; i++) {
				final String ztwSpec = zptSpecs[i].trim();
				if (ztwSpec.length() == 0) {
					continue;
				}
				final Matcher specMatcher = SPEC.matcher(ztwSpec);
				if (!specMatcher.matches()) throw new ArgonApiException("Malformed filter pattern '" + ozPattern + "'");
				final String zlctwType = specMatcher.group(1).trim().toLowerCase();
				final String ztwPattern = specMatcher.group(2).trim();
				if (ztwPattern.length() == 0) {
					continue;
				}
				try {
					if (zlctwType.startsWith("fail")) {
						oFail = Pattern.compile(ztwPattern);
					} else if (zlctwType.startsWith("warn")) {
						oWarn = Pattern.compile(ztwPattern);
					} else if (zlctwType.startsWith("info")) {
						oInfo = Pattern.compile(ztwPattern);
					} else if (zlctwType.startsWith("live")) {
						oLive = Pattern.compile(ztwPattern);
					} else
						throw new ArgonApiException("Invalid severity keyword '" + zlctwType + "' in pattern '"
								+ ozPattern + "'");
				} catch (final PatternSyntaxException exPS) {
					throw new ArgonApiException("Invalid '" + zlctwType + "' expression in pattern '" + ozPattern + "'\n"
							+ exPS.getMessage());
				}
			}
		}
		this.m_oFail = oFail;
		this.m_oWarn = oWarn;
		this.m_oInfo = oInfo;
		this.m_oLive = oLive;
	}

	private final Pattern m_oFail;
	private final Pattern m_oWarn;
	private final Pattern m_oInfo;
	private final Pattern m_oLive;
}
