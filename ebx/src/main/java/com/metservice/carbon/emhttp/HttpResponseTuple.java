/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.metservice.argon.Binary;
import com.metservice.argon.Ds;
import com.metservice.beryllium.BerylliumIO;
import com.metservice.neon.EmMutableAccessor;
import com.metservice.neon.EmMutablePropertyAccessor;
import com.metservice.neon.EsExecutionContext;

/**
 * @author roach
 */
public class HttpResponseTuple {

	public void send(HttpServletResponse rp)
			throws ServletException, IOException {
		if (rp == null) throw new IllegalArgumentException("object is null");
		m_sendable.send(rp);
	}

	@Override
	public String toString() {
		return m_sendable.toString();
	}

	private static boolean expectContent(int statusCode) {
		return (statusCode == HttpServletResponse.SC_OK || statusCode == HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION);
	}

	private static boolean isStandardDateHeader(String qccPropertyName) {
		if (qccPropertyName.equalsIgnoreCase(CProp.expires)) return true;
		if (qccPropertyName.equalsIgnoreCase(CProp.lastModified)) return true;
		return false;
	}

	private static boolean isStandardHeader(String qccPropertyName) {
		assert qccPropertyName != null && qccPropertyName.length() > 0;
		if (qccPropertyName.equals(CProp.statusCode) || qccPropertyName.equals(CProp.content)) return false;
		final char ch0 = qccPropertyName.charAt(0);
		return Character.isUpperCase(ch0);
	}

	private static Map<String, Object> newStandardHeaderMap(EsExecutionContext ecx, HttpResponseEm src)
			throws InterruptedException {
		assert src != null;
		final EmMutableAccessor ma = new EmMutableAccessor(ecx, src);
		final List<String> zlPropertyNames = src.esPropertyNames();
		final int pcount = zlPropertyNames.size();
		final Map<String, Object> zm = new HashMap<String, Object>(pcount);
		for (int i = 0; i < pcount; i++) {
			final String pname = zlPropertyNames.get(i);
			if (!isStandardHeader(pname)) {
				continue;
			}
			final EmMutablePropertyAccessor pa = ma.newPropertyAccessor(pname);
			if (!pa.esType.isDatum) {
				continue;
			}
			final boolean isDateHeader = isStandardDateHeader(pname);
			if (isDateHeader) {
				zm.put(pname, pa.timeValue());
			} else {
				zm.put(pname, pa.ztwStringValue());
			}
		}
		return zm;
	}

	public HttpResponseTuple(EsExecutionContext ecx, HttpResponseConstruct construct, HttpResponseEm src)
			throws InterruptedException {

		final EmMutableAccessor ma = new EmMutableAccessor(ecx, src);
		final EmMutablePropertyAccessor aStatusCode = ma.newPropertyAccessor(CProp.statusCode);
		final int statusCode = aStatusCode.intValue();
		final Map<String, Object> standardHeaderMap = newStandardHeaderMap(ecx, src);
		final Sendable sendable;
		if (expectContent(statusCode)) {
			final EmMutablePropertyAccessor aContent = ma.newPropertyAccessor(CProp.content);
			final Binary content = aContent.binaryValue();
			sendable = new SendableContent(statusCode, standardHeaderMap, content);
		} else {
			sendable = new SendableNoContent(statusCode, standardHeaderMap);
		}
		m_sendable = sendable;

	}
	private final Sendable m_sendable;

	private static abstract class Sendable {

		protected void dsa(Ds ds) {
			ds.a("statusCode", statusCode);
			ds.a("standardHeaders", standardHeaderMap);
		}

		protected void sendHeaders(HttpServletResponse rp) {
			rp.setStatus(statusCode);
			final List<String> zlHeaderNames = new ArrayList<String>(standardHeaderMap.keySet());
			Collections.sort(zlHeaderNames);
			for (final String headerName : zlHeaderNames) {
				final Object headerValue = standardHeaderMap.get(headerName);
				if (headerValue instanceof String) {
					final String zHeaderValue = (String) headerValue;
					rp.setHeader(headerName, zHeaderValue);
				} else if (headerValue instanceof Date) {
					final Date dateHeaderValue = (Date) headerValue;
					rp.setDateHeader(headerName, dateHeaderValue.getTime());
				} else if (headerValue instanceof Integer) {
					final Integer intHeaderValue = (Integer) headerValue;
					rp.setIntHeader(headerName, intHeaderValue.intValue());
				}
			}
		}

		public abstract void send(HttpServletResponse rp)
				throws ServletException, IOException;

		protected Sendable(int statusCode, Map<String, Object> standardHeaderMap) {
			this.statusCode = statusCode;
			this.standardHeaderMap = standardHeaderMap;
		}
		final int statusCode;
		final Map<String, Object> standardHeaderMap;
	}

	private static class SendableContent extends Sendable {

		@Override
		public void send(HttpServletResponse rp)
				throws ServletException, IOException {
			sendHeaders(rp);
			BerylliumIO.writeStream(rp, content);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("SendableContent");
			super.dsa(ds);
			ds.a("content", content);
			return ds.s();
		}

		public SendableContent(int statusCode, Map<String, Object> standardHeaderMap, Binary content) {
			super(statusCode, standardHeaderMap);
			assert content != null;
			this.content = content;
		}
		final Binary content;
	}

	private static class SendableNoContent extends Sendable {

		@Override
		public void send(HttpServletResponse rp)
				throws ServletException, IOException {
			sendHeaders(rp);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("SendableNoContent");
			super.dsa(ds);
			return ds.s();
		}

		public SendableNoContent(int statusCode, Map<String, Object> standardHeaderMap) {
			super(statusCode, standardHeaderMap);
		}
	}

}
