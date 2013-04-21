/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.metservice.argon.ArgonFormatException;

/**
 * @author roach
 */
public class DefaultSaxJsonFactory implements ISaxJsonFactory {

	@Override
	public boolean allowMultipleChildren(String ename, String pename) {
		return !m_epathSingletonChildren.contains(epath(ename, pename));
	}

	@Override
	public IJsonNative createAttribute(String aname, String zValue, String ename)
			throws NumberFormatException, ArgonFormatException, JsonSchemaException {
		return JsonString.newInstance(zValue);
	}

	@Override
	public IJsonNative createText(String zValue, String ename)
			throws NumberFormatException, ArgonFormatException, JsonSchemaException {
		final String ztwValue = zValue.trim();
		return (ztwValue.length() == 0) ? null : JsonString.newInstance(ztwValue);
	}

	public void declareRequiredAttributes(String ename, Set<String> anames) {
		final List<String> zl = new ArrayList<String>(anames);
		Collections.sort(zl);
		m_ename_requiredAnames.put(ename, zl.toArray(new String[zl.size()]));
	}

	public void declareRequiredAttributes(String ename, String... anames) {
		final Set<String> zs = new HashSet<String>(anames.length);
		for (int i = 0; i < anames.length; i++) {
			zs.add(anames[i]);
		}
		declareRequiredAttributes(ename, zs);
	}

	public void declareSimpleText(String ename) {
		if (ename == null || ename.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_enameSimpleText.add(ename);
	}

	public void declareSingleton(String enameChild, String enameParent) {
		if (enameChild == null || enameChild.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (enameParent == null || enameParent.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_epathSingletonChildren.add(epath(enameChild, enameParent));
	}

	public void declareTextAttributeName(String ename, String pname) {
		if (ename == null || ename.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (pname == null || pname.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_ename_textAName.put(ename, pname);
	}

	public void declareUriPrefix(String uri, String zPrefix) {
		if (uri == null || uri.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (zPrefix == null) throw new IllegalArgumentException("object is null");
		m_uri_prefix.put(uri, zPrefix);
	}

	@Override
	public String getTextAttributeName(String ename) {
		if (ename == null || ename.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String oqpname = m_ename_textAName.get(ename);
		return oqpname == null ? SaxJsonDecoder.PropertyName_text : oqpname;
	}

	@Override
	public boolean isSimpleText(String ename) {
		return m_enameSimpleText.contains(ename);
	}

	@Override
	public String propertyName(String uri, String localName) {
		if (uri.length() == 0) return localName;
		final String ozPrefix = m_uri_prefix.get(uri);
		if (ozPrefix == null || ozPrefix.length() == 0) return localName;
		return ozPrefix + localName;
	}

	@Override
	public void validateAttributes(String ename, String[] anames)
			throws JsonSchemaException {
		final String[] ozptRequiredAnames = m_ename_requiredAnames.get(ename);
		if (ozptRequiredAnames == null || ozptRequiredAnames.length == 0) return;
		int isource = 0;
		int ireqd = 0;
		while (isource < anames.length && ireqd < ozptRequiredAnames.length) {
			final String sourceName = anames[isource];
			final String reqdName = ozptRequiredAnames[ireqd];
			final int cmp = sourceName.compareTo(reqdName);
			if (cmp < 0) {
				isource++;
			} else if (cmp > 0) {
				final String msg = "Missing required attribute '" + reqdName + "'";
				throw new JsonSchemaException(msg);
			} else {
				isource++;
				ireqd++;
			}
		}
		if (ireqd < ozptRequiredAnames.length) {
			final String reqdName = ozptRequiredAnames[ireqd];
			final String msg = "Missing required attribute '" + reqdName + "'";
			throw new JsonSchemaException(msg);
		}
	}

	private static String epath(String ename, String pename) {
		return pename + ">" + ename;
	}

	public DefaultSaxJsonFactory() {
	}
	private final Set<String> m_enameSimpleText = new HashSet<String>();
	private final Set<String> m_epathSingletonChildren = new HashSet<String>();
	private final Map<String, String> m_ename_textAName = new HashMap<String, String>();
	private final Map<String, String[]> m_ename_requiredAnames = new HashMap<String, String[]>();
	private final Map<String, String> m_uri_prefix = new HashMap<String, String>();
}
