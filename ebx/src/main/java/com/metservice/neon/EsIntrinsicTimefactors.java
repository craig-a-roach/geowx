/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.TimeFactors;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonNumberTime;
import com.metservice.argon.json.JsonType;

/**
 * This class wraps the AtFactors time date class from the ICE Core.
 * 
 * @jsobject Timefactors
 * @jsnote All of the properties are READ ONLY
 * @jsproperty year Integer number. The year.
 * @jsproperty moy Integer number. Month of the year, where Jan = 1
 * @jsproperty dom Integer number. Day of the month.
 * @jsproperty doy Integer number. Day of the year,where 1st Jan = 1
 * @jsproperty hour24 Integer number. Hour of the day.
 * @jsproperty minute Integer number. Minute of the hour.
 * @jsproperty dowTLA String value. The day of the week as a three letter abbreviation.
 * @jsproperty timeZoneOffset Elapsed number. The offset of the local time from UTC taking into account daylight saving.
 * @jsproperty inDaylightTime Boolean. true if daylight saving is in effect locally.
 * @author roach
 */
public final class EsIntrinsicTimefactors extends EsObject {

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
		if (m_propertiesLoaded) return;

		if (m_oValue != null) {
			final int moyJan1 = m_oValue.moyJan1();
			final String dowCode = m_oValue.dowCode();
			final int smsGMTAdjustedOffset = m_oValue.smsGMTAdjustedOffset();
			final boolean inDaylightTime = m_oValue.isDaylightSavingInEffect();
			add("year", EsProperty.newReadOnlyDontDelete(new EsPrimitiveNumberInteger(m_oValue.year)));
			add("moy", EsProperty.newReadOnlyDontDelete(new EsPrimitiveNumberInteger(moyJan1)));
			add("dom", EsProperty.newReadOnlyDontDelete(new EsPrimitiveNumberInteger(m_oValue.dom1)));
			add("doy", EsProperty.newReadOnlyDontDelete(new EsPrimitiveNumberInteger(m_oValue.doy1)));
			add("hour24", EsProperty.newReadOnlyDontDelete(new EsPrimitiveNumberInteger(m_oValue.hour24)));
			add("minute", EsProperty.newReadOnlyDontDelete(new EsPrimitiveNumberInteger(m_oValue.minute)));
			add("dowCode", EsProperty.newReadOnlyDontDelete(new EsPrimitiveString(dowCode)));
			add("timeZoneOffset", EsProperty.newReadOnlyDontDelete(new EsPrimitiveNumberElapsed(smsGMTAdjustedOffset)));
			add("inDaylightTime", EsProperty.newReadOnlyDontDelete(EsPrimitiveBoolean.instance(inDaylightTime)));
		}

		m_propertiesLoaded = true;
	}

	@Override
	public IJsonNative createJsonNative() {
		if (m_oValue == null) return null;
		return new JsonNumberTime(m_oValue.ts);
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicTimefactors(this);
	}

	@Override
	public String esClass() {
		return EsIntrinsicTimefactorsConstructor.ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNumberTime;
	}

	public void setValue(TimeFactors value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		m_oValue = value;
		m_oTime = new EsPrimitiveNumberTime(value.ts);
		m_propertiesLoaded = false;
	}

	@Override
	public String show(int depth) {
		return m_oValue == null ? "" : m_oValue.toString();
	}

	public TimeFactors timeFactors() {
		if (m_oValue == null) throw new IllegalStateException("setValue");
		return m_oValue;
	}

	public EsPrimitiveNumberTime timeNumberPrimitive() {
		if (m_oTime == null) throw new IllegalStateException("setValue");
		return m_oTime;
	}

	public EsPrimitiveString toPrimitiveString() {
		return new EsPrimitiveString(m_oValue == null ? "" : m_oValue.toString());
	}

	public EsIntrinsicTimefactors(EsObject prototype) {
		super(prototype);
	}
	private TimeFactors m_oValue;
	private EsPrimitiveNumberTime m_oTime;
	private boolean m_propertiesLoaded;
}
