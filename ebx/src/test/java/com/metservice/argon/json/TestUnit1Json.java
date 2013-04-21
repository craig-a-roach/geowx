/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.Binary;
import com.metservice.argon.DateFactory;
import com.metservice.argon.DateFormatter;
import com.metservice.argon.ElapsedFactory;

/**
 * @author roach
 */
public class TestUnit1Json {

	@Test
	public void t20_decodeGood() {

		final JsonEncoder JENC = new JsonEncoder(1024, 0, false, false, false);
		final String spec = " { as : \"a1\" , bs:\"b\\nc\\\"d\\\\e\u00BAf\\u00BAg\", cs:\"\", \"es\" : \"e\", fa: [ 1000Z, 2000Z ] , ga: [1, 1M, 1E0, 1.0, +1.0, -1.0], hb:~AA==~ }";
		final String exps = "{as:\"a1\",bs:\"b\\nc\\\"d\\\\e\u00BAf\u00BAg\",cs:\"\",es:\"e\",fa:[1000Z,2000Z],ga:[1,1M,1.0,1.0,1.0,-1.0],hb:~AA==~}";
		try {
			final IJsonDeValue value = JsonDecoder.Default.decode(spec, NativeDeFactory.Instance);
			final String acts = JENC.encode(value);
			Assert.assertEquals(exps, acts);
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t22_decodeGood() {

		final String spec = " { ax: {x4: [0.0, -1.0] ,  x2:[] , x3:{  } , x1 :{xx1: 0M, xx2:NaN, xx3:-2M} }, bx: {y3: true  , y2:null, y1: false}}";
		final String exps = "{ax:{x1:{xx1:0M,xx2:NaN,xx3:-2M},x2:[],x3:{},x4:[0.0,-1.0]},bx:{y1:false,y2:null,y3:true}}";
		try {
			final IJsonDeValue value = JsonDecoder.Default.decode(spec, NativeDeFactory.Instance);
			final String acts = value.toString();
			Assert.assertEquals(exps, acts);
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t30_decodeBad() {
		final String spec = " { px : \"x\\uBA++y\"}";
		try {
			JsonDecoder.Default.decode(spec, NativeDeFactory.Instance);
			Assert.fail("Did not detect syntax error");
		} catch (final ArgonFormatException ex) {
			System.out.println("GOOD");
			System.out.println(ex.getMessage());
		}
	}

	@Test
	public void t31_decodeBad() {
		final String spec = " { px : \"x\"";
		try {
			JsonDecoder.Default.decode(spec, NativeDeFactory.Instance);
			Assert.fail("Did not detect syntax error");
		} catch (final ArgonFormatException ex) {
			System.out.println("GOOD");
			System.out.println(ex.getMessage());
		}
	}

	@Test
	public void t32_decodeBad() {
		final String spec = " { px : ~AA~}";
		try {
			JsonDecoder.Default.decode(spec, NativeDeFactory.Instance);
			Assert.fail("Did not detect B64 error");
		} catch (final ArgonFormatException ex) {
			System.out.println("GOOD");
			System.out.println(ex.getMessage());
		}
	}

	@Test
	public void t50_encode() {
		final JsonEncoder JENC_0_NoEsc = new JsonEncoder(1024, 0, false, false, false);
		final JsonEncoder JENC_1_Esc = new JsonEncoder(1024, 1, false, true, false);
		final JsonEncoder JENC_0_Esc = new JsonEncoder(1024, 0, false, true, false);
		final JsonEncoder JENC_0_Qp = new JsonEncoder(1024, 0, true, true, false);

		final JsonArray J = JsonArray.newMutable();
		J.addString("pqr");
		J.addString("bs\\q\"n\nt\t");
		J.addString("\u00BA");

		final String Js = "[\"pqr\",\"bs\\\\q\\\"n\\nt\\t\",\"\u00BA\"]";
		Assert.assertEquals(Js, JENC_0_NoEsc.encode(J));

		final String Jsascii = "[\"pqr\",\"bs\\\\q\\\"n\\nt\\t\",\"\\u00BA\"]";
		Assert.assertEquals(Jsascii, JENC_0_Esc.encode(J));

		final JsonArray K = JsonArray.newMutable();
		final JsonArray L = JsonArray.newMutable();
		final JsonArray M = JsonArray.newMutable();
		for (int i = 0; i < 5; i++) {
			K.addTime(i * 123456789012L);
			L.addDouble(i * 1.1E-7);
			M.addElapsed(i * 60000);
		}
		final String Ks = "[0Z,123456789012Z,246913578024Z,370370367036Z,493827156048Z]";
		Assert.assertEquals(Ks, JENC_0_NoEsc.encode(K));
		final String Ls = "[0.0,1.1E-7,2.2E-7,3.3E-7,4.4E-7]";
		Assert.assertEquals(Ls, JENC_0_NoEsc.encode(L));
		final String Ms = "[0M,60000M,120000M,180000M,240000M]";
		Assert.assertEquals(Ms, JENC_0_NoEsc.encode(M));

		final JsonObject A = JsonObject.newMutable();
		A.put("aaa", new Bogus("aaa"));
		A.put("j", J);
		A.put("k", K);
		A.put("xxx", new Bogus("xxx"));
		A.put("l", L);

		final String As = "{j:" + Js + ",k:" + Ks + ",l:" + Ls + "}";
		Assert.assertEquals(As, JENC_0_NoEsc.encode(A));

		final JsonObject B = JsonObject.newMutable();
		B.putBoolean("e1", true);
		B.putBoolean("e2", false);
		B.putNull("e3");
		B.putString("f", "uw");

		final String Bs = "{e1:true,e2:false,e3:null,f:\"uw\"}";
		Assert.assertEquals(Bs, JENC_0_NoEsc.encode(B));

		final String Bsq = "{\"e1\":true,\"e2\":false,\"e3\":null,\"f\":\"uw\"}";
		Assert.assertEquals(Bsq, JENC_0_Qp.encode(B));

		final JsonObject C = JsonObject.newMutableInitStrings("a", "alpha", "b", null, "c", 13, "d", "");
		final String Cs = "{a:\"alpha\",c:\"13\",d:\"\"}";
		Assert.assertEquals(Cs, JENC_0_NoEsc.encode(C));

		final JsonArray N = JsonArray.newMutable();
		for (int i = 0; i < 5; i++) {
			if (i == 0 || i == 2) {
				N.add(new Bogus("i=" + i));
			} else {
				N.add(B);
			}
		}
		final String Ns = "[" + Bs + "," + Bs + "," + Bs + "]";
		Assert.assertEquals(Ns, JENC_0_NoEsc.encode(N));

		final JsonObject X = JsonObject.newMutable();
		X.put("a", A);
		X.put("b", B);
		X.putInteger("c", 13);
		X.put("d", N);

		final String Xs = "{a:" + As + ",b:" + Bs + ",c:13,d:" + Ns + "}";
		Assert.assertEquals(Xs, JENC_0_NoEsc.encode(X));
		System.out.println(JENC_1_Esc.encode(X));
	}

	@Test
	public void t60_encodeNative()
			throws ArgonFormatException {

		final JsonObject y = JsonObject.newMutable();
		y.putElapsed("timeout", ElapsedFactory.newElapsed("15s"));
		y.putBoolean("enabled", true);
		y.putNull("pwd");
		y.putBinary("sha", Binary.newFromTransient(new byte[] { 0x13, 0x6 }));

		final JsonArray a = JsonArray.newMutable();
		a.addString("red");
		a.addString("green");
		a.addString("blue");

		final JsonObject x = JsonObject.newMutable();
		x.putTime("now", DateFactory.newDateConstantFromT8("20100722T0314Z15M000"));
		x.put("cfg", y);
		x.put("colours", a);

		final String expj = "{cfg:{enabled:true,pwd:null,sha:~EwY=~,timeout:15000M},colours:[\"red\",\"green\",\"blue\"],now:1279768455000Z}";
		final String expp = "cfg_enabled=true\ncfg_pwd=\ncfg_sha=EwY=\ncfg_timeout=15s\ncolours_0=red\ncolours_1=green\ncolours_2=blue\nnow=20100722T0314Z15M000\n";
		Assert.assertEquals(expj, x.toString());
		Assert.assertEquals(expp, x.toPropertiesString());
	}

	@Test
	public void t70_decodeNative() {

		final String spec = "{credentials: {user: \"admin\", password:~EwY=~}, timeout:15000M, site:\"ncep.org\", files:[\"f1.txt\",\"f2.txt\"], early:20100722T0314Z15M000}";
		final JsonDecoder de = new JsonDecoder();
		try {
			final IJsonNative decode = de.decode(spec);
			Assert.assertEquals(JsonObject.class, decode.getClass());
			final JsonObject x = (JsonObject) decode;
			final IJsonNative siteValue = x.get("site");
			Assert.assertEquals(JsonString.class, siteValue.getClass());
			Assert.assertEquals("ncep.org", ((JsonString) siteValue).zValue);
			final JsonAccessor atimeout = x.accessor("timeout");
			Assert.assertEquals(15000L, atimeout.datumElapsed().sms);
			final JsonAccessor aearly = x.accessor("early");
			Assert.assertEquals("20100722T0314Z15M000", DateFormatter.newT8FromDate(aearly.datumDate()));
			final JsonAccessor acredentials = x.accessor("credentials");
			final JsonObject xcredentials = acredentials.datumObject();
			final JsonAccessor apassword = xcredentials.accessor("password");
			Assert.assertArrayEquals(new byte[] { 0x13, 0x06 }, apassword.datumBinary().zptReadOnly);
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		} catch (final JsonSchemaException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	private static class Bogus implements IJsonNative {

		@Override
		public JsonType getJsonType() {
			return null;
		}

		@Override
		public IJsonNative replicate(boolean immutable) {
			return new Bogus(x);
		}

		@Override
		public String toString() {
			return x;
		}

		public Bogus(String x) {
			this.x = x;
		}
		private final String x;
	}
}
