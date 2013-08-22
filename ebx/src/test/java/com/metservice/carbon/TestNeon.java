/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.management.InstanceAlreadyExistsException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonJoiner;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.beryllium.BerylliumSupportId;
import com.metservice.neon.EsCallableEntryPoint;
import com.metservice.neon.EsCompletionThrow;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsGlobal;
import com.metservice.neon.EsPrimitiveNull;
import com.metservice.neon.EsPrimitiveUndefined;
import com.metservice.neon.EsRequest;
import com.metservice.neon.EsResponse;
import com.metservice.neon.EsType;
import com.metservice.neon.IEmInstaller;
import com.metservice.neon.IEsOperand;
import com.metservice.neon.NeonException;
import com.metservice.neon.NeonImpException;
import com.metservice.neon.NeonScriptException;
import com.metservice.neon.NeonSourceProviderDefaultClasspath;
import com.metservice.neon.NeonSpace;
import com.metservice.neon.NeonSpaceCfg;
import com.metservice.neon.NeonSpaceId;

/**
 * @author roach
 */
public abstract class TestNeon {

	private static NeonSpace c_space;

	private static final int ResourceQuotaBc = 8 * CArgon.M;

	private static void debugNotice(NeonSpaceId spaceId, NeonSpaceCfg spaceCfg) {
		final Pattern oAutoDebugPattern = spaceCfg.getAutoDebugPattern();
		if (oAutoDebugPattern == null) return;
		System.out.println("Debug " + oAutoDebugPattern + " on port " + spaceId.listenPort()
				+ " (make sure scripts are in NeonIndex.txt)");
	}

	static String zEnsureCR(String zIn) {
		final int len = zIn.length();
		final StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			final char ch = zIn.charAt(i);
			if (ch != '\r') {
				if (ch == '\n') {
					sb.append("\r\n");
				} else {
					sb.append(ch);
				}
			}
		}
		return sb.toString();
	}

	static String zStripCR(String zIn) {
		final int len = zIn.length();
		final StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			final char ch = zIn.charAt(i);
			if (ch != '\r') {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	@AfterClass
	public static void destroySpace() {
		if (c_space != null) {
			c_space.shutdown();
			c_space = null;
		}
	}

	protected void jsassert(Expectation expected, String jsfile, EsCallableEntryPoint oep, IEmInstaller... ozptInstallers) {
		final Response response = new Response(expected);
		try {
			run(response, jsfile, oep, ozptInstallers);
			validate(jsfile, response);
		} catch (final NeonScriptException ex) {
			if (expected.expectFail) {
				System.out.println("Good Exception: " + ex.getMessage());
			} else {
				Assert.fail(ex.getMessage());
			}
		} catch (final NeonImpException ex) {
			Assert.fail(ex.getMessage());
		} catch (final InterruptedException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	protected void jsassert(Expectation expected, String jsfile, IEmInstaller... ozptInstallers) {
		jsassert(expected, jsfile, null, ozptInstallers);
	}

	protected void run(EsResponse response, String jsfile, EsCallableEntryPoint oep, IEmInstaller... ozptInstallers)
			throws NeonScriptException, NeonImpException, InterruptedException {
		if (c_space == null) return;
		final EsRequest request = new EsRequest(BerylliumSupportId.Loopback, jsfile + ".js");
		request.setEntryPoint(oep);
		if (ozptInstallers != null) {
			request.add(ozptInstallers);
		}
		c_space.run(request, response);
	}

	protected void validate(String jsfile, Response response) {
		if (response.mismatches.isEmpty()) return;
		Assert.fail(ArgonJoiner.zComma(response.mismatches));
	}

	@Before
	public void setupSpace() {
		if (c_space != null) return;
		try {
			final NeonSpaceId spaceId = NeonSpaceId.newInstance("7474");
			final NeonSpaceCfg spaceCfg = new NeonSpaceCfg();
			spaceCfg.setFilterPatternLog("fail=.*,warn=.*,info=.*,live=Script:Emit:.*");
			spaceCfg.setShellSessionMaxIdleSec(300000);
			spaceCfg.setShellConsoleQuota(12);
			spaceCfg.setAutoDebugPatternFromSystemProperty("jsdebug");
			final NeonSourceProviderDefaultClasspath provider = new NeonSourceProviderDefaultClasspath(getClass());
			final NeonSpace space = new NeonSpace(spaceId, provider, spaceCfg);
			space.start();
			debugNotice(spaceId, spaceCfg);
			c_space = space;
		} catch (final NeonException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		} catch (final InstanceAlreadyExistsException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	private static class Response extends EsResponse {

		private void check(EsExecutionContext ecx, String pname, IEsOperand actual)
				throws InterruptedException {
			final ExpectedValue oExpected = expectation.map.get(pname);
			if (oExpected == null) return;
			final String oqMismatch = oExpected.mismatch(ecx, actual);
			if (oqMismatch != null) {
				mismatches.add(pname + " " + oqMismatch);
			}
		}

		@Override
		protected void saveGlobals(EsRequest request, EsGlobal global, EsExecutionContext ecx)
				throws InterruptedException {
			for (final String pname : expectation.map.keySet()) {
				if (pname.equals("return") || pname.equals("throw")) {
					continue;
				}
				check(ecx, pname, global.esGet(pname));
			}
		}

		@Override
		protected void saveReturn(EsRequest request, EsExecutionContext ecx, IEsOperand callResult)
				throws InterruptedException {
			check(ecx, "return", callResult);
		}

		@Override
		protected void saveThrow(EsRequest request, EsExecutionContext ecx, EsCompletionThrow completion)
				throws InterruptedException {
			check(ecx, "throw", completion);
		}

		public Response(Expectation e) {
			this.expectation = e;
		}
		public final Expectation expectation;
		public final List<String> mismatches = new ArrayList<String>();
	}

	protected static class Expectation {

		private void addPairs(Object[] pairs) {
			final int pcNeo = pairs.length / 2;
			int i = 0;
			for (int p = 0; p < pcNeo; p++) {
				final String pa = pairs[i].toString();
				if (pa.length() < 2) throw new IllegalArgumentException("invalid pname>" + pa + "<");
				i++;
				final Object value = pairs[i];
				if (value == null) throw new IllegalArgumentException("null value expected for " + pa);
				i++;
				final char ch = pa.charAt(0);
				final String pname;
				final char target;
				if (Character.isLetter(ch)) {
					pname = pa;
					target = '$';
				} else {
					pname = pa.substring(1);
					target = ch;
				}
				map.put(pname, new ExpectedValue(value, target));
			}
		}

		public Expectation add(Object... pairs) {
			addPairs(pairs);
			return this;
		}

		public void expectFail(boolean expect) {
			expectFail = expect;
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Expectation");
			ds.a("map", map);
			return ds.s();
		}

		public Expectation(Object... pairs) {
			addPairs(pairs);
		}

		public final Map<String, ExpectedValue> map = new HashMap<String, ExpectedValue>();
		public boolean expectFail = false;
	}

	protected static class ExpectedValue {

		public String mismatch(EsExecutionContext ecx, IEsOperand actual)
				throws InterruptedException {
			final EsType esActual = actual.esType();
			if (esActual == EsType.TUndefined && (value instanceof EsPrimitiveUndefined)) return null;
			if (esActual == EsType.TNull && (value instanceof EsPrimitiveNull)) return null;

			Object oCanonActual = actual;
			Object canonExpected = value;
			switch (target) {
				case '$':
					oCanonActual = actual.toCanonicalString(ecx);
					canonExpected = value.toString();
				break;
				case '#':
					oCanonActual = new Integer(actual.toNumber(ecx).intVerified());
				break;
				case '@':
					oCanonActual = actual.createJsonNative();
				break;
			}
			final String expectedClass = canonExpected.getClass().getName();
			if (oCanonActual == null) return "Expected='" + canonExpected + "' (" + expectedClass + ") Actual is null";
			if (canonExpected.equals(oCanonActual)) return null;
			return "Expected='" + canonExpected + "' (" + expectedClass + ") Actual='" + oCanonActual + "'";
		}

		@Override
		public String toString() {
			return value.toString() + " (" + target + ")";
		}

		ExpectedValue(Object value, char target) {
			this.value = value;
			this.target = target;
		}
		final Object value;
		final char target;
	}

	protected class Resource {

		@Override
		public String toString() {
			return zValue;
		}

		public String toStringCRLF() {
			return zEnsureCR(zValue);
		}

		public Resource(String fileName) {
			if (fileName == null || fileName.length() == 0) throw new IllegalArgumentException("string is null or empty");
			this.fileName = fileName;
			final InputStream oIn = TestNeon.this.getClass().getResourceAsStream(fileName);
			if (oIn == null) throw new IllegalArgumentException("resource not found>" + fileName + "<");
			try {
				final Binary resource = Binary.newFromInputStream(oIn, 0L, fileName, ResourceQuotaBc);
				final String zResource = resource.newStringUTF8();
				zValue = zStripCR(zResource);
			} catch (final ArgonQuotaException ex) {
				throw new IllegalArgumentException("Invalid resource" + fileName, ex);
			} catch (final ArgonStreamReadException ex) {
				throw new IllegalArgumentException("Cannot read resource" + fileName, ex);
			}
		}
		public final String fileName;
		public final String zValue;
	}

}
