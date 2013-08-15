/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.Binary;

/**
 * 
 * @author roach
 */
public class EsIntrinsicW3cDomConstructor extends EsIntrinsicConstructor {

	public static final String ClassName = "W3cDom";
	public static final String OptionValidating = "[validating]";
	public static final String PropertyName_document = "document";
	public static final String PropertyName_fatalError = "fatalError";
	public static final String PropertyName_validationErrors = "validationErrors";
	public static final String PropertyName_warnings = "warnings";
	public static final String PropertyName_xsltFatalError = "xsltFatalError";
	public static final String PropertyName_xsltValidationErrors = "xsltValidationErrors";
	public static final String PropertyName_xsltWarnings = "xsltWarnings";
	public static final EsIntrinsicMethod[] Methods = { method_transform() };

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsIntrinsicW3cDom self = thisIntrinsicObject(ecx, EsIntrinsicW3cDom.class);
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final IEsOperand contentOperand = ac.esOperandDatum(0);
		final EsType contentType = contentOperand.esType();
		Binary oContent = null;
		if (contentType == EsType.TObject) {
			if (contentOperand instanceof EsIntrinsicBinary) {
				oContent = ((EsIntrinsicBinary) contentOperand).value();
			}
		}
		if (oContent == null) {
			oContent = Binary.newFromStringUTF8(contentOperand.toCanonicalString(ecx));
		}
		final String zlcOptions = ac.defaulted(1) ? "" : ac.zStringValue(1).toLowerCase();
		final boolean validating = zlcOptions.contains(OptionValidating);
		self.setValue(ecx, oContent, validating);
		return self;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicW3cDom(global.prototypeObject);
	}

	private static EsIntrinsicMethod method_transform() {
		return new EsIntrinsicMethod("transform", new String[] { "source" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicW3cDom self = thisIntrinsicObject(ecx, EsIntrinsicW3cDom.class);
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final EsIntrinsicW3cNode source = ac.esObject(0, ClassName, EsIntrinsicW3cNode.class);
				return self.transform(ecx, source);
			}
		};
	}

	public static EsIntrinsicW3cDomConstructor newInstance() {
		return new EsIntrinsicW3cDomConstructor();
	}

	private EsIntrinsicW3cDomConstructor() {
		super(ClassName, new String[] { "content", "options" }, 1);
	}
}
