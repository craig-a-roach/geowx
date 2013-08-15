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

import com.metservice.argon.Binary;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class BerylliumDownloadHttpDispatcher extends BerylliumHttpDispatcher {

	public void handle(IBerylliumDownloadHttpService service, BerylliumPath path, Request rq, HttpServletResponse rp)
			throws IOException, ServletException {
		if (service == null) throw new IllegalArgumentException("object is null");
		if (path == null) throw new IllegalArgumentException("object is null");
		if (rq == null) throw new IllegalArgumentException("object is null");
		if (rp == null) throw new IllegalArgumentException("object is null");
		try {
			final JsonObject jrq = UBeryllium.transformParameterMapToJson(rq);
			final BerylliumBinaryHttpPayload oPayload = service.newPayload(path, rq, jrq);
			if (oPayload == null) {
				onWarn(path, rq, rp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not construct payload");
			} else {
				final Binary contentBinary = oPayload.content();
				final String qlcContentType = oPayload.qlcContentType();
				final Date lastModified = oPayload.lastModified();
				traceResponse(qlcContentType, rq, contentBinary);
				BerylliumIO.writeStreamNoCache(rp, qlcContentType, contentBinary, lastModified);
			}
		} catch (final BerylliumHttpNotFoundException ex) {
			onStatus(path, rq, rp, HttpServletResponse.SC_NOT_FOUND, ex);
		} catch (final BerylliumHttpBadRequestException ex) {
			onWarn(path, rq, rp, HttpServletResponse.SC_BAD_REQUEST, ex);
		} catch (final JsonSchemaException ex) {
			onWarn(path, rq, rp, HttpServletResponse.SC_BAD_REQUEST, ex);
		} catch (final InterruptedException ex) {
			onWarn(path, rq, rp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Cancelled");
			Thread.currentThread().interrupt();
		} catch (final RuntimeException ex) {
			onWarn(path, rq, rp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
		}
	}

	public BerylliumDownloadHttpDispatcher(IBerylliumNetProbe probe) {
		super(probe);
	}
}
