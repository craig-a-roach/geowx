/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Binary;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class BerylliumUploadHttpDispatcher extends BerylliumHttpDispatcher {

	public void handle(IBerylliumUploadHttpService service, BerylliumPath path, Request rq, HttpServletResponse rp)
			throws IOException, ServletException {
		if (service == null) throw new IllegalArgumentException("object is null");
		if (path == null) throw new IllegalArgumentException("object is null");
		if (rq == null) throw new IllegalArgumentException("object is null");
		if (rp == null) throw new IllegalArgumentException("object is null");
		try {
			final JsonObject jrq = UBeryllium.transformParameterMapToJson(rq);
			final String oqlctwContentType = oqlctwContentType(rq);
			if (oqlctwContentType == null) {
				onWarn(path, rq, rp, HttpServletResponse.SC_BAD_REQUEST, "Missing content-type header");
			} else {
				final Date oInLastModifed = getLastModifedHeader(rq);
				if (oInLastModifed == null) {
					onWarn(path, rq, rp, HttpServletResponse.SC_BAD_REQUEST, "Missing last-modified header");
				} else {
					final String qlcInType = oqlctwContentType;
					final long tsIn = oInLastModifed.getTime();
					final Binary inContent = UBeryllium.readBinaryInput(rq, m_bcQuota);
					final BerylliumBinaryHttpPayload in = new BerylliumBinaryHttpPayload(qlcInType, inContent, tsIn);
					BerylliumBinaryHttpPayload vOut = service.createPayload(path, rq, jrq, in);
					if (vOut == null) {
						vOut = BerylliumBinaryHttpPayload.newEmpty(tsIn);
					}
					final Binary contentBinary = vOut.content();
					final String qlcContentType = vOut.qlcContentType();
					final Date lastModified = vOut.lastModified();
					traceResponse(qlcContentType, rq, contentBinary);
					BerylliumIO.writeStreamNoCache(rp, qlcContentType, contentBinary, lastModified);
				}
			}
		} catch (final BerylliumHttpNotFoundException ex) {
			onStatus(path, rq, rp, HttpServletResponse.SC_NOT_FOUND, ex);
		} catch (final BerylliumHttpBadRequestException ex) {
			onWarn(path, rq, rp, HttpServletResponse.SC_BAD_REQUEST, ex);
		} catch (final JsonSchemaException ex) {
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

	public BerylliumUploadHttpDispatcher(IBerylliumNetProbe probe, int bcQuota) {
		super(probe);
		m_bcQuota = bcQuota;
	}
	private final int m_bcQuota;
}
