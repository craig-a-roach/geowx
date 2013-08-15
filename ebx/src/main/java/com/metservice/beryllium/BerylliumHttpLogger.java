/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.PrintStream;

import org.eclipse.jetty.util.DateCache;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.management.ArgonRecordType;
import com.metservice.argon.management.ArgonRoller;
import com.metservice.argon.management.IArgonSpaceId;

/**
 * @author roach
 */
public class BerylliumHttpLogger implements Logger {

	private static final String DateFormat = "yyyy-MM-dd zzz HH:mm:ss";
	private static final ArgonRecordType RecType = new ArgonRecordType("jetty");
	private static final String DefaultMsg = "none";
	private static final String Braces = "{}";

	private String format(String msgIn, Object... args) {
		final String msg = msgIn == null ? "" : msgIn;
		final StringBuilder builder = new StringBuilder();
		int start = 0;
		for (final Object arg : args) {
			final int bracesIndex = msg.indexOf(Braces, start);
			if (bracesIndex < 0) {
				builder.append(msg.substring(start));
				builder.append(" ");
				builder.append(arg);
				start = msg.length();
			} else {
				builder.append(msg.substring(start, bracesIndex));
				builder.append(String.valueOf(arg));
				start = bracesIndex + Braces.length();
			}
		}
		builder.append(msg.substring(start));
		return builder.toString();
	}

	private String ts() {
		final String d = m_dateCache.now();
		final int ms = m_dateCache.lastMs();
		return d + (ms > 99 ? "." : (ms > 0 ? ".0" : ".00")) + ms;
	}

	@Override
	public void debug(String msg, Object... args) {
		if (m_enableDebug) {
			m_ps.println(ts() + ":DBUG:" + format(msg, args));
		}
	}

	@Override
	public void debug(String msg, Throwable thrown) {
		if (m_enableDebug) {
			m_ps.println(ts() + ":DBUG:" + msg);
			thrown.printStackTrace(m_ps);
		}
	}

	@Override
	public void debug(Throwable thrown) {
		debug(DefaultMsg, thrown);
	}

	@Override
	public Logger getLogger(String name) {
		return this;
	}

	@Override
	public String getName() {
		return m_loggerName;
	}

	@Override
	public void ignore(Throwable ignored) {
	}

	@Override
	public void info(String msg, Object... args) {
		m_ps.println(ts() + ":INFO:" + format(msg, args));
	}

	@Override
	public void info(String msg, Throwable thrown) {
		m_ps.println(ts() + ":INFO:" + msg);
		thrown.printStackTrace(m_ps);
	}

	@Override
	public void info(Throwable thrown) {
		info(DefaultMsg, thrown);
	}

	@Override
	public boolean isDebugEnabled() {
		return m_enableDebug;
	}

	@Override
	public void setDebugEnabled(boolean enabled) {
		m_enableDebug = enabled;
	}

	@Override
	public void warn(String msg, Object... args) {
		m_ps.println(ts() + ":WARN:" + format(msg, args));
	}

	@Override
	public void warn(String msg, Throwable thrown) {
		m_ps.println(ts() + ":WARN:" + msg);
		thrown.printStackTrace(m_ps);
	}

	@Override
	public void warn(Throwable thrown) {
		warn(DefaultMsg, thrown);
	}

	public static BerylliumHttpLogger createInstance(ArgonServiceId sid, IArgonSpaceId idSpace, boolean enableDebug) {
		if (sid == null) throw new IllegalArgumentException("object is null");
		if (idSpace == null) throw new IllegalArgumentException("object is null");
		try {
			final String loggerName = sid.qtwDomain + "." + idSpace.format();
			final PrintStream ps = ArgonRoller.printStream(sid, RecType, idSpace);
			return new BerylliumHttpLogger(loggerName, enableDebug, ps);
		} catch (final ArgonPermissionException ex) {
			System.err.println("Cannot enable rolling log; using default (" + ex.getMessage() + ")");
		}
		return null;
	}

	public static BerylliumHttpLogger install(ArgonServiceId sid, IArgonSpaceId idSpace, boolean enableDebug) {
		final BerylliumHttpLogger oLogger = createInstance(sid, idSpace, enableDebug);
		if (oLogger != null) {
			Log.setLog(oLogger);
		}
		return oLogger;
	}

	private BerylliumHttpLogger(String loggerName, boolean enableDebug, PrintStream ps) {
		assert loggerName != null && loggerName.length() > 0;
		assert ps != null;
		m_loggerName = loggerName;
		m_enableDebug = enableDebug;
		m_ps = ps;
	}

	private final String m_loggerName;
	private final PrintStream m_ps;
	private final DateCache m_dateCache = new DateCache(DateFormat);
	private volatile boolean m_enableDebug;
}