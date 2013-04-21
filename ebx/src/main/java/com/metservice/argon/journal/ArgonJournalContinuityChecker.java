/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.journal;

import java.util.concurrent.atomic.AtomicReference;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class ArgonJournalContinuityChecker {

	private static final String Csq = "Recovery of state will be incomplete";

	public final Ds verified(ArgonJournalIn neo) {
		if (neo == null) throw new IllegalArgumentException("object is null");
		final ArgonJournalIn oPre = m_pre.get();
		final Ds odsVerify = verify(oPre, neo);
		m_pre.set(neo);
		return odsVerify;
	}

	private static Ds verify(ArgonJournalIn oPre, ArgonJournalIn neo) {
		if (oPre == null) return null;
		final long serialNeo = neo.serial();
		final long serialPre = oPre.serial();
		final long serialExp = serialPre + 1L;
		if (serialNeo < serialExp) {
			final Ds ds = Ds.invalidBecause("Journal entries are being read out of order", Csq);
			ds.a("previousEntry", oPre);
			ds.a("thisEntry", neo);
			return ds;
		}
		if (serialNeo > serialExp) {
			final Ds ds = Ds.invalidBecause("Missing one or more event journal entries", Csq);
			ds.a("previousEntry", oPre);
			ds.a("thisEntry", neo);
			return ds;
		}
		return null;
	}

	public ArgonJournalContinuityChecker() {
	}
	private final AtomicReference<ArgonJournalIn> m_pre = new AtomicReference<ArgonJournalIn>();
}
