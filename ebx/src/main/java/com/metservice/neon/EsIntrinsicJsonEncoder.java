/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.nio.charset.Charset;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonEncoder;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsIntrinsicJsonEncoder extends EsObject {

	public static final String PropertyName_charset = "charset";
	public static final String PropertyValue_charset = ArgonText.CHARSET_NAME_ASCII;
	public static final EsPrimitiveString PropertyOperand_charset = new EsPrimitiveString(PropertyValue_charset);

	public static final String PropertyName_indent = "indent";
	public static final int PropertyValue_indent = 1;
	public static final EsPrimitiveNumberInteger PropertyOperand_indent = new EsPrimitiveNumberInteger(PropertyValue_indent);

	public static final String PropertyName_quotedProperties = "quotedProperties";
	public static final boolean PropertyValue_quotedProperties = false;
	public static final EsPrimitiveBoolean PropertyOperand_quotedProperties = EsPrimitiveBoolean
			.instance(PropertyValue_quotedProperties);

	void loadPrototype() {
		add(PropertyName_charset, EsProperty.newDontDelete(PropertyOperand_charset));
		add(PropertyName_indent, EsProperty.newDontDelete(PropertyOperand_indent));
		add(PropertyName_quotedProperties, EsProperty.newDontDelete(PropertyOperand_quotedProperties));
	}

	JsonEncoder newEncoder(EsExecutionContext ecx, boolean quotedProperties, boolean escape0080)
			throws InterruptedException {
		final int indent = UNeon.property_int(ecx, this, PropertyName_indent);
		return new JsonEncoder(1024, indent, quotedProperties, escape0080, false);
	}

	Charset propertyCharset(EsExecutionContext ecx)
			throws InterruptedException {
		return UNeon.property_charset(ecx, this, PropertyName_charset);
	}

	boolean propertyQuotedProperties(EsExecutionContext ecx)
			throws InterruptedException {
		return UNeon.property_boolean(this, PropertyName_quotedProperties);
	}

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	@Override
	public IJsonNative createJsonNative() {
		return m_oSource == null ? null : m_oSource.createJsonNative();
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicJsonEncoder(this);
	}

	@Override
	public String esClass() {
		return EsIntrinsicJsonEncoderConstructor.ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return m_oSource == null ? null : m_oSource.getJsonType();
	}

	public Binary newBinary(EsExecutionContext ecx)
			throws InterruptedException {
		return newBinary(ecx, propertyQuotedProperties(ecx));
	}

	public Binary newBinary(EsExecutionContext ecx, boolean quotedProperties)
			throws InterruptedException {
		if (m_oSource == null) return Binary.Empty;
		final Charset charset = propertyCharset(ecx);
		final boolean escape0080 = charset.equals(ArgonText.ASCII);
		final String qenc = newEncoder(ecx, quotedProperties, escape0080).encode(m_oSource);
		return Binary.newFromString(charset, qenc);
	}

	public String newString(EsExecutionContext ecx)
			throws InterruptedException {
		return newString(ecx, propertyQuotedProperties(ecx));
	}

	public String newString(EsExecutionContext ecx, boolean quotedProperties)
			throws InterruptedException {
		if (ecx == null) throw new IllegalArgumentException("ecx is null");
		if (m_oSource == null) return "";
		return newEncoder(ecx, quotedProperties, true).encode(m_oSource);
	}

	public String qlctwContentType(EsExecutionContext ecx)
			throws InterruptedException {
		return "text/plain;" + ArgonText.charsetName(propertyCharset(ecx)).toLowerCase();
	}

	public void setSource(EsObject source) {
		if (source == null) throw new IllegalArgumentException("source is null");
		m_oSource = source;
	}

	@Override
	public String show(int depth) {
		return "JsonEncoder(" + (m_oSource == null ? "" : m_oSource.show(depth)) + ")";
	}

	public EsPrimitiveString toPrimitiveString(EsExecutionContext ecx)
			throws InterruptedException {
		return new EsPrimitiveString(newString(ecx));
	}

	public EsIntrinsicJsonEncoder(EsObject prototype) {
		super(prototype);
	}

	private EsObject m_oSource;
}
