/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import java.util.Date;

import com.metservice.argon.DateFormatter;

/**
 * @author roach
 */
public class JsonNumberTime implements IJsonNumberTime, IJsonNativeNumber {

	@Override
	public double doubleValue() {
		return ts;
	}

	public boolean equals(JsonNumberTime rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return ts == rhs.ts;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof JsonNumberTime)) return false;
		return equals((JsonNumberTime) o);
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNumberTime;
	}

	@Override
	public int hashCode() {
		return (int) (ts ^ (ts >>> 32));
	}

	@Override
	public long jsonDatum() {
		return ts;
	}

	@Override
	public long longValue() {
		return ts;
	}

	public Date newDate() {
		return new Date(ts);
	}

	@Override
	public IJsonNative replicate(boolean immutable) {
		return this;
	}

	@Override
	public String toString() {
		return DateFormatter.newT8FromTs(ts);
	}

	public JsonNumberTime(Date date) {
		if (date == null) throw new IllegalArgumentException("object is null");
		this.ts = date.getTime();
	}

	public JsonNumberTime(long ts) {
		this.ts = ts;
	}

	public final long ts;
}
