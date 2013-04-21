/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.xml;

import java.util.Collections;
import java.util.List;

/**
 * @author roach
 */
public class W3cTransformedNode {
	public W3cNode node() {
		if (m_oNode == null) throw new IllegalStateException("wasTransformed");
		return m_oNode;
	}

	public String oqTransformFatalError() {
		return m_oqFatalError;
	}

	public String qTransformFatalError() {
		if (m_oqFatalError == null) throw new IllegalStateException("wasTransformed");
		return m_oqFatalError;
	}

	@Override
	public String toString() {
		if (m_oNode != null) return m_oNode.toString();
		if (m_oqFatalError != null) return m_oqFatalError;
		return "NotInitialized";
	}

	public boolean wasTransformed() {
		return m_oNode != null && m_oqFatalError == null;
	}

	public List<String> zlTransformErrors() {
		return m_zlErrors;
	}

	public List<String> zlTransformWarnings() {
		return m_zlWarnings;
	}

	public static W3cTransformedNode newFatalError(String qFatalError) {
		if (qFatalError == null || qFatalError.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return new W3cTransformedNode(null, qFatalError, null, null);
	}

	public static W3cTransformedNode newFatalError(String qFatalError, List<String> ozlErrors) {
		if (qFatalError == null || qFatalError.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return new W3cTransformedNode(null, qFatalError, ozlErrors, null);
	}

	public static W3cTransformedNode newTransformed(W3cNode node, List<String> ozlErrors, List<String> ozlWarnings) {
		if (node == null) throw new IllegalArgumentException("object is null");
		return new W3cTransformedNode(node, null, ozlErrors, ozlWarnings);
	}

	private W3cTransformedNode(W3cNode oNode, String oqFatalError, List<String> ozlErrors, List<String> ozlWarnings) {
		m_oNode = oNode;
		m_oqFatalError = oqFatalError;
		if (ozlErrors == null) {
			m_zlErrors = Collections.emptyList();
		} else {
			m_zlErrors = ozlErrors;
		}
		if (ozlWarnings == null) {
			m_zlWarnings = Collections.emptyList();
		} else {
			m_zlWarnings = ozlErrors;
		}
	}
	private final W3cNode m_oNode;
	private final String m_oqFatalError;
	private final List<String> m_zlErrors;

	private final List<String> m_zlWarnings;
}
