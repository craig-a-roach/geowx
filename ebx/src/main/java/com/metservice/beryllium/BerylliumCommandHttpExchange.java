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
public class BerylliumCommandHttpExchange extends BerylliumHttpExchange {

	public static BerylliumCommandHttpExchange newInstance(Address addr, BerylliumPath uri) {
		final BerylliumCommandHttpExchange neo = new BerylliumCommandHttpExchange();
		makePost(neo, addr, null, null);
		makeUri(neo, uri);
		return neo;
	}

	public static BerylliumCommandHttpExchange newInstance(Address addr, BerylliumPathQuery uri) {
		final BerylliumCommandHttpExchange neo = new BerylliumCommandHttpExchange();
		makePost(neo, addr, null, null);
		makeUri(neo, uri);
		return neo;
	}

	public static BerylliumCommandHttpExchange newInstance(IBerylliumHttpAddressable ha, BerylliumPath uri) {
		if (ha == null) throw new IllegalArgumentException("object is null");
		return newInstance(ha.httpAddress(), uri);
	}

	public static BerylliumCommandHttpExchange newInstance(IBerylliumHttpAddressable ha, BerylliumPathQuery uri) {
		if (ha == null) throw new IllegalArgumentException("object is null");
		return newInstance(ha.httpAddress(), uri);
	}

	private void onCompleteStatusOK(String oqlctwContentType, Binary content, Date oLastModified) {
		BerylliumBinaryHttpPayload oPayload = null;
		if (oqlctwContentType != null) {
			final long tsLastModified = oLastModified == null ? ArgonClock.tsNow() : oLastModified.getTime();
			oPayload = new BerylliumBinaryHttpPayload(oqlctwContentType, content, tsLastModified);
		}

		if (isLiveNet()) {
			liveNet("CLIENT RECEIVED (COMMAND) OK");
		}
		raiseCompleteCommandResponse(oPayload);
	}

	private void raiseCompleteCommandResponse(BerylliumBinaryHttpPayload oPayload) {
		final IBerylliumHttpTracker oTracker = getTracker();
		if (oTracker == null) return;
		if (!(oTracker instanceof IBerylliumCommandHttpTracker)) {
			final Ds ds = Ds.invalidBecause("Expected Command Tracker", CsqNoResponseNotify);
			ds.a("trackerClass", oTracker.getClass().getName());
			warnNet(adest(ds));
			return;
		}
		final IBerylliumCommandHttpTracker tracker = (IBerylliumCommandHttpTracker) oTracker;
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

	private BerylliumCommandHttpExchange() {
		super(null);
	}
}
