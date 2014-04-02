/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.nickel;

import java.util.List;

import com.metservice.argon.DateFormatter;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class NickelProbeErr {

	private boolean admit(String qcctwSubject) {
		return m_oLimiter == null || m_oLimiter.admit(qcctwSubject);
	};

	private String now() {
		return DateFormatter.newT8PlatformDHMFromTs(System.currentTimeMillis());
	}

	private String qtwSubject(String oz) {
		return oz == null || oz.length() == 0 ? "Error" : oz;
	}

	private String zContainment(String oz) {
		return oz == null || oz.length() == 0 ? "" : (" " + oz);
	}

	private String zOperator(Ds ods) {
		return ods == null ? "" : (" " + ods.ss());
	};

	public void emit(String subject, String ozContainment, Ds oOperator) {
		emit(subject, ozContainment, oOperator, null);
	}

	public void emit(String subject, String ozContainment, Ds oOperator, String ozDeveloper) {
		final String qtwSubject = qtwSubject(subject);
		if (!admit(qtwSubject)) return;
		final String zcn = zContainment(ozContainment);
		final String zop = zOperator(oOperator);
		System.err.println(now() + " " + qtwSubject + zcn + zop + " " + m_source + "...");
		if (ozDeveloper != null && ozDeveloper.length() > 0) {
			System.err.println(ozDeveloper);
		}
		System.err.println("..." + qtwSubject + " " + m_source);
	}

	public void emitElided() {
		if (m_oLimiter == null) return;
		final List<String> zlSubjectsAsc = m_oLimiter.zlElidedSubjectsAsc();
		if (zlSubjectsAsc.isEmpty()) return;
		System.err.println("Elided reports...");
		for (final String subject : zlSubjectsAsc) {
			System.err.println(subject + " +" + m_oLimiter.count(subject, true) + " more");
		}
	}

	public NickelProbeErr(String source, NickelProbeLimiter oLimiter) {
		m_source = source;
		m_oLimiter = oLimiter;
	}

	private final String m_source;
	private final NickelProbeLimiter m_oLimiter;

}
