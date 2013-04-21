/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.net;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
public class ArgonPlatform {

	private static final String DefaultNicSpec = "?IPv4 G:S:L";
	private static final String DefaultLoopbackHostName = "localhost";
	private static final String DefaultLoopbackHostEmail = "localhost.net";

	public static boolean isOsWindows() {
		return qcctwOsName().startsWith("Windows");
	}

	public static String oqcctwEnvironment(String pname) {
		if (pname == null || pname.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return ArgonText.oqtw(System.getenv(pname));
	}

	public static String oqlctwEnvironmentHostName() {
		String oqcctwHost = oqcctwEnvironment("HOSTNAME");
		if (oqcctwHost == null) {
			final String oqcctwComputer = oqcctwEnvironment("COMPUTERNAME");
			if (oqcctwComputer != null) {
				final String oqcctwDomain = oqcctwEnvironment("USERDNSDOMAIN");
				if (oqcctwDomain == null) {
					oqcctwHost = oqcctwComputer;
				} else {
					oqcctwHost = oqcctwComputer + "." + oqcctwDomain;
				}
			}
		}
		return oqcctwHost == null ? null : oqcctwHost.toLowerCase();
	}

	public static String qccDiscoveredLocalHostName() {
		return qccDiscoveredLocalHostName(DefaultNicSpec, DefaultLoopbackHostName);
	}

	public static String qccDiscoveredLocalHostName(String qNicSpec, String qLoopbackName) {
		if (qNicSpec == null || qNicSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qLoopbackName == null || qLoopbackName.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		try {
			final ArgonNic oNic = ArgonNicDiscoverer.findUnicastNonLoopback(qNicSpec);
			final ArgonNicHost oNicHost = oNic == null ? null : ArgonNicHost.newInstance(oNic);
			if (oNicHost != null) return oNicHost.qlcName;
		} catch (final ArgonPermissionException ex) {
		} catch (final ArgonFormatException ex) {
			throw new IllegalStateException("Cannot access local host name", ex);
		}
		return qLoopbackName;
	}

	public static String qcctwOsArchitecture() {
		return qcctwSystemProperty("os.arch");
	}

	public static String qcctwOsName() {
		return qcctwSystemProperty("os.name");
	}

	public static String qcctwOsVersion() {
		return qcctwSystemProperty("os.version");
	}

	public static String qcctwSystemProperty(String pname) {
		if (pname == null || pname.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String oqtw = ArgonText.oqtw(System.getProperty(pname));
		if (oqtw == null) throw new IllegalStateException("Cannot access system property '" + pname + "'");
		return oqtw;
	}

	public static String qcctwTmpDir() {
		return qcctwSystemProperty("java.io.tmpdir");
	}

	public static String qcctwUserDir() {
		return qcctwSystemProperty("user.dir");
	}

	public static String qcctwUserHome() {
		return qcctwSystemProperty("user.home");
	}

	public static String qcctwUserName() {
		return qcctwSystemProperty("user.name");
	}

	public static String qlcLocalEmailAddress() {
		return qlcLocalEmailAddress(DefaultNicSpec);
	}

	public static String qlcLocalEmailAddress(String qNicSpec) {
		final StringBuilder sb = new StringBuilder();
		sb.append(qcctwUserName());
		sb.append('@');
		final String oqlctwHostName = oqlctwEnvironmentHostName();
		if (oqlctwHostName == null) {
			sb.append(qccDiscoveredLocalHostName(qNicSpec, DefaultLoopbackHostEmail));
		} else {
			sb.append(oqlctwHostName);
		}
		return sb.toString().toLowerCase();
	}

	public static String zccLineSeparator() {
		return zccSystemProperty("line.separator");
	}

	public static String zccSystemProperty(String pname) {
		if (pname == null || pname.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String oz = System.getProperty(pname);
		if (oz == null) throw new IllegalStateException("Cannot access system property '" + pname + "'");
		return oz;
	}

	private ArgonPlatform() {
	}
}
