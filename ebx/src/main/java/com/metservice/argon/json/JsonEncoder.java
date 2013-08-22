/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import java.util.List;

import com.metservice.argon.Binary;

/**
 * @author roach
 */
public class JsonEncoder {

	private static final char Indenter = ' ';
	public static final JsonEncoder Default = new JsonEncoder(1024, 0, false, true, false);
	public static final JsonEncoder Standard = new JsonEncoder(1024, 0, true, true, false);
	public static final JsonEncoder Debug = new JsonEncoder(1024, 2, false, true, false);

	private static void encodeUni(StringBuilder sb, int ch) {
		sb.append("\\u");
		final String hex = Integer.toHexString(ch).toUpperCase();
		final int pad = 4 - hex.length();
		for (int i = 0; i < pad; i++) {
			sb.append('0');
		}
		sb.append(hex);
	}

	private void addName(StringBuilder sb, int depth, boolean tail, String oqname) {
		indent(sb, depth);
		if (tail) {
			sb.append(',');
		}
		if (oqname != null) {
			if (m_quotedProperties) {
				sb.append('"');
			}
			sb.append(oqname);
			if (m_quotedProperties) {
				sb.append('"');
			}
			sb.append(':');
		}
	}

	private void encodeArray(StringBuilder sb, int depth, IJsonArray jarray) {
		assert sb != null;
		assert jarray != null;
		sb.append('[');
		final int memberCount = jarray.jsonMemberCount();
		boolean tail = false;
		for (int i = 0; i < memberCount; i++) {
			final IJsonValue ojvalue = jarray.jsonValue(i);
			if (ojvalue == null) {
				final String m = "Array member " + i + " is java-null";
				throw new IllegalStateException(m);
			}
			if (encodeValue(sb, depth + 1, tail, null, ojvalue)) {
				tail = true;
			}
		}
		indent(sb, depth);
		sb.append(']');
	}

	private void encodeObject(StringBuilder sb, int depth, IJsonObject jobject) {
		assert sb != null;
		assert jobject != null;
		final List<String> ozlNames = jobject.jsonNames();
		if (ozlNames == null) {
			final String m = "Object has a java-null property name list";
			throw new IllegalStateException(m);
		}
		final int pairCount = ozlNames.size();
		sb.append('{');
		boolean tail = false;
		for (int i = 0; i < pairCount; i++) {
			final String ozname = ozlNames.get(i);
			if (ozname == null) {
				final String m = "Object property index " + i + " has a java-null name";
				throw new IllegalStateException(m);
			}
			final String ztwname = ozname.trim();
			if (ztwname.length() == 0) {
				final String m = "Object property index " + i + " has an empty name";
				throw new IllegalStateException(m);
			}
			final IJsonValue ojvalue = jobject.jsonValue(ozname);
			if (ojvalue == null) {
				final String m = "Object property " + ztwname + " is java-null";
				throw new IllegalStateException(m);
			}
			if (encodeValue(sb, depth + 1, tail, ztwname, ojvalue)) {
				tail = true;
			}
		}
		indent(sb, depth);
		sb.append('}');
	}

	private boolean encodeValue(StringBuilder sb, int depth, boolean tail, String oqname, IJsonValue jvalue) {
		assert jvalue != null;
		if (jvalue instanceof IJsonString) {
			addName(sb, depth, tail, oqname);
			encodeValueString(sb, (IJsonString) jvalue);
			return true;
		}
		if (jvalue instanceof IJsonNumber) {
			addName(sb, depth, tail, oqname);
			encodeValueNumber(sb, (IJsonNumber) jvalue);
			return true;
		}
		if (jvalue instanceof IJsonBoolean) {
			addName(sb, depth, tail, oqname);
			encodeValueBoolean(sb, (IJsonBoolean) jvalue);
			return true;
		}
		if (jvalue instanceof IJsonNull) {
			addName(sb, depth, tail, oqname);
			encodeValueNull(sb);
			return true;
		}
		if (jvalue instanceof IJsonBinary) {
			addName(sb, depth, tail, oqname);
			encodeValueBinary(sb, (IJsonBinary) jvalue);
			return true;
		}
		if (jvalue instanceof IJsonObject) {
			addName(sb, depth, tail, oqname);
			encodeObject(sb, depth, (IJsonObject) jvalue);
			return true;
		}
		if (jvalue instanceof IJsonArray) {
			addName(sb, depth, tail, oqname);
			encodeArray(sb, depth, (IJsonArray) jvalue);
			return true;
		}
		return false;
	}

	private void encodeValueBinary(StringBuilder sb, IJsonBinary jvalue) {
		assert jvalue != null;
		final Binary datum = jvalue.jsonDatum();
		final String zB64 = datum.newB64MIME();
		sb.append('~');
		sb.append(zB64);
		sb.append('~');
	}

	private void encodeValueBoolean(StringBuilder sb, IJsonBoolean jvalue) {
		assert jvalue != null;
		final boolean datum = jvalue.jsonDatum();
		sb.append(datum ? "true" : "false");
	}

	private void encodeValueNull(StringBuilder sb) {
		sb.append("null");
	}

	private void encodeValueNumber(StringBuilder sb, IJsonNumber jvalue) {
		assert jvalue != null;
		if (jvalue instanceof IJsonNumberInteger) {
			final long datum = ((IJsonNumberInteger) jvalue).jsonDatum();
			sb.append(Long.toString(datum));
			return;
		}
		if (jvalue instanceof IJsonNumberTime) {
			final long datum = ((IJsonNumberTime) jvalue).jsonDatum();
			sb.append(Long.toString(datum));
			sb.append(CJson.SuffixTime);
			return;
		}
		if (jvalue instanceof IJsonNumberDouble) {
			final double datum = ((IJsonNumberDouble) jvalue).jsonDatum();
			sb.append(Double.toString(datum));
			return;
		}
		if (jvalue instanceof IJsonNumberElapsed) {
			final long datum = ((IJsonNumberElapsed) jvalue).jsonDatum();
			sb.append(Long.toString(datum));
			sb.append(CJson.SuffixElapsed);
			return;
		}
		final String m = "Unexpected number class " + jvalue.getClass();
		throw new UnsupportedOperationException(m);
	}

	private void encodeValueString(StringBuilder sb, IJsonString jstring) {
		assert jstring != null;
		final String ozdatum = jstring.jsonDatum();
		if (ozdatum == null) {
			final String m = "String value encoder returned java-null";
			throw new IllegalStateException(m);
		}
		final int len = ozdatum.length();
		if (len == 0) {
			sb.append("\"\"");
			return;
		}
		sb.append('"');
		for (int i = 0; i < len; i++) {
			final char ch = ozdatum.charAt(i);
			final int ic = ch;
			if (ic < 32) {
				switch (ch) {
					case '\b':
						sb.append("\\b");
					break;
					case '\f':
						sb.append("\\f");
					break;
					case '\n':
						sb.append("\\n");
					break;
					case '\r':
						sb.append("\\r");
					break;
					case '\t':
						sb.append("\\t");
					break;
					default: {
						encodeUni(sb, ch);
					}
				}
			} else if (ic >= 127) {
				if (m_escape0080) {
					encodeUni(sb, ch);
				} else {
					sb.append(ch);
				}
			} else {
				switch (ch) {
					case '"':
						sb.append("\\\"");
					break;
					case '\\':
						sb.append("\\\\");
					break;
					case '/':
						sb.append(m_escapeFwdSlash ? "\\/" : "/");
					break;
					default:
						sb.append(ch);
				}
			}
		}

		sb.append('"');
	}

	private void indent(StringBuilder sb, int depth) {
		if (m_indent > 0 && depth > 0) {
			sb.append('\n');
			for (int d = 0; d < depth; d++) {
				for (int i = 0; i < m_indent; i++) {
					sb.append(Indenter);
				}
			}
		}
	}

	public String encode(IJsonValue root) {
		if (root == null) throw new IllegalArgumentException("object is null");
		final StringBuilder sb = new StringBuilder(m_initialCapacity);
		encodeValue(sb, 0, false, null, root);
		return sb.toString();
	}

	public JsonEncoder(int initialCapacity, int indent, boolean quotedProperties, boolean escape0080, boolean escapeFwdSlash) {
		m_initialCapacity = initialCapacity;
		m_indent = indent;
		m_quotedProperties = quotedProperties;
		m_escape0080 = escape0080;
		m_escapeFwdSlash = escapeFwdSlash;
	}
	private final int m_initialCapacity;
	private final int m_indent;
	private final boolean m_quotedProperties;
	private final boolean m_escape0080;
	private final boolean m_escapeFwdSlash;
}
