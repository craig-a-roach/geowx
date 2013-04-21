/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.metservice.argon.Binary;

/**
 * @author roach
 */
public class W3cDom {
	public boolean isTransformer() {
		return m_isT;
	}

	public boolean isValidDocument() {
		return m_oDocument != null && m_oqDocFatalError == null;
	}

	public void lock() {
		m_docLock.lock();
	}

	public W3cNode newAtomicDocumentNode() {
		if (m_oDocument == null) throw new IllegalStateException("isValidDocument");
		return W3cNode.newInstance(this, m_oDocument);
	}

	public String oqClean(boolean suppressWarnings) {
		if (!m_zlDocErrors.isEmpty()) return m_zlDocErrors.get(0);
		if (m_oqDocFatalError != null) return m_oqDocFatalError;
		if (!suppressWarnings && !m_zlDocWarnings.isEmpty()) return m_zlDocWarnings.get(0);
		if (!m_isT) return null;
		if (!m_zlTranErrors.isEmpty()) return m_zlTranErrors.get(0);
		if (m_oqTranFatalError != null) return m_oqTranFatalError;
		if (!suppressWarnings && !m_zlTranWarnings.isEmpty()) return m_zlTranWarnings.get(0);
		return null;
	}

	public String oqDocumentFatalError() {
		return m_oqDocFatalError;
	}

	public String oqTransformFatalError() {
		return m_oqTranFatalError;
	}

	public String qDocumentFatalError() {
		if (m_oqDocFatalError == null) throw new IllegalStateException("isValidDocument");
		return m_oqDocFatalError;
	}

	@Override
	public String toString() {
		if (m_oDocument != null) return m_oDocument.getDocumentElement().getNodeName();
		if (m_oqDocFatalError != null) return m_oqDocFatalError;
		return "NotInitialized";
	}

	public W3cTransformedNode transform(W3cNode sourceNode) {
		if (m_oqDocFatalError != null) return W3cTransformedNode.newFatalError(m_oqDocFatalError);
		if (!m_isT) return W3cTransformedNode.newFatalError(NotATransform);
		if (m_oqTranFatalError != null) return W3cTransformedNode.newFatalError(m_oqTranFatalError);
		if (m_oTransformer == null) return W3cTransformedNode.newFatalError(UnspecifiedTransformError);

		sourceNode.lock();
		m_docLock.lock();
		final CErrorListener errorListener = new CErrorListener();
		final DOMSource domSource = new DOMSource(sourceNode.node());
		final DOMResult domResult = new DOMResult();
		try {
			m_oTransformer.reset();
			m_oTransformer.setErrorListener(errorListener);
			m_oTransformer.transform(domSource, domResult);
		} catch (final TransformerException exTR) {
			if (errorListener.oqFatalError == null) {
				errorListener.oqFatalError = exTR.getMessageAndLocation();
			}
		} finally {
			if (m_oTransformer != null) {
				m_oTransformer.reset();
			}
			m_docLock.unlock();
			sourceNode.unlock();
		}
		final List<String> oxlValidationErrors = errorListener.oxlValidationErrors();
		final List<String> oxlWarnings = errorListener.oxlWarnings();
		final W3cTransformedNode transformed;
		if (errorListener.oqFatalError == null) {
			final W3cNode oNode = W3cNode.createTransformed(domResult);
			if (oNode == null) {
				transformed = W3cTransformedNode.newFatalError(TransformIncomplete, oxlValidationErrors);
			} else {
				transformed = W3cTransformedNode.newTransformed(oNode, oxlValidationErrors, oxlWarnings);
			}
		} else {
			transformed = W3cTransformedNode.newFatalError(errorListener.oqFatalError, oxlValidationErrors);
		}
		return transformed;
	}

	public void unlock() {
		m_docLock.unlock();
	}

	public List<String> zlDocumentErrors() {
		return m_zlDocErrors;
	}

	public List<String> zlDocumentWarnings() {
		return m_zlDocWarnings;
	}

	public List<String> zlTransformErrors() {
		return m_zlTranErrors;
	}

	public List<String> zlTransformWarnings() {
		return m_zlTranWarnings;
	}

	public static W3cDom newInstance(Binary content, boolean validated) {
		if (content == null) throw new IllegalArgumentException("object is null");
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		domFactory.setValidating(validated);
		final CErrorHandler errorHandler = new CErrorHandler();
		final InputStream ins = content.getInputStream();
		Document oDocument = null;
		String oqDocFatalError = null;
		List<String> zlDocErrors = Collections.emptyList();
		List<String> zlDocWarnings = Collections.emptyList();
		boolean isTransformer = false;
		Transformer oTransformer = null;
		String oqTranFatalError = null;
		List<String> zlTranErrors = Collections.emptyList();
		List<String> zlTranWarnings = Collections.emptyList();
		try {
			final DocumentBuilder builder = domFactory.newDocumentBuilder();
			builder.setErrorHandler(errorHandler);
			oDocument = builder.parse(ins);
			if (errorHandler.lzyxlValidationErrors != null) {
				zlDocErrors = errorHandler.lzyxlValidationErrors;
			}
			if (errorHandler.lzyxlWarnings != null) {
				zlDocWarnings = errorHandler.lzyxlWarnings;
			}
			final Element oDocumentElement = oDocument.getDocumentElement();
			final String ozDocumentElementNS = oDocumentElement == null ? null : oDocumentElement.getNamespaceURI();
			if (ozDocumentElementNS != null && ozDocumentElementNS.length() > 0) {
				if (PATT_XSLT_NSURI.matcher(ozDocumentElementNS).matches()) {
					isTransformer = true;
					final TransformerFactory transformerFactory = TransformerFactory.newInstance();
					final DOMSource source = new DOMSource(oDocument);
					final CErrorListener errorListener = new CErrorListener();
					transformerFactory.setErrorListener(errorListener);
					try {
						oTransformer = transformerFactory.newTransformer(source);
					} catch (final TransformerConfigurationException exTC) {
						if (errorListener.oqFatalError == null) {
							errorListener.oqFatalError = exTC.getMessageAndLocation();
						}
					}
					oqTranFatalError = errorListener.oqFatalError;
					if (errorListener.lzyxlValidationErrors != null) {
						zlTranErrors = errorListener.lzyxlValidationErrors;
					}
					if (errorListener.lzyxlWarnings != null) {
						zlTranWarnings = errorListener.lzyxlWarnings;
					}
				}
			}
		} catch (final ParserConfigurationException exPC) {
			oqDocFatalError = "Cannot find a namespace-aware W3CDom builder (" + exPC.getMessage() + ")";
		} catch (final SAXException exSAX) {
			if (errorHandler.oqFatalError == null) {
				final String ozMessage = exSAX.getMessage();
				errorHandler.oqFatalError = ozMessage == null || ozMessage.length() == 0 ? UnspecifiedFatalError
						: ozMessage;
			}
			oqDocFatalError = errorHandler.oqFatalError;
		} catch (final IOException exIO) {
			oqDocFatalError = "Error reading content byte stream (" + exIO.getMessage() + ")";
		} finally {
			try {
				ins.close();
			} catch (final IOException exIO) {
			}
		}
		return new W3cDom(oDocument, oqDocFatalError, zlDocErrors, zlDocWarnings, isTransformer, oTransformer,
				oqTranFatalError, zlTranErrors, zlTranWarnings);
	}

	public static W3cDom newInstance(String zContent, boolean validated) {
		if (zContent == null) throw new IllegalArgumentException("object is null");
		return newInstance(Binary.newFromStringUTF8(zContent), validated);
	}

	private W3cDom(Document oD, String oqDF, List<String> zlDEs, List<String> zlDWs, boolean isT, Transformer oT, String oqTF,
			List<String> zlTEs, List<String> zlTWs) {
		m_oDocument = oD;
		m_oqDocFatalError = oqDF;
		m_zlDocErrors = zlDEs;
		m_zlDocWarnings = zlDWs;
		m_isT = isT;
		m_oTransformer = oT;
		m_oqTranFatalError = oqTF;
		m_zlTranErrors = zlTEs;
		m_zlTranWarnings = zlTWs;
	}

	public static final Pattern PATT_XSLT_NSURI = Pattern.compile("http://www.w3.org/[\\w\\d]+/XSL/Transform",
			Pattern.CASE_INSENSITIVE);

	private static final String UnspecifiedFatalError = "Unspecified parse error";

	private static final String UnspecifiedTransformError = "Unspecified transform error";

	private static final String NotATransform = "Document is not a transform";
	private static final String TransformIncomplete = "Transform did not complete";
	private final Document m_oDocument;
	private final String m_oqDocFatalError;
	private final List<String> m_zlDocErrors;
	private final List<String> m_zlDocWarnings;
	private final boolean m_isT;
	private final Transformer m_oTransformer;
	private final String m_oqTranFatalError;

	private final List<String> m_zlTranErrors;

	private final List<String> m_zlTranWarnings;

	private final Lock m_docLock = new ReentrantLock();

	private static class CErrorHandler implements ErrorHandler {
		public void error(SAXParseException exception)
				throws SAXException {
			final String m = exception.getMessage() + "..detected at line no " + exception.getLineNumber();
			if (lzyxlValidationErrors == null) {
				lzyxlValidationErrors = new ArrayList<String>();
			}
			lzyxlValidationErrors.add(m);
		}

		public void fatalError(SAXParseException exception)
				throws SAXException {
			final String m = exception.getMessage() + "..detected at line no " + exception.getLineNumber();
			if (oqFatalError == null) {
				oqFatalError = m;
			}
		}

		public void warning(SAXParseException exception)
				throws SAXException {
			final String m = exception.getMessage() + "..detected at line no " + exception.getLineNumber();
			if (lzyxlWarnings == null) {
				lzyxlWarnings = new ArrayList<String>();
			}
			lzyxlWarnings.add(m);
		}

		public CErrorHandler() {
		}

		String oqFatalError;

		List<String> lzyxlValidationErrors;

		List<String> lzyxlWarnings;
	}

	private static class CErrorListener implements ErrorListener {
		public void error(TransformerException exception) {
			final String m = exception.getMessageAndLocation();
			if (lzyxlValidationErrors == null) {
				lzyxlValidationErrors = new ArrayList<String>();
			}
			lzyxlValidationErrors.add(m);
		}

		public void fatalError(TransformerException exception) {
			final String m = exception.getMessageAndLocation();
			if (oqFatalError == null) {
				oqFatalError = m;
			}
		}

		public List<String> oxlValidationErrors() {
			return lzyxlValidationErrors;
		}

		public List<String> oxlWarnings() {
			return lzyxlWarnings;
		}

		public void warning(TransformerException exception) {
			final String m = exception.getMessageAndLocation();
			if (lzyxlWarnings == null) {
				lzyxlWarnings = new ArrayList<String>();
			}
			lzyxlWarnings.add(m);
		}

		public CErrorListener() {
		}

		String oqFatalError;

		List<String> lzyxlValidationErrors;

		List<String> lzyxlWarnings;
	}
}
