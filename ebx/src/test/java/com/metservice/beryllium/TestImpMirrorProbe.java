/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.Ds;
import com.metservice.argon.text.ArgonNumber;
import com.metservice.beryllium.mirror.IBerylliumMirrorProbe;

/**
 * @author roach
 */
public class TestImpMirrorProbe implements IBerylliumMirrorProbe {

	private String qtsid() {
		return ArgonNumber.longToDec(ts(), 6) + " " + id;
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
	public void failFile(Ds diagnostic, File ofile) {
		if (m_sysout.get()) {
			System.out.println(qtsid() + " failFile[" + diagnostic.ss() + "]");
		}
		addFail("File", diagnostic.s());
	}

	@Override
	public void failNet(Ds diagnostic) {
		if (m_sysout.get()) {
			if (m_shutdown.get()) {
				System.out.println(qtsid() + " failNet[shutdown]");
			} else {
				System.out.println(qtsid() + " failNet[" + diagnostic.ss() + "]");
			}
		}
		addFail("Net", diagnostic.s());
	}

	@Override
	public void failSoftware(Ds diagnostic) {
		if (m_sysout.get()) {
			System.out.println(qtsid() + " failSoftware[" + diagnostic.ss() + "]");
		}
		addFail("Software", diagnostic.s());
	}

	@Override
	public void failSoftware(RuntimeException exRT) {
		final String diagnostic = Ds.format(exRT);
		if (m_sysout.get()) {
			System.out.println(qtsid() + " failSoftware[" + diagnostic + "]");
		}
		addFail("Software", diagnostic);
	}

	@Override
	public void infoNet(String message) {
		System.out.println(qtsid() + " infoNet[" + message + "]");
	}

	@Override
	public boolean isLiveMirror() {
		return (m_sysout.get() && m_showLiveMirror);
	}

	@Override
	public boolean isLiveNet() {
		return (m_sysout.get() && m_showLiveNet);
	}

	@Override
	public void liveMirror(String message, Object... args) {
		if (isLiveMirror()) {
			final StringBuilder sb = new StringBuilder();
			sb.append(message);
			for (int i = 0; i < args.length; i++) {
				sb.append(' ');
				sb.append(args[i]);
			}
			System.out.println(qtsid() + " liveMirror[" + sb + "]");
		}
	}

	@Override
	public void liveNet(String message, Object... args) {
		if (isLiveNet()) {
			final StringBuilder sb = new StringBuilder();
			sb.append(message);
			for (int i = 0; i < args.length; i++) {
				sb.append(' ');
				sb.append(args[i]);
			}
			System.out.println(qtsid() + " liveNet[" + sb + "]");
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

	public void notifyShutdown() {
		m_shutdown.set(true);
		if (m_sysout.get()) {
			System.out.println("Shutting down " + id);
		}
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
		if (m_sysout.get()) {
			System.out.println(qtsid() + " warnFile[" + diagnostic.ss() + "]");
		}
		addWarn("File", diagnostic.s());
	}

	@Override
	public void warnMirror(String diagnostic) {
		if (m_sysout.get()) {
			System.out.println(qtsid() + " warnMirror[" + diagnostic + "]");
		}
		addWarn("Mirror", diagnostic);
	}

	@Override
	public void warnNet(Ds diagnostic) {
		if (m_sysout.get()) {
			if (m_shutdown.get()) {
				System.out.println(qtsid() + " warnNet[shutdown]");
			} else {
				System.out.println(qtsid() + " warnNet[" + diagnostic.ss() + "]");
			}
		}
		addWarn("Net", diagnostic.s());
	}

	public TestImpMirrorProbe(String id, boolean sysout, boolean showLiveNet, boolean showLiveMirror) {
		this.id = id;
		m_sysout = new AtomicBoolean(sysout);
		m_showLiveNet = showLiveNet;
		m_showLiveMirror = showLiveMirror;
		m_shutdown = new AtomicBoolean();
		m_tsBase = System.currentTimeMillis();
	}
	public final String id;
	private final AtomicBoolean m_sysout;
	private final boolean m_showLiveNet;
	private final boolean m_showLiveMirror;
	private final AtomicBoolean m_shutdown;
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
