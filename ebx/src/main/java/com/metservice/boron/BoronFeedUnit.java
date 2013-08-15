/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class BoronFeedUnit {

	public static final BoronFeedUnit FeedUnitLineTerminator = new BoronFeedUnit(Control.LineTerminator);

	public static final BoronFeedUnit FeedUnitStreamTerminator = new BoronFeedUnit(Control.StreamTerminator);

	public static BoronFeedUnit newByteInstance(byte[] zptValue) {
		if (zptValue == null) throw new IllegalArgumentException("object is null");
		return new BoronFeedUnit(new FeedByte(zptValue));
	}

	public static BoronFeedUnit newInstance(IBoronFeed feed) {
		if (feed == null) throw new IllegalArgumentException("object is null");
		return new BoronFeedUnit(feed);
	}

	public static BoronFeedUnit newStringInstance(String zValue) {
		if (zValue == null) throw new IllegalArgumentException("object is null");
		return new BoronFeedUnit(new FeedString(zValue));
	}

	public IBoronFeed getFeed() {
		return m_oControl != null ? null : m_oFeed;
	}

	public IBoronFeed getLineTerminator(IBoronFeed stdFeed) {
		if (stdFeed == null) throw new IllegalArgumentException("object is null");
		return m_oControl == null || m_oControl != Control.LineTerminator ? null : stdFeed;
	}

	public IBoronFeed getStreamTerminator(IBoronFeed stdFeed) {
		if (stdFeed == null) throw new IllegalArgumentException("object is null");
		return m_oControl == null || m_oControl != Control.StreamTerminator ? null : stdFeed;
	}

	@Override
	public String toString() {
		if (m_oFeed != null) return m_oFeed.toString();
		if (m_oControl != null) return m_oControl.toString();
		return "";
	}

	private BoronFeedUnit(Control control) {
		m_oFeed = null;
		m_oControl = control;
	}

	private BoronFeedUnit(IBoronFeed feed) {
		m_oFeed = feed;
		m_oControl = null;
	}

	private final IBoronFeed m_oFeed;
	private final Control m_oControl;

	private static enum Control {
		LineTerminator, StreamTerminator;
	}

	private static class FeedByte implements IBoronFeedByte {

		@Override
		public boolean isTerminal() {
			return false;
		}

		@Override
		public String toString() {
			return Ds.dump(zptPayload);
		}

		@Override
		public byte[] zptPayloadBytes() {
			return zptPayload;
		}

		public FeedByte(byte[] zptPayload) {
			this.zptPayload = zptPayload;
		}
		final byte[] zptPayload;
	}

	private static class FeedString implements IBoronFeedString {

		@Override
		public boolean isTerminal() {
			return false;
		}

		@Override
		public String toString() {
			return zPayload;
		}

		@Override
		public String zPayloadString() {
			return zPayload;
		}

		public FeedString(String zPayload) {
			this.zPayload = zPayload;
		}
		final String zPayload;
	}
}
