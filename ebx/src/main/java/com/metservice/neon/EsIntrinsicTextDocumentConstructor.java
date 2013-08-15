/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.Binary;

public class EsIntrinsicTextDocumentConstructor extends EsIntrinsicConstructor {

	public static final String ClassName = "TextDocument";
	private static final String PNAME_DEFAULT_LINE_TERMINATOR = "defaultLineTerminator";
	private static final EsPrimitiveString PVALUE_DEFAULT_LINE_TERMINATOR = new EsPrimitiveString("\n");
	private static final String PNAME_DEFAULT_CHARSET = "defaultCharset";
	private static final EsPrimitiveString PVALUE_DEFAULT_CHARSET = new EsPrimitiveString("UTF-8");

	public static final EsIntrinsicMethod[] Methods = { method_toString(), method_toBinary(), method_add(), method_align(),
			method_pad(), method_border() };

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsActivation activation = ecx.activation();
		final EsArguments arguments = activation.arguments();
		final int argCount = arguments.length();
		final EsIntrinsicTextDocument neo = thisIntrinsicObject(ecx, EsIntrinsicTextDocument.class);
		for (int i = 0; i < argCount; i++) {
			neo.add(ecx, arguments.operand(i), "", 0, "");
		}
		return neo;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		final EsIntrinsicTextDocument neo = new EsIntrinsicTextDocument(global.prototypeObject);
		neo.add(PNAME_DEFAULT_LINE_TERMINATOR, EsProperty.newDontDelete(PVALUE_DEFAULT_LINE_TERMINATOR));
		neo.add(PNAME_DEFAULT_CHARSET, EsProperty.newDontDelete(PVALUE_DEFAULT_CHARSET));
		return neo;
	}

	/**
	 * @jsmethod add
	 * @jsnote Add an item to the text document with optional layout parameters.
	 * @jsnote If the part is an Array then the items of the array are added as comma separated values.
	 * @jsparam part Can be a TextDocument, an Array, HtmlNode, a boolean, a number or a string.
	 * @jsparam compass A string. Optional. Can be "above", "below", "left" or "right".
	 * @jsparam width An integer. Optional. The width of the field.
	 * @jsparam align A string. Optional. The alignment of the text, can be "left", "centre", "right", "none" or
	 *          "auto"
	 */
	private static EsIntrinsicMethod method_add() {
		return new EsIntrinsicMethod("add", new String[] { "part", "compass", "width", "align" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments arguments = ecx.activation().arguments();
				final int argCount = arguments.length();
				final EsIntrinsicTextDocument self = thisIntrinsicObject(ecx, EsIntrinsicTextDocument.class);
				final IEsOperand value = arguments.operand(0);
				final String zCompass = argCount < 2 ? "" : arguments.operand(1).toCanonicalString(ecx);
				final int width = argCount < 3 ? 0 : arguments.operand(2).toNumber(ecx).intVerified();
				final String zAlign = argCount < 4 ? "" : arguments.operand(3).toCanonicalString(ecx);
				self.add(ecx, value, zCompass, width, zAlign);
				return self;
			}
		};
	}

	/**
	 * @jsmethod align
	 * @jsparma align A string. Required. The alignment of the text, can be "left", "centre", "right", "none" or
	 *          "auto"
	 * @jsnote Aligns all of the parts of the text docuemnt in the same way.
	 */
	private static EsIntrinsicMethod method_align() {
		return new EsIntrinsicMethod("align", new String[] { "align" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments arguments = ecx.activation().arguments();
				final EsIntrinsicTextDocument self = thisIntrinsicObject(ecx, EsIntrinsicTextDocument.class);
				final String zAlign = arguments.operand(0).toCanonicalString(ecx);
				self.align(zAlign);
				return self;
			}
		};
	}

	/**
	 * @jsmethod border
	 * @jsparam compass A string. Required. Can be "above", "below", "left" or "right". Specifies which border to set.
	 * @jsparam line A string. Required.
	 * @jsparam cornerA A string. Optional.
	 * @jsparam cornerB A string. Optional.
	 */
	private static EsIntrinsicMethod method_border() {
		return new EsIntrinsicMethod("border", new String[] { "compass", "line", "cornerA", "cornerB" }, 2) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments arguments = ecx.activation().arguments();
				final int argCount = arguments.length();
				final EsIntrinsicTextDocument self = thisIntrinsicObject(ecx, EsIntrinsicTextDocument.class);
				final String zCompass = arguments.operand(0).toCanonicalString(ecx);
				final String zLine = arguments.operand(1).toCanonicalString(ecx);
				final String zCornerA = argCount < 3 ? zLine : arguments.operand(2).toCanonicalString(ecx);
				final String zCornerB = argCount < 4 ? zCornerA : arguments.operand(3).toCanonicalString(ecx);
				self.border(zCompass, zLine, zCornerA, zCornerB);
				return self;
			}
		};
	}

	/**
	 * @jsmethod pad
	 * @jsnote Specify the amount of padding around the outside of the text document.
	 * @jsnote If no arguments are given then the padding is set to zero for all four sides.
	 * @jsparam left A number. Optional.
	 * @jsparam right A number. Optional.
	 * @jsparam above A number. Optional.
	 * @jsparam below A number. Optional.
	 * 
	 */
	private static EsIntrinsicMethod method_pad() {
		return new EsIntrinsicMethod("pad", new String[] { "left", "right", "above", "below" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments arguments = ecx.activation().arguments();
				final int argCount = arguments.length();
				final EsIntrinsicTextDocument self = thisIntrinsicObject(ecx, EsIntrinsicTextDocument.class);
				final int left = argCount < 1 ? 0 : arguments.operand(0).toNumber(ecx).intVerified();
				final int right = argCount < 2 ? 0 : arguments.operand(1).toNumber(ecx).intVerified();
				final int above = argCount < 3 ? 0 : arguments.operand(2).toNumber(ecx).intVerified();
				final int below = argCount < 4 ? 0 : arguments.operand(3).toNumber(ecx).intVerified();
				self.pad(left, right, above, below);
				return self;
			}
		};
	}

	/**
	 * @jsmethod toBinary
	 * @jsparam lineTerminator Optional, a string containing the characters used to terminate a line.
	 * @jsparam charset Optional, A string describing the character set.
	 * @jsreturn A Binary object containing a representation of the text document.
	 */
	private static EsIntrinsicMethod method_toBinary() {
		return new EsIntrinsicMethod("toBinary", new String[] { "lineTerminator", "charset" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicTextDocument self = thisIntrinsicObject(ecx, EsIntrinsicTextDocument.class);
				final EsArguments args = ecx.activation().arguments();
				final int argCount = args.length();
				final IEsOperand lineTerminatorOperand = (argCount < 1) ? self.esGet(PNAME_DEFAULT_LINE_TERMINATOR) : args
						.operand(0);
				final IEsOperand charsetOperand = (argCount < 2) ? self.esGet(PNAME_DEFAULT_CHARSET) : args.operand(1);
				final String zLineTerminator = lineTerminatorOperand.toCanonicalString(ecx);
				final String zCharset = charsetOperand.toCanonicalString(ecx);
				final Binary binary = self.newBinary(zLineTerminator, zCharset);
				return ecx.global().newIntrinsicBinary(binary);
			}
		};
	}

	/**
	 * @jsmethod toString
	 * @jsparam lineTerminator Optional, a string containing the characters used to terminate a line.
	 * @jsreturn The text document as a string.
	 */
	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString", new String[] { "lineTerminator" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicTextDocument self = thisIntrinsicObject(ecx, EsIntrinsicTextDocument.class);
				final EsArguments args = ecx.activation().arguments();
				final int argCount = args.length();
				final IEsOperand lineTerminatorOperand = (argCount == 0) ? self.esGet(PNAME_DEFAULT_LINE_TERMINATOR) : args
						.operand(0);
				final String zLineTerminator = lineTerminatorOperand.toCanonicalString(ecx);
				return self.toPrimitiveString(zLineTerminator);
			}
		};
	}

	public static EsIntrinsicTextDocumentConstructor newInstance() {
		return new EsIntrinsicTextDocumentConstructor();
	}

	/**
	 * @jsconstructor TextDocument
	 * @jsparam parts Optional. Can be a TextDocument, an Array, HtmlNode, a Boolean, a Number or a string. May be
	 *          repeated.
	 */
	protected EsIntrinsicTextDocumentConstructor() {
		super(ClassName, new String[] { "parts" }, 0);
	}
}
