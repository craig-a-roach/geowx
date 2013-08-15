/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.metservice.argon.Binary;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonString;
import com.metservice.argon.json.JsonType;
import com.metservice.argon.xml.W3cNode;

/**
 * 
 * @author roach
 */
public final class EsIntrinsicW3cNode extends EsObject {

	private void loadAttributeProperties(EsExecutionContext ecx) {
		final W3cNode node = node();
		final String oqAttributeName = node.oqAttributeName();
		if (oqAttributeName == null) return;
		final EsPrimitiveString aname = new EsPrimitiveString(oqAttributeName);
		final EsPrimitiveString avalue = new EsPrimitiveString(node.zAttributeValue());
		add(EsIntrinsicW3cNodeConstructor.PropertyName_name, EsProperty.newDefined(aname));
		add(EsIntrinsicW3cNodeConstructor.PropertyName_value, EsProperty.newDefined(avalue));
	}

	private void loadCharacterDataProperties(EsExecutionContext ecx) {
		final W3cNode node = node();
		final EsPrimitiveString characterData = new EsPrimitiveString(node.zCharacterData());
		add(EsIntrinsicW3cNodeConstructor.PropertyName_value, EsProperty.newDefined(characterData));
	}

	private void loadDocumentProperties(EsExecutionContext ecx) {
	}

	private void loadElementProperties(EsExecutionContext ecx) {
		final W3cNode node = node();
		final String zLocalName = node.zLocalName();
		final EsPrimitiveString name = new EsPrimitiveString(zLocalName);
		add(EsIntrinsicW3cNodeConstructor.PropertyName_name, EsProperty.newDefined(name));
		final Map<String, Map<String, String>> zmNamespaceURI_xmAttributeLocalName_Value = node
				.zmNamespaceURI_xmAttributeLocalName_Value();
		if (zmNamespaceURI_xmAttributeLocalName_Value.isEmpty()) return;

		final EsIntrinsicObject attributesObject = ecx.global().newIntrinsicObject();
		for (final String zNamespaceUri : zmNamespaceURI_xmAttributeLocalName_Value.keySet()) {
			final Map<String, String> oxmAttributeLocalName_Value = zmNamespaceURI_xmAttributeLocalName_Value
					.get(zNamespaceUri);
			if (oxmAttributeLocalName_Value == null || oxmAttributeLocalName_Value.isEmpty()) {
				continue;
			}
			final boolean nsGlobal = zNamespaceUri.length() == 0;
			final EsIntrinsicObject nsObject = nsGlobal ? attributesObject : ecx.global().newIntrinsicObject();
			for (final Map.Entry<String, String> e : oxmAttributeLocalName_Value.entrySet()) {
				nsObject.put(e.getKey(), new EsPrimitiveString(e.getValue()));
			}
			if (!nsGlobal) {
				attributesObject.put(zNamespaceUri, nsObject);
			}
		}
		add(EsIntrinsicW3cNodeConstructor.PropertyName_attributes, EsProperty.newDefined(attributesObject));
	}

	private void loadNodeProperties(EsExecutionContext ecx) {
		final W3cNode node = node();
		final EsPrimitiveString name = new EsPrimitiveString(node.zNodeName());
		add(EsIntrinsicW3cNodeConstructor.PropertyName_name, EsProperty.newDefined(name));
		final EsPrimitiveString value = new EsPrimitiveString(node.zNodeValue());
		add(EsIntrinsicW3cNodeConstructor.PropertyName_value, EsProperty.newDefined(value));
	}

	private W3cNode node() {
		if (m_oValue == null) throw new IllegalStateException("test null value");
		return m_oValue;
	}

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
		if (m_propertiesLoaded) return;

		if (m_oValue == null) {
			m_propertiesLoaded = true;
			return;
		}

		add(EsIntrinsicW3cNodeConstructor.PropertyName_type, nodeType(m_oValue.getNodeType()));
		if (m_oValue.isElement()) {
			loadElementProperties(ecx);
		} else if (m_oValue.isCharacterData()) {
			loadCharacterDataProperties(ecx);
		} else if (m_oValue.isAttribute()) {
			loadAttributeProperties(ecx);
		} else if (m_oValue.isDocument()) {
			loadDocumentProperties(ecx);
		} else {
			loadNodeProperties(ecx);
		}

		m_propertiesLoaded = true;
	}

	public EsPrimitiveBoolean containsText() {
		return EsPrimitiveBoolean.instance(m_oValue != null && m_oValue.containsText());
	}

	@Override
	public IJsonNative createJsonNative() {
		if (m_oValue == null) return null;
		return JsonString.newInstance(m_oValue.toXmlString(true));
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicW3cNode(this);
	}

	@Override
	public String esClass() {
		return EsIntrinsicW3cNodeConstructor.ClassName;
	}

	public IEsOperand getChildNodes(EsExecutionContext ecx, boolean includeElementContentWhitespace) {
		if (m_oValue == null) return ecx.global().newIntrinsicArray();
		return neo(ecx, m_oValue.getChildNodes(includeElementContentWhitespace));
	}

	public IEsOperand getFirstChild(EsExecutionContext ecx) {
		if (m_oValue == null) return EsPrimitiveNull.Instance;
		return neo(ecx, m_oValue.getFirstChild());
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TString;
	}

	public IEsOperand getLastChild(EsExecutionContext ecx) {
		if (m_oValue == null) return EsPrimitiveNull.Instance;
		return neo(ecx, m_oValue.getLastChild());
	}

	public IEsOperand getNamespaceURI() {
		if (m_oValue == null) return EsPrimitiveNull.Instance;
		return new EsPrimitiveString(m_oValue.zNamespaceURI());
	}

	public IEsOperand getNextSibling(EsExecutionContext ecx) {
		if (m_oValue == null) return EsPrimitiveNull.Instance;
		return neo(ecx, m_oValue.getNextSibling());
	}

	public IEsOperand getParent(EsExecutionContext ecx) {
		if (m_oValue == null) return EsPrimitiveNull.Instance;
		return neo(ecx, m_oValue.getParent());
	}

	public IEsOperand getPrefix() {
		if (m_oValue == null) return EsPrimitiveNull.Instance;
		return new EsPrimitiveString(m_oValue.zPrefix());
	}

	public W3cNode getValue() {
		return m_oValue;
	}

	public EsPrimitiveBoolean hasChildNodes() {
		return EsPrimitiveBoolean.instance(m_oValue != null && m_oValue.hasChildNodes());
	}

	public IEsOperand lookupNamespaceURI(String qPrefix) {
		if (qPrefix == null || qPrefix.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (m_oValue == null) return EsPrimitiveNull.Instance;
		return new EsPrimitiveString(m_oValue.zLookupNamespaceURI(qPrefix));
	}

	public IEsOperand lookupPrefix(String qNamespaceURI) {
		if (qNamespaceURI == null || qNamespaceURI.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (m_oValue == null) return EsPrimitiveNull.Instance;
		return new EsPrimitiveString(m_oValue.zLookupPrefix(qNamespaceURI));
	}

	public void setValue(W3cNode value) {
		if (value == null) throw new IllegalArgumentException("object is null");
		m_oValue = value;
		m_propertiesLoaded = false;
	}

	@Override
	public String show(int depth) {
		return m_oValue == null ? "(none)" : m_oValue.toString();
	}

	public EsPrimitiveString toFlatPrimitiveString(boolean spaced) {
		if (m_oValue == null) return EsPrimitiveString.EMPTY;
		final String ztwFlat = m_oValue.toFlatString(spaced);
		if (ztwFlat.length() == 0) return EsPrimitiveString.EMPTY;
		return new EsPrimitiveString(ztwFlat);
	}

	public Binary toXmlBinary() {
		return (m_oValue == null) ? Binary.Empty : m_oValue.toXmlBinary();
	}

	public EsPrimitiveString toXmlPrimitiveString(boolean outer) {
		if (m_oValue == null) return EsPrimitiveString.EMPTY;
		return new EsPrimitiveString(m_oValue.toXmlString(outer));
	}

	public String ztwFlatConcatenated() {
		return m_oValue == null ? "" : m_oValue.ztwFlatConcatenated();
	}

	public String ztwFlatSpaced() {
		return m_oValue == null ? "" : m_oValue.ztwFlatSpaced();
	}

	private static EsIntrinsicArray neo(EsExecutionContext ecx, List<W3cNode> ozlNodes) {
		if (ozlNodes == null) return ecx.global().newIntrinsicArray();
		final int count = ozlNodes.size();
		if (count == 0) return ecx.global().newIntrinsicArray();

		final List<IEsOperand> zlMembers = new ArrayList<IEsOperand>(count);
		for (int i = 0; i < count; i++) {
			zlMembers.add(ecx.global().newIntrinsicW3cNode(ozlNodes.get(i)));
		}
		return ecx.global().newIntrinsicArray(zlMembers);
	}

	private static IEsOperand neo(EsExecutionContext ecx, W3cNode oNode) {
		return oNode == null ? EsPrimitiveNull.Instance : ecx.global().newIntrinsicW3cNode(oNode);
	}

	private static EsProperty nodeType(int nodeType) {
		switch (nodeType) {
			case Node.ATTRIBUTE_NODE:
				return PROP_ATTRIBUTE;
			case Node.CDATA_SECTION_NODE:
				return PROP_CDATA_SECTION;
			case Node.COMMENT_NODE:
				return PROP_COMMENT;
			case Node.DOCUMENT_FRAGMENT_NODE:
				return PROP_DOCUMENT_FRAGMENT;
			case Node.DOCUMENT_NODE:
				return PROP_DOCUMENT;
			case Node.DOCUMENT_TYPE_NODE:
				return PROP_DOCUMENT_TYPE;
			case Node.ELEMENT_NODE:
				return PROP_ELEMENT;
			case Node.ENTITY_NODE:
				return PROP_ENTITY;
			case Node.ENTITY_REFERENCE_NODE:
				return PROP_ENTITY_REFERENCE;
			case Node.NOTATION_NODE:
				return PROP_NOTATION;
			case Node.PROCESSING_INSTRUCTION_NODE:
				return PROP_PROCESSING_INSTRUCTION;
			case Node.TEXT_NODE:
				return PROP_TEXT;
			default:
				return EsProperty.newReadOnlyDontDelete(new EsPrimitiveNumberInteger(nodeType));
		}
	}

	public static List<W3cNode> new_zlNodes(List<EsIntrinsicW3cNode> zlEsNodes) {
		assert zlEsNodes != null;
		final int count = zlEsNodes.size();
		final List<W3cNode> zlNodes = new ArrayList<W3cNode>(count);
		for (int i = 0; i < count; i++) {
			final W3cNode oNode = zlEsNodes.get(i).m_oValue;
			if (oNode != null) {
				zlNodes.add(oNode);
			}
		}
		return zlNodes;
	}

	public static Binary toXmlByteBox(List<EsIntrinsicW3cNode> zlEsNodes) {
		if (zlEsNodes == null) throw new IllegalArgumentException("object is null");
		return W3cNode.toXmlBinary(new_zlNodes(zlEsNodes));
	}

	public static Binary toXmlByteBox(String ozData) {
		return W3cNode.toXmlBinary(ozData);
	}

	public EsIntrinsicW3cNode(EsObject prototype) {
		super(prototype);
	}
	private W3cNode m_oValue;
	private boolean m_propertiesLoaded;
	private static final EsProperty PROP_ATTRIBUTE = EsProperty.newReadOnlyDontDelete(new EsPrimitiveString("attribute"));
	private static final EsProperty PROP_CDATA_SECTION = EsProperty.newReadOnlyDontDelete(new EsPrimitiveString("cdata"));
	private static final EsProperty PROP_COMMENT = EsProperty.newReadOnlyDontDelete(new EsPrimitiveString("comment"));
	private static final EsProperty PROP_DOCUMENT_FRAGMENT = EsProperty.newReadOnlyDontDelete(new EsPrimitiveString(
			"documentFragment"));
	private static final EsProperty PROP_DOCUMENT = EsProperty.newReadOnlyDontDelete(new EsPrimitiveString("document"));
	private static final EsProperty PROP_DOCUMENT_TYPE = EsProperty.newReadOnlyDontDelete(new EsPrimitiveString("documentType"));
	private static final EsProperty PROP_ELEMENT = EsProperty.newReadOnlyDontDelete(new EsPrimitiveString("element"));

	private static final EsProperty PROP_ENTITY = EsProperty.newReadOnlyDontDelete(new EsPrimitiveString("entity"));

	private static final EsProperty PROP_ENTITY_REFERENCE = EsProperty.newReadOnlyDontDelete(new EsPrimitiveString(
			"entityReference"));

	private static final EsProperty PROP_NOTATION = EsProperty.newReadOnlyDontDelete(new EsPrimitiveString("notation"));

	private static final EsProperty PROP_PROCESSING_INSTRUCTION = EsProperty.newReadOnlyDontDelete(new EsPrimitiveString(
			"processingInstruction"));

	private static final EsProperty PROP_TEXT = EsProperty.newReadOnlyDontDelete(new EsPrimitiveString("text"));
}
