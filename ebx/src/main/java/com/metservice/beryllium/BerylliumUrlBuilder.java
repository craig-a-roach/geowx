/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.URIUtil;

import com.metservice.argon.ArgonText;
import com.metservice.argon.CArgon;

/**
 * @author roach
 */
public class BerylliumUrlBuilder {

	private void makeHost(StringBuilder sb) {
		if (m_oqtwHost == null) {
			sb.append("localhost");
		} else {
			sb.append(m_oqtwHost);
		}
	}

	private void makePathQueryFragment(StringBuilder sb, boolean requireAbsolute) {
		if (m_oPath != null) {
			if (requireAbsolute && !m_oPath.isAbsolute) {
				sb.append('/');
			}
			sb.append(m_oPath.ztwPathEncoded());
			if (m_oQuery != null) {
				sb.append('?');
				sb.append(m_oQuery.format());
			}
			if (m_oqtwFragment != null) {
				sb.append('#');
				URIUtil.encodePath(sb, m_oqtwFragment);
			}
		}
	}

	private void makePort(StringBuilder sb) {
		if (m_oPort != null) {
			sb.append(':');
			sb.append(m_oPort.intValue());
		}
	}

	private void makeScheme(StringBuilder sb) {
		if (m_oqtwScheme == null) {
			sb.append("http");
		} else {
			sb.append(m_oqtwScheme);
		}
		sb.append("://");
	}

	public void addPath(BerylliumPath oPath) {
		if (oPath == null) return;
		if (m_oPath == null) {
			m_oPath = oPath;
		} else {
			m_oPath = m_oPath.newPath(oPath);
		}
	}

	public BerylliumPath getPath() {
		return m_oPath;
	}

	public String qtwEncoded() {
		return m_oqtwHost == null ? qtwEncodedBaselined() : qtwEncodedAbsolute();
	}

	public String qtwEncodedAbsolute() {
		final StringBuilder sb = new StringBuilder();
		makeScheme(sb);
		makeHost(sb);
		makePort(sb);
		makePathQueryFragment(sb, true);
		return sb.toString();
	}

	public String qtwEncodedBaselined() {
		final StringBuilder sb = new StringBuilder();
		makePathQueryFragment(sb, false);
		return sb.toString();
	}

	public void setFragment(String ozFragment) {
		m_oqtwFragment = ArgonText.oqtw(ozFragment);
	}

	public void setHost(String ozHost) {
		m_oqtwHost = ArgonText.oqtw(ozHost);
	}

	public void setPath(BerylliumPath oPath) {
		m_oPath = oPath;
	}

	public void setPort(int port) {
		if (port > 0 && port < CArgon.LIMIT_PORT_HI) {
			m_oPort = new Integer(port);
		}
	}

	public void setQuery(BerylliumQuery oQuery) {
		m_oQuery = oQuery;
	}

	public void setScheme(String ozScheme) {
		m_oqtwScheme = ArgonText.oqtw(ozScheme);
	}

	public void setSchemeHostPort(Request oRequest) {
		if (oRequest == null) return;
		setScheme(oRequest.getScheme());
		setHost(oRequest.getServerName());
		setPort(oRequest.getServerPort());
	}

	public void setSchemeHostPortPath(Request oRequest, int upNodes) {
		if (oRequest == null) return;
		setSchemeHostPort(oRequest);
		final BerylliumPath path = BerylliumPath.newInstance(oRequest);
		final int cindexToex = Math.max(0, path.depth - upNodes);
		m_oPath = path.subPathHead(cindexToex);
	}

	@Override
	public String toString() {
		return qtwEncoded();
	}

	public BerylliumUrlBuilder() {
	}

	private String m_oqtwScheme;
	private String m_oqtwHost;
	private Integer m_oPort;
	private BerylliumPath m_oPath;
	private BerylliumQuery m_oQuery;
	private String m_oqtwFragment;
}
