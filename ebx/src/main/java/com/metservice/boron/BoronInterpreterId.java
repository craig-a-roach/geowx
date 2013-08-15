/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
public class BoronInterpreterId implements Comparable<BoronInterpreterId> {

	public static final BoronInterpreterId IntrinsicBash = new BoronInterpreterId("bash");
	public static final BoronInterpreterId IntrinsicPython = new BoronInterpreterId("python");
	public static final BoronInterpreterId IntrinsicWinCmd = new BoronInterpreterId("WinCmd");

	@Override
	public int compareTo(BoronInterpreterId rhs) {
		return m_qccId.compareTo(rhs.m_qccId);
	}

	public boolean equals(BoronInterpreterId rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_qccId.equals(rhs.m_qccId);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof BoronInterpreterId)) return false;
		return equals((BoronInterpreterId) o);
	}

	@Override
	public int hashCode() {
		return m_qccId.hashCode();
	}

	public String qccId() {
		return m_qccId;
	}

	@Override
	public String toString() {
		return m_qccId;
	}

	public static BoronInterpreterId newInstance(String qccId) {
		if (qccId.equalsIgnoreCase("bash") || qccId.equalsIgnoreCase("linux")) return IntrinsicBash;
		if (qccId.equalsIgnoreCase("python")) return IntrinsicPython;
		if (qccId.equalsIgnoreCase("win") || qccId.equalsIgnoreCase("wincmd")) return IntrinsicWinCmd;
		return new BoronInterpreterId(qccId);
	}

	public BoronInterpreterId(String qccId) {
		if (qccId == null || qccId.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_qccId = qccId;
	}

	private final String m_qccId;
}
