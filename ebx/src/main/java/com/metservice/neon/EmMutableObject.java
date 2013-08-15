/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.TimeZone;

import com.metservice.argon.Binary;
import com.metservice.argon.Ds;
import com.metservice.argon.Elapsed;
import com.metservice.argon.EnumDecoder;
import com.metservice.argon.TimeOfDayRule;
import com.metservice.argon.json.JsonObject;

/**
 * @author roach
 */
public abstract class EmMutableObject<C, T> extends EmObject {

	protected final C construct() {
		if (isPrototype || m_oConstruct == null) throw new EsProtectionCodeException("Operation not allowed on prototype");
		return m_oConstruct;
	}

	protected abstract T newTuple(EsExecutionContext ecx, C construct)
			throws InterruptedException;

	protected abstract void putDefaultProperties(EsExecutionContext ecx);

	protected abstract void putInstanceProperties(EsExecutionContext ecx, C construct);

	protected final void setConstruct(C construct) {
		if (isPrototype) throw new EsProtectionCodeException("Operation not allowed on prototype");
		m_oConstruct = construct;
	}

	public String exType(String qccPropertyName, Throwable oex) {
		if (qccPropertyName == null || qccPropertyName.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		final String ozCause = oex == null ? null : oex.getMessage();
		final String zCause = ozCause == null || ozCause.length() == 0 ? "" : "..." + ozCause;
		return "Value of property '" + qccPropertyName + "' is malformed" + zCause;
	}

	public final T newTuple(EsExecutionContext ecx)
			throws InterruptedException {
		loadProperties(ecx);
		return newTuple(ecx, construct());
	}

	public Binary property_binary(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_binary(ecx, this, qccPropertyName);
	}

	public boolean property_boolean(String qccPropertyName) {
		return UNeon.property_boolean(this, qccPropertyName);
	}

	public Charset property_charset(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_charset(ecx, this, qccPropertyName);
	}

	public Charset property_charset(EsExecutionContext ecx, String qccPropertyName, Charset whenEmpty)
			throws InterruptedException {
		return UNeon.property_charset(ecx, this, qccPropertyName, whenEmpty);
	}

	public Elapsed property_elapsed(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_elapsed(ecx, this, qccPropertyName);
	}

	public <E extends Enum<?>> E property_enum(EsExecutionContext ecx, String qccPropertyName, EnumDecoder<E> decoder)
			throws InterruptedException {
		return UNeon.property_enum(ecx, this, qccPropertyName, decoder);
	}

	public int property_int(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_int(ecx, this, qccPropertyName);
	}

	public JsonObject property_jsonObject(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_jsonObject(ecx, this, qccPropertyName);
	}

	public Elapsed property_oElapsed(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_oElapsed(ecx, this, qccPropertyName);
	}

	public <E extends Enum<?>> E property_oEnum(EsExecutionContext ecx, String qccPropertyName, EnumDecoder<E> decoder)
			throws InterruptedException {
		return UNeon.property_oEnum(ecx, this, qccPropertyName, decoder);
	}

	public String property_ozString(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_ozString(ecx, this, qccPropertyName);
	}

	public String property_qtwString(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_qtwString(ecx, this, qccPropertyName);
	}

	public int property_secondOfDay(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_secondOfDay(ecx, this, qccPropertyName);
	}

	public Date property_time(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return new Date(UNeon.property_tsTimeDatum(ecx, this, qccPropertyName));
	}

	public TimeOfDayRule property_timeOfDayRule(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_timeOfDayRuleDatum(ecx, this, qccPropertyName);
	}

	public TimeZone property_timeZone(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_timeZoneDatum(ecx, this, qccPropertyName);
	}

	public TimeZone property_timeZone(EsExecutionContext ecx, String qccPropertyName, TimeZone whenEmpty)
			throws InterruptedException {
		return UNeon.property_timeZone(ecx, this, qccPropertyName, whenEmpty);
	}

	public long property_tsTime(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_tsTimeDatum(ecx, this, qccPropertyName);
	}

	public String property_ztwString(EsExecutionContext ecx, String qccPropertyName)
			throws InterruptedException {
		return UNeon.property_ztwString(ecx, this, qccPropertyName);
	}

	@Override
	public void putProperties(EsExecutionContext ecx)
			throws InterruptedException {
		if (isPrototype) {
			putDefaultProperties(ecx);
		} else {
			if (m_oConstruct != null) {
				putInstanceProperties(ecx, m_oConstruct);
			}
		}
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("construct", m_oConstruct);
		ds.a("super", super.toString());
		return ds.s();
	}

	public EmMutableObject(EmClass emClass) {
		super(emClass);
		this.isPrototype = true;
	}

	public EmMutableObject(EmObject prototype) {
		super(prototype);
		this.isPrototype = false;
	}
	protected final boolean isPrototype;
	private C m_oConstruct;
}
