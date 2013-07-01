/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import java.util.List;

/**
 * @author roach
 */
class WktStructure {

	private static final int MaxInline = 100;

	@Override
	public String toString() {
		return m_text;
	}

	public WktStructure(String keyword, Object... zptTerms) {
		if (keyword == null || keyword.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final StringBuilder sb = new StringBuilder(512);
		sb.append(keyword.toUpperCase());
		final int termCount = zptTerms.length;
		if (termCount > 0) {
			sb.append('[');
		}
		boolean comma = false;
		for (int i = 0; i < termCount; i++) {
			final Object oTerm = zptTerms[i];
			if (oTerm == null) {
				continue;
			}
			if (oTerm instanceof IWktEmit) {
				final IWktEmit e = (IWktEmit) oTerm;
				final WktStructure wkt = e.toWkt();
				final String swkt = wkt.toString();
				if (swkt.length() > MaxInline) {
					sb.append("\n");
				}
				if (comma) {
					sb.append(',');
				}
				sb.append(swkt);
				comma = true;
				continue;
			}
			if (oTerm instanceof IWktList) {
				final IWktList list = (IWktList) oTerm;
				final List<? extends IWktEmit> listEmit = list.listEmit();
				final int ecount = listEmit.size();
				for (int ei = 0; ei < ecount; ei++) {
					final IWktEmit e = listEmit.get(ei);
					final WktStructure wkt = e.toWkt();
					final String swkt = wkt.toString();
					sb.append("\n");
					if (comma) {
						sb.append(',');
					}
					sb.append(swkt);
					comma = true;
				}
				continue;
			}
			if ((oTerm instanceof Double) || (oTerm instanceof Integer)) {
				if (comma) {
					sb.append(',');
				}
				sb.append(oTerm.toString());
				comma = true;
			}
			if (comma) {
				sb.append(',');
			}
			sb.append("\"");
			sb.append(oTerm.toString());
			sb.append("\"");
			comma = true;
		}
		if (termCount > 0) {
			sb.append(']');
		}
		m_text = sb.toString();
	}
	private final String m_text;
}
