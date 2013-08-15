/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.metservice.argon.Ds;
import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
class FeedQueue {

	private boolean put(KernelCfg kc, BoronFeedUnit feedUnit, Elapsed oTimeout)
			throws InterruptedException {
		if (feedUnit == null) throw new IllegalArgumentException("object is null");
		IBoronFeed oFeed = feedUnit.getFeed();
		if (oFeed != null) return put(kc, oFeed, oTimeout);
		oFeed = feedUnit.getLineTerminator(m_feedLineTerminator);
		if (oFeed != null) return put(kc, oFeed, oTimeout);
		oFeed = feedUnit.getStreamTerminator(m_feedStreamTerminator);
		if (oFeed != null) return put(kc, oFeed, oTimeout);
		return true;
	}

	private boolean put(KernelCfg kc, IBoronFeed feed, Elapsed oTimeout)
			throws InterruptedException {
		if (kc == null) throw new IllegalArgumentException("object is null");
		if (feed == null) throw new IllegalArgumentException("object is null");
		boolean accepted = false;
		try {
			if (oTimeout == null) {
				m_queue.put(feed);
				accepted = true;
			} else if (oTimeout.sms <= 0L) {
				accepted = m_queue.offer(feed);
			} else {
				accepted = m_queue.offer(feed, oTimeout.sms, TimeUnit.MILLISECONDS);
			}
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo("Put process feeds on queue", ex);
			ds.a("queue", m_queue);
			kc.probe.failSoftware(ds);
		}
		return accepted;
	}

	public int bcBuffer() {
		return m_bcBuffer;
	}

	public List<BoronFeedUnit> put(KernelCfg kc, List<BoronFeedUnit> zlFeeds, Elapsed oTimeout)
			throws InterruptedException {
		if (zlFeeds == null) throw new IllegalArgumentException("object is null");

		List<BoronFeedUnit> lzyRejects = null;
		final int feedCount = zlFeeds.size();
		for (int i = 0; i < feedCount; i++) {
			final BoronFeedUnit feedUnit = zlFeeds.get(i);
			if (lzyRejects == null) {
				if (!put(kc, feedUnit, oTimeout)) {
					lzyRejects = new ArrayList<BoronFeedUnit>(feedCount);
				}
			}
			if (lzyRejects != null) {
				lzyRejects.add(feedUnit);
			}
		}

		if (lzyRejects == null) return Collections.emptyList();
		return lzyRejects;
	}

	public Charset stdEncoding() {
		return m_stdEncoding;
	}

	public IBoronFeed take()
			throws InterruptedException {
		return m_queue.take();
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("bcBuffer", m_bcBuffer);
		ds.a("stdEncoding", m_stdEncoding);
		ds.a("queue", m_queue);
		return ds.s();
	}

	public FeedQueue(IBoronScript script) {
		if (script == null) throw new IllegalArgumentException("object is null");
		m_stdEncoding = script.stdioEncoding();
		m_bcBuffer = script.bcBufferStdIn();
		m_queue = new ArrayBlockingQueue<IBoronFeed>(Math.max(1, script.maxFeedQueueDepth()));
		m_feedLineTerminator = new FeedLineTerminator(script);
		m_feedStreamTerminator = new FeedStreamTerminator();
	}

	private final int m_bcBuffer;
	private final Charset m_stdEncoding;
	private final BlockingQueue<IBoronFeed> m_queue;
	private final FeedLineTerminator m_feedLineTerminator;
	private final FeedStreamTerminator m_feedStreamTerminator;

	private static class FeedLineTerminator implements IBoronFeedByte {

		@Override
		public boolean isTerminal() {
			return false;
		}

		@Override
		public String toString() {
			return "LineTerminator";
		}

		@Override
		public byte[] zptPayloadBytes() {
			return m_stdLineTerminator;
		}

		public FeedLineTerminator(IBoronScript script) {
			assert script != null;
			m_stdLineTerminator = script.stdioLineTerminator();
		}
		private final byte[] m_stdLineTerminator;
	}

	private static class FeedStreamTerminator implements IBoronFeedByte {

		@Override
		public boolean isTerminal() {
			return true;
		}

		@Override
		public String toString() {
			return "StreamTerminator";
		}

		@Override
		public byte[] zptPayloadBytes() {
			return m_stdStreamTerminator;
		}

		public FeedStreamTerminator() {
			m_stdStreamTerminator = new byte[0];
		}
		private final byte[] m_stdStreamTerminator;
	}
}
