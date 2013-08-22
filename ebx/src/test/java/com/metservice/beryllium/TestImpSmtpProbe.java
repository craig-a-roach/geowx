/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.ArgonNumber;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class TestImpSmtpProbe implements IBerylliumSmtpProbe {

	private String qts() {
		return ArgonNumber.longToDec(ts(), 6);
	}

	private int ts() {
		return (int) (System.currentTimeMillis() - m_tsBase);
	}

	protected void addFail(String tag, String zargs) {
		assert tag != null;
		assert zargs != null;
		final Event e = new Event("fail", tag, zargs, ts());
		m_lock.lock();
		try {
			m_zlEvents.add(e);
			m_zlFails.add(e);
		} finally {
			m_lock.unlock();
		}
	}

	protected void addWarn(String tag, String zargs) {
		assert tag != null;
		assert zargs != null;
		final Event e = new Event("warn", tag, zargs, ts());
		m_lock.lock();
		try {
			m_zlEvents.add(e);
			m_zlWarns.add(e);
		} finally {
			m_lock.unlock();
		}
	}

	public int countFail() {
		return m_zlFails.size();
	}

	public int countWarn() {
		return m_zlWarns.size();
	}

	@Override
	public void failSmtp(Ds diagnostic) {
		if (m_sysout) {
			System.out.println(qts() + " failSmtp[" + diagnostic.ss() + "]");
		}
		addFail("Smtp", diagnostic.s());
	}

	@Override
	public void infoSmtp(String message, Object... args) {
		final StringBuilder sb = new StringBuilder();
		sb.append(message);
		for (int i = 0; i < args.length; i++) {
			sb.append(' ');
			sb.append(args[i]);
		}
		System.out.println(qts() + " infoSmtp[" + sb + "]");
	}

	@Override
	public boolean isLiveSmtp() {
		return (m_sysout && m_showLive);
	}

	@Override
	public void liveSmtp(String message, Object... args) {
		if (isLiveSmtp()) {
			final StringBuilder sb = new StringBuilder();
			sb.append(message);
			for (int i = 0; i < args.length; i++) {
				sb.append(' ');
				sb.append(args[i]);
			}
			System.out.println(qts() + " liveSmtp[" + sb + "]");
		}
	}

	public String matchFail(int index) {
		if (index >= m_zlFails.size()) return "";
		return m_zlFails.get(index).match();
	}

	public String matchFailTag(int index) {
		if (index >= m_zlFails.size()) return "";
		return m_zlFails.get(index).tag;
	}

	public String matchWarn(int index) {
		if (index >= m_zlWarns.size()) return "";
		return m_zlWarns.get(index).match();
	}

	public String matchWarnTag(int index) {
		if (index >= m_zlWarns.size()) return "";
		return m_zlWarns.get(index).tag;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (final Event e : m_zlEvents) {
			sb.append(e).append('\n');
		}
		return sb.toString();
	}

	@Override
	public void warnSmtp(Ds diagnostic) {
		if (m_sysout) {
			System.out.println(qts() + " warnSmtp[" + diagnostic.ss() + "]");
		}
		addWarn("Smtp", diagnostic.s());
	}

	public TestImpSmtpProbe(boolean sysout, boolean showLive) {
		m_sysout = sysout;
		m_showLive = showLive;
		m_tsBase = System.currentTimeMillis();
	}
	private final boolean m_sysout;
	private final boolean m_showLive;
	private final long m_tsBase;
	private final Lock m_lock = new ReentrantLock();
	private final List<Event> m_zlEvents = new ArrayList<Event>();
	private final List<Event> m_zlFails = new ArrayList<Event>();
	private final List<Event> m_zlWarns = new ArrayList<Event>();

	protected static class Event {

		public String match() {
			return tag + "(" + zargs + ")" + type;
		}

		@Override
		public String toString() {
			return tag + "(" + zargs + ")" + type + "@t" + ts;
		}

		Event(String type, String tag, String zargs, int ts) {
			this.type = type;
			this.tag = tag;
			this.zargs = zargs;
			this.ts = ts;
		}

		final String type;
		final String tag;
		final String zargs;
		final int ts;
	}
}
