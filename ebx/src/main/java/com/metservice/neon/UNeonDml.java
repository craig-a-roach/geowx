/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.metservice.argon.HashCoder;

/**
 * 
 * @author roach
 */
class UNeonDml {

	public static final String PNAME_LHS = "lhs";
	public static final String PNAME_RHS = "rhs";

	private static final IEsOperand[] ZOPERANDS = new IEsOperand[0];
	private static final int INSERTION_SORT_THRESHOLD = 7;
	private static final int MIN_INITIAL_HASH_CAPACITY = 16;

	private static void insertionSort(EsExecutionContext ecx, Composite src[], Composite dest[], int low, int high)
			throws InterruptedException {
		assert src != null;
		assert dest != null;

		for (int i = low; i < high; i++) {
			for (int j = i; j > low && dest[j - 1].compareTo(ecx, dest[j]) > 0; j--) {
				final Composite t = dest[j];
				dest[j] = dest[j - 1];
				dest[j - 1] = t;
			}
		}
	}

	private static IEsOperand intersect(EsExecutionContext ecx, String[] zptKeyNamesAsc, Composite lhsComposite,
			Composite rhsComposite, EsFunction oBinaryFn)
			throws InterruptedException {
		final EsObject lhsRow = lhsComposite.row;
		final EsObject rhsRow = rhsComposite.row;
		if (oBinaryFn == null) {
			final EsObject result = ecx.global().newIntrinsicObject();
			lhsComposite.save(result, zptKeyNamesAsc);
			result.esPut(PNAME_LHS, lhsRow);
			result.esPut(PNAME_RHS, rhsRow);
			return result;
		}

		final EsList argsBinary = new EsList();
		argsBinary.add(lhsRow);
		argsBinary.add(rhsRow);
		final EsActivation activation = EsActivation.newInstance(ecx.global(), oBinaryFn, argsBinary);
		final EsExecutionContext neoExecutionContext = ecx.newInstance(oBinaryFn, activation, null);
		final IEsCallable callable = oBinaryFn.callable();
		return callable.call(neoExecutionContext);
	}

	private static void mergeSort(EsExecutionContext ecx, Composite src[], Composite dest[], int low, int high, int off)
			throws InterruptedException {
		assert src != null;
		assert dest != null;

		final int length = high - low;
		if (length < INSERTION_SORT_THRESHOLD) {
			insertionSort(ecx, src, dest, low, high);
			return;
		}

		final int destLo = low;
		final int destHi = high;
		final int offLo = low + off;
		final int offHi = high + off;
		final int mid = (low + high) >> 1;
		mergeSort(ecx, dest, src, offLo, mid, -off);
		mergeSort(ecx, dest, src, mid, offHi, -off);

		if (src[mid - 1].compareTo(ecx, src[mid]) <= 0) {
			System.arraycopy(src, offLo, dest, destLo, length);
		} else {
			for (int i = destLo, p = offLo, q = mid; i < destHi; i++) {
				if (q >= offHi || p < mid && src[p].compareTo(ecx, src[q]) <= 0) {
					dest[i] = src[p++];
				} else {
					dest[i] = src[q++];
				}
			}
		}
	}

	private static int rangeEnd(EsExecutionContext ecx, Composite[] zptCompositesAsc, int start)
			throws InterruptedException {
		final int length = zptCompositesAsc.length;
		final int pos = start;
		int end = start + 1;
		while (end < length && zptCompositesAsc[pos].compareTo(ecx, zptCompositesAsc[end]) == 0) {
			end++;
		}
		return end;
	}

	private static void sort(EsExecutionContext ecx, Composite dest[])
			throws InterruptedException {
		assert dest != null;
		final int length = dest.length;
		final Composite[] aux = new Composite[length];
		System.arraycopy(dest, 0, aux, 0, length);
		mergeSort(ecx, aux, dest, 0, length, 0);
	}

	private static Composite[] zptCompositesAsc(EsExecutionContext ecx, EsObject[] rows, String[] zptKeyNamesAsc)
			throws InterruptedException {
		assert rows != null;
		assert zptKeyNamesAsc != null;

		final int length = rows.length;
		final int keyDepth = zptKeyNamesAsc.length;

		final Composite[] zptComposites = new Composite[length];
		for (int i = 0; i < rows.length; i++) {
			zptComposites[i] = new Composite(ecx, rows[i], zptKeyNamesAsc);
		}
		if (length > 1 && keyDepth > 0) {
			sort(ecx, zptComposites);
		}
		return zptComposites;
	}

	private static String[] zptKeyNamesAsc(EsObject[] lhsRows, EsObject[] rhsRows, String[] ozptKeyNames) {
		final String[] zptKeyNamesAsc;
		if (ozptKeyNames == null) {
			final String[] zptLhsNamesAsc = zptNamesAsc(lhsRows);
			final String[] zptRhsNamesAsc = zptNamesAsc(rhsRows);
			zptKeyNamesAsc = zptNameIntersection(zptLhsNamesAsc, zptRhsNamesAsc);
		} else {
			zptKeyNamesAsc = zptKeyNamesAsc(ozptKeyNames);
		}
		return zptKeyNamesAsc;
	}

	private static String[] zptKeyNamesAsc(String[] zptKeyNames) {
		final int length = zptKeyNames.length;
		final String[] zptKeyNamesAsc = new String[length];
		System.arraycopy(zptKeyNames, 0, zptKeyNamesAsc, 0, length);
		Arrays.sort(zptKeyNamesAsc);
		return zptKeyNamesAsc;
	}

	private static String[] zptNameIntersection(String[] zptLhsNamesAsc, String[] zptRhsNamesAsc) {
		assert zptLhsNamesAsc != null;
		assert zptRhsNamesAsc != null;

		final List<String> zlCommon = new ArrayList<String>();
		int lhsPos = 0;
		int rhsPos = 0;
		while (lhsPos < zptLhsNamesAsc.length && rhsPos < zptRhsNamesAsc.length) {
			final String qLhs = zptLhsNamesAsc[lhsPos];
			final String qRhs = zptRhsNamesAsc[rhsPos];
			final int c = qLhs.compareTo(qRhs);
			if (c < 0) {
				lhsPos++;
			} else if (c > 0) {
				rhsPos++;
			} else {
				zlCommon.add(qLhs);
				lhsPos++;
				rhsPos++;
			}
		}

		return zlCommon.toArray(new String[zlCommon.size()]);
	}

	private static String[] zptNamesAsc(EsObject[] rows) {
		assert rows != null;
		final Set<String> zsNames = new HashSet<String>();
		for (int i = 0; i < rows.length; i++) {
			final EsObject row = rows[i];
			final List<String> zlPropertyNamesAsc = row.esPropertyNames();
			zsNames.addAll(zlPropertyNamesAsc);
		}
		final String[] zptNamesAsc = zsNames.toArray(new String[zsNames.size()]);
		Arrays.sort(zptNamesAsc);
		return zptNamesAsc;
	}

	public static EsIntrinsicArray intersection(EsExecutionContext ecx, EsObject[] lhsRows, EsObject[] rhsRows,
			String[] ozptKeyNames, EsFunction oBinaryFn)
			throws InterruptedException {
		final String[] zptKeyNamesAsc = zptKeyNamesAsc(lhsRows, rhsRows, ozptKeyNames);
		final Composite[] zptLhsCompositesAsc = zptCompositesAsc(ecx, lhsRows, zptKeyNamesAsc);
		final Composite[] zptRhsCompositesAsc = zptCompositesAsc(ecx, rhsRows, zptKeyNamesAsc);
		final int lhsLength = zptLhsCompositesAsc.length;
		final int rhsLength = zptRhsCompositesAsc.length;
		int lhsPos = 0;
		int rhsPos = 0;
		int lhsEnd = rangeEnd(ecx, zptLhsCompositesAsc, lhsPos);
		int rhsEnd = rangeEnd(ecx, zptRhsCompositesAsc, rhsPos);
		final List<IEsOperand> zlResults = new ArrayList<IEsOperand>(lhsLength);
		while (lhsPos < lhsLength && rhsPos < rhsLength) {
			final Composite lhsComposite = zptLhsCompositesAsc[lhsPos];
			final Composite rhsComposite = zptRhsCompositesAsc[rhsPos];
			final int cmp = lhsComposite.compareTo(ecx, rhsComposite);
			if (cmp < 0) {
				lhsPos = lhsEnd;
				lhsEnd = rangeEnd(ecx, zptLhsCompositesAsc, lhsPos);
			} else if (cmp > 0) {
				rhsPos = rhsEnd;
				rhsEnd = rangeEnd(ecx, zptRhsCompositesAsc, rhsPos);
			} else {
				for (int ilhs = lhsPos; ilhs < lhsEnd; ilhs++) {
					for (int irhs = rhsPos; irhs < rhsEnd; irhs++) {
						zlResults.add(intersect(ecx, zptKeyNamesAsc, zptLhsCompositesAsc[ilhs],
								zptRhsCompositesAsc[irhs], oBinaryFn));
					}
				}
				lhsPos = lhsEnd;
				rhsPos = rhsEnd;
				lhsEnd = rangeEnd(ecx, zptLhsCompositesAsc, lhsPos);
				rhsEnd = rangeEnd(ecx, zptRhsCompositesAsc, rhsPos);
			}
		}
		return ecx.global().newIntrinsicArray(zlResults);
	}

	public static IEsOperand map(EsExecutionContext ecx, IEsOperand src, int index, EsObject array, EsFunction callbackFn,
			EsObject oThis, boolean definedOnly)
			throws InterruptedException {
		if (definedOnly && src.esType() == EsType.TUndefined) return src;

		final EsList args = new EsList();
		args.add(src);
		args.add(new EsPrimitiveNumberInteger(index));
		args.add(array);
		final EsActivation activation = EsActivation.newInstance(ecx.global(), callbackFn, args);
		final EsExecutionContext neoExecutionContext = ecx.newInstance(callbackFn, activation, oThis);
		final IEsCallable callable = callbackFn.callable();
		final IEsOperand result = callable.call(neoExecutionContext);
		return result;
	}

	public static EsIntrinsicObject newPartitioned(EsExecutionContext ecx, IEsOperand[] lhs, IEsOperand[] rhs)
			throws InterruptedException {
		final EsIntrinsicObject result = ecx.global().newIntrinsicObject();
		result.put(PNAME_LHS, ecx.global().newIntrinsicArray(lhs));
		result.put(PNAME_RHS, ecx.global().newIntrinsicArray(rhs));
		return result;
	}

	public static EsIntrinsicObject partition(EsExecutionContext ecx, IEsOperand[] src, EsObject array, EsFunction predicateFn,
			EsObject oThis)
			throws InterruptedException {
		assert src != null;
		assert predicateFn != null;
		final int srcLength = src.length;
		for (int i = 0; i < srcLength; i++) {
			final IEsOperand candidate = src[i];
			if (satisfied(ecx, candidate, i, array, predicateFn, oThis, true)) return partition(ecx, src, i);
		}

		return partition(ecx, src, srcLength);
	}

	public static EsIntrinsicObject partition(EsExecutionContext ecx, IEsOperand[] src, int relStartIndex)
			throws InterruptedException {
		assert src != null;
		final int srcLength = src.length;
		final int startIndex = relStartIndex < 0 ? Math.max(0, srcLength + relStartIndex) : Math.min(srcLength, relStartIndex);
		final IEsOperand[] lhs;
		final IEsOperand[] rhs;
		if (startIndex == 0) {
			lhs = ZOPERANDS;
			rhs = src;
		} else if (startIndex >= srcLength) {
			lhs = src;
			rhs = ZOPERANDS;
		} else {
			final int lhsLength = startIndex;
			final int rhsLength = srcLength - startIndex;
			lhs = new IEsOperand[lhsLength];
			rhs = new IEsOperand[rhsLength];
			System.arraycopy(src, 0, lhs, 0, lhsLength);
			System.arraycopy(src, startIndex, rhs, 0, rhsLength);
		}
		return newPartitioned(ecx, lhs, rhs);
	}

	public static IEsOperand reduce(EsExecutionContext ecx, IEsOperand previousValue, IEsOperand currentValue, int index,
			EsObject array, EsFunction callbackFn)
			throws InterruptedException {
		final EsList args = new EsList();
		args.add(previousValue);
		args.add(currentValue);
		args.add(new EsPrimitiveNumberInteger(index));
		args.add(array);
		final EsActivation activation = EsActivation.newInstance(ecx.global(), callbackFn, args);
		final EsExecutionContext neoExecutionContext = ecx.newInstance(callbackFn, activation, null);
		final IEsCallable callable = callbackFn.callable();
		final IEsOperand result = callable.call(neoExecutionContext);
		return result;
	}

	public static boolean satisfied(EsExecutionContext ecx, IEsOperand candidate, int index, EsObject array,
			EsFunction predicateFn, EsObject oThis, boolean definedOnly)
			throws InterruptedException {
		if (definedOnly && candidate.esType() == EsType.TUndefined) return false;
		final IEsOperand result = map(ecx, candidate, index, array, predicateFn, oThis, definedOnly);
		return result.toCanonicalBoolean();
	}

	public static EsIntrinsicArray subtract(EsExecutionContext ecx, EsObject[] lhsRows, EsObject[] rhsRows, String[] ozptKeyNames)
			throws InterruptedException {
		final String[] zptKeyNamesAsc = zptKeyNamesAsc(lhsRows, rhsRows, ozptKeyNames);
		final Composite[] zptLhsCompositesAsc = zptCompositesAsc(ecx, lhsRows, zptKeyNamesAsc);
		final Composite[] zptRhsCompositesAsc = zptCompositesAsc(ecx, rhsRows, zptKeyNamesAsc);
		final int lhsLength = zptLhsCompositesAsc.length;
		final CompositeHashSet rhsSet = new CompositeHashSet(ecx, zptRhsCompositesAsc);
		final List<IEsOperand> zlResults = new ArrayList<IEsOperand>(lhsLength);
		for (int lhsPos = 0; lhsPos < lhsLength; lhsPos++) {
			final Composite lhsComposite = zptLhsCompositesAsc[lhsPos];
			if (!rhsSet.contains(ecx, lhsComposite)) {
				zlResults.add(lhsComposite.row);
			}
		}
		return ecx.global().newIntrinsicArray(zlResults);
	}

	private UNeonDml() {
	}

	private static class Composite {

		public int compareTo(EsExecutionContext ecx, Composite rhsComposite)
				throws InterruptedException {
			assert rhsComposite != null;

			final int lhsLen = zKeys.length;
			final int rhsLen = rhsComposite.zKeys.length;
			if (lhsLen < rhsLen) return -1;
			if (lhsLen > rhsLen) return 1;

			for (int i = 0; i < lhsLen; i++) {
				final EsPrimitive oLhs = zKeys[i];
				final EsPrimitive oRhs = rhsComposite.zKeys[i];
				if (oLhs == null && oRhs == null) {
					continue;
				}
				if (oLhs == null) return -1;
				if (oRhs == null) return 1;
				final EsType lhsType = oLhs.esType();
				final EsType rhsType = oRhs.esType();
				if (lhsType == EsType.TUndefined && rhsType == EsType.TUndefined) {
					continue;
				}
				if (lhsType == EsType.TUndefined) return 1;
				if (rhsType == EsType.TUndefined) return -1;
				if (lhsType == EsType.TNull && rhsType == EsType.TNull) {
					continue;
				}
				if (lhsType == EsType.TNull) return -1;
				if (rhsType == EsType.TNull) return 1;
				if (lhsType == EsType.TString && rhsType == EsType.TString) {
					final EsPrimitiveString lhsString = (EsPrimitiveString) oLhs;
					final EsPrimitiveString rhsString = (EsPrimitiveString) oRhs;
					if (lhsString.isLessThan(rhsString)) return -1;
					if (rhsString.isLessThan(lhsString)) return 1;
				} else {
					final EsPrimitiveNumber lhsNumber = oLhs.toNumber(ecx);
					final EsPrimitiveNumber rhsNumber = oRhs.toNumber(ecx);
					if (!EsPrimitiveNumber.canRelate(lhsNumber, rhsNumber)) {
						continue;
					}
					if (lhsNumber.isLessThan(rhsNumber)) return -1;
					if (rhsNumber.isLessThan(lhsNumber)) return 1;
				}
			}

			return 0;
		}

		public boolean equals(EsExecutionContext ecx, Composite rhsComposite)
				throws InterruptedException {
			assert rhsComposite != null;

			final int lhsLen = zKeys.length;
			final int rhsLen = rhsComposite.zKeys.length;
			if (lhsLen != rhsLen) return false;

			for (int i = 0; i < lhsLen; i++) {
				final EsPrimitive oLhs = zKeys[i];
				final EsPrimitive oRhs = rhsComposite.zKeys[i];
				if (oLhs == null && oRhs == null) {
					continue;
				}
				if (oLhs == null && oRhs != null) return false;
				if (oLhs != null && oRhs == null) return false;
				if (!ecx.isEqual(oLhs, oRhs, false)) return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		public void save(EsObject target, String[] zptKeyNamesAsc) {
			final int valueCount = zKeys.length;
			final int keyCount = zptKeyNamesAsc.length;
			for (int i = 0; i < valueCount && i < keyCount; i++) {
				final String keyName = zptKeyNamesAsc[i];
				final EsPrimitive oKeyValue = zKeys[i];
				if (oKeyValue != null) {
					target.esPut(keyName, oKeyValue);
				}
			}
		}

		public Composite(EsExecutionContext ecx, EsObject row, String[] zptKeyNamesAsc) throws InterruptedException {
			final int keyDepth = zptKeyNamesAsc.length;
			zKeys = new EsPrimitive[keyDepth];
			int chc = HashCoder.INIT;
			for (int i = 0; i < keyDepth; i++) {
				final String pname = zptKeyNamesAsc[i];
				final IEsOperand operand = row.esGet(pname);
				final EsType operandType = operand.esType();
				final EsPrimitive oPrimitive;
				if (operandType == EsType.TBoolean || operandType == EsType.TNumber || operandType == EsType.TString) {
					oPrimitive = operand.toPrimitive(ecx, EsType.TNumber);
				} else if (operandType == EsType.TObject) {
					final EsObject object = operand.toObject(ecx);
					if (object instanceof EsFunction) {
						oPrimitive = null;
					} else {
						oPrimitive = object.toPrimitive(ecx, EsType.TNumber);
					}
				} else {
					oPrimitive = null;
				}
				zKeys[i] = oPrimitive;
				if (oPrimitive != null) {
					final int phc = oPrimitive.toHash();
					chc = HashCoder.and(chc, phc);
				}
			}
			this.row = row;
			this.hashCode = chc;
		}
		final EsPrimitive[] zKeys;
		final EsObject row;
		final int hashCode;
	}// Composite

	private static class CompositeEntry {

		public CompositeEntry(Composite composite) {
			assert composite != null;
			this.composite = composite;
		}
		final Composite composite;
		CompositeEntry oNext;
	}// CompositeEntry

	private static class CompositeHashSet {

		private boolean addToTable(EsExecutionContext ecx, Composite neo)
				throws InterruptedException {
			final int tableIndex = toIndex(neo.hashCode);
			final CompositeEntry oHead = m_table[tableIndex];
			if (oHead == null) {
				m_table[tableIndex] = new CompositeEntry(neo);
				return true;
			}

			CompositeEntry tail = oHead;
			while (true) {
				final Composite candidate = tail.composite;
				if (candidate.equals(ecx, neo)) return false;

				final CompositeEntry oNextTail = tail.oNext;
				if (oNextTail == null) {
					tail.oNext = new CompositeEntry(neo);
					return true;
				}

				tail = oNextTail;
			}
		}

		private int toIndex(int hash) {
			return (m_table.length - 1) & hash;
		}

		public boolean contains(EsExecutionContext ecx, Composite target)
				throws InterruptedException {
			if (target == null) throw new IllegalArgumentException("object is null");

			final int tableIndex = toIndex(target.hashCode);
			CompositeEntry oTail = m_table[tableIndex];
			while (oTail != null) {
				final Composite candidate = oTail.composite;
				if (candidate.equals(ecx, target)) return true;
				oTail = oTail.oNext;
			}
			return false;
		}

		private static int canonCap(int reqd) {
			int cap = MIN_INITIAL_HASH_CAPACITY;
			while (cap < reqd) {
				cap = cap * 2;
			}
			return cap;
		}

		CompositeHashSet(EsExecutionContext ecx, Composite[] src) throws InterruptedException {
			assert src != null;
			final int srcLength = src.length;
			m_table = new CompositeEntry[canonCap(srcLength)];
			for (int i = 0; i < srcLength; i++) {
				addToTable(ecx, src[i]);
			}
		}

		private final CompositeEntry[] m_table;
	}// CompositeHashSet
}
