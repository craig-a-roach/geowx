/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.List;

import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsActivation extends EsObject {

	public static final String PName_arguments = "arguments";

	private static final int ATTMASK_ARGUMENTS = EsProperty.ATT_DONTDELETE;
	private static final int ATTMASK_ARG = EsProperty.ATT_DONTDELETE | EsProperty.ATT_DONTENUM;

	/**
	 * @see ECMA 10.1.8
	 * @param global
	 *              [<i>non null</i>]
	 * @param callee
	 *              [<i>non null</i>] - the function object being executed
	 * @param argumentList
	 *              [<i>non null</i>]
	 * @return [<i>never null</i>]
	 */
	public static EsActivation newInstance(EsGlobal global, EsFunction callee, EsList argumentList) {
		if (global == null) throw new IllegalArgumentException("global is null");
		if (callee == null) throw new IllegalArgumentException("callee is null");
		if (argumentList == null) throw new IllegalArgumentException("argumentList is null");

		final IEsCallable callable = callee.callable();
		final boolean isIntrinsic = callable.isIntrinsic();
		final List<String> zlFormalParameterNames = callable.zlFormalParameterNames();
		final int formalParameterCount = zlFormalParameterNames.size();
		final int requiredArgumentCount = callable.requiredArgumentCount();
		final int argumentCount = argumentList.length();
		if (argumentCount < requiredArgumentCount) {
			final String msg;
			if (argumentCount < formalParameterCount) {
				msg = "Value of required formal parameter '" + zlFormalParameterNames.get(argumentCount) + "' is undefined";
			} else {
				msg = "One or more required argument values are undefined";
			}
			throw new EsTypeCodeException(msg);
		}

		final EsArguments arguments = new EsArguments(global.prototypeObject, zlFormalParameterNames);
		if (!isIntrinsic) {
			arguments.putLengthReadOnly(argumentCount);
		}

		final EsActivation activation = new EsActivation(arguments);

		final IEsOperand[] ozptArgumentValues;
		if (isIntrinsic) {
			ozptArgumentValues = new IEsOperand[argumentCount];
		} else {
			ozptArgumentValues = null;
		}
		for (int i = 0; i < argumentCount; i++) {
			final String argName = Integer.toString(i);
			final IEsOperand argValue = argumentList.operand(i);
			final EsProperty argProperty = EsProperty.newDefined(argValue, ATTMASK_ARG);
			if (i < formalParameterCount) {
				final String formalParameterName = zlFormalParameterNames.get(i);
				activation.add(formalParameterName, argProperty);
			}
			if (ozptArgumentValues == null) {
				arguments.add(argName, argProperty);
			} else {
				ozptArgumentValues[i] = argValue;
			}
		}
		arguments.setArgumentValues(ozptArgumentValues);

		activation.add(PName_arguments, EsProperty.newDefined(arguments, ATTMASK_ARGUMENTS));
		return activation;
	}

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	public EsArguments arguments() {
		return m_arguments;
	}

	@Override
	public IJsonNative createJsonNative() {
		return null;
	}

	@Override
	public EsObject createObject() {
		return null;
	}

	@Override
	public String esClass() {
		return "Activation";
	}

	@Override
	public JsonType getJsonType() {
		return null;
	}

	@Override
	public String show(int depth) {
		return super.show(depth) + "\narguments[" + m_arguments.show(depth) + "]";
	}

	private EsActivation(EsArguments arguments) {
		assert arguments != null;
		m_arguments = arguments;
	}

	private final EsArguments m_arguments;
}
