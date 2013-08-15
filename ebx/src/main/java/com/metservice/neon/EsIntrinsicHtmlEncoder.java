/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.nio.charset.Charset;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonType;

/**
 * @author roach
 */
public class EsIntrinsicHtmlEncoder extends EsObject {

	void loadPrototype() {
		add(PropertyName_docType, EsProperty.newDontDelete(PropertyOperand_docType));
		add(PropertyName_dtdLocation, EsProperty.newDontDelete(PropertyOperand_dtdLocation));
		add(PropertyName_charset, EsProperty.newDontDelete(PropertyOperand_charset));
		add(PropertyName_indent, EsProperty.newDontDelete(PropertyOperand_indent));
	}

	UNeonHtmlEncode.Args newEncoderArgs(EsExecutionContext ecx)
			throws InterruptedException {
		final UNeonHtmlEncode.Args args = UNeonHtmlEncode.newArgs();
		args.oRoot = m_oRootSource;
		args.oqtwDocType = UNeon.property_oqtwString(ecx, this, PropertyName_docType);
		args.oqtwDtdLocation = UNeon.property_oqtwString(ecx, this, PropertyName_dtdLocation);
		args.indent = UNeon.property_int(ecx, this, PropertyName_indent);
		return args;
	}

	@Override
	protected void loadProperties(EsExecutionContext ecx)
			throws InterruptedException {
	}

	public Charset charset(EsExecutionContext ecx)
			throws InterruptedException {
		return UNeon.property_charset(ecx, this, PropertyName_charset);
	}

	@Override
	public IJsonNative createJsonNative() {
		return m_oRootSource == null ? null : m_oRootSource.createJsonNative();
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicHtmlEncoder(this);
	}

	@Override
	public String esClass() {
		return EsIntrinsicHtmlEncoderConstructor.ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return m_oRootSource == null ? null : m_oRootSource.getJsonType();
	}

	public EsObject getRootSource() {
		return m_oRootSource;
	}

	public Binary newBinary(EsExecutionContext ecx)
			throws InterruptedException {
		final UNeonHtmlEncode.Args encoderArgs = newEncoderArgs(ecx);
		final Charset charset = charset(ecx);
		final String zEncoded = UNeonHtmlEncode.encode(ecx, encoderArgs);
		return Binary.newFromString(charset, zEncoded);
	}

	public String newString(EsExecutionContext ecx)
			throws InterruptedException {
		final UNeonHtmlEncode.Args encoderArgs = newEncoderArgs(ecx);
		return UNeonHtmlEncode.encode(ecx, encoderArgs);
	}

	public void setRoot(EsObject oRootSource) {
		m_oRootSource = oRootSource;
	}

	@Override
	public String show(int depth) {
		final StringBuilder sb = new StringBuilder();
		sb.append("HtmlEncoder");
		if (depth > 0) {
			if (m_oRootSource != null) {
				sb.append("\n");
				sb.append(m_oRootSource.show(depth - 1));
			}
		}
		return sb.toString();
	}

	public EsPrimitiveString toPrimitiveString(EsExecutionContext ecx)
			throws InterruptedException {
		return new EsPrimitiveString(newString(ecx));
	}

	public EsIntrinsicHtmlEncoder(EsObject prototype) {
		super(prototype);
	}

	private EsObject m_oRootSource;

	public static final String PropertyName_charset = "charset";
	public static final String PropertyValue_charset = ArgonText.CHARSET_NAME_UTF8;
	public static final EsPrimitiveString PropertyOperand_charset = new EsPrimitiveString(PropertyValue_charset);

	public static final String PropertyName_indent = "indent";
	public static final int PropertyValue_indent = 2;
	public static final EsPrimitiveNumberInteger PropertyOperand_indent = new EsPrimitiveNumberInteger(PropertyValue_indent);

	public static final String PropertyName_docType = "docType";
	public static final String PropertyValue_docType = "-//W3C//DTD HTML 4.01 Transitional//EN";
	public static final EsPrimitiveString PropertyOperand_docType = new EsPrimitiveString(PropertyValue_docType);

	public static final String PropertyName_dtdLocation = "dtdLocation";
	public static final String PropertyValue_dtdLocation = "http://www.w3.org/TR/html4/strict.dtd";
	public static final EsPrimitiveString PropertyOperand_dtdLocation = new EsPrimitiveString(PropertyValue_dtdLocation);
}
