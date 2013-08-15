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
public abstract class EsIntrinsicSingleton extends EsObject {

	protected final void installConstant(String qccName, double x) {
		add(qccName, EsProperty.newReadOnlyDontDelete(new EsPrimitiveNumberDouble(x)));
	}

	@Override
	protected void loadProperties(EsExecutionContext ecx)
			throws InterruptedException {
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
		return m_qccName;
	}

	@Override
	public JsonType getJsonType() {
		return null;
	}

	public String qDetail() {
		return m_qccName;
	}

	protected EsIntrinsicSingleton(EsIntrinsicObject prototype, String qccName) {
		super(prototype);
		if (qccName == null || qccName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_qccName = qccName;
	}

	private final String m_qccName;
}
