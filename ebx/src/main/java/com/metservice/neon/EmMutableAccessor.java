/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
public class EmMutableAccessor {

	public EsType esType(String qccPropertyName) {
		return src.esGet(qccPropertyName).esType();
	}

	public EmMutablePropertyAccessor newPropertyAccessor(String qccPropertyName) {
		return new EmMutablePropertyAccessor(this, qccPropertyName);
	}

	public EmMutableAccessor(EsExecutionContext ecx, EmMutableObject<?, ?> src) {
		if (ecx == null) throw new IllegalArgumentException("object is null");
		if (src == null) throw new IllegalArgumentException("object is null");
		this.ecx = ecx;
		this.src = src;
	}

	final EsExecutionContext ecx;
	final EmMutableObject<?, ?> src;
}
