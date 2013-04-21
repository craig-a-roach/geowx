/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.collection;

/**
 * @author roach
 */
public abstract class DynamicArray<T> {

	public final void applyQuota(int quota, int pctTolerance) {
		if (array == null) return;
		final int overflow = count - quota;
		if (overflow <= 0) return;
		final int pctOver = overflow * 100 / quota;
		if (pctOver < pctTolerance) return;
		System.arraycopy(array, overflow, array, 0, quota);
		count = quota;
	}

	public final void clear() {
		if (array == null) return;
		final int exCap = array.length;
		for (int i = 0; i < exCap; i++) {
			array[i] = null;
		}
		count = 0;
	}

	public final void ensure() {
		if (array == null) {
			array = newArray(0);
		}
	}

	public final void ensure(int reqd) {
		if (array == null) {
			final int neoCap = Math.max(reqd, initialCapacity());
			array = newArray(neoCap);
		} else {
			final int exCap = array.length;
			if (exCap < reqd) {
				final int neoCap = Math.max(reqd, exCap * 3 / 2);
				final T[] save = array;
				array = newArray(neoCap);
				System.arraycopy(save, 0, array, 0, exCap);
			}
		}
	}

	public final T getLast() {
		return (count == 0) ? null : array[count - 1];
	}

	public final void growBy(int growth) {
		ensure(count + growth);
	}

	public int initialCapacity() {
		return 16;
	}

	public final boolean isEmpty() {
		return count == 0;
	}

	public final boolean isNotEmpty() {
		return count > 0;
	}

	public abstract T[] newArray(int cap);

	public final T poll() {
		if (count == 0) return null;
		final T oLast = array[count - 1];
		count--;
		return oLast;
	}

	public final T pop() {
		if (count == 0) throw new IllegalStateException("array is empty");
		final T oLast = array[count - 1];
		count--;
		return oLast;
	}

	public final void push(T t) {
		if (t == null) throw new IllegalArgumentException("object is null");
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
		final T[] save = array;
		array = newArray(count);
		System.arraycopy(save, 0, array, 0, exCap);
	}

	public final T[] zpt() {
		ensure();
		trim();
		return array;
	}

	protected DynamicArray() {
	}

	public T[] array;
	public int count;
}
