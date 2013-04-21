/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.collection;

/**
 * @author roach
 */
public class DynamicArrayInt {

	private int[] newArray(int cap) {
		return new int[cap];
	}

	public final void clear() {
		count = 0;
	}

	public final void ensure() {
		if (array == null) {
			array = newArray(0);
		}
	}

	public final void ensure(int reqd) {
		if (array == null) {
			final int neoCap = Math.max(reqd, DefaultInitialCapacity);
			array = newArray(neoCap);
		} else {
			final int exCap = array.length;
			if (exCap < reqd) {
				final int neoCap = Math.max(reqd, exCap * 3 / 2);
				final int[] save = array;
				array = newArray(neoCap);
				System.arraycopy(save, 0, array, 0, exCap);
			}
		}
	}

	public final void growBy(int growth) {
		ensure(count + growth);
	}

	public final boolean isEmpty() {
		return count == 0;
	}

	public final int last() {
		if (count == 0) throw new IllegalStateException("array is empty");
		return array[count - 1];
	}

	public final int pop() {
		if (count == 0) throw new IllegalStateException("array is empty");
		final int last = array[count - 1];
		count--;
		return last;
	}

	public final void push(int t) {
		ensure(count + 1);
		array[count] = t;
		count++;
	}

	@Override
	public String toString() {
		if (array == null || count == 0) return "";
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(array[i]);
		}
		sb.append("]");
		return sb.toString();
	}

	public final void trim() {
		if (array == null) return;
		final int exCap = array.length;
		if (exCap == count) return;
		final int[] save = array;
		array = newArray(count);
		System.arraycopy(save, 0, array, 0, exCap);
	}

	public final void zero() {
		if (array == null) return;
		final int exCap = array.length;
		for (int i = 0; i < exCap; i++) {
			array[i] = 0;
		}
	}

	public final int[] zpt() {
		ensure();
		trim();
		return array;
	}

	public DynamicArrayInt() {
		this(DefaultInitialCapacity);
	}

	public DynamicArrayInt(int initialCapacity) {
		array = new int[initialCapacity];
	}

	public static final int DefaultInitialCapacity = 16;

	public int[] array;
	public int count;
}
