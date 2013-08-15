/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import org.junit.Test;

import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonBoolean;
import com.metservice.argon.json.JsonNull;
import com.metservice.argon.json.JsonNumberElapsed;
import com.metservice.argon.json.JsonNumberInteger;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonString;

/**
 * @author roach
 */
public class TestUnitEsJsonDecoder extends TestNeon {

	@Test
	public void t10_Level01() {
		final JsonObject u1files0 = JsonObject.newMutable();
		u1files0.put("nm", JsonString.newInstance("f0.txt"));
		u1files0.put("sz", JsonNumberInteger.newInstance(16));
		final JsonObject u1files2 = JsonObject.newMutable();
		u1files2.put("nm", JsonString.newInstance("f2.txt"));
		u1files2.put("sz", JsonNumberInteger.newInstance(32));
		final JsonObject u1files5 = JsonObject.newMutable();
		u1files5.put("nm", JsonString.newInstance("f5.txt"));

		final JsonArray u1files = JsonArray.newMutable();
		u1files.add(u1files0);
		u1files.add(JsonNull.Instance);
		u1files.add(u1files2);
		u1files.add(JsonNull.Instance);
		u1files.add(JsonNull.Instance);
		u1files.add(u1files5);

		final JsonObject u1 = JsonObject.newMutable();
		u1.put("user", JsonString.newInstance("met"));
		u1.put("timeout", JsonNumberElapsed.newInstance(30000L));
		u1.put("passive", JsonBoolean.select(true));
		u1.put("files", u1files);
		final Expectation x = new Expectation("x1a", "30s", "x1b", "f1.txt", "x2a", "30s");
		x.add("x1", new Resource("JsonDecoder.Level01.x1.txt"));
		x.add("y1", new Resource("JsonDecoder.Level01.y1.txt"));
		x.add("z1", "30s");
		x.add("@u1", u1);
		x.add("w1", "f2.txt");
		x.add("y2", "empty");
		final JsonInstaller ins = new JsonInstaller(u1);
		jsassert(x, "JsonDecoder.Level01", ins);
	}

	private static class JsonInstaller extends EmAbstractInstaller {

		@Override
		public void install(EsExecutionContext ecx)
				throws InterruptedException {
			putView(ecx, "uin", m_json);
		}

		public JsonInstaller(JsonObject json) {
			assert json != null;
			m_json = json;
		}
		private final JsonObject m_json;
	}
}
