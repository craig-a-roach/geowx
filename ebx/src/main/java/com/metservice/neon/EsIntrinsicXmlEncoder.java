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
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public class EsIntrinsicXmlEncoder extends EsObject {

	void loadPrototype() {
		add(PropertyName_validationMethod, EsProperty.newDontDelete(PropertyOperand_validationMethod));
		add(PropertyName_defaultForm, EsProperty.newDontDelete(PropertyOperand_defaultForm));
		add(PropertyName_charset, EsProperty.newDontDelete(PropertyOperand_charset));
		add(PropertyName_indent, EsProperty.newDontDelete(PropertyOperand_indent));
	}

	UNeonXmlEncode.Args newEncoderArgs(EsExecutionContext ecx)
			throws InterruptedException {
		final UNeonXmlEncode.Args args = UNeonXmlEncode.newArgs(qtwRootTag());
		args.oRoot = m_oRootSource;
		args.oqtwNamespaceUri = UNeon.property_oqtwString(ecx, this, PropertyName_namespaceUri);
		args.oqtwSchemaLocation = UNeon.property_oqtwString(ecx, this, PropertyName_schemaLocation);
		args.oqtwDtdLocation = UNeon.property_oqtwString(ecx, this, PropertyName_dtdLocation);
		args.validationMethod = UNeon.property_enum(ecx, this, PropertyName_validationMethod,
				UNeonXmlEncode.DecoderValidationMethod);
		args.defaultForm = UNeon.property_enum(ecx, this, PropertyName_defaultForm, UNeonXmlEncode.DecoderDefaultForm);
		args.charset = UNeon.property_charset(ecx, this, PropertyName_charset);
		args.indent = UNeon.property_int(ecx, this, PropertyName_indent);
		return args;
	}

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
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
		return new EsIntrinsicXmlEncoder(this);
	}

	@Override
	public String esClass() {
		return EsIntrinsicXmlEncoderConstructor.ClassName;
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
		final UNeonXmlEncode.Args encoderArgs = newEncoderArgs(ecx);
		final String zEncoded = UNeonXmlEncode.encode(ecx, encoderArgs);
		return Binary.newFromString(encoderArgs.charset, zEncoded);
	}

	public String newString(EsExecutionContext ecx)
			throws InterruptedException {
		final UNeonXmlEncode.Args encoderArgs = newEncoderArgs(ecx);
		return UNeonXmlEncode.encode(ecx, encoderArgs);
	}

	public String qtwRootTag() {
		if (m_oqtwRootTag == null) throw new IllegalStateException("member m_oqtwRootTag is null");
		return m_oqtwRootTag;
	}

	public void setRoot(String qtwRootTag, EsObject oRootSource) {
		if (qtwRootTag == null || qtwRootTag.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_oqtwRootTag = qtwRootTag;
		m_oRootSource = oRootSource;
	}

	@Override
	public String show(int depth) {
		final StringBuilder sb = new StringBuilder();
		sb.append("XmlEncoder(");
		if (m_oqtwRootTag != null) {
			sb.append(m_oqtwRootTag);
		}
		sb.append(")");
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

	public EsIntrinsicXmlEncoder(EsObject prototype) {
		super(prototype);
	}

	private String m_oqtwRootTag;
	private EsObject m_oRootSource;

	public static final String PropertyName_charset = "charset";
	public static final String PropertyValue_charset = ArgonText.CHARSET_NAME_UTF8;
	public static final EsPrimitiveString PropertyOperand_charset = new EsPrimitiveString(PropertyValue_charset);

	public static final String PropertyName_indent = "indent";
	public static final int PropertyValue_indent = 2;
	public static final EsPrimitiveNumberInteger PropertyOperand_indent = new EsPrimitiveNumberInteger(PropertyValue_indent);

	public static final String PropertyName_validationMethod = "validationMethod";
	public static final UNeonXmlEncode.ValidationMethod PropertyValue_validationMethod = UNeonXmlEncode.ValidationMethod.W3Schema;
	public static final EsPrimitiveString PropertyOperand_validationMethod = new EsPrimitiveString(PropertyValue_validationMethod);

	public static final String PropertyName_defaultForm = "defaultForm";
	public static final UNeonXmlEncode.DefaultForm PropertyValue_defaultForm = UNeonXmlEncode.DefaultForm.ElementText;
	public static final EsPrimitiveString PropertyOperand_defaultForm = new EsPrimitiveString(PropertyValue_defaultForm);

	public static final String PropertyName_namespaceUri = "namespaceUri";
	public static final String PropertyName_schemaLocation = "schemaLocation";
	public static final String PropertyName_dtdLocation = "dtdLocation";
}
