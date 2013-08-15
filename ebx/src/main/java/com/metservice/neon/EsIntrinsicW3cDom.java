/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.List;

import com.metservice.argon.Binary;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonString;
import com.metservice.argon.json.JsonType;
import com.metservice.argon.xml.W3cDom;
import com.metservice.argon.xml.W3cNode;
import com.metservice.argon.xml.W3cTransformedNode;

/**
 * 
 * @author roach
 */
public final class EsIntrinsicW3cDom extends EsObject {

	private static final EsPrimitiveString NotInitialized = new EsPrimitiveString("NotInitialized");

	private static final EsPrimitiveString SourceNotInitialized = new EsPrimitiveString("SourceNotInitialized");

	private void putMessage(String qccPropertyName, String ozMessage) {
		if (ozMessage != null && ozMessage.length() > 0) {
			final EsPrimitiveString message = new EsPrimitiveString(ozMessage);
			add(qccPropertyName, EsProperty.newReadOnlyDontDelete(message));
		}
	}

	private void putMessages(EsExecutionContext ecx, String qccPropertyName, List<String> ozlMessages) {
		if (ozlMessages != null && !ozlMessages.isEmpty()) {
			final EsIntrinsicArray messageArray = ecx.global().newIntrinsicStringArray(ozlMessages, true);
			add(qccPropertyName, EsProperty.newReadOnlyDontDelete(messageArray));
		}
	}

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	@Override
	public IJsonNative createJsonNative() {
		if (m_oValue == null) return null;
		return JsonString.newInstance(m_oValue.toString());
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicW3cDom(this);
	}

	@Override
	public String esClass() {
		return EsIntrinsicW3cDomConstructor.ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TString;
	}

	public W3cNode newAtomicDocumentNode() {
		if (m_oValue == null) throw new EsApiCodeException("WcDom not initialized");
		return m_oValue.newAtomicDocumentNode();
	}

	public void setValue(EsExecutionContext ecx, Binary content, boolean validated) {
		if (content == null) throw new IllegalArgumentException("object is null");

		final W3cDom dom = W3cDom.newInstance(content, validated);
		setValue(ecx, dom);
	}

	public void setValue(EsExecutionContext ecx, W3cDom value) {
		if (value == null) throw new IllegalArgumentException("object is null");

		delete(EsIntrinsicW3cDomConstructor.PropertyName_document);
		delete(EsIntrinsicW3cDomConstructor.PropertyName_fatalError);
		delete(EsIntrinsicW3cDomConstructor.PropertyName_validationErrors);
		delete(EsIntrinsicW3cDomConstructor.PropertyName_warnings);
		delete(EsIntrinsicW3cDomConstructor.PropertyName_xsltFatalError);
		delete(EsIntrinsicW3cDomConstructor.PropertyName_xsltValidationErrors);
		delete(EsIntrinsicW3cDomConstructor.PropertyName_xsltWarnings);

		putMessage(EsIntrinsicW3cDomConstructor.PropertyName_fatalError, value.oqDocumentFatalError());
		putMessages(ecx, EsIntrinsicW3cDomConstructor.PropertyName_validationErrors, value.zlDocumentErrors());
		putMessages(ecx, EsIntrinsicW3cDomConstructor.PropertyName_warnings, value.zlDocumentWarnings());

		if (value.isValidDocument()) {
			final EsIntrinsicW3cNode documentNode = ecx.global().newIntrinsicW3cNode(value.newAtomicDocumentNode());
			add(EsIntrinsicW3cDomConstructor.PropertyName_document, EsProperty.newReadOnlyDontDelete(documentNode));
			if (value.isTransformer()) {
				putMessage(EsIntrinsicW3cDomConstructor.PropertyName_xsltFatalError, value.oqTransformFatalError());
				putMessages(ecx, EsIntrinsicW3cDomConstructor.PropertyName_xsltValidationErrors, value.zlTransformErrors());
				putMessages(ecx, EsIntrinsicW3cDomConstructor.PropertyName_xsltWarnings, value.zlTransformWarnings());
			}
		}
		m_oValue = value;
	}

	@Override
	public String show(int depth) {
		return m_oValue == null ? "NotInitialized" : m_oValue.toString();
	}

	public EsIntrinsicObject transform(EsExecutionContext ecx, EsIntrinsicW3cNode source) {
		if (source == null) throw new IllegalArgumentException("source is null");

		final EsIntrinsicObject resultObject = ecx.global().newIntrinsicObject();
		if (m_oValue == null) {
			resultObject.add(EsIntrinsicW3cNodeConstructor.PropertyName_fatalError,
					EsProperty.newReadOnlyDontDelete(NotInitialized));
		} else {
			final W3cNode oSourceValue = source.getValue();
			if (oSourceValue == null) {
				resultObject.add(EsIntrinsicW3cNodeConstructor.PropertyName_fatalError,
						EsProperty.newReadOnlyDontDelete(SourceNotInitialized));
			} else {
				final W3cTransformedNode tnode = m_oValue.transform(oSourceValue);
				final EsIntrinsicArray tErrors = ecx.global().newIntrinsicStringArray(tnode.zlTransformErrors(), true);
				add(EsIntrinsicW3cDomConstructor.PropertyName_validationErrors, EsProperty.newReadOnlyDontDelete(tErrors));
				final EsIntrinsicArray tWarnings = ecx.global().newIntrinsicStringArray(tnode.zlTransformWarnings(), true);
				add(EsIntrinsicW3cDomConstructor.PropertyName_warnings, EsProperty.newReadOnlyDontDelete(tWarnings));
				if (tnode.wasTransformed()) {
					final EsIntrinsicW3cNode esNode = ecx.global().newIntrinsicW3cNode(tnode.node());
					resultObject.add(EsIntrinsicW3cNodeConstructor.PropertyName_node,
							EsProperty.newReadOnlyDontDelete(esNode));
				} else {
					final EsPrimitiveString fatalError = new EsPrimitiveString(tnode.qTransformFatalError());
					resultObject.add(EsIntrinsicW3cNodeConstructor.PropertyName_fatalError,
							EsProperty.newReadOnlyDontDelete(fatalError));
				}
			}
		}
		return resultObject;
	}

	public EsIntrinsicW3cDom(EsObject prototype) {
		super(prototype);
	}

	private W3cDom m_oValue;
}
