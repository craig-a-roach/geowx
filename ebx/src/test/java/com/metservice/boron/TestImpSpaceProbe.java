/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class TestImpSpaceProbe implements ISpaceProbe {

	private void addFail(String tag, String zargs) {
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

	private void addWarn(String tag, String zargs) {
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

	private int ts() {
		return (int) (System.currentTimeMillis() - m_tsBase);
	}

	public int countFail() {
		return m_zlFails.size();
	}

	public int countWarn() {
		return m_zlWarns.size();
	}

	@Override
	public void failFile(Ds diagnostic, File ofile) {
		addFail("File", diagnostic.s());
	}

	@Override
	public void failSoftware(Ds diagnostic) {
		addFail("Software", diagnostic.s());
	}

	@Override
	public void failSoftware(RuntimeException exRT) {
		exRT.printStackTrace();
		addFail("Software", exRT.toString());
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
	public void warnFile(Ds diagnostic, File ofile) {
		addWarn("File", diagnostic.s());
	}

	@Override
	public void warnSoftware(Ds diagnostic) {
		addWarn("Software", diagnostic.s());
	}

	public TestImpSpaceProbe() {
		m_tsBase = System.currentTimeMillis();
	}

	private final long m_tsBase;
	private final Lock m_lock = new ReentrantLock();
	private final List<Event> m_zlEvents = new ArrayList<Event>();
	private final List<Event> m_zlFails = new ArrayList<Event>();
	private final List<Event> m_zlWarns = new ArrayList<Event>();

	private static class Event {

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
