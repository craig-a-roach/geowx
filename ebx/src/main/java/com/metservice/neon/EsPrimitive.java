/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * 
 * @author roach
 */
public abstract class EsPrimitive implements IEsOperand {
	public final boolean isDefined() {
		return !(this instanceof EsPrimitiveUndefined);
	}

	public final boolean isDefinedNonNull() {
		return !(this instanceof EsPrimitiveUndefined) && !(this instanceof EsPrimitiveNull);
	}

	public abstract String toCanonicalString();

	public String toCanonicalString(EsExecutionContext ecx) {
		return toCanonicalString();
	}

	public abstract int toHash();

	public EsPrimitive toPrimitive(EsExecutionContext ecx, EsType oPreference) {
		return this;
	}

	public final EsPrimitiveBoolean toPrimitiveBoolean() {
		return EsPrimitiveBoolean.instance(toCanonicalBoolean());
	}

	public final EsPrimitiveString toPrimitiveString(EsExecutionContext ecx) {
		return new EsPrimitiveString(toCanonicalString(ecx));
	}

	@Override
	public String toString() {
		return show(1);
	}
}
