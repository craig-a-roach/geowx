/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
enum ShellLanguage {
	EcmaScript, Python, Xml, Shell, Text;

	public static ShellLanguage newInstance(String qcctwName) {
		if (qcctwName == null || qcctwName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String qcctwBase = NeonFileExtension.qcctwBaseName(qcctwName);
		if (qcctwBase.endsWith(NeonFileExtension.SuffixEcmascript)) return EcmaScript;
		if (qcctwBase.endsWith(NeonFileExtension.SuffixXml)) return Xml;
		if (qcctwBase.endsWith(NeonFileExtension.SuffixPython)) return Python;
		if (qcctwBase.endsWith(NeonFileExtension.SuffixShell)) return Shell;
		return Text;
	}
}
