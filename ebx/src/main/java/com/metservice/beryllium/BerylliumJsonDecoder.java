/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.IOException;
import java.nio.charset.Charset;

import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonDecoder;
import com.metservice.argon.json.JsonObject;

/**
 * @author roach
 */
public class BerylliumJsonDecoder {

	public static IJsonNative newJsonDecode(Request rq, int bcQuota)
			throws BerylliumHttpBadRequestException, ArgonQuotaException, ArgonFormatException, ArgonStreamReadException,
			IOException {
		if (rq == null) throw new IllegalArgumentException("object is null");
		final String ozMethod = rq.getMethod();
		if (ozMethod == null) throw new BerylliumHttpBadRequestException("HTTP Method not specified");
		if (ozMethod.equals(HttpMethods.POST)) return newJsonDecodePOST(rq, bcQuota);
		if (ozMethod.equals(HttpMethods.GET)) return newJsonDecodeGET(rq);
		throw new BerylliumHttpBadRequestException("Unsupported HTTP Method '" + ozMethod + "'");
	}

	public static IJsonNative newJsonDecodeGET(Request rq)
			throws BerylliumHttpBadRequestException {
		return UBeryllium.transformParameterMapToJson(rq);
	}

	public static IJsonNative newJsonDecodePOST(Request rq, int bcQuota)
			throws BerylliumHttpBadRequestException, ArgonQuotaException, ArgonFormatException, ArgonStreamReadException,
			IOException {
		final BerylliumContentType contentType = BerylliumContentType.newInstance(rq);
		if (contentType.www_form_urlencoded()) return newJsonDecodePOSTForm(rq);
		if (contentType.text_plain()) return newJsonDecodePOSTPlain(rq, contentType, bcQuota);
		throw new BerylliumHttpBadRequestException("Unsupported POST ContentType '" + contentType + "'");
	}

	public static IJsonNative newJsonDecodePOSTForm(Request rq)
			throws BerylliumHttpBadRequestException {
		return UBeryllium.transformParameterMapToJson(rq);
	}

	public static IJsonNative newJsonDecodePOSTPlain(Request rq, BerylliumContentType contentType, int bcQuota)
			throws ArgonQuotaException, ArgonFormatException, ArgonStreamReadException, IOException {
		if (rq == null) throw new IllegalArgumentException("object is null");
		if (contentType == null) throw new IllegalArgumentException("object is null");
		final Binary breq = UBeryllium.readBinaryInput(rq, bcQuota);
		final Charset charset = contentType.charset(ArgonText.UTF8);
		final String zSpec = breq.newString(charset);
		return JsonDecoder.Default.decode(zSpec);
	}

	public static JsonObject newJsonObjectDecode(Request rq, int bcQuota)
			throws BerylliumHttpBadRequestException, ArgonQuotaException, ArgonFormatException, ArgonStreamReadException,
			IOException {
		final IJsonNative decode = newJsonDecode(rq, bcQuota);
		if (!(decode instanceof JsonObject)) {
			final String m = "Expecting JSON request to be an object, but is '" + decode.getJsonType() + "'";
			throw new BerylliumHttpBadRequestException(m);
		}
		return (JsonObject) decode;
	}

	private BerylliumJsonDecoder() {
	}
}
