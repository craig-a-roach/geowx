/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.IOException;
import java.nio.charset.Charset;

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.http.HttpStatus;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.Ds;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonDecoder;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class BerylliumJsonHttpExchange extends BerylliumHttpExchange {

	private static void makePostJson(BerylliumJsonHttpExchange neo, Address addr, BerylliumPath uri) {
		if (neo == null) throw new IllegalArgumentException("object is null");
		if (addr == null) throw new IllegalArgumentException("object is null");
		if (uri == null) throw new IllegalArgumentException("object is null");
		makePost(neo, addr, CBeryllium.JsonContentType, null);
		makeUri(neo, uri);
	}

	private static void makePostJson(BerylliumJsonHttpExchange neo, IBerylliumHttpAddressable ha, BerylliumPath uri) {
		if (ha == null) throw new IllegalArgumentException("object is null");
		makePostJson(neo, ha.httpAddress(), uri);
	}

	private static Binary newJsonBinary(IBerylliumJsonHttpContent content) {
		if (content == null) throw new IllegalArgumentException("object is null");
		final JsonObject jsonContent = JsonObject.newMutable();
		content.saveTo(jsonContent);
		return newJsonBinary(jsonContent, false);
	}

	private static Binary newJsonBinary(JsonObject content, boolean strict) {
		if (content == null) throw new IllegalArgumentException("object is null");
		final String qSpec = CBeryllium.jsonEncoder(strict).encode(content);
		return Binary.newFromStringASCII(qSpec);
	}

	public static BerylliumJsonHttpExchange newInstance(Address addr, BerylliumPath uri, IBerylliumJsonHttpContent content) {
		final BerylliumJsonHttpExchange neo = new BerylliumJsonHttpExchange(newJsonBinary(content));
		makePostJson(neo, addr, uri);
		return neo;
	}

	public static BerylliumJsonHttpExchange newInstance(Address addr, BerylliumPath uri, JsonObject content, boolean strict) {
		final BerylliumJsonHttpExchange neo = new BerylliumJsonHttpExchange(newJsonBinary(content, strict));
		makePostJson(neo, addr, uri);
		return neo;
	}

	public static BerylliumJsonHttpExchange newInstance(IBerylliumHttpAddressable ha, BerylliumPath uri,
			IBerylliumJsonHttpContent content) {
		final BerylliumJsonHttpExchange neo = new BerylliumJsonHttpExchange(newJsonBinary(content));
		makePostJson(neo, ha, uri);
		return neo;
	}

	public static BerylliumJsonHttpExchange newInstance(IBerylliumHttpAddressable ha, BerylliumPath uri, JsonObject content) {
		final BerylliumJsonHttpExchange neo = new BerylliumJsonHttpExchange(newJsonBinary(content, false));
		makePostJson(neo, ha, uri);
		return neo;
	}

	private void onCompleteStatusOK(String oqlcContentType, Charset oCharset, Binary content) {
		if (oqlcContentType == null) {
			final Ds ds = Ds.invalidBecause(RsnMissingContentType, CsqOnMalformedResponse);
			failNet(adest(ds));
			raiseCompleteMalformedResponse();
		} else {
			if (oqlcContentType.equals(CBeryllium.JsonAcceptContentType)) {
				final Charset charset = oCharset == null ? ArgonText.UTF8 : oCharset;
				final String zSpec = content.newString(charset);
				onCompleteStatusOKJsonSpec(zSpec);
			} else {
				final Ds ds = Ds.invalidBecause("Json-incompatible content type header in http response",
						CsqOnMalformedResponse);
				ds.a("contentType", oqlcContentType);
				failNet(adest(ds));
				raiseCompleteMalformedResponse();
			}
		}
	}

	private void onCompleteStatusOKJsonSpec(String zSpec) {
		assert zSpec != null;
		try {
			final IJsonNative decode = JsonDecoder.Default.decode(zSpec);
			if (decode instanceof JsonObject) {
				final JsonObject jsonObject = (JsonObject) decode;
				if (isLiveNet()) {
					liveNet("CLIENT RECEIVED (JSON) OK");
				}
				raiseCompleteResponse(jsonObject);
			} else {
				final Ds ds = Ds.invalidBecause("Expecting Json Object", CsqOnMalformedResponse);
				ds.a("jsonType", decode.getJsonType());
				failNet(adest(ds));
				raiseCompleteMalformedResponse();
			}
		} catch (final ArgonFormatException ex) {
			final Ds ds = Ds.triedTo("Decode Json response", ex, CsqOnMalformedResponse);
			failNet(adest(ds));
			raiseCompleteMalformedResponse();
		}
	}

	private void raiseCompleteMalformedResponse() {
		final IBerylliumHttpTracker oTracker = getTracker();
		if (oTracker == null) return;
		if (!(oTracker instanceof IBerylliumJsonHttpTracker)) {
			final Ds ds = Ds.invalidBecause("Expected Json Tracker", CsqNoResponseNotify);
			ds.a("trackerClass", oTracker.getClass().getName());
			warnNet(adest(ds));
			return;
		}
		final IBerylliumJsonHttpTracker tracker = (IBerylliumJsonHttpTracker) oTracker;
		try {
			tracker.raiseCompleteMalformedResponse();
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo(TryRaiseCompleteMalformedNotify, ex, CsqPartResponseNotify);
			failNet(adest(ds));
		}
	}

	private void raiseCompleteResponse(JsonObject jsonObject) {
		assert jsonObject != null;
		final IBerylliumHttpTracker oTracker = getTracker();
		if (oTracker == null) return;
		if (!(oTracker instanceof IBerylliumJsonHttpTracker)) return;

		final IBerylliumJsonHttpTracker tracker = (IBerylliumJsonHttpTracker) oTracker;
		try {
			tracker.raiseCompleteResponse(jsonObject);
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		} catch (final JsonSchemaException ex) {
			final Ds ds = Ds.triedTo("Access required Json properties", ex, CsqOnMalformedResponse);
			failNet(adest(ds));
			raiseCompleteMalformedResponse();
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo(TryRaiseCompleteNotify, ex, CsqPartResponseNotify);
			ds.a("jsonObject", jsonObject);
			failNet(adest(ds));
		}
	}

	@Override
	protected final void onResponseComplete()
			throws IOException {
		super.onResponseComplete();
		if (haveHttpStatus()) {
			final int httpStatus = httpStatus();
			if (httpStatus == HttpStatus.OK_200) {
				final String oqlcContentType = oqlcContentType();
				final Charset oCharset = findContentTypeCharset();
				final Binary content = popBinary();
				onCompleteStatusOK(oqlcContentType, oCharset, content);
			} else {
				final String oqHttpStatusReason = oqHttpStatusReason();
				onCompleteStatusNotOK(httpStatus, oqHttpStatusReason);
			}
		} else {
			final Ds ds = Ds.invalidBecause(RsnMissingStatusCode, CsqOnUnresponsive);
			failNet(adest(ds));
			raiseUnresponsive();
		}
	}

	private BerylliumJsonHttpExchange(Binary oContentTx) {
		super(oContentTx);
	}
}
