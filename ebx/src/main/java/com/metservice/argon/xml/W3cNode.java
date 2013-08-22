/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.metservice.argon.ArgonTransformer;
import com.metservice.argon.Binary;

/**
 * @author roach
 */
public class W3cNode {
	private W3cNode createNode(Node oNode) {
		if (oNode == null) return null;
		return new W3cNode(m_odom, oNode);
	}

	private W3cNode newNode(Node node) {
		assert node != null;
		return new W3cNode(m_odom, node);
	}

	void lock() {
		if (m_odom != null) {
			m_odom.lock();
		}
	}

	Node node() {
		return m_node;
	}

	void unlock() {
		if (m_odom != null) {
			m_odom.unlock();
		}
	}

	public Object applyXPath(XPathExpression expression, QName returnType)
			throws XPathExpressionException {
		if (expression == null) throw new IllegalArgumentException("object is null");
		if (returnType == null) throw new IllegalArgumentException("object is null");
		lock();
		try {
			return expression.evaluate(m_node, returnType);
		} finally {
			unlock();
		}
	}

	public boolean containsText() {
		lock();
		try {
			return containsText(m_node);
		} finally {
			unlock();
		}
	}

	public List<W3cNode> getChildNodes(boolean includeElementContentWhitespace) {
		lock();
		try {
			final NodeList nodeList = m_node.getChildNodes();
			final int listLength = nodeList.getLength();
			final List<W3cNode> zlMembers = new ArrayList<W3cNode>(listLength);
			for (int i = 0; i < listLength; i++) {
				final Node item = nodeList.item(i);
				boolean include = true;
				if (!includeElementContentWhitespace) {
					if (item instanceof Text) {
						final String ozTextData = ((Text) item).getData();
						final String ztwTextData = ozTextData == null ? "" : ozTextData.trim();
						include = ztwTextData.length() > 0;
					}
				}
				if (include) {
					zlMembers.add(newNode(item));
				}
			}
			return zlMembers;
		} finally {
			unlock();
		}
	}

	public W3cNode getFirstChild() {
		lock();
		try {
			return createNode(m_node.getFirstChild());
		} finally {
			unlock();
		}
	}

	public W3cNode getLastChild() {
		lock();
		try {
			return createNode(m_node.getLastChild());
		} finally {
			unlock();
		}
	}

	public String getLocalName() {
		lock();
		try {
			return m_node.getLocalName();
		} finally {
			unlock();
		}
	}

	public String getNamespaceURI() {
		lock();
		try {
			return m_node.getNamespaceURI();
		} finally {
			unlock();
		}
	}

	public W3cNode getNextSibling() {
		lock();
		try {
			return createNode(m_node.getNextSibling());
		} finally {
			unlock();
		}
	}

	public String getNodeName() {
		lock();
		try {
			return m_node.getNodeName();
		} finally {
			unlock();
		}
	}

	public int getNodeType() {
		lock();
		try {
			return m_node.getNodeType();
		} finally {
			unlock();
		}
	}

	public String getNodeValue() {
		lock();
		try {
			return m_node.getNodeValue();
		} finally {
			unlock();
		}
	}

	public W3cNode getParent() {
		lock();
		try {
			return createNode(m_node.getParentNode());
		} finally {
			unlock();
		}
	}

	public String getPrefix() {
		lock();
		try {
			return m_node.getPrefix();
		} finally {
			unlock();
		}
	}

	public boolean hasChildNodes() {
		lock();
		try {
			return m_node.hasChildNodes();
		} finally {
			unlock();
		}
	}

	public boolean isAttribute() {
		return (m_node instanceof Attr);
	}

	public boolean isCharacterData() {
		return (m_node instanceof CharacterData);
	}

	public boolean isDocument() {
		return (m_node instanceof Document);
	}

	public boolean isElement() {
		return (m_node instanceof Element);
	}

	public boolean isHtmlPre() {
		lock();
		try {
			final String zNS = z(m_node.getNamespaceURI());
			if (zNS.length() == 0 || zNS.equals(INTRINSIC_NS) || PATT_XHTMLNS_URI.matcher(zNS).matches()) {
				final String zlcLocal = z(m_node.getLocalName()).toLowerCase();
				return zlcLocal.equals("pre");
			}
			return false;
		} finally {
			unlock();
		}
	}

	public boolean isIntrinsic() {
		lock();
		try {
			final String zNS = z(m_node.getNamespaceURI());
			final String zLocal = z(m_node.getLocalName());
			if (!zNS.equals(INTRINSIC_NS)) return false;
			if (zLocal.equals(NODELIST_TAG)) return true;
			if (zLocal.equals(TEXT_TAG)) return true;
			if (zLocal.equals(ATTR_TAG)) return true;
			return false;
		} finally {
			unlock();
		}
	}

	public boolean isText() {
		return (m_node instanceof Text);
	}

	public String lookupNamespaceURI(String qPrefix) {
		if (qPrefix == null || qPrefix.length() == 0) throw new IllegalArgumentException("string is null or empty");
		lock();
		try {
			return m_node.lookupNamespaceURI(qPrefix);
		} finally {
			unlock();
		}
	}

	public String lookupPrefix(String qNamespaceURI) {
		if (qNamespaceURI == null || qNamespaceURI.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		lock();
		try {
			return m_node.lookupPrefix(qNamespaceURI);
		} finally {
			unlock();
		}
	}

	public String oqAttributeName() {
		lock();
		try {
			if (m_node instanceof Attr) return ((Attr) m_node).getName();
			return oq(m_node.getNodeName());
		} finally {
			unlock();
		}
	}

	public String oqLocalName() {
		return oq(getLocalName());
	}

	public String oqNamespaceURI() {
		return oq(getNamespaceURI());
	}

	public String oqNodeName() {
		return oq(getNodeName());
	}

	public String oqNodeValue() {
		return oq(getNodeValue());
	}

	public String oqPrefix() {
		return oq(getPrefix());
	}

	public String toFlatString(boolean spaced) {
		lock();
		try {
			return spaced ? ztwFlatSpaced() : ztwFlatConcatenated();
		} finally {
			unlock();
		}
	}

	@Override
	public String toString() {
		return m_node.getNodeName();
	}

	public Binary toXmlBinary() {
		lock();
		try {
			return Binary.newFromStringUTF8(toXml(m_node, true));
		} finally {
			unlock();
		}
	}

	public String toXmlString(boolean outer) {
		lock();
		try {
			return toXml(m_node, outer);
		} finally {
			unlock();
		}
	}

	public String zAttributeValue() {
		lock();
		try {
			if (m_node instanceof Attr) return ((Attr) m_node).getValue();
			return z(m_node.getNodeValue());
		} finally {
			unlock();
		}
	}

	public String zCharacterData() {
		lock();
		try {
			if (m_node instanceof CharacterData) return ((CharacterData) m_node).getData();
			return z(getNodeValue());
		} finally {
			unlock();
		}
	}

	public List<W3cNode> zlNodes(NodeList nodeList) {
		if (nodeList == null) throw new IllegalArgumentException("object is null");
		final int length = nodeList.getLength();
		if (length == 0) return Collections.emptyList();

		final List<W3cNode> zlNodes = new ArrayList<W3cNode>(length);
		for (int i = 0; i < length; i++) {
			final W3cNode neo = new W3cNode(m_odom, nodeList.item(i));
			zlNodes.add(neo);
		}
		return zlNodes;
	}

	public String zLocalName() {
		return z(getLocalName());
	}

	public String zLookupNamespaceURI(String qPrefix) {
		return z(lookupNamespaceURI(qPrefix));
	}

	public String zLookupPrefix(String qNamespaceURI) {
		return z(lookupPrefix(qNamespaceURI));
	}

	public Map<String, Map<String, String>> zmNamespaceURI_xmAttributeLocalName_Value() {
		if (!(m_node instanceof Element)) return Collections.emptyMap();
		final Element element = (Element) m_node;
		lock();
		try {
			final NamedNodeMap oAttributes = element.getAttributes();
			if (oAttributes == null) return Collections.emptyMap();
			final int attributeCount = oAttributes.getLength();
			if (attributeCount == 0) return Collections.emptyMap();

			final Map<String, Map<String, String>> out = new HashMap<String, Map<String, String>>();
			for (int i = 0; i < attributeCount; i++) {
				final Node attributeNode = oAttributes.item(i);
				final String ozAttrLocalName = attributeNode.getLocalName();
				if (!(attributeNode instanceof Attr)) {
					continue;
				}
				if (ozAttrLocalName == null || ozAttrLocalName.length() == 0) {
					continue;
				}

				final String qAttrLocalName = ozAttrLocalName;
				final Attr attr = (Attr) attributeNode;
				final String ozAttrNamespaceURI = attr.getNamespaceURI();
				final String zAttrNamespaceURI = (ozAttrNamespaceURI == null) ? "" : ozAttrNamespaceURI;
				if (zAttrNamespaceURI.length() == 0 || !PATT_XMLNS_URI.matcher(zAttrNamespaceURI).matches()) {
					Map<String, String> vxmAttribute = out.get(zAttrNamespaceURI);
					if (vxmAttribute == null) {
						vxmAttribute = new HashMap<String, String>();
						out.put(zAttrNamespaceURI, vxmAttribute);
					}
					final String zValue = attr.getValue();
					vxmAttribute.put(qAttrLocalName, zValue);
				}
			}
			return out;
		} finally {
			unlock();
		}
	}

	public String zNamespaceURI() {
		return z(getNamespaceURI());
	}

	public String zNodeName() {
		return z(getNodeName());
	}

	public String zNodeValue() {
		return z(getNodeValue());
	}

	public String zPrefix() {
		return z(getPrefix());
	}

	public String ztwFlatConcatenated() {
		lock();
		try {
			return ztwFlat(m_node, false);
		} finally {
			unlock();
		}
	}

	public String ztwFlatSpaced() {
		lock();
		try {
			return ztwFlat(m_node, true);
		} finally {
			unlock();
		}
	}

	private static void attributeMapXml(StringBuilder b, Element element, NSContext nscx) {
		assert element != null;
		assert nscx != null;
		final NamedNodeMap oAttributes = element.getAttributes();
		if (oAttributes == null) return;
		final int attributeCount = oAttributes.getLength();
		for (int i = 0; i < attributeCount; i++) {
			final Node attributeNode = oAttributes.item(i);
			if (attributeNode instanceof Attr) {
				attributeXml(b, (Attr) attributeNode, nscx);
			}
		}
	}

	private static void attributeXml(StringBuilder b, Attr attr, NSContext nscx) {
		assert attr != null;
		assert nscx != null;
		final String oqQualifiedName = nscx.oqQualifiedName(attr);
		if (oqQualifiedName == null) return;

		final String ozValue = attr.getValue();
		if (ozValue == null || ozValue.length() == 0) return;
		final String qEscValue = ArgonTransformer.zXmlEncode(ozValue, false, true, true, true);
		b.append(' ').append(oqQualifiedName).append("=\"").append(qEscValue).append('\"');
	}

	private static boolean containsText(Node node) {
		if (node instanceof Text) {
			final String ozTextData = ((Text) node).getData();
			final String ztwTextData = ozTextData == null ? "" : ozTextData.trim();
			return ztwTextData.length() > 0;
		}
		final NodeList childNodeList = node.getChildNodes();
		final int childListLength = childNodeList.getLength();
		for (int i = 0; i < childListLength; i++) {
			final Node child = childNodeList.item(i);
			if (containsText(child)) return true;
		}
		return false;
	}

	private static void elementXml(StringBuilder b, Element element, NSContext nscx) {
		final String oqQualifiedName = nscx.oqQualifiedName(element);
		if (oqQualifiedName == null) return;
		b.append('<').append(oqQualifiedName);
		attributeMapXml(b, element, nscx);
		final NodeList nodeList = element.getChildNodes();
		final int childNodeCount = nodeList.getLength();
		if (childNodeCount == 0) {
			b.append("/>");
		} else {
			b.append('>');
			nodeListXml(b, nodeList, nscx);
			b.append("</").append(oqQualifiedName).append(">");
		}
	}

	private static void encoding(StringBuilder b) {
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(EOL);
	}

	private static void flatztw(StringBuilder b, Node node, boolean spaced) {
		if (node instanceof Text) {
			final Text text = (Text) node;
			final String ozData = text.getData();
			final String ztwData = ozData == null ? "" : ozData.trim();
			if (ztwData.length() > 0) {
				if (spaced && b.length() > 0) {
					b.append(' ');
				}
				b.append(ztwData);
			}
		} else {
			final NodeList childNodes = node.getChildNodes();
			final int childNodeCount = childNodes.getLength();
			for (int i = 0; i < childNodeCount; i++) {
				flatztw(b, childNodes.item(i), spaced);
			}
		}
	}

	private static void nodeListXml(StringBuilder b, List<W3cNode> zlNodes, NSContext nscx) {
		final int listLength = zlNodes.size();
		for (int i = 0; i < listLength; i++) {
			final W3cNode item = zlNodes.get(i);
			item.lock();
			try {
				nodeXml(b, item.m_node, nscx);
			} finally {
				item.unlock();
			}
		}
	}

	private static void nodeListXml(StringBuilder b, NodeList nodeList, NSContext nscx) {
		final int listLength = nodeList.getLength();
		for (int i = 0; i < listLength; i++) {
			final Node item = nodeList.item(i);
			nodeXml(b, item, nscx);
		}
	}

	private static void nodeXml(StringBuilder b, Node node, NSContext nscx) {
		if (node instanceof Text) {
			textXml(b, (Text) node);
		} else if (node instanceof Element) {
			elementXml(b, (Element) node, nscx);
		}
	}

	private static String oq(String oz) {
		return oz == null || oz.length() == 0 ? null : oz;
	}

	private static String oqEscapedValue(Attr attr) {
		final String ozValue = attr.getValue();
		if (ozValue == null || ozValue.length() == 0) return null;
		return ArgonTransformer.zXmlEncode(ozValue, false, true, true, true);
	}

	private static void textXml(StringBuilder b, Text text) {
		b.append(zEscapedData(text));
	}

	private static String toAttributeXml(Attr attr, boolean outer) {
		assert attr != null;
		final StringBuilder b = new StringBuilder(256);
		if (outer) {
			encoding(b);
			b.append(INTRINSIC_ATTR);
		}
		final String oqLocalName = oqNodeLocalName(attr);
		final String oqPrefix = oqNodePrefix(attr);
		final String oqNamespaceURI = oqNodeNamespaceURI(attr);
		final String oqEscValue = oqEscapedValue(attr);
		if (oqLocalName != null && oqEscValue != null) {
			final boolean qualified = oqPrefix != null && oqNamespaceURI != null;
			b.append(' ');
			if (qualified) {
				b.append(oqPrefix).append(':');
			}
			b.append(oqLocalName).append("=\"").append(oqEscValue).append('\"');
			if (outer) {
				if (qualified) {
					b.append(" xmlns:").append(oqPrefix).append("=\"").append(oqNamespaceURI).append('\"');
				}
			}
		}
		if (outer) {
			b.append("/>");
		}
		return b.toString();
	}

	private static String toElementXml(Element element, boolean outer) {
		assert element != null;
		final NSContext nscx = new NSContext(element);
		final String oqQualifiedName = nscx.oqQualifiedName(element);
		if (oqQualifiedName == null) return "";
		final StringBuilder b = new StringBuilder(256);
		if (outer) {
			encoding(b);
		}
		b.append('<').append(oqQualifiedName);
		attributeMapXml(b, element, nscx);
		final NodeList nodeList = element.getChildNodes();
		final int childNodeCount = nodeList.getLength();
		if (childNodeCount == 0) {
			b.append("/>");
		} else {
			final StringBuilder bnodeList = new StringBuilder(4096);
			nodeListXml(bnodeList, nodeList, nscx);
			b.append(nscx);
			b.append('>');
			b.append(bnodeList);
			b.append("</").append(oqQualifiedName).append(">");
		}
		return b.toString();
	}

	private static String toNodeListXml(List<W3cNode> zlNodes, boolean outer) {
		assert zlNodes != null;
		final NSContext nscx = new NSContext();
		nscx.putPrefix(INTRINSIC_NS, INTRINSIC_PREFIX);
		final StringBuilder b = new StringBuilder(256);
		if (outer) {
			encoding(b);
			b.append('<').append(INTRINSIC_PREFIX).append(':').append(NODELIST_TAG);
			if (zlNodes.isEmpty()) {
				b.append(nscx);
				b.append("/>");
			} else {
				final StringBuilder bnodeList = new StringBuilder(4096);
				nodeListXml(bnodeList, zlNodes, nscx);
				b.append(nscx);
				b.append('>');
				b.append(bnodeList);
				b.append("</").append(INTRINSIC_PREFIX).append(':').append(NODELIST_TAG).append(">");
			}
		} else {
			nodeListXml(b, zlNodes, nscx);
		}
		return b.toString();
	}

	private static String toTextXml(String ozData, boolean outer) {
		final StringBuilder b = new StringBuilder(256);
		if (outer) {
			encoding(b);
			b.append(INTRINSIC_TEXT_OPEN);
		}
		b.append(zEscapedData(ozData));
		if (outer) {
			b.append(INTRINSIC_TEXT_CLOSE);
		}
		return b.toString();
	}

	private static String toTextXml(Text text, boolean outer) {
		assert text != null;
		final StringBuilder b = new StringBuilder(256);
		if (outer) {
			encoding(b);
			b.append(INTRINSIC_TEXT_OPEN);
		}
		b.append(zEscapedData(text));
		if (outer) {
			b.append(INTRINSIC_TEXT_CLOSE);
		}
		return b.toString();
	}

	private static String toXml(Node node, boolean outer) {
		assert node != null;
		if (node instanceof Attr) return toAttributeXml((Attr) node, outer);

		if (node instanceof Text) return toTextXml((Text) node, outer);

		if (node instanceof Document) {
			final Document doc = (Document) node;
			final Element oDocElement = doc.getDocumentElement();
			return oDocElement == null ? "" : toXml(oDocElement, outer);
		}

		if (node instanceof Element) return toElementXml((Element) node, outer);

		return "";
	}

	private static String z(String oz) {
		return oz == null ? "" : oz;
	}

	private static String zEscapedData(String ozData) {
		if (ozData == null || ozData.length() == 0) return "";
		return ArgonTransformer.zXmlEncode(ozData, false, false, false, false);
	}

	private static String zEscapedData(Text text) {
		final String ozData = text.getData();
		if (ozData == null || ozData.length() == 0) return "";
		return ArgonTransformer.zXmlEncode(ozData, false, false, false, false);
	}

	private static String ztwFlat(Node node, boolean spaced) {
		assert node != null;
		final StringBuilder b = new StringBuilder();
		flatztw(b, node, spaced);
		return b.toString();
	}

	static String oqNodeLocalName(Node node) {
		assert node != null;
		return oq(node.getLocalName());
	}

	static String oqNodeNamespaceURI(Node node) {
		assert node != null;
		return oq(node.getNamespaceURI());
	}

	static String oqNodePrefix(Node node) {
		assert node != null;
		return oq(node.getPrefix());
	}

	public static W3cNode createTransformed(DOMResult domResult) {
		final Node oNode = domResult.getNode();
		if (oNode == null) return null;
		return new W3cNode(null, oNode);
	}

	public static W3cNode newInstance(W3cDom dom, Node node) {
		if (dom == null) throw new IllegalArgumentException("object is null");
		if (node == null) throw new IllegalArgumentException("object is null");
		return new W3cNode(dom, node);
	}

	public static Binary toXmlBinary(List<W3cNode> zlNodes) {
		if (zlNodes == null) throw new IllegalArgumentException("object is null");
		return Binary.newFromStringUTF8(toNodeListXml(zlNodes, true));
	}

	public static Binary toXmlBinary(String ozData) {
		return Binary.newFromStringUTF8(toTextXml(ozData, true));
	}

	private W3cNode(W3cDom odom, Node node) {
		assert node != null;
		m_odom = odom;
		m_node = node;
	}

	public static final String EOL = "\n";

	public static final int INDENT = 2;

	public static final String INTRINSIC_NS = "argonw3c";

	public static final String INTRINSIC_PREFIX = "argon";

	public static final String ATTR_TAG = "attr";

	public static final String TEXT_TAG = "text";

	public static final String NODELIST_TAG = "nodeList";

	static final Pattern PATT_XMLNS_URI = Pattern.compile("http://www.w3.org/[\\w\\d]+/xmlns.*", Pattern.CASE_INSENSITIVE);

	static final Pattern PATT_XHTMLNS_URI = Pattern.compile("http://www.w3.org/[\\w\\d]+/xhtml.*", Pattern.CASE_INSENSITIVE);

	static final String INTRINSIC_ATTR = "<" + INTRINSIC_PREFIX + ":" + ATTR_TAG + " xmlns:" + INTRINSIC_PREFIX + "=\""
			+ INTRINSIC_NS + "\"";

	static final String INTRINSIC_TEXT_OPEN = "<" + INTRINSIC_PREFIX + ":" + TEXT_TAG + " xmlns:" + INTRINSIC_PREFIX + "=\""
			+ INTRINSIC_NS + "\">";

	static final String INTRINSIC_TEXT_CLOSE = "</" + INTRINSIC_PREFIX + ":" + TEXT_TAG + ">";

	private final W3cDom m_odom;

	private final Node m_node;

	private static class NSContext {
		public boolean exclude(Attr attr) {
			final String oqURI = oqNodeNamespaceURI(attr);
			return (oqURI != null && PATT_XMLNS_URI.matcher(oqURI).matches());
		}

		public String oqPrefix(Attr attr) {
			assert attr != null;
			final String oqURI = oqNodeNamespaceURI(attr);
			final String oqPrefix = oqNodePrefix(attr);
			putPrefix(oqURI, oqPrefix);
			return oqPrefix;
		}

		public String oqPrefix(Element element) {
			assert element != null;
			final String oqURI = oqNodeNamespaceURI(element);
			final String oqPrefix = oqNodePrefix(element);
			putPrefix(oqURI, oqPrefix);

			if (oqURI == null || oqRootNamespaceURI == null) return oqPrefix;

			if (oqURI.equals(oqRootNamespaceURI)) return null;

			return oqPrefix;
		}

		public String oqQualifiedName(Attr attr) {
			final String oqLocalName = oqNodeLocalName(attr);
			if (oqLocalName == null || exclude(attr)) return null;
			final String oqPrefix = oqPrefix(attr);
			return (oqPrefix == null) ? oqLocalName : oqPrefix + ":" + oqLocalName;
		}

		public String oqQualifiedName(Element element) {
			assert element != null;
			final String ozLocalName = element.getLocalName();
			if (ozLocalName == null || ozLocalName.length() == 0) return null;
			final String oqPrefix = oqPrefix(element);
			return (oqPrefix == null) ? ozLocalName : oqPrefix + ":" + ozLocalName;
		}

		public void putPrefix(String oqURI, String oqPrefix) {
			if (oqURI != null && oqPrefix != null) {
				zmPrefixNamespace.put(oqPrefix, oqURI);
			}
		}

		@Override
		public String toString() {
			final StringBuilder b = new StringBuilder();
			if (oqRootNamespaceURI != null) {
				b.append(" xmlns=\"").append(oqRootNamespaceURI).append("\"");
			}
			final List<String> zlPrefixAsc = new ArrayList<String>(zmPrefixNamespace.keySet());
			Collections.sort(zlPrefixAsc);
			for (final String qPrefix : zlPrefixAsc) {
				final String oqNamespace = zmPrefixNamespace.get(qPrefix);
				if (oqNamespace != null) {
					if (oqRootNamespaceURI == null || !oqRootNamespaceURI.equals(oqNamespace)) {
						b.append(" xmlns:").append(qPrefix).append("=\"").append(oqNamespace).append("\"");
					}
				}
			}
			return b.toString();
		}

		NSContext() {
			this.oqRootNamespaceURI = null;
		}

		NSContext(Node root) {
			assert root != null;
			final String oqURI = oqNodeNamespaceURI(root);
			final String oqPrefix = oqNodePrefix(root);
			if (oqURI == null) {
				oqRootNamespaceURI = null;
			} else {
				if (oqPrefix == null) {
					oqRootNamespaceURI = oqURI;
				} else {
					oqRootNamespaceURI = null;
					zmPrefixNamespace.put(oqPrefix, oqURI);
				}
			}
		}

		final String oqRootNamespaceURI;

		final Map<String, String> zmPrefixNamespace = new HashMap<String, String>();
	}
}
