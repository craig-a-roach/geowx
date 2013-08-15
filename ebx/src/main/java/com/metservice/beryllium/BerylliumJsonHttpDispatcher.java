/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Binary;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class BerylliumJsonHttpDispatcher extends BerylliumHttpDispatcher {

	public void handle(IBerylliumJsonHttpService service, BerylliumPath path, Request rq, HttpServletResponse rp)
			throws IOException, ServletException {
		if (service == null) throw new IllegalArgumentException("object is null");
		if (path == null) throw new IllegalArgumentException("object is null");
		if (rq == null) throw new IllegalArgumentException("object is null");
		if (rp == null) throw new IllegalArgumentException("object is null");
		try {
			final JsonObject jrq = BerylliumJsonDecoder.newJsonObjectDecode(rq, m_bcQuota);
			traceRequest("JSON", rq);
			final JsonObject jrp = JsonObject.newMutable();
			service.handle(path, rq, jrq, jrp);
			final String qSpec = CBeryllium.jsonEncoder(m_strict).encode(jrp);
			final Binary contentBinary = Binary.newFromStringASCII(qSpec);
			traceResponse("JSON", rq, contentBinary);
			BerylliumIO.writeStreamNoCache(rp, CBeryllium.JsonContentType, contentBinary, null);
		} catch (final BerylliumHttpNotFoundException ex) {
			onStatus(path, rq, rp, HttpServletResponse.SC_NOT_FOUND, ex);
		} catch (final BerylliumHttpBadRequestException ex) {
			onWarn(path, rq, rp, HttpServletResponse.SC_BAD_REQUEST, ex);
		} catch (final JsonSchemaException ex) {
			onWarn(path, rq, rp, HttpServletResponse.SC_BAD_REQUEST, ex);
		} catch (final ArgonFormatException ex) {
			onWarn(path, rq, rp, HttpServletResponse.SC_BAD_REQUEST, ex);
		} catch (final ArgonQuotaException ex) {
			onWarn(path, rq, rp, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, ex);
		} catch (final ArgonStreamReadException ex) {
			onWarn(path, rq, rp, HttpServletResponse.SC_REQUEST_TIMEOUT, ex);
		} catch (final InterruptedException ex) {
			onWarn(path, rq, rp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Cancelled");
			Thread.currentThread().interrupt();
		} catch (final RuntimeException ex) {
			onWarn(path, rq, rp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
		}
	}

	public BerylliumJsonHttpDispatcher(IBerylliumNetProbe probe, int bcQuota, boolean strict) {
		super(probe);
		m_bcQuota = bcQuota;
		m_strict = strict;
	}
	private final int m_bcQuota;
	private final boolean m_strict;
}
