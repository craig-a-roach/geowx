/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.concurrent.atomic.AtomicBoolean;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class RunnableIn extends ARunnable {

	private static final String TryWrite = "Write input to process";

	private void flush()
			throws IOException {
		m_out.flush();
	}

	private void write(ByteBuffer bbuff)
			throws IOException, InterruptedException {
		if (m_cancelled.get() || Thread.interrupted()) throw new InterruptedException();
		bbuff.flip();
		while (bbuff.hasRemaining()) {
			m_out.write(bbuff.get());
		}
	}

	private void write(ByteBuffer bbuff, byte[] zptPayload)
			throws IOException, InterruptedException {
		assert bbuff != null;
		assert zptPayload != null;
		final int blen = zptPayload.length;
		if (blen == 0) return;
		final int bxfercap = bbuff.capacity();
		int bstart = 0;
		while (bstart < blen) {
			final int brem = blen - bstart;
			final int bxfer = Math.min(bxfercap, brem);
			bbuff.clear();
			bbuff.put(zptPayload, bstart, bxfer);
			write(bbuff);
			bstart += bxfer;
		}
	}

	private void write(CharBuffer cbuff, CharsetEncoder charsetEncoder, ByteBuffer bbuff, String zPayload)
			throws IOException, InterruptedException {
		assert cbuff != null;
		assert charsetEncoder != null;
		assert bbuff != null;
		assert zPayload != null;
		final int clen = zPayload.length();
		if (clen == 0) return;
		final int cxfercap = cbuff.capacity();
		int cstart = 0;
		while (cstart < clen) {
			final int crem = clen - cstart;
			final int cxfer = Math.min(cxfercap, crem);
			final int cend = cstart + cxfer;
			cbuff.clear();
			cbuff.put(zPayload, cstart, cend);
			cstart = cend;
			final boolean endOfInput = cend == clen;
			boolean moreEncode = true;
			while (moreEncode) {
				cbuff.flip();
				bbuff.clear();
				final CoderResult coderResult = charsetEncoder.encode(cbuff, bbuff, endOfInput);
				if (endOfInput) {
					charsetEncoder.reset();
				}
				write(bbuff);
				if (coderResult.isUnderflow()) {
					moreEncode = false;
				} else {
					cbuff.compact();
				}
			}
		}
	}

	@Override
	protected void doWork() {
		final int bcBuffer = m_feedQueue.bcBuffer();
		final ByteBuffer bbuff = ByteBuffer.allocate(bcBuffer);
		final CharsetEncoder charsetEncoder = m_feedQueue.stdEncoding().newEncoder();
		final float avgBytesPerChar = Math.max(1.0f, charsetEncoder.averageBytesPerChar());
		final int ccBuffer = Math.max(1, (int) (bcBuffer / avgBytesPerChar));
		final CharBuffer cbuff = CharBuffer.allocate(ccBuffer);

		charsetEncoder.onMalformedInput(CodingErrorAction.REPLACE);
		charsetEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		charsetEncoder.replaceWith(CBoron.CharsetEncodeErrorReplacement7);
		try {
			boolean more = true;
			while (more) {
				final IBoronFeed feed = m_feedQueue.take();
				if (feed instanceof IBoronFeedString) {
					final IBoronFeedString stringFeed = (IBoronFeedString) feed;
					final String zPayload = stringFeed.zPayloadString();
					write(cbuff, charsetEncoder, bbuff, zPayload);
				} else if (feed instanceof IBoronFeedByte) {
					final IBoronFeedByte byteFeed = (IBoronFeedByte) feed;
					final byte[] zptPayload = byteFeed.zptPayloadBytes();
					write(bbuff, zptPayload);
				}
				flush();
				more = !feed.isTerminal();
			}
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo(TryWrite, ex);
			ds.a("cbuff", cbuff).a("bbuff", bbuff);
			ds.a("feedQueue", m_feedQueue);
			kc.probe.failSoftware(ds);
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo(TryWrite, ex);
			ds.a("cbuff", cbuff).a("bbuff", bbuff);
			ds.a("feedQueue", m_feedQueue);
			kc.probe.failSoftware(ds);
		} catch (final InterruptedException ex) {
		} finally {
			try {
				m_out.close();
			} catch (final IOException exIO) {
			}
		}
	}

	public void cancel() {
		m_cancelled.set(true);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("cancelled", m_cancelled);
		return ds.s();
	}

	public RunnableIn(KernelCfg kc, OutputStream out, FeedQueue feedQueue) {
		super(kc);
		assert out != null;
		assert feedQueue != null;
		m_out = out;
		m_feedQueue = feedQueue;
		m_cancelled = new AtomicBoolean(false);
	}

	private final OutputStream m_out;
	private final FeedQueue m_feedQueue;
	private final AtomicBoolean m_cancelled;
}
