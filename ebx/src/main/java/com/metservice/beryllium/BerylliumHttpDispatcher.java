/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.DateFactory;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class BerylliumHttpDispatcher {

	private static final String TryDispatch = "Respond to json http request";
	private static final String CsqIgnore = "Request ignored and http error status sent to client";

	protected static Date getLastModifedHeader(Request rq) {
		if (rq == null) throw new IllegalArgumentException("object is null");
		final long otsLastModified = rq.getDateHeader(HttpHeaders.LAST_MODIFIED);
		return otsLastModified < 0L ? null : DateFactory.newDate(otsLastModified);
	}

	protected static String oqlctwContentType(Request rq) {
		if (rq == null) throw new IllegalArgumentException("object is null");
		final String oqtw = ArgonText.oqtw(rq.getHeader(HttpHeaders.CONTENT_TYPE));
		return oqtw == null ? null : oqtw.toLowerCase();
	}

	protected final void onStatus(BerylliumPath path, Request rq, HttpServletResponse rp, int statusCode, String reason)
			throws IOException {
		rp.setStatus(statusCode);
		final PrintWriter writer = rp.getWriter();
		writer.println(reason);
	}

	protected final void onStatus(BerylliumPath path, Request rq, HttpServletResponse rp, int statusCode, Throwable cause)
			throws IOException {
		rp.setStatus(statusCode);
		final PrintWriter writer = rp.getWriter();
		writer.println(Ds.message(cause));
	}

	protected final void onWarn(BerylliumPath path, Request rq, HttpServletResponse rp, int statusCode, String reason)
			throws IOException {
		onStatus(path, rq, rp, statusCode, reason);
		final Ds ds = Ds.invalidBecause(reason, CsqIgnore);
		ds.a("path", path);
		ds.a("remoteAddress", rq.getRemoteAddr());
		ds.a("responseCode", statusCode);
		probe.warnNet(ds);
	}

	protected final void onWarn(BerylliumPath path, Request rq, HttpServletResponse rp, int statusCode, Throwable cause)
			throws IOException {
		onStatus(path, rq, rp, statusCode, cause);
		final Ds ds = Ds.triedTo(TryDispatch, cause, CsqIgnore);
		ds.a("path", path);
		ds.a("remoteAddress", rq.getRemoteAddr());
		ds.a("responseCode", statusCode);
		probe.warnNet(ds);
	}

	protected final void traceRequest(String type, Request orq) {
		if (orq == null || !probe.isLiveNet()) return;
		final String oqRequestURL = orq.getRequestURL().toString();
		final String oqRemoteAddr = orq.getRemoteAddr();
		probe.liveNet("SERVER REQUEST (" + type + ")", oqRequestURL, "FROM CLIENT", oqRemoteAddr);
	}

	protected final void traceResponse(String type, Request orq, Binary oResponse) {
		if (orq == null || !probe.isLiveNet()) return;
		final String oqRemoteAddr = orq.getRemoteAddr();
		final String bc = oResponse == null ? "n/a" : (oResponse.byteCount() + "bytes");
		probe.liveNet("SERVER RESPONSE (" + type + ")", bc, "TO CLIENT", oqRemoteAddr);
	}

	protected BerylliumHttpDispatcher(IBerylliumNetProbe probe) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		this.probe = probe;
	}
	protected final IBerylliumNetProbe probe;
}
