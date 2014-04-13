/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.ArrayList;
import java.util.List;

/**
 * @author roach
 */
public class ArgonArgs {

	private static final char NOCODE = ' ';
	private static final char CODESEPARATOR = ':';

	static Tag newTag(String qTagSpec) {
		if (qTagSpec == null || qTagSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final int codePos = qTagSpec.indexOf(CODESEPARATOR);
		final String zTagName = codePos < 0 ? qTagSpec : qTagSpec.substring(0, codePos);
		final String zTagCode = codePos < 0 ? "" : qTagSpec.substring(codePos + 1);
		final int nameLen = zTagName.length();
		final int codeLen = zTagCode.length();
		if (nameLen > 0 && codeLen > 0) return new Tag(zTagName, zTagCode.charAt(0));
		if (nameLen > 0 && codeLen == 0) return new Tag(zTagName);
		if (nameLen == 0 && codeLen > 0) return new Tag(zTagCode.charAt(0));
		final String m = "Malformed Tag Spec '" + qTagSpec + "'";
		throw new IllegalArgumentException(m);
	}

	private ArgonArgsAccessor consumeAllTagValuePairs(Tag tag) {
		assert tag != null;
		final boolean hasCode = tag.hasCode();
		final List<String> zlqtwValues = new ArrayList<String>();
		int imatch = -1;
		for (int i = 0; i < m_ztqtw.length; i++) {
			final String oarg = m_ztqtw[i];
			if (oarg == null) {
				imatch = -1;
				continue;
			}
			final boolean isTag = oarg.charAt(0) == '-';
			if (imatch >= 0) {
				if (!isTag) {
					zlqtwValues.add(oarg);
					m_ztqtw[imatch] = null;
					m_ztqtw[i] = null;
				}
				imatch = -1;
			} else {
				if (isTag) {
					final int alen = oarg.length();
					if (alen >= 2 && oarg.substring(1).equals(tag.qtwName)) {
						imatch = i;
					} else if (alen >= 3 && oarg.charAt(1) == '-' && oarg.substring(2).equals(tag.qtwName)) {
						imatch = i;
					} else if (hasCode && alen == 2 && oarg.charAt(1) == tag.code) {
						imatch = i;
					}
				}
			}
		}
		return new ArgonArgsAccessor(tag.qtwName, zlqtwValues);
	}

	private boolean consumeFlag(char flagCode) {
		for (int i = 0; i < m_ztqtw.length; i++) {
			final String oarg = m_ztqtw[i];
			if (oarg == null) {
				continue;
			}
			if (oarg.charAt(0) != '-') {
				continue;
			}
			final int alen = oarg.length();
			if (alen >= 2 && oarg.charAt(1) == '-') {
				continue;
			}
			final String zPattern = oarg.substring(1);
			final int codePos = zPattern.indexOf(flagCode);
			if (codePos < 0) {
				continue;
			}
			final String zNeo = zPattern.substring(0, codePos) + zPattern.substring(codePos + 1);
			if (zNeo.length() == 0) {
				m_ztqtw[i] = null;
			} else {
				m_ztqtw[i] = "-" + zNeo;
			}
			return true;
		}
		return false;
	}

	boolean consumeFlag(Tag tag) {
		assert tag != null;
		for (int i = 0; i < m_ztqtw.length; i++) {
			final String oarg = m_ztqtw[i];
			if (oarg == null) {
				continue;
			}
			if (oarg.charAt(0) != '-') {
				continue;
			}
			final int alen = oarg.length();
			if (alen >= 2 && oarg.substring(1).equals(tag.qtwName)) {
				m_ztqtw[i] = null;
				return true;
			} else if (alen >= 3 && oarg.charAt(1) == '-' && oarg.substring(2).equals(tag.qtwName)) {
				m_ztqtw[i] = null;
				return true;
			}
		}
		if (tag.hasCode()) return consumeFlag(tag.code);
		return false;
	}

	public ArgonArgsAccessor consumeAllTagValuePairs(String qTagSpec) {
		return consumeAllTagValuePairs(newTag(qTagSpec));
	}

	public String[] consumeAllUntaggedValues() {
		final List<String> zlValues = new ArrayList<String>();
		boolean pretag = false;
		for (int i = 0; i < m_ztqtw.length; i++) {
			final String oarg = m_ztqtw[i];
			if (oarg == null) {
				pretag = false;
				continue;
			}
			if (pretag) {
				pretag = false;
			} else {
				if (oarg.charAt(0) == '-') {
					pretag = true;
				} else {
					zlValues.add(oarg);
					m_ztqtw[i] = null;
				}
			}
		}
		return zlValues.toArray(new String[zlValues.size()]);
	}

	public ArgonArgsAccessor consumeAllUntaggedValues(String label) {
		return new ArgonArgsAccessor(label, consumeAllUntaggedValues());
	}

	public boolean consumeFlag(String qTagSpec) {
		return consumeFlag(newTag(qTagSpec));
	}

	public String[] consumeRemainder() {
		final List<String> zl = new ArrayList<String>();
		for (int i = 0; i < m_ztqtw.length; i++) {
			final String oarg = m_ztqtw[i];
			if (oarg != null) {
				if (oarg.length() > 0) {
					zl.add(oarg);
				}
				m_ztqtw[i] = null;
			}
		}
		return zl.toArray(new String[zl.size()]);
	}

	public ArgonArgsAccessor consumeRemainder(String label) {
		return new ArgonArgsAccessor(label, consumeRemainder());
	}

	public boolean isEmpty() {
		for (int i = 0; i < m_ztqtw.length; i++) {
			if (m_ztqtw[i] != null) return false;
		}
		return true;
	}

	public ArgonArgs newRemainder() {
		return new ArgonArgs(m_ztqtw);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m_ztqtw.length; i++) {
			final String oarg = m_ztqtw[i];
			if (oarg != null) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append(oarg);
			}

		}
		return sb.toString();
	}

	public void verifyUnsupported()
			throws ArgonArgsException {
		final String[] zptqtwRem = consumeRemainder();
		if (zptqtwRem.length == 1) {
			final String m = "The argument '" + zptqtwRem[0] + "' is unsupported";
			throw new ArgonArgsException(m);
		}
		if (zptqtwRem.length > 1) {
			final String m = "The following arguments are unsupported..." + UArgon.msgComma(zptqtwRem);
			throw new ArgonArgsException(m);
		}
	}

	public ArgonArgs(String[] args) {
		if (args == null || args.length == 0) {
			m_ztqtw = new String[0];
		} else {
			final List<String> zlqtw = new ArrayList<String>(args.length);
			for (int i = 0; i < args.length; i++) {
				final String oarg = args[i];
				if (oarg != null) {
					final String ztw = oarg.trim();
					if (ztw.length() > 0) {
						zlqtw.add(ztw);
					}
				}
			}
			m_ztqtw = zlqtw.toArray(new String[zlqtw.size()]);
		}
	}

	private final String[] m_ztqtw;

	static class Tag {

		public boolean hasCode() {
			return this.code != NOCODE;
		}

		@Override
		public String toString() {
			return hasCode() ? (qtwName + ":" + code) : qtwName;
		}

		public Tag(char code) {
			this.qtwName = Character.toString(code);
			this.code = code;
		}

		public Tag(String qtwName) {
			assert qtwName != null && qtwName.length() > 0;
			this.qtwName = qtwName;
			this.code = NOCODE;
		}

		public Tag(String qtwName, char code) {
			assert qtwName != null && qtwName.length() > 0;
			this.qtwName = qtwName;
			this.code = code;
		}

		public final String qtwName;
		public final char code;
	}
}
