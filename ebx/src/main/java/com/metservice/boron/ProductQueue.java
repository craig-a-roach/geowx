/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.argon.Ds;
import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
class ProductQueue {

	private boolean allowProduct(OutStreamType type) {
		return type == OutStreamType.StdOut || !m_redirectStdErrToOut;
	}

	private void condExitCodeProduct()
			throws InterruptedException {
		if (!isStdStateEof()) return;
		final BoronExitCode oExitCode = getExitCode();
		if (oExitCode == null) return;
		putInBand(new BoronProductExitCode(oExitCode));
	}

	private BoronExitCode getExitCode() {
		m_rwlockExitCode.readLock().lock();
		try {
			return m_oExitCode;
		} finally {
			m_rwlockExitCode.readLock().unlock();
		}
	}

	private StreamState isStdErrStateLk() {
		return m_mapStreamState.get(OutStreamType.StdErr);
	}

	private StreamState isStdOutStateLk() {
		return m_mapStreamState.get(OutStreamType.StdOut);
	}

	private boolean isStdStateEof() {
		m_rwlockStreamState.readLock().lock();
		try {
			return isStdErrStateLk().eof && isStdOutStateLk().eof;
		} finally {
			m_rwlockStreamState.readLock().unlock();
		}
	}

	private void putInBand(IBoronProduct product)
			throws InterruptedException {
		assert product != null;
		m_queueInBand.put(product);
	}

	private void putOutBand(IBoronProduct product) {
		assert product != null;
		if (!m_queueInBand.offer(product)) {
			m_queueOutBand.offer(product);
		}
	}

	private StreamState stateLk(OutStreamType type) {
		if (type == null) throw new IllegalArgumentException("object is null");
		final StreamState oState = m_mapStreamState.get(type);
		if (oState == null) throw new IllegalArgumentException("invalid type>" + type + "<");
		return oState;
	}

	public int bcBuffer(OutStreamType type) {
		return stateLk(type).bcBuffer;
	}

	public BoronStdioPrompt getStdioPrompt() {
		return m_oStdioPrompt;
	}

	public boolean isDrained() {
		return m_isDrained.get();
	}

	public boolean isHalted() {
		return m_isHalted.get();
	}

	public void putOutStreamCancelled(OutStreamType type) {
		m_rwlockStreamState.writeLock().lock();
		try {
			stateLk(type).cancelled = true;
		} finally {
			m_rwlockStreamState.writeLock().unlock();
		}
		putOutBand(BoronProductCancellation.Instance);
	}

	public void putOutStreamDecodeWarning(OutStreamType type)
			throws InterruptedException {
		m_rwlockStreamState.writeLock().lock();
		try {
			stateLk(type).decodeWarning = true;
		} finally {
			m_rwlockStreamState.writeLock().unlock();
		}
		if (allowProduct(type)) {
			putInBand(new BoronProductStreamWarnDecode(type));
		}
	}

	public void putOutStreamEnd(OutStreamType type)
			throws InterruptedException {
		m_rwlockStreamState.writeLock().lock();
		try {
			stateLk(type).eof = true;
		} finally {
			m_rwlockStreamState.writeLock().unlock();
		}
		if (allowProduct(type)) {
			putInBand(new BoronProductStreamEnd(type));
		}
		condExitCodeProduct();
	}

	public void putOutStreamFail(OutStreamType type) {
		m_rwlockStreamState.writeLock().lock();
		try {
			stateLk(type).failed = true;
		} finally {
			m_rwlockStreamState.writeLock().unlock();
		}
		putOutBand(BoronProductManagementFailure.Instance);
	}

	public void putOutStreamLine(OutStreamType type, boolean isPrompt, String zLine)
			throws InterruptedException {
		if (allowProduct(type)) {
			putInBand(new BoronProductStreamLine(type, isPrompt, zLine));
		}
	}

	public void putProcessCreateCancelled() {
		putOutBand(BoronProductCancellation.Instance);
		m_isHalted.set(true);
	}

	public void putProcessCreateFailed() {
		putOutBand(BoronProductManagementFailure.Instance);
		m_isHalted.set(true);
	}

	public void putProcessCreateInvalid(BoronInterpreterFailure interpreterFailure) {
		putOutBand(new BoronProductInterpreterFailure(interpreterFailure));
		m_isHalted.set(true);
	}

	public void putProcessExit(int exitCode)
			throws InterruptedException {
		m_rwlockExitCode.writeLock().lock();
		try {
			m_oExitCode = new BoronExitCode(exitCode);
		} finally {
			m_rwlockExitCode.writeLock().unlock();
		}
		condExitCodeProduct();
		m_isHalted.set(true);
	}

	public void putProcessExitWaitFailed() {
		putOutBand(BoronProductManagementFailure.Instance);
		m_isHalted.set(true);
	}

	public void putProcessInterrupted() {
		putOutBand(BoronProductCancellation.Instance);
		m_isHalted.set(true);
	}

	public Charset stdEncoding() {
		return m_stdEncoding;
	}

	public IBoronProduct takeProduct(Elapsed oTimeout)
			throws InterruptedException {
		final IBoronProduct oOutBandProduct = m_queueOutBand.poll();
		if (oOutBandProduct != null) {
			m_isDrained.set(true);
			return oOutBandProduct;
		}

		final IBoronProduct oInBandProduct;
		if (oTimeout == null) {
			oInBandProduct = m_queueInBand.take();
		} else if (oTimeout.sms <= 0L) {
			oInBandProduct = m_queueInBand.poll();
		} else {
			oInBandProduct = m_queueInBand.poll(oTimeout.sms, TimeUnit.MILLISECONDS);
		}

		if (oInBandProduct == null) {
			m_isDrained.set(true);
			return BoronProductCancellation.Instance;
		}

		if (oInBandProduct.isTerminal()) {
			m_isDrained.set(true);
		}

		return oInBandProduct;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("isHalted", m_isHalted);
		ds.a("isDrained", m_isDrained);
		ds.a("mapStreamState", m_mapStreamState);
		ds.a("oExitCode", m_oExitCode);
		ds.a("stdEncoding", m_stdEncoding);
		ds.a("redirectStdErrToOut", m_redirectStdErrToOut);
		ds.a("stdioPrompt", m_oStdioPrompt);
		ds.a("queueInBand", m_queueInBand);
		ds.a("queueOutBand", m_queueOutBand);
		return ds.s();
	}

	public ProductQueue(IBoronScript script) {
		if (script == null) throw new IllegalArgumentException("object is null");
		final boolean redirectStdErrToOut = script.redirectStdErrToOut();
		m_stdEncoding = script.stdioEncoding();
		m_redirectStdErrToOut = redirectStdErrToOut;
		m_oStdioPrompt = script.getStdioPrompt();
		m_queueInBand = new ArrayBlockingQueue<IBoronProduct>(Math.max(1, script.maxProductQueueDepth()));
		m_queueOutBand = new ArrayBlockingQueue<IBoronProduct>(1);
		m_isHalted = new AtomicBoolean(false);
		m_isDrained = new AtomicBoolean(false);
		m_mapStreamState.put(OutStreamType.StdErr, new StreamState(script.bcBufferStdErr(), redirectStdErrToOut));
		m_mapStreamState.put(OutStreamType.StdOut, new StreamState(script.bcBufferStdOut(), false));
	}

	private final Charset m_stdEncoding;
	private final boolean m_redirectStdErrToOut;
	private final BoronStdioPrompt m_oStdioPrompt;
	private final BlockingQueue<IBoronProduct> m_queueInBand;
	private final BlockingQueue<IBoronProduct> m_queueOutBand;
	private final AtomicBoolean m_isHalted;

	private final AtomicBoolean m_isDrained;
	private final ReadWriteLock m_rwlockStreamState = new ReentrantReadWriteLock();
	private final Map<OutStreamType, StreamState> m_mapStreamState = new EnumMap<OutStreamType, StreamState>(OutStreamType.class);
	private final ReadWriteLock m_rwlockExitCode = new ReentrantReadWriteLock();

	private BoronExitCode m_oExitCode;

	private static class StreamState {

		@Override
		public String toString() {
			final Ds ds = Ds.o("StreamState");
			ds.a("eof", eof);
			ds.a("decodeWarning", decodeWarning);
			ds.a("cancelled", cancelled);
			ds.a("failed", failed);
			ds.a("bcBuffer", bcBuffer);
			return ds.s();
		}

		StreamState(int bcBuffer, boolean disabled) {
			this.bcBuffer = Math.max(8, bcBuffer);
			this.eof = disabled;
		}

		final int bcBuffer;
		boolean eof;
		boolean decodeWarning;
		boolean cancelled;
		boolean failed;
	}

}
