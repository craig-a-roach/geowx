/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonSplitter;
import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
public class ConvertTabToXml {

	private static final Pattern PatternDesc = Pattern.compile("([^\\[]+)\\[([^\\]]*)\\]");

	private static String cleanDesc(String qtwDesc) {
		final int len = qtwDesc.length();
		boolean reqsep = true;
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			final char ch = qtwDesc.charAt(i);
			if (ArgonText.isLetterOrDigit(ch)) {
				sb.append(ch);
				reqsep = true;
			} else {
				if (reqsep) {
					sb.append('_');
					reqsep = false;
				}
			}
		}
		return sb.toString();
	}

	private static void emit(StringBuilder dst, int nbr, String sid, String qtwDesc, String ztwUnit) {
		final String fid = cleanDesc(qtwDesc);
		final String x = " sid=\"" + sid + "\" number=\"" + nbr + "\" unit=\"" + ztwUnit + "\" fid=\"" + fid
				+ "\" description=\"" + qtwDesc + "\"";
		dst.append("\t<quantity");
		dst.append(x);
		dst.append("/>\n");
	}

	private static void save(StringBuilder dst, File tabFile) {
		final String fn = tabFile.getName();
		final StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<quantities");
		sb.append(" description=\"").append(fn).append("\"");
		sb.append(">\n");
		sb.append(dst.toString());
		sb.append("</quantities>\n");
		System.out.println(sb);
		final File xml = new File(tabFile.getParentFile(), fn + ".xml");
		FileWriter ofw = null;
		try {
			ofw = new FileWriter(xml);
			ofw.append(sb.toString());
		} catch (final IOException ex) {
			System.err.println("Cannot save " + xml);
		} finally {
			if (ofw != null) {
				try {
					ofw.close();
				} catch (final IOException ex) {
				}
			}
		}
		System.out.println("Saved " + xml);
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Usage:  tabFile");
			return;
		}
		final File tabFile = new File(args[0]);
		if (!tabFile.canRead()) {
			System.err.println("Cannot find tab file " + tabFile);
			return;
		}
		final StringBuilder dst = new StringBuilder();
		BufferedReader obr = null;
		try {
			obr = new BufferedReader(new FileReader(tabFile));
			boolean moreLines = true;
			while (moreLines) {
				final String ozLine = obr.readLine();
				if (ozLine == null) {
					moreLines = false;
					continue;
				}
				final String ztwLine = ozLine.trim();
				if (ztwLine.length() == 0) {
					continue;
				}
				final String[] zptqtwFields = ArgonSplitter.zptqtwSplit(ztwLine, ':');
				if (zptqtwFields.length < 3) {
					System.err.println("Malformed line: " + ztwLine);
					continue;
				}
				int number = -1;
				try {
					number = Integer.parseInt(zptqtwFields[0]);
				} catch (final NumberFormatException ex) {
					System.err.println("Malformed line: " + ztwLine);
				}
				if (number <= 0 || number >= 255) {
					continue;
				}
				final String sid = zptqtwFields[1];
				final String qtwTail = zptqtwFields[2];
				final Matcher matcherTail = PatternDesc.matcher(qtwTail);
				if (!matcherTail.find()) {
					System.err.println("Missing unit: " + ztwLine);
					continue;
				}
				final String ztwDesc = matcherTail.group(1).trim();
				final String ztwUnit = matcherTail.group(2).trim();
				if (ztwDesc.length() == 0) {
					System.err.println("Missing description: " + ztwDesc);
					continue;
				}
				emit(dst, number, sid, ztwDesc, ztwUnit);
			}
			save(dst, tabFile);
		} catch (final FileNotFoundException ex) {
			System.err.println("Cannot find tab table file " + tabFile);
		} catch (final IOException ex) {
			System.err.println("Cannot read " + tabFile + "; " + ex.getMessage());
		} finally {
			if (obr != null) {
				try {
					obr.close();
				} catch (final IOException ex) {
				}
			}
		}
	}
}
