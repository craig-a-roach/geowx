/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class HashCoder {

	public static final int INIT = 1;

	private static final int PRIME = 31;

	public static int and(int result, boolean b) {
		return PRIME * result + field(b);
	}

	public static int and(int result, double d) {
		return PRIME * result + field(d);
	}

	public static int and(int result, int i) {
		return PRIME * result + i;
	}

	public static int and(int result, long l) {
		return PRIME * result + field(l);
	}

	public static int and(int result, Object o) {
		return PRIME * result + field(o);
	}

	public static int and(int result, Object[] ozpt) {
		return PRIME * result + field(ozpt);
	}

	public static int field(boolean b) {
		return b ? 1231 : 1237;
	}

	public static int field(double d) {
		return field(Double.doubleToLongBits(d));
	}

	public static int field(long l) {
		return (int) (l ^ (l >>> 32));
	}

	public static int field(Object o) {
		return o == null ? 0 : o.hashCode();
	}

	public static int field(Object[] ozpt) {
		int result = INIT;
		if (ozpt != null) {
			final int count = ozpt.length;
			for (int i = 0; i < count; i++) {
				result = and(result, ozpt[i]);
			}
		}
		return result;
	}

	public static int fields(Object... zptFields) {
		int result = INIT;
		if (zptFields != null) {
			final int count = zptFields.length;
			for (int i = 0; i < count; i++) {
				result = and(result, zptFields[i]);
			}
		}
		return result;
	}

	public static int fields2(Object a, Object b) {
		int result = INIT;
		result = and(result, a);
		result = and(result, b);
		return result;
	}

	public static int fields3(Object a, Object b, Object c) {
		int result = INIT;
		result = and(result, a);
		result = and(result, b);
		result = and(result, c);
		return result;
	}

	public static HashCoder init() {
		return new HashCoder();
	}

	public static HashCoder init(double d) {
		return (new HashCoder()).and(d);
	}

	public static HashCoder init(int i) {
		return (new HashCoder()).and(i);
	}

	public static HashCoder init(long l) {
		return (new HashCoder()).and(l);
	}

	public static HashCoder init(Object o) {
		return (new HashCoder()).and(o);
	}

	public HashCoder and(double d) {
		result = and(result, d);
		return this;
	}

	public HashCoder and(int i) {
		result = and(result, i);
		return this;
	}

	public HashCoder and(long l) {
		result = and(result, l);
		return this;
	}

	public HashCoder and(Object o) {
		result = and(result, o);
		return this;
	}

	public Integer newResult() {
		return new Integer(result);
	}

	public int result() {
		return result;
	}

	private HashCoder() {
	}
	private int result = INIT;
}
