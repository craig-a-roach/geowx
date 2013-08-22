/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.PrintWriter;

import com.metservice.argon.ArgonNumber;
import com.metservice.argon.ArgonTransformer;

/**
 * @author roach
 */
public class EsSourceHtml {

	private static int addToken(StringBuilder bline, int bpos, Token token) {
		final String zToken = token.toScript();
		final int tlen = zToken.length();
		final int tstart = token.startIndex();
		for (int i = bpos; i < tstart; i++) {
			bline.append(" ");
		}
		final String zheToken = ArgonTransformer.zHtmlEncode(zToken, false, " ", "\\n", "\\t", "\'", "\"");
		final String oqStyleClass = oqStyleClass(token);
		if (oqStyleClass == null) {
			bline.append(zheToken);
		} else {
			bline.append("<span class=\"").append(oqStyleClass).append("\">").append(zheToken).append("</span>");
		}

		return tstart + tlen;
	}

	private static void newLine(StringBuilder bline, int depth) {
		bline.setLength(0);
		for (int i = 0; i < depth; i++) {
			bline.append("&nbsp;&nbsp;&nbsp;&nbsp;");
		}
	}

	private static String oqStyleClass(Token token) {
		if (token instanceof TokenReservedWord) return "jsrsv";
		if (token instanceof TokenIdentifier) return "jsident";
		if (token instanceof TokenLiteralNumeric) return "jslitn";
		if (token instanceof TokenLiteralString) return "jslits";
		if (token instanceof TokenLiteralRegexp) return "jslitr";
		if (token instanceof TokenLiteralXml) return "jslitx";
		return null;
	}

	public static EsSourceHtml newInstance(Token[] xptTokens, String oqAuthors, String oqPurpose) {
		if (xptTokens == null) throw new IllegalArgumentException("object is null");
		final int tokenCount = xptTokens.length;
		if (tokenCount == 0) throw new IllegalArgumentException("empty array");
		final Token eof = xptTokens[tokenCount - 1];
		final int lineCount = eof.lineIndex;
		final String[] ztHtml = new String[lineCount];
		final StringBuilder bline = new StringBuilder();
		int bufferLineIndex = 0;
		int bufferPos = 0;
		int depth = 0;
		for (int i = 0; i < tokenCount; i++) {
			final Token token = xptTokens[i];
			final int lineIndex = token.lineIndex;
			if ((token instanceof TokenPunctuator) && ((TokenPunctuator) token).punctuator == Punctuator.RBRACE) {
				depth--;
			}
			if (lineIndex > bufferLineIndex) {
				ztHtml[bufferLineIndex] = bline.toString();
				newLine(bline, depth);
				bufferLineIndex = lineIndex;
				bufferPos = 0;
			}
			bufferPos = addToken(bline, bufferPos, token);
			if ((token instanceof TokenPunctuator) && ((TokenPunctuator) token).punctuator == Punctuator.LBRACE) {
				depth++;
			}
		}
		return new EsSourceHtml(ztHtml, oqAuthors, oqPurpose);
	}

	private int numberingWidth() {
		final int lineCount = m_ztHtml.length;
		if (lineCount < 10) return 1;
		if (lineCount < 100) return 2;
		if (lineCount < 1000) return 3;
		if (lineCount < 10000) return 4;
		return 5;
	}

	public String oqAuthors() {
		return m_oqAuthors;
	}

	public String oqPurpose() {
		return m_oqPurpose;
	}

	public void writeErrorView(PrintWriter writer, EsSyntaxException ex, int reveal) {
		final int lineCount = m_ztHtml.length;
		final int lineIndexErr = ex.lineIndex;
		final int rb = Math.min(lineIndexErr, reveal / 2);
		final int ra = Math.min(lineCount - lineIndexErr, reveal - rb);
		final int lineIndexStart = Math.max(0, lineIndexErr - rb);
		final int lineIndexEnd = Math.min(lineCount, lineIndexErr + ra);

		final int nw = numberingWidth();
		writer.println("<ul class=\"js\">");
		final StringBuilder bline = new StringBuilder();
		for (int i = lineIndexStart; i < lineIndexEnd; i++) {
			final boolean isErr = i == lineIndexErr;
			final String qLineNo = ArgonNumber.intToDec(i + 1, nw);
			if (isErr) {
				bline.append("<li class=\"jsbp\">");
			} else {
				bline.append("<li class=\"js\">");
			}
			bline.append("<span class=\"jsno\">").append(qLineNo).append("</span>");
			bline.append("&nbsp;&nbsp;");
			final String ozHtml = m_ztHtml[i];
			if (ozHtml != null) {
				bline.append(ozHtml);
			}
			bline.append("</li>");
			writer.println(bline.toString());
			bline.setLength(0);
			if (isErr) {
				writer.println("<li class=\"jserm\">" + ex.problem + "</li>");
			}
		}
		writer.println("</ul>");
	}

	public void writeListingDebug(PrintWriter writer, int lineIndexHighlight, int reveal, EsBreakpointLines bpl) {
		if (bpl == null) throw new IllegalArgumentException("object is null");
		final int lineCount = m_ztHtml.length;
		int lineIndexStart = 0;
		if (reveal < lineCount) {
			final int rb = Math.min(lineIndexHighlight, reveal / 3);
			lineIndexStart = Math.max(0, lineIndexHighlight - rb);
		}

		final int nw = numberingWidth();
		final int[] zptBreakpointLineIndexAsc = bpl.zptLineIndexAsc();
		int ibpl = 0;
		final int cbpl = zptBreakpointLineIndexAsc.length;
		writer.println("<ul class=\"js\">");
		final StringBuilder bline = new StringBuilder();
		for (int i = lineIndexStart, rcount = 0; i < lineCount && rcount < reveal; i++) {
			final String ozHtml = m_ztHtml[i];
			final boolean isBlack = ozHtml != null && ozHtml.length() > 0;
			if (isBlack) {
				final boolean highlighted = (i == lineIndexHighlight);
				boolean bpmark = false;
				if (ibpl < cbpl && i == zptBreakpointLineIndexAsc[ibpl]) {
					bpmark = true;
					ibpl++;
				}
				String lineClass = "js";
				if (highlighted) {
					lineClass = "jshi";
				} else if (bpmark) {
					lineClass = "jsbp";
				}
				final String qLineNo = ArgonNumber.intToDec(i + 1, nw);
				bline.append("<li class=\"").append(lineClass).append("\">");
				bline.append("<span class=\"jsno\">").append(qLineNo).append("</span>");
				bline.append("&nbsp;&nbsp;");
				bline.append(ozHtml);
				bline.append("</li>");
				writer.println(bline.toString());
				bline.setLength(0);
				rcount++;
			}
		}
		writer.println("</ul>");
	}

	public void writeListingProfile(PrintWriter writer, ProfileSamplePI program) {
		if (program == null) throw new IllegalArgumentException("object is null");
		writer.println("<h3>Cumulative Totals</h3>");
		writer.println("<ul>");
		writer.println("<li>Execution Count: " + program.count + "</li>");
		writer.println("<li>Lines Cumulative: " + UNeonProfile.qTiming(program.nsCumLines) + "</li>");
		writer.println("<li>Run Cumulative: " + UNeonProfile.qTiming(program.nsCumRun) + "</li>");
		writer.println("<li>Lines Executed: " + program.cumLinesExecuted + "</li>");
		writer.println("</ul>");

		final int lineCount = m_ztHtml.length;
		final int nw = numberingWidth();
		writer.println("<table class=\"js\">");
		writer.println("<tbody>");
		final StringBuilder bline = new StringBuilder();
		for (int i = 0; i < lineCount; i++) {
			final String ozHtml = m_ztHtml[i];
			final boolean isBlack = ozHtml != null && ozHtml.length() > 0;
			if (isBlack) {
				final String qLineNo = ArgonNumber.intToDec(i + 1, nw);
				bline.append("<tr class=\"js\">");
				bline.append("<td><span class=\"jsno\">").append(qLineNo).append("</span></td>");
				final ProfileSampleLI oLI = program.lineItemMap[i];
				if (oLI == null) {
					bline.append("<td>---</td>");
					bline.append("<td>---</td>");
					bline.append("<td>---</td>");
				} else {
					final int pct = oLI.pct(program);
					final int pctClass = UNeonProfile.pctClass(pct);
					bline.append("<td class=\"prf").append(pctClass).append("\">").append(pct).append("%</td>");
					bline.append("<td>*").append(oLI.count).append("</td>");
					bline.append("<td>").append(UNeonProfile.qTiming(oLI.nsCum)).append("</td>");
				}

				bline.append("<td>");
				bline.append(ozHtml);
				bline.append("</td>");

				if (oLI == null) {
					bline.append("<td>&nbsp;</td>");
				} else {
					bline.append("<td>").append(oLI.zCallInfo()).append("</td>");
				}

				bline.append("</tr>");
				writer.println(bline.toString());
				bline.setLength(0);
			}
		}
		writer.println("</tbody></table>");
	}

	public void writeListingView(PrintWriter writer, boolean compact) {
		final int lineCount = m_ztHtml.length;
		final int nw = numberingWidth();
		writer.println("<ul class=\"js\">");
		final StringBuilder bline = new StringBuilder();
		boolean preBlack = false;
		for (int i = 0; i < lineCount; i++) {
			final String ozHtml = m_ztHtml[i];
			final boolean isBlack = ozHtml != null && ozHtml.length() > 0;
			if (isBlack || (preBlack && !compact)) {
				final String qLineNo = ArgonNumber.intToDec(i + 1, nw);
				bline.append("<li class=\"js\">");
				bline.append("<span class=\"jsno\">").append(qLineNo).append("</span>");
				bline.append("&nbsp;&nbsp;");
				if (ozHtml != null) {
					bline.append(ozHtml);
				}
				bline.append("</li>");
				writer.println(bline.toString());
				bline.setLength(0);
			}
			preBlack = isBlack;
		}
		writer.println("</ul>");
	}

	private EsSourceHtml(String[] ztHtml, String oqAuthors, String oqPurpose) {
		assert ztHtml != null;
		m_ztHtml = ztHtml;
		m_oqAuthors = oqAuthors;
		m_oqPurpose = oqPurpose;
	}
	private final String[] m_ztHtml;
	private final String m_oqAuthors;
	private final String m_oqPurpose;
}
