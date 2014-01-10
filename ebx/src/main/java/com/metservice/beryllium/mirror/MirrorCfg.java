/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonSpaceId;

class MirrorCfg {

	@Override
	public String toString() {
		final Ds ds = Ds.o("BerylliumMirror.Cfg");
		ds.a("serviceId", serviceId);
		ds.a("peerAddress", peerAddress);
		ds.a("spaceId", spaceId);
		ds.ae("discoverInterval", discoverIntervalMs);
		ds.ae("connectTimeout", connectTimeoutMs);
		ds.ae("timeout", timeoutMs);
		ds.ae("minRetryInterval", minRetryIntervalMs);
		ds.a("requestBufferSize", requestBufferSize);
		ds.a("responseBufferSize", responseBufferSize);
		ds.a("payloadQuota", payloadQuotaBc);
		return ds.s();
	}

	public MirrorCfg(BerylliumMirror.Config src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		this.serviceId = src.serviceId;
		this.listenPort = src.listenPort;
		this.peerAddress = new PeerAddress(src.qnctwPeerHost, src.peerPort);
		this.spaceId = new SpaceId(src.listenPort);
		this.discoverIntervalMs = Math.max(0, src.discoverIntervalMs);
		this.connectTimeoutMs = Math.max(0, src.connectTimeoutMs);
		this.timeoutMs = Math.max(0, src.timeoutMs);
		this.minRetryIntervalMs = Math.max(0, src.minRetryIntervalMs);
		this.requestBufferSize = Math.max(1024, src.requestBufferSize);
		this.responseBufferSize = Math.max(1024, src.responseBufferSize);
		this.payloadQuotaBc = Math.max(1024, src.payloadQuotaBc);
	}

	public final ArgonServiceId serviceId;
	public final int listenPort;
	public final PeerAddress peerAddress;
	public final IArgonSpaceId spaceId;
	public final int discoverIntervalMs;
	public final int connectTimeoutMs;
	public final int timeoutMs;
	public final int minRetryIntervalMs;
	public final int requestBufferSize;
	public final int responseBufferSize;
	public final int payloadQuotaBc;
}