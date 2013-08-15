/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Ds;
import com.metservice.beryllium.BerylliumHttpBadRequestException;
import com.metservice.beryllium.BerylliumHttpNotFoundException;
import com.metservice.neon.NeonImpException;
import com.metservice.neon.NeonScriptException;
import com.metservice.neon.NeonScriptRunException;

/**
 * @author roach
 */
public class HttpError {

	public static final String StatusCodePrefix = "HTTP";
	public static final int StatusCodeLo = 100;
	public static final int StatusCodeHi = 599;

	public String format() {
		final StringBuilder sb = new StringBuilder();
		sb.append(StatusCodePrefix);
		sb.append(statusCode);
		if (ztwMessage.length() > 0) {
			sb.append(' ');
			sb.append(ztwMessage);
		}
		return sb.toString();
	}

	public void send(HttpServletResponse rp)
			throws IOException {
		if (rp == null) throw new IllegalArgumentException("object is null");
		if (ztwMessage.length() == 0) {
			rp.sendError(statusCode);
		} else {
			rp.sendError(statusCode, ztwMessage);
		}
	}

	@Override
	public String toString() {
		return format();
	}

	private static int decodeStatus(String qtw) {
		final String qlc = qtw.toLowerCase();
		if (qlc.equals("bad request")) return HttpServletResponse.SC_BAD_REQUEST;
		if (qlc.equals("not found")) return HttpServletResponse.SC_NOT_FOUND;
		if (qlc.equals("forbidden")) return HttpServletResponse.SC_FORBIDDEN;
		if (qlc.equals("method not allowed")) return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
		if (qlc.contains("timeout")) return HttpServletResponse.SC_REQUEST_TIMEOUT;
		if (qlc.endsWith("too large")) return HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;
		return -1;
	}

	private static boolean validStatusCode(int sc) {
		return sc >= StatusCodeLo && sc <= StatusCodeHi;
	}

	public static HttpError newBadRequest(String ztwMessage) {
		if (ztwMessage == null) throw new IllegalArgumentException("object is null");
		return new HttpError(HttpServletResponse.SC_BAD_REQUEST, ztwMessage);
	}

	public static HttpError newInstance(ArgonFormatException ex) {
		return newInstance(HttpServletResponse.SC_BAD_REQUEST, ex);
	}

	public static HttpError newInstance(ArgonQuotaException ex) {
		return newInstance(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, ex);
	}

	public static HttpError newInstance(ArgonStreamReadException ex) {
		return newInstance(HttpServletResponse.SC_REQUEST_TIMEOUT, ex);
	}

	public static HttpError newInstance(BerylliumHttpBadRequestException ex) {
		return newInstance(HttpServletResponse.SC_BAD_REQUEST, ex);
	}

	public static HttpError newInstance(BerylliumHttpNotFoundException ex) {
		return newInstance(HttpServletResponse.SC_NOT_FOUND, ex);
	}

	public static HttpError newInstance(int statusCode, Throwable throwable) {
		final String ztwMessage = Ds.message(throwable).trim();
		return new HttpError(statusCode, ztwMessage);
	}

	public static HttpError newInstance(InterruptedException ex) {
		return newInstance(HttpServletResponse.SC_SERVICE_UNAVAILABLE, ex);
	}

	public static HttpError newInstance(IOException ex) {
		return newInstance(HttpServletResponse.SC_REQUEST_TIMEOUT, ex);
	}

	public static HttpError newInstance(NeonImpException ex) {
		return newInstance(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
	}

	public static HttpError newInstance(NeonScriptException ex) {
		if (ex instanceof NeonScriptRunException) {
			final NeonScriptRunException rex = (NeonScriptRunException) ex;
			return newInstance(rex.causeMessage(), HttpServletResponse.SC_NOT_FOUND);
		}

		return newInstance(HttpServletResponse.SC_NOT_IMPLEMENTED, ex);
	}

	public static HttpError newInstance(RuntimeException ex) {
		return newInstance(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
	}

	public static HttpError newInstance(String ozSpec) {
		return newInstance(ozSpec, HttpServletResponse.SC_NOT_FOUND);
	}

	public static HttpError newInstance(String ozSpec, int statusCodeDefault) {
		final String ztwSpec = ozSpec == null ? "" : ozSpec.trim();
		int statusCode = statusCodeDefault;
		String ztwMessage = ztwSpec;
		final int len = ztwSpec.length();
		if (len >= 7) {
			final String ztwHead = ztwSpec.substring(0, 7);
			final boolean codeTerm = len == 7 || Character.isWhitespace(ztwSpec.charAt(7));
			if (ztwHead.startsWith(StatusCodePrefix) && codeTerm) {
				final String ztwCode = ztwHead.substring(4);
				try {
					final int sc = Integer.parseInt(ztwCode);
					if (validStatusCode(sc)) {
						statusCode = sc;
						ztwMessage = ztwSpec.substring(7).trim();
					}
				} catch (final NumberFormatException ex) {
				}
			}
		}
		return new HttpError(statusCode, ztwMessage);
	}

	public static HttpError newInstance(String qtwStatusCode, String ztwMessage, int statusCodeDefault) {
		if (qtwStatusCode == null || qtwStatusCode.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		try {
			final int sc = Integer.parseInt(qtwStatusCode);
			if (validStatusCode(sc)) return new HttpError(sc, ztwMessage);
		} catch (final NumberFormatException ex) {
		}

		final int sc = decodeStatus(qtwStatusCode);
		if (sc > 0) return new HttpError(sc, ztwMessage);

		return new HttpError(statusCodeDefault, ztwMessage + " (" + qtwStatusCode + ")");
	}

	public static HttpError newNotFound(String ztwMessage) {
		if (ztwMessage == null) throw new IllegalArgumentException("object is null");
		return new HttpError(HttpServletResponse.SC_NOT_FOUND, ztwMessage);
	}

	private HttpError(int statusCode, String ztwMessage) {
		if (ztwMessage == null) throw new IllegalArgumentException("object is null");
		this.statusCode = statusCode;
		this.ztwMessage = ztwMessage;
	}

	public final int statusCode;
	public final String ztwMessage;
}
