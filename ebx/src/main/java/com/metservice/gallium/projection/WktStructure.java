/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author roach
 */
class WktStructure {

	public static final String DefaultTab = "  ";
	public static final int DefaultFlatLimit = 80;

	private void emitFlat(StringBuilder dst) {
		final int tcount = m_terms.size();
		dst.append(m_keyword);
		dst.append('[');
		for (int i = 0; i < tcount; i++) {
			final ITerm t = m_terms.get(i);
			if (i > 0) {
				dst.append(',');
			}
			if (t instanceof TextTerm) {
				final String value = ((TextTerm) t).value;
				dst.append(value);
				continue;
			}
			if (t instanceof StructureTerm) {
				final WktStructure value = ((StructureTerm) t).value;
				value.emitFlat(dst);
				continue;
			}
		}
		dst.append(']');
	}

	private void emitMulti(StringBuilder dst, int depth, String tab, int flatLimit) {
		final int tcount = m_terms.size();
		if (depth > 0) {
			indent(dst, depth, tab);
		}
		dst.append(m_keyword);
		dst.append('[');
		for (int i = 0; i < tcount; i++) {
			final ITerm t = m_terms.get(i);
			if (i > 0) {
				dst.append(',');
			}
			if (t instanceof TextTerm) {
				final String value = ((TextTerm) t).value;
				dst.append(value);
				continue;
			}
			if (t instanceof StructureTerm) {
				final WktStructure value = ((StructureTerm) t).value;
				if (value.m_charCount > flatLimit) {
					value.emitMulti(dst, depth + 1, tab, flatLimit);
				} else {
					indent(dst, depth, tab);
					value.emitFlat(dst);
				}
				continue;
			}
		}
		indent(dst, depth, tab);
		dst.append(']');
	}

	private void indent(StringBuilder dst, int depth, String tab) {
		dst.append('\n');
		if (tab != null && tab.length() == 0) return;
		for (int i = 0; i < depth; i++) {
			dst.append(tab);
		}
	}

	public String format() {
		return format(DefaultTab, DefaultFlatLimit);
	}

	public String format(String tab, int flatLimit) {
		final StringBuilder sb = new StringBuilder(512);
		emitMulti(sb, 0, tab, flatLimit);
		return sb.toString();
	}

	public String formatFlat() {
		final StringBuilder sb = new StringBuilder(512);
		emitFlat(sb);
		return sb.toString();
	}

	@Override
	public String toString() {
		return format();
	}

	public WktStructure(String keyword, Object... zptTerms) {
		if (keyword == null || keyword.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final int inCount = zptTerms.length;
		m_keyword = keyword;
		m_terms = new ArrayList<>(inCount);
		for (int i = 0; i < inCount; i++) {
			final Object oTerm = zptTerms[i];
			if (oTerm == null) {
				continue;
			}
			if ((oTerm instanceof Double) || (oTerm instanceof Integer)) {
				final String text = oTerm.toString();
				m_terms.add(new TextTerm(text));
				continue;
			}
			if (oTerm instanceof Title) {
				final Title t = (Title) oTerm;
				final String text = "\"" + t.toString() + "\"";
				m_terms.add(new TextTerm(text));
				continue;
			}
			if (oTerm instanceof String) {
				final String text = "\"" + oTerm.toString() + "\"";
				m_terms.add(new TextTerm(text));
				continue;
			}
			if (oTerm instanceof IWktEmit) {
				final IWktEmit we = (IWktEmit) oTerm;
				final WktStructure ws = we.toWkt();
				m_terms.add(new StructureTerm(ws));
				continue;
			}
			if (oTerm instanceof IWktList) {
				final IWktList wl = (IWktList) oTerm;
				final List<? extends IWktEmit> listEmit = wl.listEmit();
				final int ecount = listEmit.size();
				for (int ei = 0; ei < ecount; ei++) {
					final IWktEmit we = listEmit.get(ei);
					final WktStructure ws = we.toWkt();
					m_terms.add(new StructureTerm(ws));
				}
				continue;
			}
			if (oTerm instanceof WktStructure) {
				final WktStructure ws = (WktStructure) oTerm;
				m_terms.add(new StructureTerm(ws));
				continue;
			}
			if (oTerm instanceof WktStructure[]) {
				final WktStructure[] wsa = (WktStructure[]) oTerm;
				for (int ai = 0; ai < wsa.length; ai++) {
					m_terms.add(new StructureTerm(wsa[ai]));
				}
				continue;
			}
		}
		final int termCount = m_terms.size();
		int charCount = m_keyword.length();
		for (int i = 0; i < termCount; i++) {
			charCount += m_terms.get(i).charCount();
		}
		m_charCount = charCount;
	}
	private final String m_keyword;
	private final List<ITerm> m_terms;
	private final int m_charCount;

	private static interface ITerm {

		public int charCount();
	}

	private static class StructureTerm implements ITerm {

		@Override
		public int charCount() {
			return value.m_charCount;
		}

		StructureTerm(WktStructure value) {
			this.value = value;
		}
		final WktStructure value;
	}

	private static class TextTerm implements ITerm {

		@Override
		public int charCount() {
			return value.length();
		}

		TextTerm(String value) {
			this.value = value;
		}
		final String value;
	}
}
