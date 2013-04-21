/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
public class ArgonRecordType implements Comparable<ArgonRecordType> {

	@Override
	public int compareTo(ArgonRecordType rhs) {
		return m_qcctwName.compareTo(rhs.m_qcctwName);
	}

	public boolean equals(ArgonRecordType rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_qcctwName.equals(rhs.m_qcctwName);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof ArgonRecordType)) return false;
		return equals((ArgonRecordType) o);
	}

	public String format() {
		return m_qcctwName;
	}

	@Override
	public int hashCode() {
		return m_qcctwName.hashCode();
	}

	@Override
	public String toString() {
		return m_qcctwName;
	}

	public ArgonRecordType(String qccSpec) {
		try {
			m_qcctwName = ArgonText.qtwPosixName(qccSpec);
		} catch (final ArgonApiException ex) {
			throw new IllegalArgumentException("Record type name is unsuitable..." + ex.getMessage());
		}
	}

	private final String m_qcctwName;
}
