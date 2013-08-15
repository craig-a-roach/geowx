/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import java.io.IOException;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.json.JsonObject;
import com.metservice.beryllium.BerylliumHttpBadRequestException;
import com.metservice.beryllium.BerylliumJsonDecoder;
import com.metservice.neon.EmException;
import com.metservice.neon.EmViewObject;
import com.metservice.neon.EsExecutionContext;

/**
 * @author roach
 */
class HttpRequestEm extends EmViewObject {

	private void putViewHeaderDate(String qccHeaderName) {
		try {
			final long ots = m_imp.getDateHeader(qccHeaderName);
			if (ots > 0L) {
				putViewTime(qccHeaderName, ots);
			}
		} catch (final RuntimeException ex) {
		}
	}

	private void putViewHeaderString(String qccHeaderName) {
		final String ozValue = m_imp.getHeader(qccHeaderName);
		if (ozValue != null && ozValue.length() > 0) {
			putView(qccHeaderName, ozValue);
		}
	}

	public JsonObject newContentJsonObject() {
		try {
			return BerylliumJsonDecoder.newJsonObjectDecode(m_imp, m_bcQuota);
		} catch (final BerylliumHttpBadRequestException ex) {
			throw new EmException(HttpError.newInstance(ex).format());
		} catch (final ArgonQuotaException ex) {
			throw new EmException(HttpError.newInstance(ex).format());
		} catch (final ArgonFormatException ex) {
			throw new EmException(HttpError.newInstance(ex).format());
		} catch (final ArgonStreamReadException ex) {
			throw new EmException(HttpError.newInstance(ex).format());
		} catch (final IOException ex) {
			throw new EmException(HttpError.newInstance(ex).format());
		}
	}

	@Override
	public void putProperties(EsExecutionContext ecx)
			throws InterruptedException {
		final String ozMethod = m_imp.getMethod();
		final HttpURI uri = m_imp.getUri();
		putViewIfQtw(CProp.method, ozMethod);
		putViewIfQtw(CProp.path, uri.getDecodedPath());
		putViewIfQtw(CProp.serverHost, m_imp.getServerName());
		putViewInteger(CProp.serverPort, m_imp.getServerPort());
		putViewIfQtw(CProp.remoteAddress, m_imp.getRemoteAddr());

		if (ozMethod != null) {
			if (ozMethod.equals(HttpMethods.POST) || ozMethod.equals(HttpMethods.PUT)) {
				putView(CProp.contentType, m_imp.getContentType());
			}
			if (ozMethod.equals(HttpMethods.GET)) {
				putViewHeaderDate(HttpHeaders.IF_MODIFIED_SINCE);
				putViewHeaderDate(HttpHeaders.IF_UNMODIFIED_SINCE);
			}
			putViewHeaderString(HttpHeaders.HOST);
			putViewHeaderString(HttpHeaders.ACCEPT);
			putViewHeaderString(HttpHeaders.REFERER);
		}
	}

	public HttpRequestEm(Request imp, int bcQuota) {
		super(HttpRequestEmClass.Instance);
		if (imp == null) throw new IllegalArgumentException("object is null");
		m_imp = imp;
		m_bcQuota = bcQuota;
	}

	private final Request m_imp;
	private final int m_bcQuota;
}
