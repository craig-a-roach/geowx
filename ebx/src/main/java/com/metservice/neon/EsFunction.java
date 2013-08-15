/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsFunction extends EsObject {

	public static final String ClassName = "Function";
	public static final String PName_constructor = "constructor";
	public static final String PName_prototype = "prototype";

	private static final int ATTMASK_CONSTRUCTOR = EsProperty.ATT_DONTENUM;
	private static final int ATTMASK_DEFAULT_PROTOTYPE = EsProperty.ATT_DONTDELETE;
	private static final int ATTMASK_INTRINSIC_PROTOTYPE = EsProperty.ATT_DONTDELETE | EsProperty.ATT_DONTENUM
			| EsProperty.ATT_READONLY;

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	public IEsCallable callable() {
		return m_callable;
	}

	@Override
	public IJsonNative createJsonNative() {
		return null;
	}

	@Override
	public EsObject createObject() {
		return null;
	}

	public void enableConstruction(EsExecutionContext ecx) {
		final EsObject prototype = ecx.global().newIntrinsicObject();
		prototype.add(PName_constructor, EsProperty.newDefined(this, ATTMASK_CONSTRUCTOR));
		add(PName_prototype, EsProperty.newDefined(prototype, ATTMASK_DEFAULT_PROTOTYPE));
	}

	public void enableConstruction(EsExecutionContext ecx, EsObject prototype)
			throws InterruptedException {
		if (prototype == null) throw new IllegalArgumentException("prototype is null");
		prototype.toObject(ecx);
		prototype.add(PName_constructor, EsProperty.newDefined(this, ATTMASK_CONSTRUCTOR));
		add(PName_prototype, EsProperty.newDefined(prototype, ATTMASK_INTRINSIC_PROTOTYPE));
	}

	@Override
	public String esClass() {
		return ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return null;
	}

	/**
	 * A scope chain that defines the environment in which this function object is executed.
	 * 
	 * @see ECMA 8.6.2
	 * @return [<i>possibly null</i>] - null for provider callables
	 */
	public EsScopeChain getScope() {
		return m_oScope;
	}

	public EsFunction(EsObject prototype, EsScopeChain scope, IEsCallable callable) {
		super(prototype);
		if (scope == null) throw new IllegalArgumentException("scope is null");
		if (callable == null) throw new IllegalArgumentException("callable is null");
		m_oScope = scope;
		m_callable = callable;
		putLengthReadOnly(callable.zlFormalParameterNames().size());
	}

	public EsFunction(IEsCallable callable) {
		m_oScope = null;
		m_callable = callable;
		putLengthReadOnly(callable.zlFormalParameterNames().size());
	}

	private final EsScopeChain m_oScope;

	private final IEsCallable m_callable;
}
