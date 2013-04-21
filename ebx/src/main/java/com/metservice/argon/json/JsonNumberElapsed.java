/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import com.metservice.argon.Elapsed;
import com.metservice.argon.ElapsedFormatter;

/**
 * @author roach
 */
public class JsonNumberElapsed implements IJsonNumberElapsed, IJsonNativeNumber {

	public static final JsonNumberElapsed Zero = new JsonNumberElapsed(0L);

	@Override
	public double doubleValue() {
		return sms;
	}

	public boolean equals(JsonNumberElapsed rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return sms == rhs.sms;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof JsonNumberElapsed)) return false;
		return equals((JsonNumberElapsed) o);
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNumberElapsed;
	}

	@Override
	public int hashCode() {
		return (int) (sms ^ (sms >>> 32));
	}

	@Override
	public long jsonDatum() {
		return sms;
	}

	@Override
	public long longValue() {
		return sms;
	}

	public Elapsed newElapsed() {
		return new Elapsed(sms);
	}

	@Override
	public IJsonNative replicate(boolean immutable) {
		return this;
	}

	@Override
	public String toString() {
		return ElapsedFormatter.formatSingleUnit(sms);
	}

	public static JsonNumberElapsed newInstance(Elapsed e) {
		if (e == null) throw new IllegalArgumentException("object is null");
		return newInstance(e.sms);
	}

	public static JsonNumberElapsed newInstance(long sms) {
		return sms == 0L ? Zero : new JsonNumberElapsed(sms);
	}

	JsonNumberElapsed(long sms) {
		this.sms = sms;
	}

	public final long sms;
}
