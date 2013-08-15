/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
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
public class BerylliumUploadHttpExchange extends BerylliumHttpExchange {

	private static void makePut(BerylliumUploadHttpExchange neo, Address addr, BerylliumBinaryHttpPayload payload) {
		if (payload == null) throw new IllegalArgumentException("object is null");
		makePut(neo, addr, payload.qlcContentType(), payload.lastModified());
	}

	private static void makePut(BerylliumUploadHttpExchange neo, IBerylliumHttpAddressable ha, BerylliumBinaryHttpPayload payload) {
		if (ha == null) throw new IllegalArgumentException("object is null");
		makePut(neo, ha.httpAddress(), payload);
	}

	private static BerylliumUploadHttpExchange newInstance(BerylliumBinaryHttpPayload payload) {
		if (payload == null) throw new IllegalArgumentException("object is null");
		return new BerylliumUploadHttpExchange(payload.content());
	}

	public static BerylliumUploadHttpExchange newInstance(Address addr, BerylliumPath uri, BerylliumBinaryHttpPayload payload) {
		final BerylliumUploadHttpExchange neo = newInstance(payload);
		makePut(neo, addr, payload);
		makeUri(neo, uri);
		return neo;
	}

	public static BerylliumUploadHttpExchange newInstance(Address addr, BerylliumPathQuery uri, BerylliumBinaryHttpPayload payload) {
		final BerylliumUploadHttpExchange neo = newInstance(payload);
		makePut(neo, addr, payload);
		makeUri(neo, uri);
		return neo;
	}

	public static BerylliumUploadHttpExchange newInstance(IBerylliumHttpAddressable ha, BerylliumPath uri,
			BerylliumBinaryHttpPayload payload) {
		final BerylliumUploadHttpExchange neo = newInstance(payload);
		makePut(neo, ha, payload);
		makeUri(neo, uri);
		return neo;
	}

	public static BerylliumUploadHttpExchange newInstance(IBerylliumHttpAddressable ha, BerylliumPathQuery uri,
			BerylliumBinaryHttpPayload payload) {
		final BerylliumUploadHttpExchange neo = newInstance(payload);
		makePut(neo, ha, payload);
		makeUri(neo, uri);
		return neo;
	}

	private void onCompleteStatusOK(String oqlctwContentType, Binary content, Date oLastModified) {
		BerylliumBinaryHttpPayload oPayload = null;
		if (oqlctwContentType != null) {
			final long tsLastModified = oLastModified == null ? ArgonClock.tsNow() : oLastModified.getTime();
			oPayload = new BerylliumBinaryHttpPayload(oqlctwContentType, content, tsLastModified);
		}

		if (isLiveNet()) {
			liveNet("CLIENT RECEIVED (UPLOAD) OK");
		}
		raiseCompleteUploadResponse(oPayload);
	}

	private void raiseCompleteUploadResponse(BerylliumBinaryHttpPayload oPayload) {
		final IBerylliumHttpTracker oTracker = getTracker();
		if (oTracker == null) return;
		if (!(oTracker instanceof IBerylliumUploadHttpTracker)) {
			final Ds ds = Ds.invalidBecause("Expected Upload Tracker", CsqNoResponseNotify);
			ds.a("trackerClass", oTracker.getClass().getName());
			warnNet(adest(ds));
			return;
		}
		final IBerylliumUploadHttpTracker tracker = (IBerylliumUploadHttpTracker) oTracker;
		try {
			tracker.raiseCompleteResponse(oPayload);
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo(TryRaiseCompleteNotify, ex, CsqPartResponseNotify);
			ds.a("payload", oPayload);
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

	private BerylliumUploadHttpExchange(Binary oContentTx) {
		super(oContentTx);
	}
}
