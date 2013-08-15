/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;

import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonString;
import com.metservice.argon.json.JsonType;
import com.metservice.argon.xml.W3cNode;

/**
 * 
 * @author roach
 */
public final class EsIntrinsicXPath extends EsObject {

	private EsIntrinsicArray newNodeArray(EsExecutionContext ecx, List<W3cNode> ozlNodes) {
		if (ozlNodes == null) return ecx.global().newIntrinsicArray();
		final int count = ozlNodes.size();
		if (count == 0) return ecx.global().newIntrinsicArray();

		final List<IEsOperand> zlMembers = new ArrayList<IEsOperand>(count);
		for (int i = 0; i < count; i++) {
			zlMembers.add(ecx.global().newIntrinsicW3cNode(ozlNodes.get(i)));
		}
		return ecx.global().newIntrinsicArray(zlMembers);
	}

	private EsIntrinsicArray newNodeArray(EsExecutionContext ecx, W3cNode node) {
		if (node == null) throw new IllegalArgumentException("object is null");
		final EsIntrinsicW3cNode esNode = ecx.global().newIntrinsicW3cNode(node);
		return ecx.global().newIntrinsicArray(esNode);
	}

	private EsIntrinsicArray newNodeArray(EsExecutionContext ecx, W3cNode ref, NodeList nodeList) {
		if (nodeList == null) throw new IllegalArgumentException("object is null");
		return newNodeArray(ecx, ref.zlNodes(nodeList));
	}

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	@Override
	public IJsonNative createJsonNative() {
		return JsonString.newInstance(m_oqSource == null ? "Identity" : m_oqSource);
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicXPath(this);
	}

	@Override
	public String esClass() {
		return EsIntrinsicXPathConstructor.ClassName;
	}

	public EsPrimitiveBoolean evaluateBoolean(EsExecutionContext ecx, W3cNode node) {
		if (node == null) throw new IllegalArgumentException("object is null");

		if (m_oExpression == null) return EsPrimitiveBoolean.FALSE;

		try {
			final Object result = node.applyXPath(m_oExpression, XPathConstants.BOOLEAN);
			if (result == null) return EsPrimitiveBoolean.FALSE;
			if (result instanceof Boolean) return EsPrimitiveBoolean.instance((Boolean) result);
			throw new EsIntrinsicException("Expecting XPath expression '" + m_oExpression
					+ "' to return a Boolean, but returned '" + result + "'");
		} catch (final XPathExpressionException exXP) {
			final String ozReason = exXP.getMessage();
			throw new EsApiCodeException("Boolean expression cannot be evaluated"
					+ (ozReason == null || ozReason.length() == 0 ? "" : "..." + ozReason));
		}
	}

	public EsPrimitiveNumberInteger evaluateIntegerNumber(EsExecutionContext ecx, W3cNode node) {
		if (node == null) throw new IllegalArgumentException("object is null");

		if (m_oExpression == null) return EsPrimitiveNumberInteger.ZERO;

		try {
			final Object result = node.applyXPath(m_oExpression, XPathConstants.NUMBER);
			if (result == null) return EsPrimitiveNumberInteger.ZERO;
			if (result instanceof Double) {
				final double d = ((Double) result);
				final long ld = Math.round(d);
				if (ld >= Integer.MIN_VALUE && ld <= Integer.MAX_VALUE) return new EsPrimitiveNumberInteger((int) ld);
				throw new EsApiCodeException("Result of expression (" + ld + ") cannot be expressed as an integer");
			}
			throw new EsIntrinsicException("Expecting XPath expression '" + m_oExpression
					+ "' to return a Number, but returned '" + result + "'");
		} catch (final XPathExpressionException exXP) {
			final String ozReason = exXP.getMessage();
			throw new EsApiCodeException("Number expression cannot be evaluated"
					+ (ozReason == null || ozReason.length() == 0 ? "" : "..." + ozReason));
		}
	}

	public EsIntrinsicArray evaluateNodeArray(EsExecutionContext ecx, W3cNode node) {
		if (node == null) throw new IllegalArgumentException("object is null");

		if (m_oExpression == null) return newNodeArray(ecx, node);

		try {
			final Object result = node.applyXPath(m_oExpression, XPathConstants.NODESET);
			if (result == null) return ecx.global().newIntrinsicArray();
			if (result instanceof NodeList) return newNodeArray(ecx, node, (NodeList) result);
			throw new EsIntrinsicException("Expecting XPath expression '" + m_oExpression
					+ "' to return a NodeList, but returned '" + result + "'");
		} catch (final XPathExpressionException exXP) {
			final String ozReason = exXP.getMessage();
			throw new EsApiCodeException("Node set expression cannot be evaluated"
					+ (ozReason == null || ozReason.length() == 0 ? "" : "..." + ozReason));
		}
	}

	public EsPrimitiveNumber evaluateNumber(EsExecutionContext ecx, W3cNode node) {
		if (node == null) throw new IllegalArgumentException("object is null");

		if (m_oExpression == null) return EsPrimitiveNumberInteger.ZERO;

		try {
			final Object result = node.applyXPath(m_oExpression, XPathConstants.NUMBER);
			if (result == null) return EsPrimitiveNumberInteger.ZERO;
			if (result instanceof Double) {
				final double d = ((Double) result);
				if (Math.rint(d) == d) {
					final long ld = Math.round(d);
					if (ld >= Integer.MIN_VALUE && ld <= Integer.MAX_VALUE)
						return new EsPrimitiveNumberInteger((int) ld);
				}
				return new EsPrimitiveNumberDouble(d);
			}
			throw new EsIntrinsicException("Expecting XPath expression '" + m_oExpression
					+ "' to return a Number, but returned '" + result + "'");
		} catch (final XPathExpressionException exXP) {
			final String ozReason = exXP.getMessage();
			throw new EsApiCodeException("Number expression cannot be evaluated"
					+ (ozReason == null || ozReason.length() == 0 ? "" : "..." + ozReason));
		}
	}

	public EsPrimitiveString evaluateString(EsExecutionContext ecx, W3cNode node) {
		if (node == null) throw new IllegalArgumentException("object is null");

		if (m_oExpression == null) return EsPrimitiveString.EMPTY;

		try {
			final Object result = node.applyXPath(m_oExpression, XPathConstants.STRING);
			if (result == null) return EsPrimitiveString.EMPTY;
			return new EsPrimitiveString(result.toString());
		} catch (final XPathExpressionException exXP) {
			final String ozReason = exXP.getMessage();
			throw new EsApiCodeException("String expression cannot be evaluated"
					+ (ozReason == null || ozReason.length() == 0 ? "" : "..." + ozReason));
		}
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TString;
	}

	public void setValue(XPathExpression oExpression, String oqSource) {
		m_oExpression = oExpression;
		m_oqSource = oqSource;
	}

	@Override
	public String show(int depth) {
		return (m_oqSource == null) ? "Identity" : m_oqSource;
	}

	public EsPrimitiveString toPrimitiveString() {
		return new EsPrimitiveString(m_oqSource == null ? "Identity" : m_oqSource);
	}

	public EsIntrinsicXPath(EsObject prototype) {
		super(prototype);
	}

	private XPathExpression m_oExpression;
	private String m_oqSource;
}
