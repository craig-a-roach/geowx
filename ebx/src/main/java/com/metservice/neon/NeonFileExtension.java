/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ArgonText;
import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
public class NeonFileExtension {

	public static final String PrefixIgnore = ".";
	public static final String BodyTest = "test";
	public static final String SuffixTest = "Test";
	public static final int SuffixTestL = SuffixTest.length();
	public static final String SuffixBackup = "~";
	public static final String SuffixWip = "-";
	public static final int SuffixWipL = SuffixWip.length();
	public static final String SuffixEcmascript = ".js";
	public static final String SuffixPython = ".py";
	public static final String SuffixXml = ".xml";
	public static final String SuffixShell = ".sh";

	public static String applyWip(String qcctwName) {
		if (qcctwName == null || qcctwName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return isWipName(qcctwName) ? qcctwName : qcctwName + SuffixWip;
	}

	public static boolean isAssureName(String qcctwName) {
		if (qcctwName == null || qcctwName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qcctwName.equals(BodyTest)) return true;
		final int posKeyword = qcctwName.indexOf(SuffixTest);
		if (posKeyword < 0) return false;
		final String zTail = qcctwName.substring(posKeyword + SuffixTestL);
		if (zTail.length() == 0) return true;
		return !ArgonText.isLetter(zTail.charAt(0));
	}

	public static boolean isAssurePath(String zcctwPath) {
		final BerylliumPath path = BerylliumPath.newInstance(zcctwPath);
		for (int i = 0; i < path.depth; i++) {
			if (isAssureName(path.qtwNode(i))) return true;
		}
		return false;
	}

	public static boolean isWipName(String qcctwName) {
		if (qcctwName == null || qcctwName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return qcctwName.endsWith(SuffixWip);
	}

	public static String qcctwBaseName(String qcctwName) {
		if (qcctwName == null || qcctwName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final int len = qcctwName.length();
		if (len >= 2 && qcctwName.endsWith(SuffixWip)) return qcctwName.substring(0, len - SuffixWipL);
		return qcctwName;
	}

	private NeonFileExtension() {
	}
}
