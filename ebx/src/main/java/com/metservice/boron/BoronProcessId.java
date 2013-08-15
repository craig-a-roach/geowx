/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import com.metservice.argon.text.ArgonNumber;

/**
 * @author roach
 */
public class BoronProcessId implements Comparable<BoronProcessId> {

	public static final long Init = 1L;

	public static String qId(long id) {
		return ArgonNumber.longToB36Full(id);
	}

	@Override
	public int compareTo(BoronProcessId rhs) {
		if (id < rhs.id) return +1;
		if (id > rhs.id) return -1;
		return 0;
	}

	public boolean equals(BoronProcessId rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return id == rhs.id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof BoronProcessId)) return false;
		return equals((BoronProcessId) o);
	}

	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}

	public BoronProcessId max(BoronProcessId oRhs) {
		return (oRhs != null && oRhs.id > id) ? oRhs : this;
	}

	public String qId() {
		return qId(id);
	}

	@Override
	public String toString() {
		return qId();
	}

	public BoronProcessId(long id) {
		if (id <= 0L) throw new IllegalArgumentException("invalid id>" + id + "<");
		this.id = id;
	}

	public final long id;
}
