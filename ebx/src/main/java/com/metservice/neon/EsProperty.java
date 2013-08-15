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
public class EsProperty {
	private void enableAttribute(boolean enabled, int attribute) {
		if (enabled) {
			attributeMask |= attribute;
		} else {
			attributeMask &= ~attribute;
		}
	}

	private boolean isEnabled(int attribute) {
		return (attributeMask & attribute) == attribute;
	}

	public void enableDontDelete(boolean enabled) {
		enableAttribute(enabled, ATT_DONTDELETE);
	}

	public void enableDontEnum(boolean enabled) {
		enableAttribute(enabled, ATT_DONTENUM);
	}

	public void enableReadOnly(boolean enabled) {
		enableAttribute(enabled, ATT_READONLY);
	}

	public boolean isDontDelete() {
		return isEnabled(ATT_DONTDELETE);
	}

	public boolean isDontEnum() {
		return isEnabled(ATT_DONTENUM);
	}

	public boolean isReadOnly() {
		return isEnabled(ATT_READONLY);
	}

	@Override
	public String toString() {
		return value.show(1);
	}

	public static EsProperty newDefined(IEsOperand value) {
		return new EsProperty(value, ATTMASK_DEFAULT);
	}

	public static EsProperty newDefined(IEsOperand value, int attributeMask) {
		return new EsProperty(value, attributeMask);
	}

	public static EsProperty newDontDelete(IEsOperand value) {
		return new EsProperty(value, ATT_DONTDELETE);
	}

	public static EsProperty newDontDeleteDontEnum(IEsOperand value) {
		return new EsProperty(value, ATT_DONTDELETE | ATT_DONTENUM);
	}

	public static EsProperty newReadOnlyDontDelete(IEsOperand value) {
		return new EsProperty(value, ATT_READONLY | ATT_DONTDELETE);
	}

	public static EsProperty newReadOnlyDontDeleteDontEnum(IEsOperand value) {
		return new EsProperty(value, ATT_READONLY | ATT_DONTDELETE | ATT_DONTENUM);
	}

	public static EsProperty newUndefined() {
		return newUndefined(ATTMASK_DEFAULT);
	}

	public static EsProperty newUndefined(int attributeMask) {
		return new EsProperty(EsPrimitiveUndefined.Instance, attributeMask);
	}

	private EsProperty(IEsOperand value, int attributeMask) {
		assert value != null;
		this.value = value;
		this.attributeMask = attributeMask;
	}

	public static final int ATTMASK_DEFAULT = 0x0;
	public static final int ATT_READONLY = 0x1;
	public static final int ATT_DONTENUM = 0x2;
	public static final int ATT_DONTDELETE = 0x4;

	public IEsOperand value;

	public int attributeMask;
}
