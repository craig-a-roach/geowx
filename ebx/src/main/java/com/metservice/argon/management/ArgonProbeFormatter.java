/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonNumber;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.DateFormatter;
import com.metservice.argon.IArgonSpaceId;

/**
 * @author roach
 */
public class ArgonProbeFormatter {

	private void appendIndentedMessage(StringBuilder sb, String message) {
		if (message.length() == 0) return;
		if (isMultiLine(message)) {
			sb.append("...\n").append(indented(message, ". "));
			sb.append("\n\n");
		} else {
			sb.append("| ").append(message);
		}
	}

	private String indented(String zsrc, String zpad) {
		final int len = zsrc.length();
		final StringBuilder sb = new StringBuilder(len * 5 / 4);
		sb.append(zpad);
		for (int i = 0; i < len; i++) {
			final char ch = zsrc.charAt(i);
			if (ch == '\r') {
				continue;
			}
			sb.append(ch);
			if (ch == '\n') {
				sb.append(zpad);
			}
		}
		return sb.toString();
	}

	private boolean isMultiLine(String message) {
		return message.indexOf('\n') >= 0;
	}

	private String tsString(long ts) {
		String zRelative = "";
		if (m_includeRelativeTime) {
			final long smsRelative = ts - m_tsBase;
			final String sgn = smsRelative < 0 ? "-" : "+";
			final long msRelative = Math.abs(smsRelative);
			final long secs = msRelative / 1000L;
			final long msResidual = msRelative - (secs * 1000L);
			zRelative = sgn + secs + "." + ArgonNumber.longToDec(msResidual, 3);
		}

		String zAbsolute = "";
		if (m_includeT8Time && m_includePlatformTime) {
			zAbsolute = DateFormatter.newT8PlatformDHMFromTs(ts);
		} else {
			if (m_includeT8Time) {
				zAbsolute = DateFormatter.newT8FromTs(ts);
			} else if (m_includePlatformTime) {
				zAbsolute = DateFormatter.newPlatformDHMSTYFromTs(ts);
			}
		}
		if (zAbsolute.length() == 0) return zRelative;
		return zRelative + "@" + zAbsolute;
	}

	public String console(long ts, String type, String keyword, String message) {
		final StringBuilder sb = new StringBuilder(256);
		sb.append(m_qDomain).append('.').append(m_qId);
		sb.append(' ').append(type).append(' ').append(keyword);
		sb.append(' ').append(tsString(ts));
		appendIndentedMessage(sb, message);
		return sb.toString();
	}

	public String jmx(String keyword, String message) {
		final boolean noMessage = message.length() == 0;
		return noMessage ? keyword : (keyword + "\n" + message);
	}

	public String logger(long ts, String type, String keyword, String message) {
		final StringBuilder sb = new StringBuilder(256);
		sb.append(m_qId);
		sb.append(' ').append(type).append(' ').append(keyword);
		sb.append(' ').append(tsString(ts));
		appendIndentedMessage(sb, message);
		return sb.toString();
	}

	public static ArgonProbeFormatter newInstance(ArgonServiceId sid, IArgonSpaceId idSpace) {
		return newInstance(sid, idSpace, true, true, true);
	}

	public static ArgonProbeFormatter newInstance(ArgonServiceId sid, IArgonSpaceId idSpace, boolean inAbsoluteTime,
			boolean inPlatformTime, boolean inRelativeTime) {
		if (sid == null) throw new IllegalArgumentException("object is null");
		if (idSpace == null) throw new IllegalArgumentException("object is null");
		return new ArgonProbeFormatter(sid.qtwDomain, idSpace.format(), inAbsoluteTime, inPlatformTime, inRelativeTime);
	}

	private ArgonProbeFormatter(String qDomain, String qId, boolean inT8Time, boolean inPlatformTime, boolean inRelativeTime) {
		if (qDomain == null || qDomain.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qId == null || qId.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_qDomain = qDomain;
		m_qId = qId;
		m_includeT8Time = inT8Time;
		m_includePlatformTime = inPlatformTime;
		m_includeRelativeTime = inRelativeTime;
		m_tsBase = ArgonClock.tsNow();
	}

	private final String m_qDomain;
	private final String m_qId;
	private final boolean m_includeT8Time;
	private final boolean m_includePlatformTime;
	private final boolean m_includeRelativeTime;
	private final long m_tsBase;
}
