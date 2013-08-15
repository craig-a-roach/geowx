/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ICodedEnum;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public abstract class EmClass extends EsObject {

	protected static final String[] NoArgs = new String[0];

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	@Override
	public IJsonNative createJsonNative() {
		return null;
	}

	@Override
	public EsObject createObject() {
		return null;
	}

	public boolean equals(EmClass r) {
		if (r == this) return true;
		if (r == null) return false;
		return m_qccClassName.equals(r.m_qccClassName);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof EmClass)) return false;
		return equals((EmClass) o);
	}

	@Override
	public String esClass() {
		return m_qccClassName;
	}

	@Override
	public JsonType getJsonType() {
		return null;
	}

	@Override
	public int hashCode() {
		return m_qccClassName.hashCode();
	}

	public void putConstantProperty(String qccPropertyName, ICodedEnum value) {
		if (value == null) throw new IllegalArgumentException("object is null");
		putConstantProperty(qccPropertyName, value.qCode());
	}

	public void putConstantProperty(String qccPropertyName, IEsOperand value) {
		if (qccPropertyName == null || qccPropertyName.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (value == null) throw new IllegalArgumentException("object is null");

		add(qccPropertyName, EsProperty.newReadOnlyDontDelete(value));
	}

	public void putConstantProperty(String qccPropertyName, String zValue) {
		if (zValue == null) throw new IllegalArgumentException("object is null");
		putConstantProperty(qccPropertyName, new EsPrimitiveString(zValue));
	}

	public EmClass(String qccClassName, EmMethod[] ozptMethods) {
		if (qccClassName == null || qccClassName.length() == 0) throw new IllegalArgumentException("qccClassName is empty");
		m_qccClassName = qccClassName;
		if (ozptMethods != null) {
			for (int i = 0; i < ozptMethods.length; i++) {
				final EmMethod method = ozptMethods[i];
				final String oqccMethodName = method.oqccName();
				if (oqccMethodName != null && oqccMethodName.length() > 0) {
					final EsFunction methodFunction = new EsFunction(method);
					final EsProperty methodProperty = EsProperty.newDontDeleteDontEnum(methodFunction);
					add(oqccMethodName, methodProperty);
				}
			}
		}
	}
	private final String m_qccClassName;
}
