/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.IOException;
import java.util.Date;

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.http.HttpStatus;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.Binary;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class BerylliumDownloadHttpExchange extends BerylliumHttpExchange {

	public static BerylliumDownloadHttpExchange newInstance(Address addr, BerylliumPath uri, String ozCacheControl) {
		final BerylliumDownloadHttpExchange neo = new BerylliumDownloadHttpExchange(null);
		makeGet(neo, addr, ozCacheControl);
		makeUri(neo, uri);
		return neo;
	}

	public static BerylliumDownloadHttpExchange newInstance(Address addr, BerylliumPathQuery uri, String ozCacheControl) {
		final BerylliumDownloadHttpExchange neo = new BerylliumDownloadHttpExchange(null);
		makeGet(neo, addr, ozCacheControl);
		makeUri(neo, uri);
		return neo;
	}

	public static BerylliumDownloadHttpExchange newInstance(IBerylliumHttpAddressable ha, BerylliumPath uri) {
		if (ha == null) throw new IllegalArgumentException("object is null");
		return newInstance(ha.httpAddress(), uri, null);
	}

	public static BerylliumDownloadHttpExchange newInstance(IBerylliumHttpAddressable ha, BerylliumPathQuery uri) {
		if (ha == null) throw new IllegalArgumentException("object is null");
		return newInstance(ha.httpAddress(), uri, null);
	}

	private void onCompleteStatusOK(String oqlctwContentType, Binary content, Date oLastModified) {
		if (oqlctwContentType == null) {
			final Ds ds = Ds.invalidBecause(RsnMissingContentType, CsqOnMalformedResponse);
			failNet(adest(ds));
			raiseCompleteMalformedResponse();
			return;
		}
		final long tsLastModified = oLastModified == null ? ArgonClock.tsNow() : oLastModified.getTime();
		if (isLiveNet()) {
			liveNet("CLIENT RECEIVED (DOWNLOAD) OK");
		}
		raiseCompleteDownloadResponse(new BerylliumBinaryHttpPayload(oqlctwContentType, content, tsLastModified));
	}

	private void raiseCompleteDownloadResponse(BerylliumBinaryHttpPayload payload) {
		assert payload != null;
		final IBerylliumHttpTracker oTracker = getTracker();
		if (oTracker == null) return;
		if (!(oTracker instanceof IBerylliumDownloadHttpTracker)) {
			final Ds ds = Ds.invalidBecause("Expected Download Tracker", CsqNoResponseNotify);
			ds.a("trackerClass", oTracker.getClass().getName());
			warnNet(adest(ds));
			return;
		}
		final IBerylliumDownloadHttpTracker tracker = (IBerylliumDownloadHttpTracker) oTracker;
		try {
			tracker.raiseCompleteResponse(payload);
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo(TryRaiseCompleteNotify, ex, CsqPartResponseNotify);
			ds.a("payload", payload);
			failNet(adest(ds));
		}
	}

	private void raiseCompleteMalformedResponse() {
		final IBerylliumHttpTracker oTracker = getTracker();
		if (oTracker == null) return;
		if (!(oTracker instanceof IBerylliumJsonHttpTracker)) return;

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

	@Override
	protected final void onResponseComplete()
			throws IOException {
		super.onResponseComplete();
		if (haveHttpStatus()) {
			final int httpStatus = httpStatus();
			if (httpStatus == HttpStatus.OK_200) {
				final String oqlcContentType = oqlcContentType();
				final Binary content = popBinary();
				final Date oLastModified = getLastModified();
				onCompleteStatusOK(oqlcContentType, content, oLastModified);
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

	private BerylliumDownloadHttpExchange(Binary oContentTx) {
		super(oContentTx);
	}
}
