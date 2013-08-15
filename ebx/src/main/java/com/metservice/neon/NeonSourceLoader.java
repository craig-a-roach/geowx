/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.metservice.argon.text.ArgonSplitter;

/**
 * @author roach
 */
public class NeonSourceLoader {

	private static int adler32(String qtw, int a32in) {
		assert qtw != null;
		int len = qtw.length();
		final long ua32in = a32in & 0xffffffffL;
		long s1 = ua32in & 0xffffL;
		long s2 = (ua32in >> 16) & 0xffffL;
		int bufpos = 0;
		while (len > 0) {
			int k = len < NMAX ? len : NMAX;
			len -= k;
			while (k > 0) {
				final long b = qtw.charAt(bufpos) & 0xffL;
				bufpos++;
				s1 += b;
				s2 += s1;
				k--;
			}
			s1 = s1 % MOD_ADLER;
			s2 = s2 % MOD_ADLER;
		}

		final long ua32out = (s2 << 16) | s1;
		return (int) ua32out;
	}

	private static int adler32(String qccSourceName, String[] xptztwLines) {
		int a32 = adler32(qccSourceName, 1);
		final int lineCount = xptztwLines.length;
		for (int i = 0; i < lineCount; i++) {
			final String ztwLine = xptztwLines[i];
			if (ztwLine.length() == 0) {
				continue;
			}
			a32 = adler32(ztwLine, a32);
		}
		return a32;
	}

	private static String ztwImportBase(String qccSourcePath) {
		assert qccSourcePath != null && qccSourcePath.length() > 0;
		final int posLastFwd = qccSourcePath.lastIndexOf('/');
		return (posLastFwd < 0) ? "" : qccSourcePath.substring(0, posLastFwd).trim();
	}

	static String msgSyntax(int lineSerial, String qDetail) {
		return "Source loading syntax error at line " + lineSerial + "; " + qDetail;
	}

	static String msgSyntax(int lineSerial, String qDetail, int pos) {
		return "Source loading syntax error at position " + pos + " of line " + lineSerial + "; " + qDetail;
	}

	static String msgSyntax(String qDetail) {
		return "Source loading syntax error; " + qDetail;
	}

	static String qcctwPath(int lineSerial, boolean isRoot, String ztwImportBase, String zccPath)
			throws EsSourceLoadException {
		assert zccPath != null;
		final String zcctwPath = zccPath.trim();
		final boolean isAbsolute = zcctwPath.startsWith("/");
		final String zcctwPathRel = isAbsolute ? zcctwPath.substring(1) : zcctwPath;
		if (zcctwPathRel.length() == 0) {
			final String m;
			if (isRoot) {
				m = msgSyntax("Empty path");
			} else {
				m = msgSyntax(lineSerial, "Empty import path");
			}
			throw new EsSourceLoadException(m);
		}
		final StringBuilder bpath = new StringBuilder();
		if (!isRoot && !isAbsolute && ztwImportBase.length() > 0) {
			bpath.append(ztwImportBase);
			bpath.append('/');
		}
		bpath.append(suffixEcmaScript(zcctwPathRel));
		return bpath.toString();
	}

	static String suffixEcmaScript(String qcctwPath) {
		if (qcctwPath.endsWith(NeonFileExtension.SuffixWip)) return qcctwPath;
		final String qlctwPath = qcctwPath.toLowerCase();
		if (qlctwPath.endsWith(NeonFileExtension.SuffixEcmascript)) return qcctwPath;
		return qcctwPath + NeonFileExtension.SuffixEcmascript;
	}

	static String ztwSource(KernelCfg kc, int lineSerial, boolean isRoot, String qcctwPath)
			throws EsSourceLoadException {
		return kc.sourceProvider.source(qcctwPath).trim();
	}

	public EsSource newSource(String qccSourcePath)
			throws EsSourceLoadException {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");

		final String ztwImportBase = ztwImportBase(qccSourcePath);
		final Parse parse = new Parse(kc, ztwImportBase);

		parse.addDoc(qccSourcePath);
		if (parse.blackLineCount == 0) {
			final String m = "Resource '" + qccSourcePath + "' is empty";
			throw new EsSourceLoadException(m);
		}
		if (parse.zsAuthors.isEmpty()) {
			final String m = "Resource '" + qccSourcePath + "' does not identify its author";
			throw new EsSourceLoadException(m);
		}
		if (parse.oqPurpose == null) {
			final String m = "Resource '" + qccSourcePath + "' does not describe its purpose";
			throw new EsSourceLoadException(m);
		}
		final EsSourceMeta meta = new EsSourceMeta(parse.zsAuthors, parse.oqPurpose);
		final int lineCount = parse.zlztwLines.size();
		final String[] xptztwLines = parse.zlztwLines.toArray(new String[lineCount]);
		final int adler32 = adler32(qccSourcePath, xptztwLines);
		return new EsSource(qccSourcePath, meta, xptztwLines, adler32);
	}

	public EsSourceHtml newSourceHtml(String qccSourcePath, boolean showImports)
			throws EsSourceLoadException, EsSyntaxException {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (showImports) {
			final EsSource source = newSource(qccSourcePath);
			return source.newTokenReader().newSourceHtml();
		}
		final String qcctwSourcePath = qcctwPath(1, true, "", qccSourcePath);
		final String ztwSource = ztwSource(kc, 1, true, qcctwSourcePath);
		final String[] zptztwLines = ArgonSplitter.zptzLines(ztwSource, true, true);
		if (zptztwLines.length == 0) {
			final String m = "Resource '" + qccSourcePath + "' is empty";
			throw new EsSourceLoadException(m);
		}
		final Token[] xptTokens = EsSource.new_xptTokens(qccSourcePath, zptztwLines);
		return EsSourceHtml.newInstance(xptTokens, null, null);
	}

	public void save(String qccSourcePath, String qtwJs)
			throws EsSourceSaveException {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (qtwJs == null || qtwJs.length() == 0) throw new IllegalArgumentException("string is null or empty");
		kc.sourceProvider.putSource(qccSourcePath, qtwJs);
	}

	public boolean validate(String qccSourcePath) {
		try {
			newSource(qccSourcePath).newCallable();
			return true;
		} catch (final EsSourceLoadException ex) {
			return false;
		} catch (final EsSyntaxException ex) {
			return false;
		}
	}

	public String ztwSourceText(String qccSourcePath, boolean showImports)
			throws EsSourceLoadException {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (showImports) {
			final EsSource source = newSource(qccSourcePath);
			return source.toString();
		}
		final String qcctwSourcePath = qcctwPath(1, true, "", qccSourcePath);
		return ztwSource(kc, 1, true, qcctwSourcePath);
	}

	NeonSourceLoader(KernelCfg kc) {
		this.kc = kc;
	}

	final KernelCfg kc;
	public static final String KwdImport = "IMPORT";
	public static final String KwdAuthor = "AUTHOR";
	public static final String KwdPurpose = "PURPOSE";

	private static final long MOD_ADLER = 65521L;
	private static final int NMAX = 5552;

	private static class Line {

		void consume(String qccGoal)
				throws EsSourceLoadException {
			final int lenGoal = qccGoal.length();
			for (int i = 0; i < lenGoal; i++) {
				final char g = qccGoal.charAt(i);
				if (pos == posEnd || ztw.charAt(pos) != g) {
					final String m = msgSyntax(lineSerial, "Expecting '" + qccGoal + "'", pos);
					throw new EsSourceLoadException(m);
				}
				pos++;
			}
		}

		char consumeQuote()
				throws EsSourceLoadException {
			while (pos < posEnd) {
				final char ch = ztw.charAt(pos);
				if (ch == '\'' || ch == '\"') {
					pos++;
					return ch;
				}
			}
			final String m = msgSyntax(lineSerial, "Expecting a quote character", pos);
			throw new EsSourceLoadException(m);
		}

		String consumeQuotedString(char qch)
				throws EsSourceLoadException {
			final StringBuilder sb = new StringBuilder();
			while (pos < posEnd) {
				final char ch = ztw.charAt(pos);
				if (ch == qch) {
					pos++;
					return sb.toString();
				}
				sb.append(ch);
				pos++;
			}
			final String m = msgSyntax(lineSerial, "Expecting a closing quote (" + qch + ") character", pos);
			throw new EsSourceLoadException(m);
		}

		void consumeSpace() {
			while (pos < posEnd && (ztw.charAt(pos) == ' ')) {
				pos++;
			}
		}

		public Line(int lineSerial, String ztw) {
			this.lineSerial = lineSerial;
			this.ztw = ztw;
			this.posEnd = ztw.length();
		}
		final int lineSerial;
		final String ztw;
		final int posEnd;
		int pos;
	}

	private static class Parse {

		private void addLine(String zLine)
				throws EsSourceLoadException {
			final String ztwLine = zLine.trim();
			final int lenLine = ztwLine.length();
			if (lenLine == 0) {
				zlztwLines.add(ztwLine);
				return;
			}
			final int lineSerial = lineSerial();
			if (ztwLine.startsWith(KwdImport)) {
				final Line line = new Line(lineSerial, ztwLine);
				addLineImport(line);
				return;
			}

			if (ztwLine.startsWith(KwdAuthor)) {
				final Line line = new Line(lineSerial, ztwLine);
				addLineAuthor(line);
				zlztwLines.add("//");
				return;
			}

			if (ztwLine.startsWith(KwdPurpose)) {
				final Line line = new Line(lineSerial, ztwLine);
				addLinePurpose(line);
				zlztwLines.add("//");
				return;
			}

			zlztwLines.add(ztwLine);
			blackLineCount++;
		}

		private void addLineAuthor(Line line)
				throws EsSourceLoadException {
			line.consume(KwdAuthor);
			line.consumeSpace();
			line.consume("(");
			line.consumeSpace();
			final char qch = line.consumeQuote();
			final String zccAuthor = line.consumeQuotedString(qch);
			line.consumeSpace();
			line.consume(")");
			line.consumeSpace();
			line.consume(";");
			if (zccAuthor.length() > 0) {
				zsAuthors.add(zccAuthor);
			}
		}

		private void addLineImport(Line line)
				throws EsSourceLoadException {
			line.consume(KwdImport);
			line.consumeSpace();
			line.consume("(");
			line.consumeSpace();
			final char qch = line.consumeQuote();
			final String zccPath = line.consumeQuotedString(qch);
			line.consumeSpace();
			line.consume(")");
			line.consumeSpace();
			line.consume(";");
			addDoc(zccPath);
		}

		private void addLinePurpose(Line line)
				throws EsSourceLoadException {
			line.consume(KwdPurpose);
			line.consumeSpace();
			line.consume("(");
			line.consumeSpace();
			final char qch = line.consumeQuote();
			final String zPurpose = line.consumeQuotedString(qch);
			line.consumeSpace();
			line.consume(")");
			line.consumeSpace();
			line.consume(";");
			if (zPurpose.length() > 0) {
				if (oqPurpose == null) {
					oqPurpose = zPurpose;
				}
			}
		}

		private int lineSerial() {
			return zlztwLines.size() + 1;
		}

		public void addDoc(String zccPath)
				throws EsSourceLoadException {
			final int lineSerialStart = lineSerial();
			final boolean isRoot = zsImports.isEmpty();
			final String qcctwPath = qcctwPath(lineSerialStart, isRoot, ztwImportBase, zccPath);
			if (zsImports.add(qcctwPath)) {
				if (!isRoot) {
					zlztwLines.add("//start of '" + qcctwPath + "'");
				}
				final String ztw = ztwSource(kc, lineSerialStart, isRoot, qcctwPath);
				final int len = ztw.length();
				final StringBuilder bline = new StringBuilder();
				for (int i = 0; i < len; i++) {
					final char ch = ztw.charAt(i);
					if (ch == '\r') {
						continue;
					}
					if (ch == '\n') {
						addLine(bline.toString());
						bline.setLength(0);
					} else {
						bline.append(ch);
					}
				}
				if (bline.length() > 0) {
					addLine(bline.toString());
				}
				if (!isRoot) {
					zlztwLines.add("//end of '" + qcctwPath + "'");
				}
			}
		}

		Parse(KernelCfg kc, String ztwImportBase) {
			this.kc = kc;
			this.ztwImportBase = ztwImportBase;
		}

		final KernelCfg kc;
		final String ztwImportBase;
		final List<String> zlztwLines = new ArrayList<String>(256);
		final Set<String> zsImports = new HashSet<String>(16);
		final Set<String> zsAuthors = new HashSet<String>(16);
		String oqPurpose;
		int blackLineCount;
	}
}
