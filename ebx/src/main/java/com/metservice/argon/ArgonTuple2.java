/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class ArgonTuple2<A extends Comparable<A>, B extends Comparable<B>> implements Comparable<ArgonTuple2<A, B>> {

	@Override
	public int compareTo(ArgonTuple2<A, B> rhs) {
		final int c0 = a.compareTo(rhs.a);
		if (c0 != 0) return c0;
		final int c1 = b.compareTo(rhs.b);
		return c1;
	}

	public boolean equals(ArgonTuple2<?, ?> rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return a.equals(rhs.a) && b.equals(rhs.b);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof ArgonTuple2<?, ?>)) return false;
		return equals((ArgonTuple2<?, ?>) o);
	}

	@Override
	public int hashCode() {
		return HashCoder.fields2(a, b);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(a);
		sb.append(",");
		sb.append(b);
		sb.append(')');
		return sb.toString();
	}

	public ArgonTuple2(A a, B b) {
		if (a == null) throw new IllegalArgumentException("object is null");
		if (b == null) throw new IllegalArgumentException("object is null");
		this.a = a;
		this.b = b;
	}
	public final A a;
	public final B b;
}
