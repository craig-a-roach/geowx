/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon;

import com.metservice.argon.Ds;
import com.metservice.argon.text.ArgonNumber;
import com.metservice.beryllium.IBerylliumSmtpProbe;

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

	@Override
	public void failSmtp(Ds diagnostic) {
		if (m_sysout) {
			System.out.println(qts() + " failSmtp[" + diagnostic.ss() + "]");
		}
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

	@Override
	public void warnSmtp(Ds diagnostic) {
		if (m_sysout) {
			System.out.println(qts() + " warnSmtp[" + diagnostic.ss() + "]");
		}
	}

	public TestImpSmtpProbe(boolean sysout, boolean showLive) {
		m_sysout = sysout;
		m_showLive = showLive;
		m_tsBase = System.currentTimeMillis();
	}
	private final boolean m_sysout;
	private final boolean m_showLive;
	private final long m_tsBase;
}
