/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
public class BerylliumContentType {

	private static final Pattern ParamCharset = Pattern.compile(";\\s*charset=([-\\w]+)?");
	private static final Pattern Top = Pattern.compile("([-.\\w]+)[/]([-.\\w]+)([+]xml)?(.+)?");

	public boolean application() {
		return m_qlctwMajor.equals("application");
	}

	public Charset charset(Charset defaultCharset) {
		if (defaultCharset == null) throw new IllegalArgumentException("object is null");
		return m_oCharset == null ? defaultCharset : m_oCharset;
	}

	public Charset getCharset() {
		return m_oCharset;
	}

	public boolean image() {
		return m_qlctwMajor.equals("image");
	}

	public String qlctwMajor() {
		return m_qlctwMajor;
	}

	public String qlctwMinor() {
		return m_qlctwMinor;
	}

	public boolean text() {
		return m_qlctwMajor.equals("text");
	}

	public boolean text_plain() {
		return text() && m_qlctwMinor.equals("plain");
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_qlctwMajor);
		sb.append('/');
		sb.append(m_qlctwMinor);
		if (m_xml) {
			sb.append("+xml");
		}
		if (m_oCharset != null) {
			sb.append(";charset=");
			sb.append(ArgonText.charsetName(m_oCharset).toLowerCase());
		}
		return sb.toString();
	}

	public boolean www_form_urlencoded() {
		return application() && m_qlctwMinor.equals("x-www-form-urlencoded");
	}

	public boolean xml() {
		return m_xml || m_qlctwMinor.equals("xml");
	}

	private static Charset findCharset(String ozName) {
		final String zlctwName = zlctw(ozName);
		if (zlctwName.length() == 0) return null;
		try {
			return ArgonText.selectCharset(zlctwName);
		} catch (final ArgonApiException ex) {
		}
		return null;
	}

	private static Charset parseParamCharset(String ozlcTail) {
		if (ozlcTail == null) return null;
		final String zlctwTail = ozlcTail.trim();
		if (zlctwTail.length() == 0) return null;
		final Matcher matcher = ParamCharset.matcher(zlctwTail);
		if (!matcher.find()) return null;
		final String qlctwName = matcher.group(1);
		return findCharset(qlctwName);
	}

	private static String zlctw(String oz) {
		return oz == null ? "" : oz.trim().toLowerCase();
	}

	public static BerylliumContentType createInstance(Request rq) {
		if (rq == null) throw new IllegalArgumentException("object is null");
		return createInstance(rq.getContentType());
	}

	public static BerylliumContentType createInstance(String ozContentType) {
		final String zlctwContentType = zlctw(ozContentType);
		if (zlctwContentType.length() == 0) return null;
		final Matcher matcher = Top.matcher(zlctwContentType);
		if (!matcher.find()) return null;

		final String qlctwMajor = matcher.group(1);
		final String qlctwMinor = matcher.group(2);
		final boolean xml = matcher.group(3) != null;
		final String ozlcTail = matcher.group(4);
		final Charset oCharset = parseParamCharset(ozlcTail);
		return new BerylliumContentType(qlctwMajor, qlctwMinor, xml, oCharset);
	}

	public static BerylliumContentType newInstance(Request rq)
			throws BerylliumHttpBadRequestException {
		final String ozContentType = rq.getContentType();
		if (ozContentType == null) throw new BerylliumHttpBadRequestException("Missing HTTP ContentType");
		final BerylliumContentType oContentType = BerylliumContentType.createInstance(ozContentType);
		if (oContentType == null) throw new BerylliumHttpBadRequestException("Malformed HTTP ContentType");
		return oContentType;
	}

	private BerylliumContentType(String qlctwMajor, String qlctwMinor, boolean xml, Charset oCharset) {
		assert qlctwMajor != null && qlctwMajor.length() > 0;
		assert qlctwMinor != null && qlctwMinor.length() > 0;
		m_qlctwMajor = qlctwMajor;
		m_qlctwMinor = qlctwMinor;
		m_xml = xml;
		m_oCharset = oCharset;
	}

	private final String m_qlctwMajor;
	private final String m_qlctwMinor;
	private final boolean m_xml;
	private final Charset m_oCharset;
}
