/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.ObjectName;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonClock;
import com.metservice.argon.Ds;
import com.metservice.argon.management.ArgonProbeFilter;

/**
 * @author roach
 */
class SpaceProbe implements ISpaceProbe {

	private static final int MaskJmx = 0x4;
	private static final int MaskLogger = 0x2;
	private static final int MaskConsole = 0x1;

	static final String TypeFail = "fail";
	static final String TypeWarn = "warn";
	static final String TypeInfo = "info";
	static final String TypeLive = "live";
	static final String[] Types = { TypeFail, TypeWarn, TypeInfo, TypeLive };

	static final String NotificationDescription = "A space event satisfying the filter has occurred";

	static final MBeanNotificationInfo NotificationInfo = new MBeanNotificationInfo(Types, Notification.class.getName(),
			NotificationDescription);

	static final MBeanNotificationInfo[] NotificationInfoValues = { NotificationInfo };

	private void dsa(Ds ds, Throwable ocause) {
		ds.a("cause", ocause);
	}

	private int maskFail(String k) {
		int mask = 0;
		if (m_filterJmx.fail(k)) {
			mask |= MaskJmx;
		}
		if (m_oLogger != null && m_filterLog.fail(k)) {
			mask |= MaskLogger;
		}
		if (m_filterCon.fail(k)) {
			mask |= MaskConsole;
		}
		return mask;
	}

	private int maskInfo(String k) {
		int mask = 0;
		if (m_filterJmx.info(k)) {
			mask |= MaskJmx;
		}
		if (m_oLogger != null && m_filterLog.info(k)) {
			mask |= MaskLogger;
		}
		if (m_filterCon.info(k)) {
			mask |= MaskConsole;
		}
		return mask;
	}

	private int maskLive(String k) {
		int mask = 0;
		if (m_filterJmx.live(k)) {
			mask |= MaskJmx;
		}
		if (m_oLogger != null && m_filterLog.live(k)) {
			mask |= MaskLogger;
		}
		if (m_filterCon.live(k)) {
			mask |= MaskConsole;
		}
		return mask;
	}

	private int maskWarn(String k) {
		int mask = 0;
		if (m_filterJmx.warn(k)) {
			mask |= MaskJmx;
		}
		if (m_oLogger != null && m_filterLog.warn(k)) {
			mask |= MaskLogger;
		}
		if (m_filterCon.warn(k)) {
			mask |= MaskConsole;
		}
		return mask;
	}

	private String scriptKeyword(String sub, String qccSourcePath) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Script:");
		sb.append(sub);
		sb.append(':');
		sb.append(qccSourcePath);
		return sb.toString();
	}

	private void sendConsole(long ts, String type, String keyword, String zmessage) {
		final String fmsg = m_formatter.console(ts, type, keyword, zmessage);
		if (type.equals(TypeFail)) {
			System.err.println(fmsg);
		} else {
			System.out.println(fmsg);
		}
	}

	private void sendJmx(long ts, String type, String keyword, String zmessage) {
		final long sequenceNumber = m_jmxSequenceNumber.getAndIncrement();
		final String fmsg = m_formatter.jmx(ts, type, keyword, zmessage);
		m_space.sendNotification(new Notification(type, m_sourceObjectName, sequenceNumber, ts, fmsg));
	}

	private void sendLogger(long ts, String type, String keyword, String zmessage) {
		if (m_oLogger == null) return;
		final String fmsg = m_formatter.logger(ts, type, keyword, zmessage);
		if (type.equals(TypeLive)) {
			m_oLogger.live(fmsg);
		} else if (type.equals(TypeInfo)) {
			m_oLogger.information(fmsg);
		} else if (type.equals(TypeWarn)) {
			m_oLogger.warning(fmsg);
		} else if (type.equals(TypeFail)) {
			m_oLogger.failure(fmsg);
		}
	}

	private void show(String type, String keyword, int mask, Ds ods) {
		final String zmessage = ods == null ? "" : ods.s();
		show(type, keyword, mask, zmessage);
	}

	private void show(String type, String keyword, int mask, String zmessage) {
		final long ts = ArgonClock.tsNow();

		if ((mask & MaskJmx) != 0) {
			sendJmx(ts, type, keyword, zmessage);
		}
		if ((mask & MaskLogger) != 0) {
			sendLogger(ts, type, keyword, zmessage);
		}
		if ((mask & MaskConsole) != 0) {
			sendConsole(ts, type, keyword, zmessage);
		}
	}

	String alterFilterConsole(String ozFilterPattern) {
		try {
			m_filterCon = new ArgonProbeFilter(ozFilterPattern);
			return "done";
		} catch (final ArgonApiException exL) {
			return exL.getMessage();
		}
	}

	String alterFilterJmx(String ozFilterPattern) {
		try {
			m_filterJmx = new ArgonProbeFilter(ozFilterPattern);
			return "done";
		} catch (final ArgonApiException exL) {
			return exL.getMessage();
		}
	}

	String alterFilterLog(String ozFilterPattern) {
		try {
			m_filterLog = new ArgonProbeFilter(ozFilterPattern);
			return "done";
		} catch (final ArgonApiException exL) {
			return exL.getMessage();
		}
	}

	String getConfigInfo() {
		return m_cfg.toString();
	}

	String getFilterPatternConsole() {
		return m_filterCon.toString();
	}

	String getFilterPatternJmx() {
		return m_filterJmx.toString();
	}

	String getFilterPatternLog() {
		return m_filterLog.toString();
	}

	void restoreFilterConsole() {
		m_filterCon = m_cfg.filterCon();
	}

	void restoreFilterJmx() {
		m_filterJmx = m_cfg.filterJmx();
	}

	void restoreFilterLog() {
		m_filterLog = m_cfg.filterLog();
	}

	@Override
	public void failFile(Ds diagnostic, File ofile) {
		final String k = "File";
		final int m = maskFail(k);
		if (m != 0) {
			final Ds ds = Ds.o(k);
			ds.a("diagnostic", diagnostic);
			ds.ausername();
			ds.afileinfo("file", ofile);
			show(TypeFail, k, m, ds);
		}
	}

	@Override
	public void failNet(Ds diagnostic) {
		final String k = "Net";
		final int m = maskFail(k);
		if (m != 0) {
			show(TypeFail, k, m, diagnostic);
		}
	}

	@Override
	public void failScriptCompile(String qccSourcePath, String diagnostic) {
		final String k = scriptKeyword("Compile", qccSourcePath);
		final int m = maskFail(k);
		if (m != 0) {
			show(TypeFail, k, m, diagnostic);
		}
	}

	@Override
	public void failScriptEmit(String qccSourcePath, String diagnostic) {
		final String k = scriptKeyword("Emit", qccSourcePath);
		final int m = maskFail(k);
		if (m != 0) {
			show(TypeFail, k, m, diagnostic);
		}
	}

	@Override
	public void failScriptLoad(String qccSourcePath, String diagnostic) {
		final String k = scriptKeyword("Load", qccSourcePath);
		final int m = maskFail(k);
		if (m != 0) {
			show(TypeFail, k, m, diagnostic);
		}
	}

	@Override
	public void failScriptRun(String qccSourcePath, String diagnostic) {
		final String k = scriptKeyword("Run", qccSourcePath);
		final int m = maskFail(k);
		if (m != 0) {
			show(TypeFail, k, m, diagnostic);
		}
	}

	@Override
	public void failSoftware(Ds diagnostic) {
		final String k = "Software";
		final int m = maskFail(k);
		if (m != 0) {
			show(TypeFail, k, m, diagnostic);
		}
	}

	@Override
	public void failSoftware(RuntimeException exRT) {
		final String k = "Software";
		final int m = maskFail(k);
		if (m != 0) {
			final Ds ds = Ds.o(k);
			dsa(ds, exRT);
			show(TypeFail, k, m, ds);
		}
	}

	@Override
	public void infoNet(Ds diagnostic) {
		final String k = "Net";
		final int m = maskInfo(k);
		if (m != 0) {
			show(TypeInfo, k, m, diagnostic);
		}
	}

	@Override
	public void infoShell(String diagnostic) {
		final String k = "Shell";
		final int m = maskInfo(k);
		if (m != 0) {
			show(TypeInfo, k, m, diagnostic);
		}
	}

	@Override
	public void liveScriptEmit(String qccSourcePath, String diagnostic) {
		final String k = scriptKeyword("Emit", qccSourcePath);
		final int m = maskLive(k);
		if (m != 0) {
			show(TypeLive, k, m, diagnostic);
		}
	}

	@Override
	public void warnFile(Ds diagnostic, File ofile) {
		final String k = "File";
		final int m = maskWarn(k);
		if (m != 0) {
			final Ds ds = Ds.o(k);
			ds.a("diagnostic", diagnostic);
			ds.ausername();
			ds.afileinfo("file", ofile);
			show(TypeWarn, k, m, ds);
		}
	}

	@Override
	public void warnNet(Ds diagnostic) {
		final String k = "Net";
		final int m = maskWarn(k);
		if (m != 0) {
			show(TypeWarn, k, m, diagnostic);
		}
	}

	@Override
	public void warnSoftware(Ds diagnostic) {
		final String k = "Software";
		final int m = maskWarn(k);
		if (m != 0) {
			show(TypeWarn, k, m, diagnostic);
		}
	}

	public SpaceProbe(NeonSpace space, NeonSpaceId id, NeonSpaceCfg cfg, INeonProbeControl probeControl) {
		if (space == null) throw new IllegalArgumentException("object is null");
		if (id == null) throw new IllegalArgumentException("object is null");
		if (cfg == null) throw new IllegalArgumentException("object is null");
		if (probeControl == null) throw new IllegalArgumentException("object is null");
		m_space = space;
		m_cfg = cfg;
		final INeonProbeFormatter vFormatter = probeControl.newFormatter(id, cfg);
		if (vFormatter == null) throw new IllegalStateException("expecting a non-null formatter");
		m_formatter = vFormatter;
		m_oLogger = probeControl.createLogger(id, cfg);
		m_sourceObjectName = id.spaceObjectName();
		m_filterJmx = cfg.filterJmx();
		m_filterLog = cfg.filterLog();
		m_filterCon = cfg.filterCon();
	}
	private final NeonSpace m_space;
	private final NeonSpaceCfg m_cfg;
	private final INeonProbeFormatter m_formatter;
	private final INeonLogger m_oLogger;
	private final ObjectName m_sourceObjectName;

	private final AtomicLong m_jmxSequenceNumber = new AtomicLong(0L);
	private volatile ArgonProbeFilter m_filterJmx;
	private volatile ArgonProbeFilter m_filterLog;
	private volatile ArgonProbeFilter m_filterCon;
}
