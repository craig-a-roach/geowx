/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.metservice.argon.xml.W3cNode;

/**
 * 
 * @author roach
 */
public class EsIntrinsicXPathConstructor extends EsIntrinsicConstructor {
	private String reason(String expr, XPathExpressionException exXP) {
		final StringBuilder b = new StringBuilder();
		b.append("Invalid XPath syntax in '" + expr + "'...");
		final Throwable oCause = exXP.getCause();
		final String ozCauseMessage = oCause == null ? null : oCause.getMessage();
		final String ozMessage = exXP.getMessage();
		if (ozCauseMessage != null && ozCauseMessage.length() > 0) {
			b.append(ozCauseMessage);
		} else if (ozMessage != null && ozMessage.length() > 0) {
			b.append(ozMessage);
		}
		return b.toString();
	}

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final String qExpr = ac.qStringValue(0);
		final EsObject oResolver = ac.defaulted(1) ? null : ac.esObject(1);
		final XPath xpath = m_xpathFactory.newXPath();
		if (oResolver != null) {
			xpath.setNamespaceContext(newNamespaceContext(ecx, oResolver));
		}
		try {
			final XPathExpression expr = xpath.compile(qExpr);
			final EsIntrinsicXPath neo;
			if (calledAsFunction(ecx)) {
				neo = ecx.global().newIntrinsicXPath(expr, qExpr);
			} else {
				neo = thisIntrinsicObject(ecx, EsIntrinsicXPath.class);
				neo.setValue(expr, qExpr);
			}
			return neo;
		} catch (final XPathExpressionException exXP) {
			throw new EsApiCodeException(reason(qExpr, exXP));
		}
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicXPath(global.prototypeObject);
	}

	private static EsIntrinsicMethod method_evaluateBoolean() {
		return new EsIntrinsicMethod("evaluateBoolean", new String[] { "context" }, 1) {
			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicXPath self = thisIntrinsicObject(ecx, EsIntrinsicXPath.class);
				return self.evaluateBoolean(ecx, toNode(ecx, 0));
			}
		};
	}

	private static EsIntrinsicMethod method_evaluateInteger() {
		return new EsIntrinsicMethod("evaluateInteger", new String[] { "context" }, 1) {
			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicXPath self = thisIntrinsicObject(ecx, EsIntrinsicXPath.class);
				return self.evaluateIntegerNumber(ecx, toNode(ecx, 0));
			}
		};
	}

	private static EsIntrinsicMethod method_evaluateNodes() {
		return new EsIntrinsicMethod("evaluateNodes", new String[] { "context" }, 1) {
			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicXPath self = thisIntrinsicObject(ecx, EsIntrinsicXPath.class);
				return self.evaluateNodeArray(ecx, toNode(ecx, 0));
			}
		};
	}

	private static EsIntrinsicMethod method_evaluateNumber() {
		return new EsIntrinsicMethod("evaluateNumber", new String[] { "context" }, 1) {
			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicXPath self = thisIntrinsicObject(ecx, EsIntrinsicXPath.class);
				return self.evaluateNumber(ecx, toNode(ecx, 0));
			}
		};
	}

	private static EsIntrinsicMethod method_evaluateString() {
		return new EsIntrinsicMethod("evaluateString", new String[] { "context" }, 1) {
			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicXPath self = thisIntrinsicObject(ecx, EsIntrinsicXPath.class);
				return self.evaluateString(ecx, toNode(ecx, 0));
			}
		};
	}

	/**
	 * @jsmethod toString
	 * @jsreturn A string representation of the object.
	 */
	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {
			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisIntrinsicObject(ecx, EsIntrinsicXPath.class).toPrimitiveString();
			}
		};
	}

	private static NamespaceContext newNamespaceContext(EsExecutionContext ecx, EsObject resolver)
			throws InterruptedException {
		final Map<String, String> zm = new HashMap<String, String>();
		final List<String> zlNames = resolver.esPropertyNames();
		for (final String qccPrefix : zlNames) {
			final IEsOperand nsOperand = resolver.esGet(qccPrefix);
			final EsType nsType = nsOperand.esType();
			if (nsType == EsType.TString || nsType == EsType.TObject) {
				final String zNs = nsOperand.toCanonicalString(ecx);
				if (zNs.length() > 0) {
					zm.put(qccPrefix, zNs);
				}
			}
		}
		return new ResolverNC(zm);
	}

	static W3cNode toNode(EsExecutionContext ecx, int index)
			throws InterruptedException {
		final EsArguments args = ecx.activation().arguments();
		final EsObject arg = args.operand(index).toObject(ecx);
		if (arg instanceof EsIntrinsicW3cDom) return ((EsIntrinsicW3cDom) arg).newAtomicDocumentNode();
		if (arg instanceof EsIntrinsicW3cNode) {
			final W3cNode oNode = ((EsIntrinsicW3cNode) arg).getValue();
			if (oNode == null)
				throw new EsApiCodeException("Parameter '" + args.formalParameterName(index) + "' is an undefined node");
			return oNode;
		}
		throw new EsApiCodeException("Parameter '" + args.formalParameterName(index) + "' is a '" + arg.esClass()
				+ "' object...expecting a W3cDom or W3cNode object");
	}

	public static EsIntrinsicXPathConstructor newInstance() {
		return new EsIntrinsicXPathConstructor();
	}

	private EsIntrinsicXPathConstructor() {
		super(ClassName, new String[] { "expression", "namespaceResolver" }, 1);
		m_xpathFactory = XPathFactory.newInstance();
	}

	public static final String ClassName = "XPath";

	public static final EsIntrinsicMethod[] Methods = { method_evaluateNodes(), method_evaluateString(),
			method_evaluateInteger(), method_evaluateNumber(), method_evaluateBoolean(), method_toString() };

	private final XPathFactory m_xpathFactory;

	private static class ResolverNC implements NamespaceContext {
		public String getNamespaceURI(String prefix) {
			if (prefix == null) throw new IllegalArgumentException("object is null");
			if (prefix.equals("xml")) return XMLConstants.XML_NS_URI;
			final String oqNamespaceURI = (prefix.length() == 0) ? null : m_zm.get(prefix);
			return oqNamespaceURI == null ? XMLConstants.NULL_NS_URI : oqNamespaceURI;
		}

		public String getPrefix(String namespaceURI) {
			throw new UnsupportedOperationException();
		}

		public Iterator<?> getPrefixes(String namespaceURI) {
			throw new UnsupportedOperationException();
		}

		ResolverNC(Map<String, String> zm) throws InterruptedException {
			assert zm != null;
			m_zm = zm;
		}
		private final Map<String, String> m_zm;
	}
}
