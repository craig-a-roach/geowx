/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Binary;

/**
 * @author roach
 */
public class SaxJsonDecoder {

	public static final String PropertyName_text = "text";
	public static final String PropertyName_error = "_error";
	public static final String PropertyName_error_schema = "schema";
	public static final String PropertyName_error_sax = "sax";
	public static final String PropertyName_error_rejected = "rejected";
	public static final String PropertyName_xmlElement = "_xmlElement";

	private static JsonObject parseCompact(InputStream inputStream, SAXParser saxParser, ISaxJsonFactory oFactory) {
		assert inputStream != null;
		assert saxParser != null;
		final JsonObject root = JsonObject.constructDecodeTarget(4);
		final JsonArray schemaErrors = JsonArray.newImmutable(4);
		final JsonArray rejected = JsonArray.newImmutable(4);
		final CompactHandler jh = new CompactHandler(root, schemaErrors, rejected, oFactory);
		final JsonArray saxErrors = JsonArray.newImmutable(4);
		try {
			saxParser.parse(inputStream, jh);
		} catch (final SAXException ex) {
			saxErrors.jsonAdd(JsonString.newInstance(ex.getMessage()));
		} catch (final IOException ex) {
			throw new IllegalStateException("SAX cannot read binary source", ex);
		}
		if (schemaErrors.isEmpty() && saxErrors.isEmpty()) return root;
		final JsonObject error = JsonObject.constructDecodeTarget(2);
		if (!schemaErrors.isEmpty()) {
			error.jsonPut(PropertyName_error_schema, schemaErrors);
		}
		if (!saxErrors.isEmpty()) {
			error.jsonPut(PropertyName_error_sax, saxErrors);
		}
		if (!rejected.isEmpty()) {
			error.jsonPut(PropertyName_error_rejected, rejected);
		}
		root.jsonPut(PropertyName_error, error);
		return root;
	}

	public static JsonObject getError(JsonObject result) {
		if (result == null) throw new IllegalArgumentException("object is null");
		final JsonAccessor aError = result.accessor(PropertyName_error);
		try {
			return aError.isDefinedNonNull() ? aError.datumObject() : null;
		} catch (final JsonSchemaException ex) {
			throw new IllegalArgumentException("Unexpected error property");
		}
	}

	public static JsonObject parseCompact(Binary source, ISaxJsonFactory oFactory) {
		if (source == null) throw new IllegalArgumentException("object is null");
		final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		saxFactory.setNamespaceAware(true);
		saxFactory.setValidating(false);
		try {
			return parseCompact(source.getInputStream(), saxFactory.newSAXParser(), oFactory);
		} catch (final ParserConfigurationException ex) {
			throw new IllegalStateException("Cannot create SAX parser", ex);
		} catch (final SAXException ex) {
			throw new IllegalStateException("Cannot create SAX parser", ex);
		}
	}

	public static JsonObject parseCompact(InputStream ins, int bcQuota, ISaxJsonFactory oFactory)
			throws ArgonQuotaException, ArgonStreamReadException {
		return parseCompact(Binary.newFromInputStream(ins, bcQuota), oFactory);
	}

	public static JsonObject parseCompact(String ozSource, ISaxJsonFactory oFactory) {
		return parseCompact(Binary.newFromStringUTF8(ozSource), oFactory);
	}

	private static class CompactHandler extends DefaultHandler {

		private boolean allowMultipleChildren(String ename, String pename) {
			if (m_oFactory == null) return true;
			return m_oFactory.allowMultipleChildren(ename, pename);
		}

		private IJsonNative createAttributeValue(String aname, String zValue, String ename) {
			if (m_oFactory == null) return JsonString.newInstance(zValue);
			try {
				return m_oFactory.createAttribute(aname, zValue, ename);
			} catch (final NumberFormatException ex) {
				final String msg = "Non-numeric " + aename(aname, ename) + " value '" + zValue + "'";
				m_schemaErrors.jsonAdd(JsonString.newInstance(msg));
			} catch (final ArgonFormatException ex) {
				final String msg = "Malformed " + aename(aname, ename) + "..." + ex.getMessage();
				m_schemaErrors.jsonAdd(JsonString.newInstance(msg));
			} catch (final JsonSchemaException ex) {
				final String msg = "Invalid " + aename(aname, ename) + "..." + ex.getMessage();
				m_schemaErrors.jsonAdd(JsonString.newInstance(msg));
			}
			return null;
		}

		private IJsonNative createTextValue(Node ended) {
			final String zValue = ended.btext.toString();
			final boolean isSimpleText = isSimpleText(ended.pname);
			final IJsonNative oTextValue = createTextValue(zValue, ended.pname);
			if (isSimpleText) return oTextValue;
			if (oTextValue == null) return ended.value;
			final String oqtwTextPropertyName = oqtwTextPropertyName(ended.pname);
			if (oqtwTextPropertyName != null) {
				ended.value.jsonPut(oqtwTextPropertyName, oTextValue);
			}
			return ended.value;
		}

		private IJsonNative createTextValue(String zValue, String ename) {
			if (m_oFactory == null) {
				final String ztwValue = zValue.trim();
				return ztwValue.length() == 0 ? null : JsonString.newInstance(ztwValue);
			}
			try {
				return m_oFactory.createText(zValue, ename);
			} catch (final NumberFormatException ex) {
				final String msg = "Non-numeric text value '" + zValue + "'";
				m_schemaErrors.jsonAdd(JsonString.newInstance(msg));
			} catch (final ArgonFormatException ex) {
				final String msg = "Malformed text value..." + ex.getMessage();
				m_schemaErrors.jsonAdd(JsonString.newInstance(msg));
			} catch (final JsonSchemaException ex) {
				final String msg = "Invalid text value..." + ex.getMessage();
				m_schemaErrors.jsonAdd(JsonString.newInstance(msg));
			}
			return null;
		}

		private boolean isSimpleText(String ename) {
			if (m_oFactory == null) return false;
			return m_oFactory.isSimpleText(ename);
		}

		private String oqtwTextPropertyName(String ename) {
			if (m_oFactory == null) return PropertyName_text;
			final String ozName = m_oFactory.getTextAttributeName(ename);
			if (ozName == null) return null;
			final String ztwName = ozName.trim();
			return ztwName.length() == 0 ? null : ztwName;
		}

		private String pname(String uri, String localName) {
			if (m_oFactory == null) return localName;
			return m_oFactory.propertyName(uri, localName);
		}

		private boolean validAttributes(String ename, List<String> zlANamesAsc) {
			if (m_oFactory == null) return true;
			final String[] anamesAsc = zlANamesAsc.toArray(new String[zlANamesAsc.size()]);
			try {
				m_oFactory.validateAttributes(ename, anamesAsc);
				return true;
			} catch (final JsonSchemaException ex) {
				final String msg = "Invalid '" + ename + "' element..." + ex.getMessage();
				m_schemaErrors.jsonAdd(JsonString.newInstance(msg));
			}
			return false;
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			final String zFrag = String.valueOf(ch, start, length);
			final Node oLast = m_stack.peekLast();
			if (oLast == null) throw new SAXException("Unexpected text");
			oLast.btext.append(zFrag);
		}

		@Override
		public void endElement(String uri, String localName, String qualName)
				throws SAXException {
			final Node oEnded = m_stack.pollLast();
			if (oEnded == null) throw new SAXException("Unbalanced element; " + qualName);
			final String pnameElement = oEnded.pname;

			if (oEnded.isRejected()) {
				oEnded.value.jsonPut(PropertyName_xmlElement, JsonString.newInstance(oEnded.pname));
				m_rejected.jsonAdd(oEnded.value);
				return;
			}

			final IJsonNative oNeo = createTextValue(oEnded);
			if (oNeo == null) return;

			final Node vContainer = m_stack.peekLast();
			if (vContainer == null) {
				m_root.jsonPut(pnameElement, oNeo);
			} else {
				final boolean allowMultipleChildren = allowMultipleChildren(pnameElement, vContainer.pname);
				final IJsonNative oDestValue = vContainer.value.get(pnameElement);
				if (oDestValue == null) {
					if (allowMultipleChildren) {
						final JsonArray destArray = JsonArray.newImmutable(16);
						vContainer.value.jsonPut(pnameElement, destArray);
						destArray.jsonAdd(oNeo);
					} else {
						vContainer.value.jsonPut(pnameElement, oNeo);
					}
				} else {
					if (allowMultipleChildren) {
						if (oDestValue instanceof JsonArray) {
							final JsonArray destArray = (JsonArray) oDestValue;
							destArray.jsonAdd(oNeo);
						} else {
							final String m = "Invalid '" + qualName + "' element";
							throw new SAXException(m);
						}
					} else {
						final String m = "Multiple '" + qualName + "' elements not allowed";
						throw new SAXException(m);
					}
				}
			}
		}

		@Override
		public void startElement(String uri, String localName, String qualName, Attributes attributes)
				throws SAXException {
			final String pnameElement = pname(uri, localName);
			final int acount = attributes.getLength();
			final JsonObject neo = JsonObject.constructDecodeTarget(acount + 1);
			final List<String> anames = new ArrayList<String>(acount);
			for (int i = 0; i < acount; i++) {
				final String ozALocalName = attributes.getLocalName(i);
				if (ozALocalName == null || ozALocalName.length() == 0) {
					continue;
				}
				final String ozAUri = attributes.getURI(i);
				if (ozAUri == null) {
					continue;
				}
				final String pnameAttribute = pname(ozAUri, ozALocalName);
				final String ozAValue = attributes.getValue(i);
				if (ozAValue == null) {
					continue;
				}
				final IJsonNative oattValue = createAttributeValue(pnameAttribute, ozAValue, pnameElement);
				if (oattValue != null) {
					neo.jsonPut(pnameAttribute, oattValue);
					anames.add(pnameAttribute);
				}
			}
			Collections.sort(anames);
			final Node neoNode = new Node(pnameElement, neo);

			if (!validAttributes(pnameElement, anames)) {
				neoNode.enableRejected();
			}
			m_stack.add(neoNode);
		}

		private static String aename(String aname, String ename) {
			return ename + "[" + aname + "]";
		}

		public CompactHandler(JsonObject root, JsonArray schemaErrors, JsonArray rejected, ISaxJsonFactory oFactory) {
			assert root != null;
			assert schemaErrors != null;
			assert rejected != null;
			m_root = root;
			m_schemaErrors = schemaErrors;
			m_rejected = rejected;
			m_oFactory = oFactory;
		}
		private final JsonObject m_root;
		private final JsonArray m_schemaErrors;
		private final JsonArray m_rejected;
		private final ISaxJsonFactory m_oFactory;
		private final LinkedList<Node> m_stack = new LinkedList<Node>();
	}

	private static class Node {

		public void enableRejected() {
			m_rejected = true;
		}

		public boolean isRejected() {
			return m_rejected;
		}

		Node(String pname, JsonObject value) {
			assert pname != null && pname.length() > 0;
			assert value != null;
			this.pname = pname;
			this.value = value;
		}
		public final String pname;
		public final JsonObject value;
		public final StringBuilder btext = new StringBuilder();
		private boolean m_rejected;
	}
}
