/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.Collections;
import java.util.List;

import com.metservice.argon.ArgonTransformer;

/**
 * @author roach
 */
public class BerylliumSmtpBorder {

	private static final String StyleTBODY = "font-size:75%; font-family: sans-serif";
	private static final String StyleTH = "text-align: left; color:#404080";
	private static final String StyleTD = "color:#707070";

	private static void makeHtmlBlock(StringBuilder sb, List<String[]> zl) {
		final int count = zl.size();
		if (count == 0) return;
		sb.append("<table><tbody style=\"" + StyleTBODY + "\">");
		for (int i = 0; i < count; i++) {
			makeHtmlRow(sb, zl, i);
		}
		sb.append("</tbody></table>");
	}

	private static void makeHtmlRow(StringBuilder sb, List<String[]> zl, int index) {
		final String[] ozpt = zl.get(index);
		if (ozpt == null || ozpt.length == 0) return;
		sb.append("<tr>");
		final int colCount = ozpt.length;
		for (int i = 0; i < colCount; i++) {
			final String z = z(ozpt[i]);
			final String ze = ArgonTransformer.zHtmlEncodePCDATA(z);
			final boolean useTH = (i == 0 && colCount > 1);
			final String tag = useTH ? "th" : "td";
			final String style = useTH ? StyleTH : StyleTD;
			sb.append('<').append(tag);
			sb.append(" style=\"").append(style).append("\">");
			sb.append(ze);
			sb.append("</").append(tag).append('>');
		}
		sb.append("</tr>\n");
	}

	private static void makeTextBlock(StringBuilder sb, List<String[]> zl) {
		final int count = zl.size();
		for (int i = 0; i < count; i++) {
			makeTextLine(sb, zl, i);
		}
	}

	private static void makeTextLine(StringBuilder sb, List<String[]> zl, int index) {
		final String[] ozpt = zl.get(index);
		if (ozpt == null || ozpt.length == 0) return;
		final StringBuilder bline = new StringBuilder();
		for (int i = 0; i < ozpt.length; i++) {
			final String z = z(ozpt[i]);
			if (z.length() > 0) {
				if (bline.length() > 0) {
					bline.append(' ');
				}
				bline.append(z);
			}
		}
		sb.append(bline.toString());
		sb.append("\n");
	}

	private static String oq(String oz) {
		return oz == null || oz.length() == 0 ? null : oz;
	}

	private static String z(String oz) {
		return oz == null ? "" : oz;
	}

	private static List<String[]> zl(List<String[]> ozl) {
		if (ozl == null) return Collections.emptyList();
		return ozl;
	}

	public static BerylliumSmtpBorder newInstance(String ozSuffix, List<String[]> ozlHead, List<String[]> ozlFoot) {
		return new BerylliumSmtpBorder(oq(ozSuffix), zl(ozlHead), zl(ozlFoot));
	}

	public String composeHtml(String zeBody) {
		if (zeBody == null) throw new IllegalArgumentException("object is null");
		if (m_zlHead.isEmpty() && m_zlFoot.isEmpty()) return zeBody;
		final StringBuilder sb = new StringBuilder();
		final int headCount = m_zlHead.size();
		if (headCount > 0) {
			makeHtmlBlock(sb, m_zlHead);
			sb.append("<hr>");
		}
		sb.append(zeBody);
		final int footCount = m_zlFoot.size();
		if (footCount > 0) {
			sb.append("<hr>");
			makeHtmlBlock(sb, m_zlFoot);
		}
		return sb.toString();
	}

	public String composeSubject(String qcctwBody) {
		if (qcctwBody == null) throw new IllegalArgumentException("object is null");
		return (m_oqSuffix == null) ? qcctwBody : qcctwBody + " - " + m_oqSuffix;
	}

	public String composeText(String zBody) {
		if (zBody == null) throw new IllegalArgumentException("object is null");
		if (m_zlHead.isEmpty() && m_zlFoot.isEmpty()) return zBody;
		final StringBuilder sb = new StringBuilder();
		final int headCount = m_zlHead.size();
		if (headCount > 0) {
			makeTextBlock(sb, m_zlHead);
			sb.append("\n");
		}
		sb.append(zBody);
		final int footCount = m_zlFoot.size();
		if (footCount > 0) {
			sb.append("\n\n\n");
			makeTextBlock(sb, m_zlFoot);
		}
		return sb.toString();
	}

	private BerylliumSmtpBorder(String oqSuffix, List<String[]> zlHead, List<String[]> zlFoot) {
		m_oqSuffix = oqSuffix;
		m_zlHead = zlHead;
		m_zlFoot = zlFoot;
	}
	private final String m_oqSuffix;
	private final List<String[]> m_zlHead;
	private final List<String[]> m_zlFoot;
}
