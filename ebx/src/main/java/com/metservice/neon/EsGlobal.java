/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpression;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.Binary;
import com.metservice.argon.DecimalMask;
import com.metservice.argon.TimeFactors;
import com.metservice.argon.TimeMask;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonType;
import com.metservice.argon.xml.W3cDom;
import com.metservice.argon.xml.W3cNode;
import com.metservice.beryllium.BerylliumSupportId;

/**
 * 
 * @author roach
 */
public final class EsGlobal extends EsObject {

	private static final int ATTMASK_INTRINSIC_METHODS = EsProperty.ATT_DONTDELETE | EsProperty.ATT_DONTENUM;

	static EsGlobal newInstance(ISpaceProbe probe, INeonSourceProvider provider, ShellHook shellHook) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (shellHook == null) throw new IllegalArgumentException("object is null");
		final EsGlobal global = new EsGlobal(probe, provider, shellHook);
		global.constructorObject.add("prototype", EsProperty.newReadOnlyDontDeleteDontEnum(global.prototypeObject));
		global.constructorObject.putLengthReadOnly(1);

		global.constructorFunction.add("prototype", EsProperty.newReadOnlyDontDeleteDontEnum(global.prototypeFunction));
		global.constructorFunction.putLengthReadOnly(1);

		global.prototypeObject.add("constructor", EsProperty.newReadOnlyDontDeleteDontEnum(global.constructorObject));

		global.prototypeFunction.putLengthReadOnly(1);

		global.add("Object", EsProperty.newReadOnlyDontDeleteDontEnum(global.constructorObject));
		global.add("Function", EsProperty.newReadOnlyDontDeleteDontEnum(global.constructorFunction));

		return global;
	}

	public static long tsClockNow() {
		return ArgonClock.tsNow();
	}

	private void addMethods(EsExecutionContext ecx, EsObject targetObject, IEsCallable[] ozptMethodCallables, int attributeMask) {
		if (ozptMethodCallables != null) {
			for (int i = 0; i < ozptMethodCallables.length; i++) {
				final IEsCallable methodCallable = ozptMethodCallables[i];
				final String oqccMethodName = methodCallable.oqccName();
				if (oqccMethodName != null && oqccMethodName.length() > 0) {
					final EsFunction methodFunction = ecx.newFunction(methodCallable);
					final EsProperty methodProperty = EsProperty.newDefined(methodFunction, attributeMask);
					targetObject.add(oqccMethodName, methodProperty);
				}
			}
		}
	}

	private void emit(NeonShell.EmitType emitType, String zMessage) {
		if (emitType == null) throw new IllegalArgumentException("object is null");
		if (zMessage == null) throw new IllegalArgumentException("object is null");
		final NeonShell shell = shellHook.shell;
		final String qccSourcePath = shellHook.source.qccPath();
		shell.emit(qccSourcePath, emitType, zMessage);
	}

	private EsObject installed(EsObject oProto) {
		if (oProto == null) throw new IllegalStateException("installStandardIntrinsics");
		return oProto;
	}

	private EsObject installIntrinsicConstructor(EsExecutionContext ecx, EsIntrinsicConstructor constructor,
			EsIntrinsicMethod[] ozptMethods)
			throws InterruptedException {
		assert constructor != null;
		final EsFunction intrinsicConstructorFunction = ecx.newFunction(constructor);
		final EsObject intrinsicPrototypeObject = constructor.declarePrototype(this);
		intrinsicConstructorFunction.enableConstruction(ecx, intrinsicPrototypeObject);
		add(constructor.qccName(), EsProperty.newReadOnlyDontDeleteDontEnum(intrinsicConstructorFunction));
		addMethods(ecx, intrinsicPrototypeObject, ozptMethods, ATTMASK_INTRINSIC_METHODS);
		return intrinsicPrototypeObject;
	}

	// Private
	private void installIntrinsics(EsExecutionContext ecx)
			throws InterruptedException {
		addMethods(ecx, prototypeObject, EsIntrinsicObjectConstructor.Methods, ATTMASK_INTRINSIC_METHODS);
		m_gdPrototypeIntrinsicArray = (EsIntrinsicArray) installIntrinsicConstructor(ecx,
				EsIntrinsicArrayConstructor.newInstance(), EsIntrinsicArrayConstructor.Methods);
		m_gdPrototypeIntrinsicString = (EsIntrinsicString) installIntrinsicConstructor(ecx,
				EsIntrinsicStringConstructor.newInstance(), EsIntrinsicStringConstructor.Methods);
		m_gdPrototypeIntrinsicBoolean = (EsIntrinsicBoolean) installIntrinsicConstructor(ecx,
				EsIntrinsicBooleanConstructor.newInstance(), EsIntrinsicBooleanConstructor.Methods);
		m_gdPrototypeIntrinsicNumber = (EsIntrinsicNumber) installIntrinsicConstructor(ecx,
				EsIntrinsicNumberConstructor.newInstance(), EsIntrinsicNumberConstructor.Methods);
		m_gdPrototypeIntrinsicRegExp = (EsIntrinsicRegExp) installIntrinsicConstructor(ecx,
				EsIntrinsicRegExpConstructor.newInstance(), EsIntrinsicRegExpConstructor.Methods);

		installIntrinsicSingleton(ecx, EsIntrinsicMathSingleton.declare(prototypeObject), EsIntrinsicMathSingleton.Methods);

		m_gdPrototypeIntrinsicTimezone = (EsIntrinsicTimezone) installIntrinsicConstructor(ecx,
				EsIntrinsicTimezoneConstructor.newInstance(), EsIntrinsicTimezoneConstructor.Methods);
		m_gdPrototypeIntrinsicTimemask = (EsIntrinsicTimemask) installIntrinsicConstructor(ecx,
				EsIntrinsicTimemaskConstructor.newInstance(), EsIntrinsicTimemaskConstructor.Methods);
		m_gdPrototypeIntrinsicDecimalmask = (EsIntrinsicDecimalmask) installIntrinsicConstructor(ecx,
				EsIntrinsicDecimalmaskConstructor.newInstance(), EsIntrinsicDecimalmaskConstructor.Methods);
		m_gdPrototypeIntrinsicTimefactors = (EsIntrinsicTimefactors) installIntrinsicConstructor(ecx,
				EsIntrinsicTimefactorsConstructor.newInstance(), EsIntrinsicTimefactorsConstructor.Methods);
		m_gdPrototypeIntrinsicBinary = (EsIntrinsicBinary) installIntrinsicConstructor(ecx,
				EsIntrinsicBinaryConstructor.newInstance(), EsIntrinsicBinaryConstructor.Methods);
		m_gdPrototypeIntrinsicJsonEncoder = (EsIntrinsicJsonEncoder) installIntrinsicConstructor(ecx,
				EsIntrinsicJsonEncoderConstructor.newInstance(), EsIntrinsicJsonEncoderConstructor.Methods);
		m_gdPrototypeIntrinsicJsonDecoder = (EsIntrinsicJsonDecoder) installIntrinsicConstructor(ecx,
				EsIntrinsicJsonDecoderConstructor.newInstance(), EsIntrinsicJsonDecoderConstructor.Methods);
		m_gdPrototypeIntrinsicXmlEncoder = (EsIntrinsicXmlEncoder) installIntrinsicConstructor(ecx,
				EsIntrinsicXmlEncoderConstructor.newInstance(), EsIntrinsicXmlEncoderConstructor.Methods);
		m_gdPrototypeIntrinsicHtmlEncoder = (EsIntrinsicHtmlEncoder) installIntrinsicConstructor(ecx,
				EsIntrinsicHtmlEncoderConstructor.newInstance(), EsIntrinsicHtmlEncoderConstructor.Methods);
		m_gdPrototypeIntrinsicTextDocument = (EsIntrinsicTextDocument) installIntrinsicConstructor(ecx,
				EsIntrinsicTextDocumentConstructor.newInstance(), EsIntrinsicTextDocumentConstructor.Methods);
		m_gdPrototypeIntrinsicW3cDom = (EsIntrinsicW3cDom) installIntrinsicConstructor(ecx,
				EsIntrinsicW3cDomConstructor.newInstance(), EsIntrinsicW3cDomConstructor.Methods);
		m_gdPrototypeIntrinsicW3cNode = (EsIntrinsicW3cNode) installIntrinsicConstructor(ecx,
				EsIntrinsicW3cNodeConstructor.newInstance(), EsIntrinsicW3cNodeConstructor.Methods);
		m_gdPrototypeIntrinsicXPath = (EsIntrinsicXPath) installIntrinsicConstructor(ecx,
				EsIntrinsicXPathConstructor.newInstance(), EsIntrinsicXPathConstructor.Methods);

		installIntrinsicSingleton(ecx, EsIntrinsicJvmSingleton.declare(prototypeObject), EsIntrinsicJvmSingleton.Methods);
		installIntrinsicSingleton(ecx, EsIntrinsicShellSingleton.declare(prototypeObject), EsIntrinsicShellSingleton.Methods);
	}

	private void installIntrinsicSingleton(EsExecutionContext ecx, EsIntrinsicSingleton singleton, EsIntrinsicMethod[] ozptMethods) {
		add(singleton.esClass(), EsProperty.newReadOnlyDontDeleteDontEnum(singleton));
		addMethods(ecx, singleton, ozptMethods, ATTMASK_INTRINSIC_METHODS);
	}

	EsExecutionContext initialExecutionContext()
			throws InterruptedException {
		final EsExecutionContext ecx = new EsExecutionContext(this);
		installIntrinsics(ecx);
		return ecx;
	}

	EmObject install(EsExecutionContext ecx, EmConstructor constructor)
			throws InterruptedException {
		if (ecx == null) throw new IllegalArgumentException("ecx is null");
		final EsFunction modelConstructorFunction = ecx.newFunction(constructor);
		final EmObject modelPrototypeObject = constructor.declarePrototype();
		modelConstructorFunction.enableConstruction(ecx, modelPrototypeObject);
		add(constructor.qccName(), EsProperty.newReadOnlyDontDeleteDontEnum(modelConstructorFunction));
		return modelPrototypeObject;
	}

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

	public void emitFail(String zMessage) {
		emit(NeonShell.EmitType.Fail, zMessage);
	}

	public void emitTrace(String zMessage) {
		emit(NeonShell.EmitType.Trace, zMessage);
	}

	@Override
	public String esClass() {
		return "Global";
	}

	@Override
	public JsonType getJsonType() {
		return null;
	}

	public BerylliumSupportId idSupport() {
		return shellHook.sid;
	}

	/**
	 * Create a new empty intrinsic Array
	 * 
	 * @see ECMA 15.4.2.1
	 * @return [<i>never null</i>]
	 */
	public EsIntrinsicArray newIntrinsicArray() {
		return new EsIntrinsicArray(installed(m_gdPrototypeIntrinsicArray));
	}

	/**
	 * Create a new populated intrinsic Array
	 * 
	 * @param singleMember
	 *              - [<i>non null</i>]
	 * @return [<i>never null</i>]
	 */
	public EsIntrinsicArray newIntrinsicArray(IEsOperand singleMember) {
		if (singleMember == null) throw new IllegalArgumentException("singleMember is null");
		final EsIntrinsicArray array = newIntrinsicArray();
		array.putOnly(singleMember);
		return array;
	}

	/**
	 * Create a new populated intrinsic Array
	 * 
	 * @see ECMA 15.4.2.1
	 * @return [<i>never null</i>]
	 */
	public EsIntrinsicArray newIntrinsicArray(IEsOperand[] zptValues) {
		if (zptValues == null) throw new IllegalArgumentException("zptValues is null");
		final EsIntrinsicArray array = newIntrinsicArray();
		array.put(zptValues);
		return array;
	}

	/**
	 * Create a new populated intrinsic Array
	 * 
	 * @param zValues
	 *              - nulls will be undefined array members
	 * @param length
	 *              - length of Array
	 * @return [<i>never null</i>]
	 */
	public EsIntrinsicArray newIntrinsicArray(IEsOperand[] zValues, int length) {
		if (zValues == null) throw new IllegalArgumentException("zValues is null");
		final EsIntrinsicArray array = newIntrinsicArray();
		array.put(zValues, length);
		return array;
	}

	/**
	 * Create a new populated intrinsic Array
	 * 
	 * @param zloValues
	 *              - nulls will be undefined array members
	 * @return [<i>never null</i>]
	 */
	public EsIntrinsicArray newIntrinsicArray(List<? extends IEsOperand> zloValues) {
		if (zloValues == null) throw new IllegalArgumentException("zloValues is null");
		final EsIntrinsicArray array = newIntrinsicArray();
		array.put(zloValues);
		return array;
	}

	public EsIntrinsicBinary newIntrinsicBinary(Binary value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		final EsIntrinsicBinary neo = new EsIntrinsicBinary(installed(m_gdPrototypeIntrinsicBinary));
		neo.setValue(value);
		return neo;
	}

	/**
	 * Create a new intrinsic Boolean
	 * 
	 * @see ECMA 15.6.2.1
	 * @return [<i>never null</i>]
	 */
	public EsIntrinsicBoolean newIntrinsicBoolean(EsPrimitiveBoolean value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		final EsIntrinsicBoolean neo = new EsIntrinsicBoolean(installed(m_gdPrototypeIntrinsicBoolean));
		neo.setValue(value);
		return neo;
	}

	public EsIntrinsicDecimalmask newIntrinsicDecimalmask(DecimalMask value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		final EsIntrinsicDecimalmask neo = new EsIntrinsicDecimalmask(installed(m_gdPrototypeIntrinsicDecimalmask));
		neo.setValue(value);
		return neo;
	}

	public EsIntrinsicHtmlEncoder newIntrinsicHtmlEncoder(EsObject oRootSource) {
		final EsIntrinsicHtmlEncoder neo = new EsIntrinsicHtmlEncoder(installed(m_gdPrototypeIntrinsicHtmlEncoder));
		neo.setRoot(oRootSource);
		return neo;
	}

	public EsIntrinsicJsonDecoder newIntrinsicJsonDecoder(String qtwSource) {
		if (qtwSource == null || qtwSource.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final EsIntrinsicJsonDecoder neo = new EsIntrinsicJsonDecoder(installed(m_gdPrototypeIntrinsicJsonDecoder));
		neo.setSource(qtwSource);
		return neo;
	}

	public EsIntrinsicJsonEncoder newIntrinsicJsonEncoder(EsObject source) {
		if (source == null) throw new IllegalArgumentException("source is null");
		final EsIntrinsicJsonEncoder neo = new EsIntrinsicJsonEncoder(installed(m_gdPrototypeIntrinsicJsonEncoder));
		neo.setSource(source);
		return neo;
	}

	/**
	 * Create a new intrinsic Number
	 * 
	 * @see ECMA 15.7.2.1
	 * @return [<i>never null</i>]
	 */
	public EsIntrinsicNumber newIntrinsicNumber(EsPrimitiveNumber value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		final EsIntrinsicNumber neo = new EsIntrinsicNumber(installed(m_gdPrototypeIntrinsicNumber));
		neo.setValue(value);
		return neo;
	}

	/**
	 * Create a new intrinsic Object
	 * 
	 * @see ECMA 15.2.2.1
	 * @return [<i>never null</i>]
	 */
	public EsIntrinsicObject newIntrinsicObject() {
		return new EsIntrinsicObject(prototypeObject);
	}

	/**
	 * Create a new intrinsic RegExp
	 * 
	 * @see ECMA 15.10.4.1
	 * @return [<i>never null</i>]
	 */
	public EsIntrinsicRegExp newIntrinsicRegExp(Pattern pattern) {
		if (pattern == null) throw new IllegalArgumentException("pattern is null");
		final EsIntrinsicRegExp neo = new EsIntrinsicRegExp(installed(m_gdPrototypeIntrinsicRegExp));
		neo.setValue(pattern);
		return neo;
	}

	/**
	 * Create a new intrinsic String
	 * 
	 * @see ECMA 15.5.2.1
	 * @return [<i>never null</i>]
	 */
	public EsIntrinsicString newIntrinsicString(EsPrimitiveString value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		final EsIntrinsicString neo = new EsIntrinsicString(installed(m_gdPrototypeIntrinsicString));
		neo.setValue(value);
		return neo;
	}

	/**
	 * Create a new populated intrinsic Array
	 * 
	 * @param zloValues
	 *              - [<i>non null</i>] null strings will be removed.
	 * @param discardEmpty
	 *              - empty strings will be removed if true
	 * @return [<i>never null</i>]
	 */
	public EsIntrinsicArray newIntrinsicStringArray(List<String> zlValues, boolean discardEmpty) {
		if (zlValues == null) throw new IllegalArgumentException("object is null");
		final EsIntrinsicArray array = newIntrinsicArray();

		final int count = zlValues.size();
		if (count == 0) return array;

		final List<IEsOperand> zlOperands = new ArrayList<IEsOperand>(count);
		for (int i = 0; i < count; i++) {
			final String z = zlValues.get(i);
			if (z != null) {
				if (!discardEmpty || z.length() > 0) {
					zlOperands.add(new EsPrimitiveString(z));
				}
			}
		}

		if (zlOperands.isEmpty()) return array;

		array.put(zlOperands);
		return array;
	}

	public EsIntrinsicTextDocument newIntrinsicTextDocument() {
		final EsIntrinsicTextDocument neo = new EsIntrinsicTextDocument(installed(m_gdPrototypeIntrinsicTextDocument));
		return neo;
	}

	public EsIntrinsicTimefactors newIntrinsicTimefactors(TimeFactors value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		final EsIntrinsicTimefactors neo = new EsIntrinsicTimefactors(installed(m_gdPrototypeIntrinsicTimefactors));
		neo.setValue(value);
		return neo;
	}

	public EsIntrinsicTimemask newIntrinsicTimemask(TimeMask value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		final EsIntrinsicTimemask neo = new EsIntrinsicTimemask(installed(m_gdPrototypeIntrinsicTimemask));
		neo.setValue(value);
		return neo;
	}

	public EsIntrinsicTimezone newIntrinsicTimezone(TimeZone value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		final EsIntrinsicTimezone neo = new EsIntrinsicTimezone(installed(m_gdPrototypeIntrinsicTimezone));
		neo.setValue(value);
		return neo;
	}

	public EsIntrinsicW3cDom newIntrinsicW3cDom(EsExecutionContext ecx, Binary content, boolean validating) {
		if (content == null) throw new IllegalArgumentException("object is null");
		final EsIntrinsicW3cDom neo = new EsIntrinsicW3cDom(installed(m_gdPrototypeIntrinsicW3cDom));
		neo.setValue(ecx, content, validating);
		return neo;
	}

	public EsIntrinsicW3cDom newIntrinsicW3cDom(EsExecutionContext ecx, W3cDom dom) {
		if (dom == null) throw new IllegalArgumentException("object is null");
		final EsIntrinsicW3cDom neo = new EsIntrinsicW3cDom(installed(m_gdPrototypeIntrinsicW3cDom));
		neo.setValue(ecx, dom);
		return neo;
	}

	public EsIntrinsicW3cNode newIntrinsicW3cNode(W3cNode node) {
		if (node == null) throw new IllegalArgumentException("object is null");
		final EsIntrinsicW3cNode neo = new EsIntrinsicW3cNode(installed(m_gdPrototypeIntrinsicW3cNode));
		neo.setValue(node);
		return neo;
	}

	public EsIntrinsicXmlEncoder newIntrinsicXmlEncoder(String qtwRootTag, EsObject oRootSource) {
		if (qtwRootTag == null || qtwRootTag.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final EsIntrinsicXmlEncoder neo = new EsIntrinsicXmlEncoder(installed(m_gdPrototypeIntrinsicXmlEncoder));
		neo.setRoot(qtwRootTag, oRootSource);
		return neo;
	}

	public EsIntrinsicXPath newIntrinsicXPath(XPathExpression expression, String qSource) {
		if (expression == null) throw new IllegalArgumentException("object is null");
		if (qSource == null || qSource.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final EsIntrinsicXPath neo = new EsIntrinsicXPath(installed(m_gdPrototypeIntrinsicXPath));
		neo.setValue(expression, qSource);
		return neo;
	}

	public void putReadOnly(String qccPropertyName, IEsOperand value) {
		if (qccPropertyName == null || qccPropertyName.length() == 0)
			throw new IllegalArgumentException("qccPropertyName is empty");
		if (value == null) throw new IllegalArgumentException("value is null");
		add(qccPropertyName, EsProperty.newReadOnlyDontDelete(value));
	}

	public void putSingleton(EmObject emObject) {
		if (emObject == null) throw new IllegalArgumentException("esmObject is null");
		add(emObject.esClass(), EsProperty.newReadOnlyDontDelete(emObject));
	}

	public void setInitNow(long ts) {
		m_initTimestamp.set(ts);
	}

	public long tsInitNow() {
		return m_initTimestamp.get();
	}

	private EsGlobal(ISpaceProbe probe, INeonSourceProvider provider, ShellHook shellHook) {
		assert probe != null;
		assert provider != null;
		assert shellHook != null;
		this.probe = probe;
		this.sourceProvider = provider;
		this.shellHook = shellHook;
		this.commitAgenda = new EsCommitAgenda();
		prototypeObject = new EsIntrinsicObject();
		final EsScopeChain scope = new EsScopeChain(this);
		prototypeFunction = new EsFunction(prototypeObject, scope, EsIntrinsicUtility.Void);
		constructorObject = new EsFunction(prototypeFunction, scope, EsIntrinsicUtility.Void);
		constructorFunction = new EsFunction(prototypeFunction, scope, EsIntrinsicUtility.Void);
		m_initTimestamp = new AtomicLong(ArgonClock.tsNow());
	}
	public final ISpaceProbe probe;
	public final INeonSourceProvider sourceProvider;
	public final ShellHook shellHook;
	public final EsCommitAgenda commitAgenda;
	public final EsIntrinsicObject prototypeObject; // ECMA 15.2.4
	public final EsFunction prototypeFunction; // ECMA 15.3.4
	public final EsFunction constructorObject; // ECMA 15.2.3
	public final EsFunction constructorFunction; // ECMA 15.3.3
	private final AtomicLong m_initTimestamp;
	private EsIntrinsicArray m_gdPrototypeIntrinsicArray; // ECMA 15.4.3.1
	private EsIntrinsicString m_gdPrototypeIntrinsicString; // ECMA 15.5.3.1
	private EsIntrinsicBoolean m_gdPrototypeIntrinsicBoolean; // ECMA 15.6.3.1
	private EsIntrinsicNumber m_gdPrototypeIntrinsicNumber; // ECMA 15.7.3.1
	private EsIntrinsicRegExp m_gdPrototypeIntrinsicRegExp; // ECMA 15.10.5.1
	private EsIntrinsicTimezone m_gdPrototypeIntrinsicTimezone;
	private EsIntrinsicTimemask m_gdPrototypeIntrinsicTimemask;
	private EsIntrinsicDecimalmask m_gdPrototypeIntrinsicDecimalmask;
	private EsIntrinsicTimefactors m_gdPrototypeIntrinsicTimefactors;
	private EsIntrinsicBinary m_gdPrototypeIntrinsicBinary;
	private EsIntrinsicJsonEncoder m_gdPrototypeIntrinsicJsonEncoder;
	private EsIntrinsicJsonDecoder m_gdPrototypeIntrinsicJsonDecoder;
	private EsIntrinsicXmlEncoder m_gdPrototypeIntrinsicXmlEncoder;
	private EsIntrinsicHtmlEncoder m_gdPrototypeIntrinsicHtmlEncoder;
	private EsIntrinsicTextDocument m_gdPrototypeIntrinsicTextDocument;
	private EsIntrinsicW3cDom m_gdPrototypeIntrinsicW3cDom;
	private EsIntrinsicW3cNode m_gdPrototypeIntrinsicW3cNode;
	private EsIntrinsicXPath m_gdPrototypeIntrinsicXPath;
}
