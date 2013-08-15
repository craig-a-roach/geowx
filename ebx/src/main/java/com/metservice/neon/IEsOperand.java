/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.json.IJsonDeValue;
import com.metservice.argon.json.IJsonNative;

/**
 * 
 * @author roach
 */
public interface IEsOperand extends IJsonDeValue {

	public IJsonNative createJsonNative();

	/**
	 * 
	 * @return as per ECMA 8
	 */
	public EsType esType();

	public String show(int depth);

	/**
	 * 
	 * @return as per ECMA 9.2
	 */
	public boolean toCanonicalBoolean();

	/**
	 * 
	 * @return as per ECMA 9.8
	 */
	public String toCanonicalString(EsExecutionContext ecx)
			throws InterruptedException;

	/**
	 * 
	 * @return as per ECMA 9.3
	 */
	public EsPrimitiveNumber toNumber(EsExecutionContext ecx)
			throws InterruptedException;

	/**
	 * 
	 * @return as per ECMA 9.9
	 */
	public EsObject toObject(EsExecutionContext ecx)
			throws InterruptedException;

	/**
	 * 
	 * @return as per ECMA 9.1
	 */
	public EsPrimitive toPrimitive(EsExecutionContext ecx, EsType oPreference)
			throws InterruptedException;
}
