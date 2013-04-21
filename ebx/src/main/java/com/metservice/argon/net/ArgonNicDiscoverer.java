/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
public class ArgonNicDiscoverer {

	public static final int DefaultIPV = 0;
	public static final boolean DefaultAllowGlobal = true;
	public static final boolean DefaultAllowSiteLocal = true;
	public static final boolean DefaultAllowLinkLocal = true;
	public static final String DefaultNicPrefs = "G:S:L";
	public static final String IPv4 = "IPV4";
	public static final String IPv6 = "IPV6";
	public static final String IPvX1 = "IPV?";
	public static final String IPvX2 = "IPV*";
	public static final String LOOPBACK = "?loopback";

	public static final String ScopeGlobal = "G";
	public static final String ScopeSiteLocal = "S";
	public static final String ScopeLinkLocal = "L";
	private static final char ScopeGlobalCh = ScopeGlobal.charAt(0);
	private static final char ScopeSiteLocalCh = ScopeSiteLocal.charAt(0);
	private static final char ScopeLinkLocalCh = ScopeLinkLocal.charAt(0);

	private static final char Prefix = '?';
	private static final Pattern SplitterNicPrefs = Pattern.compile("[:]");
	private static final Pattern SplitterSpec = Pattern.compile("[\\s]+");
	private static final String RegexClassDecimal = "[0-9]";
	private static final String RegexClassHex = "[0-9A-F]";
	private static final String RegexClassName = "[-0-9_A-Z]";

	private static final char FilterTypeDecimal = 'd';
	private static final char FilterTypeHex = 'h';
	private static final char FilterTypeName = 'n';

	private static void addTo(Map<String, NicList> dst, String scope, ArgonNic nic) {
		assert dst != null;
		assert scope != null;
		NicList oNicList = dst.get(scope);
		if (oNicList == null) {
			oNicList = new NicList(nic);
			dst.put(scope, oNicList);
		} else {
			oNicList.add(nic);
		}
	}

	private static char filterType(String quc) {
		int letterCount = 0;
		int sepCount = 0;
		int digit10Count = 0;
		int colonCount = 0;
		final int len = quc.length();
		for (int i = 0; i < len; i++) {
			final char ch = quc.charAt(i);
			if (ch == ':') {
				colonCount++;
				continue;
			}
			if (ch == '-' || ch == '_') {
				sepCount++;
			}
			if (ArgonText.isDigit(ch)) {
				digit10Count++;
				continue;
			}
			if (ArgonText.isLetter(ch)) {
				letterCount++;
			}
		}
		if (colonCount > 0) return FilterTypeHex;
		if (letterCount == 0 && sepCount == 0 && digit10Count > 0) return FilterTypeDecimal;
		return FilterTypeName;
	}

	private static ArgonNic findMaxName(Map<String, NicList> dst) {
		ArgonNic oNic = findMaxName(dst, ScopeGlobal);
		if (oNic != null) return oNic;
		oNic = findMaxName(dst, ScopeSiteLocal);
		if (oNic != null) return oNic;
		oNic = findMaxName(dst, ScopeLinkLocal);
		return oNic;
	}

	private static ArgonNic findMaxName(Map<String, NicList> dst, String scope) {
		assert dst != null;
		assert scope != null;
		final NicList oNicList = dst.get(scope);
		return oNicList == null ? null : oNicList.selectMaxName();
	}

	private static String scopeKey(boolean isSiteLocal, boolean isLinkLocal) {
		if (isSiteLocal) return ScopeSiteLocal;
		if (isLinkLocal) return ScopeLinkLocal;
		return ScopeGlobal;
	}

	private static Pattern toPattern(String quc, String wildcardRegex)
			throws ArgonFormatException {
		final int len = quc.length();
		final StringBuilder sb = new StringBuilder(len * 2);
		for (int i = 0; i < len; i++) {
			final char ch = quc.charAt(i);
			if (ch == '.') {
				sb.append("[.]");
			} else if (ch == '*' || ch == '+' || ch == '?') {
				sb.append(wildcardRegex);
				sb.append(ch);
			} else {
				sb.append(ch);
			}
		}
		try {
			return Pattern.compile(sb.toString());
		} catch (final PatternSyntaxException ex) {
			throw new ArgonFormatException("Malformed regex term..." + ex.getMessage());
		}
	}

	public static ArgonNic findLoopbackIPv4()
			throws ArgonPermissionException {
		final InetAddress loopback = selectLoopbackIPv4InetAddress();
		try {
			final NetworkInterface oNI = NetworkInterface.getByInetAddress(loopback);
			if (oNI == null) return null;
			return new ArgonNic(oNI.getName(), oNI, loopback);
		} catch (final SocketException ex) {
			throw new ArgonPermissionException("Cannot discover local network interfaces", ex);
		}
	}

	public static ArgonNic findUnicast(String qSpec)
			throws ArgonPermissionException, ArgonFormatException {
		if (qSpec == null || qSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String ztwSpec = qSpec.trim();
		if (ztwSpec.equalsIgnoreCase(LOOPBACK)) return findLoopbackIPv4();
		return findUnicastNonLoopback(qSpec);
	}

	/**
	 * Returns a non-loopback Nic bound to a specific IP version, and a matching the most preferred of a list
	 * (colon-separated) of alternative names.
	 * 
	 * @param ipVersion
	 *              should be 4 (IPv4), 6 (IPv6) or 0 (either)
	 * @param ozNicPrefs
	 *              colon-separated list of interface names. Null or zero length is acceptable, and means use default.
	 * @return Nic
	 * @throws ArgonPermissionException
	 *               If network interface discovery has been blocked on this host.
	 */
	public static ArgonNic findUnicastNonLoopback(int ipVersion, boolean allowGlobal, boolean allowSiteLocal,
			boolean allowLinkLocal, Pattern oIPFilter, Pattern oDNFilter, String ozNicPrefs)
			throws ArgonPermissionException {
		final String qNetPrefs = (ozNicPrefs == null || ozNicPrefs.length() == 0) ? DefaultNicPrefs : ozNicPrefs;
		final String[] xptNetPrefs = SplitterNicPrefs.split(qNetPrefs);

		try {
			final Enumeration<NetworkInterface> enNI = NetworkInterface.getNetworkInterfaces();
			final Map<String, ArgonNic> zmName_NonLoopback = new HashMap<String, ArgonNic>();
			final Map<String, NicList> zmScope_NicList = new HashMap<String, NicList>();
			while (enNI.hasMoreElements()) {
				final NetworkInterface ni = enNI.nextElement();
				final String qccNIName = ni.getName();
				final Enumeration<InetAddress> enAddresses = ni.getInetAddresses();
				InetAddress oBindAddr = null;
				String oqScopeKey = null;
				while (oBindAddr == null && enAddresses.hasMoreElements()) {
					final InetAddress inetAddr = enAddresses.nextElement();
					if (inetAddr.isMulticastAddress()) {
						continue;
					}
					if (inetAddr.isLoopbackAddress() || inetAddr.isAnyLocalAddress()) {
						continue;
					}
					final boolean isSiteLocal = inetAddr.isSiteLocalAddress();
					final boolean isLinkLocal = inetAddr.isLinkLocalAddress();
					if (isSiteLocal || isLinkLocal) {
						if (isSiteLocal && !allowSiteLocal) {
							continue;
						}
						if (isLinkLocal && !allowLinkLocal) {
							continue;
						}
					} else {
						if (!allowGlobal) {
							continue;
						}
					}
					oqScopeKey = scopeKey(isSiteLocal, isLinkLocal);
					InetAddress oVerMatch = null;
					if (inetAddr instanceof Inet4Address) {
						if (ipVersion == 4 || ipVersion == 0) {
							oVerMatch = inetAddr;
						}
					} else if (inetAddr instanceof Inet6Address) {
						if (ipVersion == 6 || ipVersion == 0) {
							oVerMatch = inetAddr;
						}
					}
					if (oVerMatch == null) {
						continue;
					}
					InetAddress oIPMatch = null;
					if (oIPFilter == null) {
						oIPMatch = oVerMatch;
					} else {
						final String qIP = oVerMatch.getHostAddress();
						if (oIPFilter.matcher(qIP).matches()) {
							oIPMatch = oVerMatch;
						}
					}
					if (oIPMatch == null) {
						continue;
					}
					InetAddress oDNMatch = null;
					if (oDNFilter == null) {
						oDNMatch = oIPMatch;
					} else {
						final String quctwDomainName = oIPMatch.getCanonicalHostName().trim().toUpperCase();
						if (oDNFilter.matcher(quctwDomainName).matches()) {
							oDNMatch = oIPMatch;
						}
					}
					if (oDNMatch == null) {
						continue;
					}
					oBindAddr = oDNMatch;
				}
				if (oBindAddr != null && oqScopeKey != null) {
					final ArgonNic nic = new ArgonNic(qccNIName, ni, oBindAddr);
					zmName_NonLoopback.put(qccNIName, nic);
					addTo(zmScope_NicList, oqScopeKey, nic);
				}
			}

			final int niCount = zmName_NonLoopback.size();
			if (niCount == 0) return null;

			ArgonNic oPrefNic = null;
			for (int i = 0; oPrefNic == null && i < xptNetPrefs.length; i++) {
				final String qNetPref = xptNetPrefs[i];
				ArgonNic oNic = zmName_NonLoopback.get(qNetPref);
				if (oNic == null) {
					oNic = findMaxName(zmScope_NicList, qNetPref);
				}
				if (oNic != null) {
					oPrefNic = oNic;
				}
			}

			if (oPrefNic == null) {
				oPrefNic = findMaxName(zmScope_NicList);
			}
			return oPrefNic;
		} catch (final SocketException ex) {
			throw new ArgonPermissionException("Cannot discover local network interfaces", ex);
		}
	}

	public static ArgonNic findUnicastNonLoopback(String qSpec)
			throws ArgonPermissionException, ArgonFormatException {
		if (qSpec == null || qSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");
		int ipVersion = DefaultIPV;
		String qNicPrefs = DefaultNicPrefs;
		boolean allowGlobal = DefaultAllowGlobal;
		boolean allowSiteLocal = DefaultAllowSiteLocal;
		boolean allowLinkLocal = DefaultAllowLinkLocal;
		Pattern oIPFilter = null;
		Pattern oDNFilter = null;
		String ztwSpec = qSpec.trim();
		if (ztwSpec.length() > 0) {
			if (ztwSpec.charAt(0) == Prefix) {
				ztwSpec = ztwSpec.substring(1);
			}
			if (ztwSpec.length() > 0) {
				final String[] xptzSpecParts = SplitterSpec.split(ztwSpec);
				final int partCount = xptzSpecParts.length;
				final String ztwSpecPart0 = xptzSpecParts[0].trim();
				final String ztwSpecPart1 = partCount <= 1 ? "" : xptzSpecParts[1].trim();
				if (ztwSpecPart0.length() < 4) {
					final String m = "Incomplete IP version constraint '" + ztwSpecPart0 + "'";
					throw new ArgonFormatException(m);
				}
				final String qucIP = ztwSpecPart0.toUpperCase();
				final String qucVer = qucIP.substring(0, 4);
				if (qucVer.equals(IPv4)) {
					ipVersion = 4;
				} else if (qucVer.equals(IPv6)) {
					ipVersion = 6;
				} else if (qucVer.equals(IPvX1) || qucVer.equals(IPvX2)) {
					ipVersion = 0;
				} else {
					final String m = "Unsupported IP version constraint '" + qucVer + "'";
					throw new ArgonFormatException(m);
				}
				final String zuc4Tail = qucIP.substring(4);
				final int tailLen = zuc4Tail.length();
				int posScopeEnd = 0;
				boolean moreScope = true;
				while (moreScope && posScopeEnd < tailLen) {
					final char ch = zuc4Tail.charAt(posScopeEnd);
					if (ch == ScopeGlobalCh || ch == ScopeSiteLocalCh || ch == ScopeLinkLocalCh) {
						posScopeEnd++;
					} else {
						moreScope = false;
					}
				}
				final String zucScope = zuc4Tail.substring(0, posScopeEnd);
				if (zucScope.length() > 0) {
					allowGlobal = zucScope.indexOf(ScopeGlobalCh) >= 0;
					allowSiteLocal = zucScope.indexOf(ScopeSiteLocalCh) >= 0;
					allowLinkLocal = zucScope.indexOf(ScopeLinkLocalCh) >= 0;
				}
				String zucFilter = zuc4Tail.substring(posScopeEnd);
				if (zucFilter.length() > 0 && zucFilter.charAt(0) == '|') {
					zucFilter = zucFilter.substring(1);
				}
				if (zucFilter.length() > 0) {
					final char filterType = filterType(zucFilter);
					switch (filterType) {
						case FilterTypeName:
							oDNFilter = toPattern(zucFilter, RegexClassName);
						break;
						case FilterTypeHex:
							oIPFilter = toPattern(zucFilter, RegexClassHex);
						break;
						case FilterTypeDecimal:
							oIPFilter = toPattern(zucFilter, RegexClassDecimal);
					}
				}
				if (ztwSpecPart1.length() > 0) {
					qNicPrefs = ztwSpecPart1;
				}
			}
		}
		return findUnicastNonLoopback(ipVersion, allowGlobal, allowSiteLocal, allowLinkLocal, oIPFilter, oDNFilter, qNicPrefs);
	}

	public static boolean isNicSpec(String qSpec) {
		if (qSpec == null || qSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return qSpec.charAt(0) == Prefix;
	}

	public static InetAddress selectLoopbackIPv4InetAddress() {
		try {
			return InetAddress.getByName("127.0.0.1");
		} catch (final UnknownHostException ex) {
			throw new IllegalStateException("Cannot resolve IPv4 loopback address");
		}
	}

	private static class NicList {

		public void add(ArgonNic nic) {
			assert nic != null;
			m_xl.add(nic);
		}

		public ArgonNic selectMaxName() {
			final int count = m_xl.size();
			ArgonNic max = m_xl.get(0);
			for (int i = 1; i < count; i++) {
				final ArgonNic nic = m_xl.get(i);
				final int cmp = nic.qccName.compareTo(max.qccName);
				if (cmp > 0) {
					max = nic;
				}
			}
			return max;
		}

		public NicList(ArgonNic nic) {
			assert nic != null;
			m_xl.add(nic);
		}
		private final List<ArgonNic> m_xl = new ArrayList<ArgonNic>();
	}

}
