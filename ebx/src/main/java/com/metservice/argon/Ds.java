/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author roach
 */
public class Ds {

	private static final int DefaultLineLimit = 100;

	private static final int DumpMaxLines = 100000;

	private static final AtomicInteger ExCounter = new AtomicInteger(0);

	private static void addConsequence(StringBuilder sb, String ozConsequently) {
		if (ozConsequently == null || ozConsequently.length() == 0) {
			sb.append("\nContainment handled at higher level");
		} else {
			sb.append("\nConsequently: ").append(ozConsequently);
		}
	}

	private static void addThrowable(StringBuilder sb, int depth, Throwable throwable, boolean forceStack) {
		if (sb == null) throw new IllegalArgumentException("object is null");
		final String c = throwable.getClass().getName();
		sb.append(c);
		sb.append('\n');

		final String ozMessage = throwable.getMessage();
		final String qMessage = (ozMessage == null || ozMessage.length() == 0) ? "No Message" : ozMessage;

		sb.append(qMessage);
		sb.append('\n');

		final boolean showStack;
		if (forceStack) {
			showStack = true;
		} else {
			if (throwable instanceof RuntimeException) {
				showStack = true;
			} else if (throwable instanceof IOException) {
				showStack = true;
			} else if (throwable instanceof ExecutionException) {
				showStack = true;
			} else {
				showStack = false;
			}
		}

		if (showStack) {
			final StackTraceElement[] ozptElements = throwable.getStackTrace();
			if (ozptElements != null) {
				for (int i = 0; i < ozptElements.length; i++) {
					final StackTraceElement ste = ozptElements[i];
					sb.append(ste.toString());
					sb.append('\n');
				}
			}
		}

		final Throwable oCause = throwable.getCause();
		if (oCause != null && oCause != throwable) {
			final int neoDepth = depth + 1;
			sb.append("Cause ").append(neoDepth).append(">\n");
			addThrowable(sb, neoDepth, oCause, forceStack);
			sb.append("<").append(neoDepth).append(" Cause\n");
		}
	}

	private static String dumpAsHex(byte[] ozptBytes, int maxDumpRows) {
		if (ozptBytes == null || ozptBytes.length == 0) return "";

		final int byteCount = ozptBytes.length;
		final StringBuilder b = new StringBuilder(512);

		b.append("dump (");
		int rc = ((byteCount - 1) / 16) + 1;
		if (rc > maxDumpRows) {
			rc = maxDumpRows;
			b.append(maxDumpRows * 16);
			b.append(" of ");
			b.append(byteCount);
		} else {
			b.append(byteCount);
		}
		b.append(")\n");
		int j = 0;
		for (int r = 0; r < rc; r++) {
			if (r > 0) {
				b.append("\n");
			}
			for (int c = 0; j < byteCount && c < 16; c++) {
				if (c > 0) {
					b.append(" ");
				}
				final int y = 0xff & ozptBytes[j];
				final String xs = Integer.toHexString(y);
				j++;
				if (xs.length() == 1) {
					b.append("0");
				}
				b.append(xs);
			}
		}
		return b.toString();
	}

	private static String dumpAsText(byte[] ozptBytes, int maxDumpRows) {
		if (ozptBytes == null || ozptBytes.length == 0) return "";
		final int byteCount = ozptBytes.length;
		final StringBuilder b = new StringBuilder(512);
		int j = 0;
		int r = 0;
		boolean lfPending = false;
		while (r < maxDumpRows && j < byteCount) {
			final char ch = (char) (ozptBytes[j]);
			if (ch == '\n' || ch == '\r') {
				b.append(ch);
				lfPending = true;
			} else {
				if (lfPending) {
					lfPending = false;
					r++;
				}
				if (r < maxDumpRows) {
					b.append(ch);
				}
			}
			j++;
		}
		if (j < byteCount) {
			b.append("(more..." + j + " of " + byteCount + ")");
		}
		return b.toString();
	}

	private static boolean isText(byte[] ozptBytes) {
		if (ozptBytes == null || ozptBytes.length == 0) return true;

		final int byteCount = ozptBytes.length;
		for (int i = 0; i < byteCount; i++) {
			final byte x = ozptBytes[i];
			if (x > 0x7e) return false;
			if (x < 0x20 && x != 0xd && x != 0xa && x != 0x9) return false;
		}
		return true;
	}

	public static String dump(byte[] ozptVal) {
		return dump(ozptVal, DumpMaxLines);
	}

	public static String dump(byte[] ozptVal, int maxLines) {
		if (isText(ozptVal)) return dumpAsText(ozptVal, maxLines);
		return dumpAsHex(ozptVal, maxLines);
	}

	public static String format(Throwable vex) {
		return format(vex, false);
	}

	public static String format(Throwable vex, boolean forceStack) {
		if (vex == null) return "Throwable is null";
		final StringBuilder sb = new StringBuilder();
		addThrowable(sb, 0, vex, forceStack);
		return sb.toString();
	}

	public static Ds g() {
		return new Ds("", null);
	}

	public static Ds invalidBecause(String reason, Class<? extends Throwable> throwClass) {
		if (throwClass == null) throw new IllegalArgumentException("object is null");
		final String csq = "Throw '" + throwClass.getName() + "' exception";
		return invalidBecause(reason, csq);
	}

	public static Ds invalidBecause(String reason, String ozConsequently) {
		if (reason == null) throw new IllegalArgumentException("object is null");
		final int counter = ExCounter.incrementAndGet();
		final StringBuilder sb = new StringBuilder();
		sb.append("Exception ").append(counter).append("\nFailed validation check because '").append(reason).append("'.");
		addConsequence(sb, ozConsequently);
		sb.append("\nDetails");
		return new Ds(sb.toString(), "Exception " + counter);
	}

	public static String message(Throwable vex) {
		if (vex == null) return "No Message";
		final String ozMessage = vex.getMessage();
		if (ozMessage != null && ozMessage.length() > 0) return ozMessage;
		return vex.getClass().getSimpleName();
	}

	public static Ds o(Class<?> cls) {
		if (cls == null) throw new IllegalArgumentException("object is null");
		return new Ds(cls.getSimpleName(), null);
	}

	public static Ds o(String className) {
		if (className == null || className.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return new Ds(className, null);
	}

	public static Ds report(String zHeadline) {
		if (zHeadline == null) throw new IllegalArgumentException("object is null");
		return new Ds(zHeadline, null);
	}

	public static Ds triedTo(String attempted, Throwable cause) {
		return triedTo(attempted, cause, "");
	}

	public static Ds triedTo(String attempted, Throwable cause, Class<? extends Throwable> throwClass) {
		if (throwClass == null) throw new IllegalArgumentException("object is null");
		final String csq = "Throw '" + throwClass.getName() + "' exception";
		return triedTo(attempted, cause, csq);
	}

	public static Ds triedTo(String attempted, Throwable cause, String zConsequently) {
		if (attempted == null) throw new IllegalArgumentException("object is null");
		if (zConsequently == null) throw new IllegalArgumentException("object is null");
		final int counter = ExCounter.incrementAndGet();
		final StringBuilder sb = new StringBuilder();
		sb.append("Exception ").append(counter).append("\nAttempt to '").append(attempted).append("' has failed.");
		addConsequence(sb, zConsequently);
		sb.append("\nDetails");
		final Ds ds = new Ds(sb.toString(), "Exception " + counter);
		ds.a("cause", cause);
		return ds;
	}

	private void add(String tag, String zval) {
		if (tag == null || tag.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (zval == null) throw new IllegalArgumentException("object is null");
		if (!m_multiLine) {
			if (zval.indexOf('\n') >= 0) {
				m_multiLine = true;
			} else {
				final int pl = 2 + tag.length() + 1 + zval.length();
				m_flatLen += pl;
				if (m_flatLen > m_lineLimit) {
					m_multiLine = true;
				}
			}
		}
		m_zlt.add(tag);
		m_zlv.add(zval);
	}

	private String outMulti() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_zHeadline).append("{\n");
		final int len = m_zlt.size();
		for (int i = 0; i < len; i++) {
			sb.append(m_zlt.get(i));
			sb.append("=");
			sb.append(m_zlv.get(i));
			sb.append("\n");
		}
		sb.append("}").append(m_zEnd);
		return sb.toString();
	}

	private String outSingle() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_zHeadline).append("{");
		final int len = m_zlt.size();
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				sb.append("  ");
			}
			sb.append(m_zlt.get(i));
			sb.append("=");
			sb.append(m_zlv.get(i));
		}
		sb.append("}");
		return sb.toString();
	}

	public Ds a(String tag, byte val) {
		add(tag, "0x" + Integer.toHexString(val & 0xFF));
		return this;
	}

	public Ds a(String tag, byte[] ozptVal) {
		return a(tag, ozptVal, DumpMaxLines);
	}

	public Ds a(String tag, byte[] ozptVal, int maxLines) {
		if (ozptVal == null) {
			add(tag, "null");
		} else {
			add(tag, dump(ozptVal, maxLines));
		}
		return this;
	}

	public Ds a(String tag, double[] oVal) {
		if (oVal == null) {
			add(tag, "null");
		} else {
			for (int i = 0; i < oVal.length; i++) {
				a(tag + "[" + i + "]", oVal[i]);
			}
		}
		return this;
	}

	public Ds a(String tag, int val) {
		add(tag, Integer.toString(val));
		return this;
	}

	public Ds a(String tag, int[] oVal) {
		if (oVal == null) {
			add(tag, "null");
		} else {
			for (int i = 0; i < oVal.length; i++) {
				a(tag + "[" + i + "]", oVal[i]);
			}
		}
		return this;
	}

	public Ds a(String tag, List<?> oVal) {
		if (oVal == null) {
			add(tag, "null");
		} else {
			final int len = oVal.size();
			for (int i = 0; i < len; i++) {
				a(tag + "(" + i + ")", oVal.get(i));
			}
		}
		return this;
	}

	public Ds a(String tag, long[] oVal) {
		if (oVal == null) {
			add(tag, "null");
		} else {
			for (int i = 0; i < oVal.length; i++) {
				a(tag + "[" + i + "]", oVal[i]);
			}
		}
		return this;
	}

	public Ds a(String tag, Map<?, ?> oVal) {
		if (oVal == null) {
			add(tag, "null");
		} else {
			final int len = oVal.size();
			final Map<String, Object> zmk = new HashMap<String, Object>(len);
			for (final Object k : oVal.keySet()) {
				zmk.put(k.toString(), k);
			}
			final List<String> ksAsc = new ArrayList<String>(zmk.keySet());
			Collections.sort(ksAsc);
			for (final String ks : ksAsc) {
				final Object k = zmk.get(ks);
				final Object o = oVal.get(k);
				a(tag + "(" + ks + ")", o);
			}
		}
		return this;
	}

	public Ds a(String tag, Object oVal) {
		if (oVal == null) {
			add(tag, "null");
		} else {
			add(tag, oVal.toString());
		}
		return this;
	}

	public Ds a(String tag, Object[] oVal) {
		if (oVal == null) {
			add(tag, "null");
		} else {
			for (int i = 0; i < oVal.length; i++) {
				a(tag + "[" + i + "]", oVal[i]);
			}
		}
		return this;
	}

	public Ds a(String tag, Set<?> oVal) {
		if (oVal == null) {
			add(tag, "null");
		} else {
			final int len = oVal.size();
			final List<String> zlzAsc = new ArrayList<String>(len);
			for (final Object e : oVal) {
				zlzAsc.add(e.toString());
			}
			Collections.sort(zlzAsc);
			final StringBuilder sb = new StringBuilder();
			sb.append('(');
			for (int i = 0; i < len; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append('\n');
				sb.append(zlzAsc.get(i));
			}
			sb.append(')');
			add(tag, sb.toString());
		}
		return this;
	}

	public Ds a(String tag, String ozVal) {
		if (ozVal == null) {
			add(tag, "null");
		} else {
			add(tag, "'" + ozVal + "'");
		}
		return this;
	}

	public Ds a(String tag, Throwable oVal) {
		if (oVal == null) {
			add(tag, "null");
		} else {
			add(tag, format(oVal, false));
		}
		return this;
	}

	public Ds aclass(String tag, Object oVal) {
		if (oVal == null) {
			add(tag, "null");
		} else {
			add(tag, oVal.getClass().getName());
		}
		return this;
	}

	public Ds ae(String tag, Elapsed oElapsed) {
		if (oElapsed == null) {
			add(tag, "null");
		} else {
			add(tag, ElapsedFormatter.formatMixedUnits(oElapsed.sms));
		}
		return this;
	}

	public Ds ae(String tag, long sms) {
		return a(tag, ElapsedFormatter.formatMixedUnits(sms));
	}

	public Ds afileinfo(String tag, File oVal) {
		if (oVal == null) {
			add(tag, "null");
		} else {
			final StringBuilder ba = new StringBuilder();
			ba.append("r" + (oVal.canRead() ? "+" : "-"));
			ba.append("w" + (oVal.canWrite() ? "+" : "-"));
			ba.append("x" + (oVal.canExecute() ? "+" : "-"));
			final String usk = (oVal.getUsableSpace() / 1024) + "kB";
			String canonPath = "n/a";
			try {
				canonPath = oVal.getCanonicalPath();
			} catch (final IOException ex) {
			}

			add(tag + ".path", oVal.getPath());
			add(tag + ".canonPath", canonPath);
			add(tag + ".atts", ba.toString());
			if (oVal.isFile()) {
				add(tag + ".length", (ba.length() + "bytes"));
			}
			if (oVal.isDirectory()) {
				final String[] ozptMembers = oVal.list();
				final int memberCount = ozptMembers == null ? 0 : ozptMembers.length;
				add(tag + ".memberCount", Integer.toString(memberCount));
			}
			add(tag + ".lastModified", DateFormatter.newT8FromTs(oVal.lastModified()));
			add(tag + ".usableSpace", usk);
		}
		return this;
	}

	public Ds at8(String tag, Date oDate) {
		if (oDate == null) {
			add(tag, "null");
		} else {
			add(tag, DateFormatter.newT8FromDate(oDate));
		}
		return this;
	}

	public Ds at8(String tag, long ts) {
		return a(tag, DateFormatter.newT8FromTs(ts));
	}

	public Ds atz(String tag, TimeZone oTz) {
		if (oTz == null) {
			add(tag, "null");
		} else {
			add(tag, TimeZoneFormatter.id(oTz));
		}
		return this;
	}

	public Ds ausername() {
		add("system.userName", UArgon.qUserName());
		return this;
	}

	public String s() {
		return m_multiLine ? outMulti() : outSingle();
	}

	public void setLineLimit(int charCount) {
		m_lineLimit = charCount;
	}

	public String sm() {
		return outMulti();
	}

	public String ss() {
		return outSingle();
	}

	@Override
	public String toString() {
		return s();
	}

	private Ds(String zHeadline, String ozEnd) {
		assert zHeadline != null;
		m_zHeadline = zHeadline;
		m_zEnd = ozEnd == null ? zHeadline : ozEnd;
		m_lineLimit = DefaultLineLimit;
	}

	private final String m_zHeadline;
	private final String m_zEnd;
	private final List<String> m_zlt = new ArrayList<String>();
	private final List<String> m_zlv = new ArrayList<String>();
	private boolean m_multiLine;
	private int m_flatLen;
	private int m_lineLimit;
}
