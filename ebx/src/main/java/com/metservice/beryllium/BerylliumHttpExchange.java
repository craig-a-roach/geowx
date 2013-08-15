/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferUtil;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.BinaryOutputStream;
import com.metservice.argon.DateFactory;
import com.metservice.argon.Ds;
import com.metservice.argon.text.ArgonNumber;

/**
 * @author roach
 */
public abstract class BerylliumHttpExchange extends HttpExchange {

	private static final int DefaultInitialCapacity = 1024;
	protected static final String TrySendRq = "Send http request to server";
	protected static final String TryConnect = "Connect to http server";
	protected static final String TryExchange = "Complete http exchange with server";
	protected static final String TryRaiseCompleteNotify = "Raise completeResponse notification";
	protected static final String TryRaiseCompleteMalformedNotify = "Raise completeMalformedResponse notification";

	protected static final String RsnMissingContentType = "Missing content-type header in http response";
	protected static final String RsnMissingStatusCode = "Missing status code in http response";

	protected static final String CsqOnUnresponsive = "Raise unresponsive server notification";
	protected static final String CsqOnStatusUnexpected = "Raise unexpected status code notification";
	protected static final String CsqOnMalformedResponse = "Raise malformed response notification";
	protected static final String CsqPartResponseNotify = "Partial response notification";
	protected static final String CsqNoResponseNotify = "No response notification";

	protected static void makeGet(BerylliumHttpExchange neo, Address addr, String ozCacheControl) {
		if (neo == null) throw new IllegalArgumentException("object is null");
		if (addr == null) throw new IllegalArgumentException("object is null");
		final String oqtwCacheControl = ArgonText.oqtw(ozCacheControl);
		neo.setScheme(HttpSchemes.HTTP_BUFFER);
		neo.setMethod(HttpMethods.GET);
		neo.setAddress(addr);
		final HttpFields requestFields = neo.getRequestFields();
		requestFields.put(HttpHeaders.HOST_BUFFER, addr.toString());
		requestFields.put(HttpHeaders.USER_AGENT_BUFFER, CBeryllium.RqUserAgent);
		if (oqtwCacheControl != null) {
			requestFields.put(HttpHeaders.CACHE_CONTROL, oqtwCacheControl);
		}
		requestFields.put(HttpHeaders.CONNECTION_BUFFER, HttpHeaderValues.KEEP_ALIVE_BUFFER);
	}

	protected static void makePost(BerylliumHttpExchange neo, Address addr, String ozMimeType, Date oLastModified) {
		if (neo == null) throw new IllegalArgumentException("object is null");
		if (addr == null) throw new IllegalArgumentException("object is null");
		final String oqtwMimeType = ArgonText.oqtw(ozMimeType);
		final String qlctwMimeType = oqtwMimeType == null ? CBeryllium.BinaryContentType : oqtwMimeType.toLowerCase();
		final int bcContentLength = neo.contentTxLength();
		final long tsLastModified = oLastModified == null ? ArgonClock.tsNow() : oLastModified.getTime();
		neo.setScheme(HttpSchemes.HTTP_BUFFER);
		neo.setMethod(HttpMethods.POST);
		neo.setAddress(addr);
		final HttpFields requestFields = neo.getRequestFields();
		requestFields.put(HttpHeaders.CONTENT_TYPE_BUFFER, qlctwMimeType);
		requestFields.putLongField(HttpHeaders.CONTENT_LENGTH_BUFFER, bcContentLength);
		requestFields.putDateField(HttpHeaders.LAST_MODIFIED_BUFFER, tsLastModified);
		requestFields.put(HttpHeaders.HOST_BUFFER, addr.toString());
		requestFields.put(HttpHeaders.USER_AGENT_BUFFER, CBeryllium.RqUserAgent);
		requestFields.put(HttpHeaders.CACHE_CONTROL, CBeryllium.PostCacheControl);
		requestFields.put(HttpHeaders.CONNECTION_BUFFER, HttpHeaderValues.KEEP_ALIVE_BUFFER);
	}

	protected static void makePut(BerylliumHttpExchange neo, Address addr, String ozMimeType, Date oLastModified) {
		if (neo == null) throw new IllegalArgumentException("object is null");
		if (addr == null) throw new IllegalArgumentException("object is null");
		final String oqtwMimeType = ArgonText.oqtw(ozMimeType);
		final String qlctwMimeType = oqtwMimeType == null ? CBeryllium.BinaryContentType : oqtwMimeType.toLowerCase();
		final int bcContentLength = neo.contentTxLength();
		final long tsLastModified = oLastModified == null ? ArgonClock.tsNow() : oLastModified.getTime();
		neo.setScheme(HttpSchemes.HTTP_BUFFER);
		neo.setMethod(HttpMethods.PUT);
		neo.setAddress(addr);
		final HttpFields requestFields = neo.getRequestFields();
		requestFields.put(HttpHeaders.CONTENT_TYPE_BUFFER, qlctwMimeType);
		requestFields.putLongField(HttpHeaders.CONTENT_LENGTH_BUFFER, bcContentLength);
		requestFields.putDateField(HttpHeaders.LAST_MODIFIED_BUFFER, tsLastModified);
		requestFields.put(HttpHeaders.HOST_BUFFER, addr.toString());
		requestFields.put(HttpHeaders.USER_AGENT_BUFFER, CBeryllium.RqUserAgent);
		requestFields.put(HttpHeaders.CONNECTION_BUFFER, HttpHeaderValues.KEEP_ALIVE_BUFFER);
	}

	protected static void makeUri(BerylliumHttpExchange neo, BerylliumPath uri) {
		if (neo == null) throw new IllegalArgumentException("object is null");
		if (uri == null) throw new IllegalArgumentException("object is null");
		neo.setRequestURI(uri.ztwPathEncoded());
	}

	protected static void makeUri(BerylliumHttpExchange neo, BerylliumPathQuery uri) {
		if (neo == null) throw new IllegalArgumentException("object is null");
		if (uri == null) throw new IllegalArgumentException("object is null");
		neo.setRequestURI(uri.ztwPathQueryEncoded());
	}

	private String qStatus() {
		final int status = getStatus();
		switch (status) {
			case STATUS_START:
				return "Start";
			case STATUS_WAITING_FOR_CONNECTION:
				return "WaitingForConnection";
			case STATUS_WAITING_FOR_COMMIT:
				return "WaitingForCommit";
			case STATUS_WAITING_FOR_RESPONSE:
				return "WaitingForResponse";
			case STATUS_COMPLETED:
				return "Completed";
		}
		return "JETTY" + ArgonNumber.intToDec2(status);
	}

	protected final Ds adest(Ds ds) {
		ds.a("address", getAddress());
		ds.a("method", getMethod());
		ds.a("scheme", getScheme());
		ds.a("status", qStatus());
		ds.a("URI", getRequestURI());
		return ds;
	}

	protected InputStream createTxInputStream() {
		return m_oContentTx == null ? null : m_oContentTx.getInputStream();
	}

	protected final void failNet(Ds ds) {
		final IBerylliumNetProbe oProbe = m_probe.get();
		if (oProbe == null) return;
		oProbe.failNet(adest(ds));
	}

	protected final boolean isLiveNet() {
		final IBerylliumNetProbe oProbe = m_probe.get();
		return oProbe != null && oProbe.isLiveNet();
	}

	protected final void liveNet(String message) {
		final IBerylliumNetProbe oProbe = m_probe.get();
		if (oProbe == null) return;
		oProbe.liveNet(message, this);
	}

	protected void onCompleteStatusNotOK(int status, String oqStatusReason) {
		final Ds ds = Ds.invalidBecause("Unexpected http status code", CsqOnStatusUnexpected);
		ds.a("httpStatus", status);
		ds.a("httpReason", oqStatusReason);
		if (status >= 200 && status < 500) {
			warnNet(adest(ds));
		} else {
			failNet(adest(ds));
		}
		raiseStatusUnexpected(status);
	}

	@Override
	protected final void onConnectionFailed(Throwable ex) {
		super.onConnectionFailed(ex);
		warnNet(Ds.triedTo(TryConnect, ex, CsqOnUnresponsive));
		raiseUnresponsive();
	}

	@Override
	protected final void onException(Throwable ex) {
		super.onException(ex);
		failNet(Ds.triedTo(TryExchange, ex, CsqOnUnresponsive));
		raiseUnresponsive();
	}

	@Override
	protected final void onExpire() {
		super.onExpire();
		warnNet(Ds.invalidBecause("Did not receive http response from server within timeout interval", CsqOnUnresponsive));
		raiseUnresponsive();
	}

	@Override
	protected final void onResponseContent(Buffer content)
			throws IOException {
		super.onResponseContent(content);
		m_lockBufferRx.lock();
		try {
			if (m_oBinaryOutputStreamRx == null) {
				int initialCapacity = DefaultInitialCapacity;
				if (haveHttpHeaders()) {
					final int contentLength = contentLength();
					if (contentLength > 0) {
						initialCapacity = contentLength;
					}
				}
				m_oBinaryOutputStreamRx = new BinaryOutputStream(initialCapacity);
			}
			content.writeTo(m_oBinaryOutputStreamRx);
		} finally {
			m_lockBufferRx.unlock();
		}
	}

	@Override
	protected final void onResponseHeader(Buffer name, Buffer value)
			throws IOException {
		super.onResponseHeader(name, value);
		if (name == null || value == null) return;
		final int header = HttpHeaders.CACHE.getOrdinal(name);
		switch (header) {
			case HttpHeaders.CONTENT_LENGTH_ORDINAL: {
				m_contentLength.set(BufferUtil.toInt(value));
			}
			break;
			case HttpHeaders.CONTENT_TYPE_ORDINAL: {
				final String zlctwMimeExpr = value.toString(ArgonText.CHARSET_NAME_ASCII).toLowerCase().trim();
				if (zlctwMimeExpr.length() > 0) {
					final int posParam = zlctwMimeExpr.indexOf(';');
					final String zlctwContentType;
					final String zlctwParam;
					if (posParam > 0) {
						zlctwContentType = zlctwMimeExpr.substring(0, posParam).trim();
						zlctwParam = zlctwMimeExpr.substring(posParam + 1).trim();
					} else {
						zlctwContentType = zlctwMimeExpr;
						zlctwParam = "";
					}
					final String zlctwCharset;
					if (zlctwParam.startsWith("charset=")) {
						final String zlcParamRhs = zlctwParam.substring(8);
						final int posParam1 = zlcParamRhs.indexOf(';');
						zlctwCharset = posParam1 < 0 ? zlcParamRhs : zlcParamRhs.substring(0, posParam1).trim();
					} else {
						zlctwCharset = "";
					}
					if (zlctwContentType.length() > 0) {
						m_contentType.set(zlctwContentType);
						if (zlctwCharset.length() > 0) {
							m_contentTypeCharset.set(zlctwCharset);
						}
					}
				}
			}
			break;
			case HttpHeaders.LAST_MODIFIED_ORDINAL: {
				final String ztwLastModified = value.toString(ArgonText.CHARSET_NAME_ASCII).trim();
				if (ztwLastModified.length() > 0) {
					final long otsParseDate = HttpFields.parseDate(ztwLastModified);
					if (otsParseDate >= 0L) {
						m_lastModified.set(DateFactory.newDate(otsParseDate));
					}
				}
			}
		}
	}

	@Override
	protected final void onResponseHeaderComplete()
			throws IOException {
		super.onResponseHeaderComplete();
		m_haveHttpHeaders.set(true);
	}

	@Override
	protected final void onResponseStatus(Buffer version, int status, Buffer reason)
			throws IOException {
		super.onResponseStatus(version, status, reason);
		m_httpStatus.set(status);
		final String ztwReason = reason == null ? "" : reason.toString(ArgonText.CHARSET_NAME_ISO8859).trim();
		if (ztwReason.length() > 0) {
			m_httpStatusReason.set(ztwReason);
		}
		m_haveHttpStatus.set(true);
	}

	@Override
	protected final void onRetry()
			throws IOException {
		super.onRetry();
		final InputStream oEx = getRequestContentSource();
		if (oEx != null) {
			try {
				oEx.close();
			} catch (final IOException ex) {
			}
		}
		final InputStream oIns = createTxInputStream();
		if (oIns != null) {
			setRequestContentSource(oIns);
		}
	}

	protected final Binary popBinary() {
		m_lockBufferRx.lock();
		try {
			if (m_oBinaryOutputStreamRx == null) return Binary.Empty;
			final Binary neo = m_oBinaryOutputStreamRx.newBinary();
			m_oBinaryOutputStreamRx = null;
			return neo;
		} finally {
			m_lockBufferRx.unlock();
		}
	}

	protected void raiseStatusUnexpected(int status) {
		final IBerylliumHttpTracker oTracker = getTracker();
		try {
			if (oTracker != null) {
				oTracker.raiseStatusUnexpected(status);
			}
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo("Raise onStatusUnexpected notification", ex, "Partial notification");
			failNet(adest(ds));
		}
	}

	protected final void raiseUnresponsive() {
		final IBerylliumHttpTracker oTracker = getTracker();
		if (oTracker == null) return;
		try {
			oTracker.raiseUnresponsive();
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		} catch (final RuntimeException ex) {
			failNet(Ds.triedTo("Raise onUnresponsive notification", ex, "Partial notification"));
		}
	}

	protected final void warnNet(Ds ds) {
		final IBerylliumNetProbe oProbe = m_probe.get();
		if (oProbe == null) return;
		oProbe.warnNet(adest(ds));
	}

	public int contentLength() {
		return m_contentLength.get();
	}

	public int contentTxLength() {
		return m_oContentTx == null ? 0 : m_oContentTx.byteCount();
	}

	public Charset findContentTypeCharset() {
		final String oqlcContentTypeCharset = oqlcContentTypeCharset();
		if (oqlcContentTypeCharset == null) return null;
		try {
			return ArgonText.selectCharset(oqlcContentTypeCharset);
		} catch (final ArgonApiException ex) {
			return null;
		}
	}

	public Date getLastModified() {
		return m_lastModified.get();
	}

	public final IBerylliumNetProbe getProbe() {
		return m_probe.get();
	}

	public final IBerylliumHttpTracker getTracker() {
		return m_tracker.get();
	}

	public boolean haveHttpHeaders() {
		return m_haveHttpHeaders.get();
	}

	public boolean haveHttpStatus() {
		return m_haveHttpStatus.get();
	}

	public int httpStatus() {
		return m_httpStatus.get();
	}

	public String oqHttpStatusReason() {
		return m_httpStatusReason.get();
	}

	public String oqlcContentType() {
		return m_contentType.get();
	}

	public String oqlcContentTypeCharset() {
		return m_contentTypeCharset.get();
	}

	public void sendFrom(HttpClient client, IBerylliumHttpTracker oTracker, IBerylliumNetProbe oProbe)
			throws InterruptedException {
		if (client == null) throw new IllegalArgumentException("object is null");
		final boolean probeLiveNet = oProbe != null && oProbe.isLiveNet();
		m_tracker.set(oTracker);
		m_probe.set(oProbe);
		final InputStream oIns = createTxInputStream();
		try {
			if (oIns != null) {
				setRequestContentSource(oIns);
			}
			if (probeLiveNet) {
				liveNet("CLIENT SENDING");
			}
			client.send(this);
			if (probeLiveNet) {
				liveNet("CLIENT SENT");
			}
		} catch (final IOException ex) {
			failNet(Ds.triedTo(TrySendRq, ex, CsqOnUnresponsive));
			raiseUnresponsive();
		} catch (final RuntimeException ex) {
			failNet(Ds.triedTo(TrySendRq, ex, CsqOnUnresponsive));
			raiseUnresponsive();
		} finally {
			try {
				if (oIns != null) {
					oIns.close();
				}
			} catch (final IOException ex) {
			}
		}
	}

	public void sendFromAwaitOutcome(HttpClient client, IBerylliumHttpTracker oTracker, IBerylliumNetProbe oProbe)
			throws InterruptedException {
		sendFrom(client, oTracker, oProbe);
		if (oTracker != null) {
			final boolean probeLiveNet = oProbe != null && oProbe.isLiveNet();
			oTracker.awaitOutcome();
			if (probeLiveNet) {
				liveNet("CLIENT OUTCOME");
			}
		}
	}

	@Override
	public String toString() {
		final String ozMethod = getMethod();
		final String zMethod = ozMethod == null ? "" : ozMethod;
		final Address oAddress = getAddress();
		final String zAddress = oAddress == null ? "" : oAddress.toString();
		final String ozUri = getRequestURI();
		final String zUri = ozUri == null ? "" : ozUri;
		final String qStatus = qStatus();
		final StringBuilder sb = new StringBuilder();
		sb.append(zMethod);
		sb.append("//");
		sb.append(zAddress);
		sb.append(zUri);
		sb.append(" ");
		sb.append(qStatus);
		return sb.toString();
	}

	protected BerylliumHttpExchange(Binary oContentTx) {
		m_oContentTx = oContentTx;
	}

	private final Binary m_oContentTx;
	private final AtomicReference<IBerylliumNetProbe> m_probe = new AtomicReference<IBerylliumNetProbe>();
	private final AtomicReference<IBerylliumHttpTracker> m_tracker = new AtomicReference<IBerylliumHttpTracker>();
	private final AtomicInteger m_httpStatus = new AtomicInteger(0);
	private final AtomicBoolean m_haveHttpStatus = new AtomicBoolean(false);
	private final AtomicInteger m_contentLength = new AtomicInteger(0);
	private final AtomicReference<String> m_httpStatusReason = new AtomicReference<String>();
	private final AtomicReference<String> m_contentType = new AtomicReference<String>();
	private final AtomicReference<String> m_contentTypeCharset = new AtomicReference<String>();
	private final AtomicReference<Date> m_lastModified = new AtomicReference<Date>();
	private final AtomicBoolean m_haveHttpHeaders = new AtomicBoolean(false);
	private final Lock m_lockBufferRx = new ReentrantLock();
	private BinaryOutputStream m_oBinaryOutputStreamRx;
}
