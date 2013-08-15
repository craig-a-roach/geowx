/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.TimeFactors;
import com.metservice.argon.TimeZoneFactory;
import com.metservice.argon.text.ArgonLsParser;

/**
 * @author roach
 */
class UNeonArray {

	public static final String PropertyName_name = "name";
	public static final String PropertyName_isDirectory = "isDirectory";
	public static final String PropertyName_lastModified = "lastModified";
	public static final String PropertyName_size = "size";

	private static final int INSERTION_SORT_THRESHOLD = 7;

	private static int compareTo(EsExecutionContext ecx, IEsOperand lhs, IEsOperand rhs, EsFunction oCompareFn)
			throws InterruptedException {
		if (lhs == null) throw new IllegalArgumentException("object is null");
		if (rhs == null) throw new IllegalArgumentException("object is null");

		final EsType lhsType = lhs.esType();
		final EsType rhsType = rhs.esType();

		if (lhsType == EsType.TUndefined && rhsType == EsType.TUndefined) return 0;
		if (lhsType == EsType.TUndefined) return 1;
		if (rhsType == EsType.TUndefined) return -1;

		if (oCompareFn == null) {
			final String zLhs = lhs.toCanonicalString(ecx);
			final String zRhs = rhs.toCanonicalString(ecx);
			return zLhs.compareTo(zRhs);
		}
		final EsList argsCompare = new EsList();
		argsCompare.add(lhs);
		argsCompare.add(rhs);
		final EsActivation activation = EsActivation.newInstance(ecx.global(), oCompareFn, argsCompare);
		final EsExecutionContext neoExecutionContext = ecx.newInstance(oCompareFn, activation, null);
		final IEsCallable callable = oCompareFn.callable();
		final IEsOperand callResult = callable.call(neoExecutionContext);
		return callResult.toNumber(ecx).intVerified();
	}

	private static void insertionSort(EsExecutionContext ecx, IEsOperand src[], IEsOperand dest[], int low, int high,
			EsFunction oCompareFn)
			throws InterruptedException {
		if (src == null) throw new IllegalArgumentException("object is null");
		if (dest == null) throw new IllegalArgumentException("object is null");

		for (int i = low; i < high; i++) {
			for (int j = i; j > low && compareTo(ecx, dest[j - 1], dest[j], oCompareFn) > 0; j--) {
				final IEsOperand t = dest[j];
				dest[j] = dest[j - 1];
				dest[j - 1] = t;
			}
		}
	}

	private static void mergeSort(EsExecutionContext ecx, IEsOperand src[], IEsOperand dest[], int inLo, int inHi, int off,
			EsFunction oCompareFn)
			throws InterruptedException {
		if (src == null) throw new IllegalArgumentException("object is null");
		if (dest == null) throw new IllegalArgumentException("object is null");

		final int length = inHi - inLo;
		if (length < INSERTION_SORT_THRESHOLD) {
			insertionSort(ecx, src, dest, inLo, inHi, oCompareFn);
			return;
		}

		final int destLo = inLo;
		final int destHi = inHi;
		final int offLo = inLo + off;
		final int offHi = inHi + off;
		final int mid = (offLo + offHi) >> 1;
		mergeSort(ecx, dest, src, offLo, mid, -off, oCompareFn);
		mergeSort(ecx, dest, src, mid, offHi, -off, oCompareFn);

		if (compareTo(ecx, src[mid - 1], src[mid], oCompareFn) <= 0) {
			System.arraycopy(src, offLo, dest, destLo, length);
		} else {
			for (int i = destLo, p = offLo, q = mid; i < destHi; i++) {
				if (q >= offHi || p < mid && compareTo(ecx, src[p], src[q], oCompareFn) <= 0) {
					dest[i] = src[p++];
				} else {
					dest[i] = src[q++];
				}
			}
		}
	}

	private static String[] zptqtw(EsExecutionContext ecx, EsObject src)
			throws InterruptedException {
		assert src != null;
		final int length = UNeon.length(ecx, src);
		final List<String> zl = new ArrayList<String>(length);
		for (int index = 0; index < length; index++) {
			final String pname = UNeon.toPropertyName(index);
			final IEsOperand valueOperand = src.esGet(pname);
			final EsType esType = valueOperand.esType();
			if (esType.isDatum) {
				final String ztw = valueOperand.toCanonicalString(ecx).trim();
				if (ztw.length() > 0) {
					zl.add(ztw);
				}
			}
		}
		return zl.toArray(new String[zl.size()]);
	}

	public static IEsOperand array_compact(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final boolean compactNulls = ac.defaulted(0) ? true : ac.booleanValue(0);
		final EsObject esThis = ac.esThis;
		final int length = UNeon.length(ecx, esThis);
		int w = 0;
		for (int r = 0; r < length; r++) {
			final IEsOperand member = UNeon.espropertyByIndex(esThis, r);
			final EsType esType = member.esType();
			if (esType.isDatum || (esType.isDefined && !compactNulls)) {
				if (w < r) {
					esThis.put(UNeon.toPropertyName(w), member);
				}
				w++;
			}
		}
		final int neoLength = w;
		while (w < length) {
			esThis.delete(UNeon.toPropertyName(w));
			w++;
		}
		if (neoLength < length) {
			esThis.putLength(neoLength, false);
			esThis.cascadeLengthUpdate(neoLength);
		}
		return esThis;
	}

	public static EsIntrinsicArray array_concat(EsExecutionContext ecx)
			throws InterruptedException {
		final EsObject esThis = ecx.thisObject();
		final EsArguments args = ecx.activation().arguments();
		final int argc = args.length();
		final List<IEsOperand> zlValues = new ArrayList<IEsOperand>(16);
		for (int ia = -1; ia < argc; ia++) {
			final IEsOperand operand = ia < 0 ? esThis : args.operand(ia);
			if (operand instanceof EsIntrinsicArray) {
				final EsIntrinsicArray arrayOperand = (EsIntrinsicArray) operand;
				final int arrayCount = arrayOperand.length();
				for (int im = 0; im < arrayCount; im++) {
					final IEsOperand member = arrayOperand.getByIndex(im);
					if (member.esType() != EsType.TUndefined) {
						zlValues.add(member);
					}
				}
			} else {
				zlValues.add(operand);
			}
		}

		return ecx.global().newIntrinsicArray(zlValues);
	}

	public static EsObject array_fill(EsExecutionContext ecx)
			throws InterruptedException {
		final EsObject esThis = ecx.thisObject();
		final EsArguments args = ecx.activation().arguments();
		final int argCount = args.length();
		final IEsOperand fillValueOperand = args.operand(0);
		final int extendLengthBy = argCount > 1 ? Math.max(0, args.operand(1).toNumber(ecx).intVerified()) : 0;
		final int exLength = UNeon.length(ecx, esThis);
		final int neoLength = exLength + extendLengthBy;
		for (int i = 0; i < neoLength; i++) {
			final String pname = UNeon.toPropertyName(i);
			final IEsOperand p = esThis.esGet(pname);
			if (p.esType() == EsType.TUndefined) {
				esThis.put(pname, fillValueOperand);
			}
		}
		if (neoLength > exLength) {
			esThis.putLength(neoLength, false);
			esThis.cascadeLengthUpdate(neoLength);
		}
		return esThis;
	}

	public static EsObject array_filter(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final EsFunction callbackFn = ac.esFunction(0);
		final EsObject oThis = ac.defaulted(1) ? null : ac.esObject(1);
		final int length = UNeon.length(ecx, ac.esThis);
		final List<IEsOperand> zlAccepted = new ArrayList<IEsOperand>(length);
		for (int i = 0; i < length; i++) {
			final IEsOperand member = UNeon.espropertyByIndex(ac.esThis, i);
			if (UNeonDml.satisfied(ecx, member, i, ac.esThis, callbackFn, oThis, true)) {
				zlAccepted.add(member);
			}
		}
		return ecx.global().newIntrinsicArray(zlAccepted);
	}

	public static IEsOperand array_first(EsExecutionContext ecx)
			throws InterruptedException {
		return ecx.thisObject().esGet("0");
	}

	public static EsObject array_forEach(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final EsObject esThis = ac.esThis;
		final EsFunction callbackFn = ac.esFunction(0);
		final EsObject oThis = ac.defaulted(1) ? null : ac.esObject(1);
		final int length = UNeon.length(ecx, esThis);
		for (int i = 0; i < length; i++) {
			final IEsOperand member = UNeon.espropertyByIndex(esThis, i);
			UNeonDml.map(ecx, member, i, esThis, callbackFn, oThis, true);
		}
		return esThis;
	}

	public static EsIntrinsicArray array_intersection(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final EsObject rhsObject = ac.esObject(0);
		final EsArgumentAccessor oaKeyNames = ac.find(1);
		final String[] ozptKeyNames = oaKeyNames == null ? null : oaKeyNames.ozptqtwStringValuesEvery();
		final EsFunction oBinaryFn = ac.defaulted(2) ? null : ac.esFunction(2);
		final IEsOperand[] zptMembersLhs = UNeon.zptOperandsEvery(ecx, ac.esThis);
		final IEsOperand[] zptMembersRhs = UNeon.zptOperandsEvery(ecx, rhsObject);
		final EsObject[] lhs = UNeon.zptEsObjectEvery(ecx, "this", zptMembersLhs);
		final EsObject[] rhs = UNeon.zptEsObjectEvery(ecx, "rhs", zptMembersRhs);
		return UNeonDml.intersection(ecx, lhs, rhs, ozptKeyNames, oBinaryFn);
	}

	public static EsPrimitiveString array_join(EsExecutionContext ecx)
			throws InterruptedException {
		final EsObject esThis = ecx.thisObject();
		final EsArguments args = ecx.activation().arguments();
		final int argCount = args.length();
		final EsPrimitiveString separatorString = (argCount == 0) ? EsPrimitiveString.COMMA : args.primitiveString(ecx, 0);
		final int length = UNeon.length(ecx, esThis);
		final String zSeparator = separatorString.zValue();
		final StringBuilder b = new StringBuilder();
		for (int index = 0; index < length; index++) {
			final String pname = UNeon.toPropertyName(index);
			final IEsOperand valueOperand = esThis.esGet(pname);
			final String stringValue;
			if (valueOperand instanceof EsPrimitiveUndefined) {
				stringValue = "";
			} else if (valueOperand instanceof EsPrimitiveNull) {
				stringValue = "";
			} else {
				stringValue = valueOperand.toCanonicalString(ecx);
			}
			if (index > 0) {
				b.append(zSeparator);
			}
			b.append(stringValue);
		}

		return new EsPrimitiveString(b.toString());
	}

	public static IEsOperand array_last(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final int offset = ac.defaulted(0) ? 0 : ac.intValue(0);
		final int length = UNeon.length(ecx, ac.esThis);
		final int index = length - 1 - offset;
		if (index < 0 || index >= length) return EsPrimitiveUndefined.Instance;
		return UNeon.espropertyByIndex(ac.esThis, index);
	}

	public static EsIntrinsicArray array_lslParse(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final String[] zptqtwLines = zptqtw(ecx, ac.esThis);
		final EsIntrinsicTimezone esoTimezone = ac.defaulted(0) ? null : ac.esoIntrinsicTimezone(0);
		final String ztwSort = ac.defaulted(1) ? "" : ac.ztwStringValue(1);
		final Comparator<ArgonLsParser.Node> oSort;
		if (ztwSort.length() == 0) {
			oSort = null;
		} else if (ztwSort.equals("t")) {
			oSort = ArgonLsParser.ByLastModified;
		} else if (ztwSort.equals("n")) {
			oSort = ArgonLsParser.ByName;
		} else if (ztwSort.equals("s")) {
			oSort = ArgonLsParser.BySize;
		} else {
			final String m = "Unsupported " + ac.formalParameterName(1) + " '" + ztwSort + "'; expecting t, n or s ";
			throw new EsApiCodeException(m);
		}
		final TimeZone timeZone = esoTimezone == null ? TimeZoneFactory.GMT : esoTimezone.timeZoneValue();
		final TimeFactors now = TimeFactors.newInstance(ArgonClock.tsNow(), timeZone);
		final List<ArgonLsParser.Node> zlNodes = ArgonLsParser.zlNodes(zptqtwLines, timeZone, now, oSort);
		final int nodeCount = zlNodes.size();
		final EsIntrinsicObject[] zptEsNodes = new EsIntrinsicObject[nodeCount];
		for (int i = 0; i < nodeCount; i++) {
			final ArgonLsParser.Node node = zlNodes.get(i);
			final EsPrimitiveString name = new EsPrimitiveString(node.qccName);
			final EsPrimitiveBoolean isDirectory = EsPrimitiveBoolean.instance(node.isDirectory);
			final EsPrimitiveNumberTime lastModified = new EsPrimitiveNumberTime(node.tsLastModified);
			final EsPrimitiveNumberInteger size = EsPrimitiveNumberInteger.newInstance(node.bcSize);
			final EsIntrinsicObject esnode = ecx.global().newIntrinsicObject();
			esnode.add(PropertyName_name, EsProperty.newReadOnlyDontDelete(name));
			esnode.add(PropertyName_isDirectory, EsProperty.newReadOnlyDontDelete(isDirectory));
			esnode.add(PropertyName_lastModified, EsProperty.newReadOnlyDontDelete(lastModified));
			esnode.add(PropertyName_size, EsProperty.newReadOnlyDontDelete(size));
			zptEsNodes[i] = esnode;
		}
		return ecx.global().newIntrinsicArray(zptEsNodes);
	}

	public static EsObject array_map(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final EsObject esThis = ac.esThis;
		final EsFunction callbackFn = ac.esFunction(0);
		final EsObject oThis = ac.defaulted(1) ? null : ac.esObject(1);
		final int length = UNeon.length(ecx, esThis);
		final List<IEsOperand> zlResults = new ArrayList<IEsOperand>(length);
		for (int i = 0; i < length; i++) {
			final IEsOperand member = UNeon.espropertyByIndex(esThis, i);
			final IEsOperand result = UNeonDml.map(ecx, member, i, esThis, callbackFn, oThis, true);
			zlResults.add(result);
		}
		return ecx.global().newIntrinsicArray(zlResults);
	}

	public static EsIntrinsicArray array_overlay(EsExecutionContext ecx)
			throws InterruptedException {
		final EsObject esThis = ecx.thisObject();
		final EsArguments args = ecx.activation().arguments();
		final int argCount = args.length();
		int maxExtent = 0;
		for (int ia = -1; ia < argCount; ia++) {
			final IEsOperand operand = ia < 0 ? esThis : args.operand(ia);
			final int extent = (operand instanceof EsIntrinsicArray) ? ((EsIntrinsicArray) operand).length() : 1;
			maxExtent = Math.max(maxExtent, extent);
		}
		final IEsOperand[] zOverlay = new IEsOperand[maxExtent];
		for (int ia = -1; ia < argCount; ia++) {
			final IEsOperand operand = ia < 0 ? esThis : args.operand(ia);
			if (operand instanceof EsIntrinsicArray) {
				final EsIntrinsicArray layer = (EsIntrinsicArray) operand;
				final int layerLength = layer.length();
				for (int im = 0; im < layerLength; im++) {
					final IEsOperand layerMember = layer.getByIndex(im);
					if (layerMember.esType() != EsType.TUndefined) {
						zOverlay[im] = layerMember;
					}
				}
			} else {
				zOverlay[0] = operand;
			}
		}

		return ecx.global().newIntrinsicArray(zOverlay, maxExtent);
	}

	public static EsObject array_partition(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final IEsOperand esDatum = ac.esOperandDatum(0);
		final IEsOperand[] src = UNeon.zptOperandsEvery(ecx, ac.esThis);
		if (esDatum instanceof EsFunction) {
			final EsObject oThis = ac.defaulted(1) ? null : ac.esObject(1);
			return UNeonDml.partition(ecx, src, ac.esThis, (EsFunction) esDatum, oThis);
		}
		return UNeonDml.partition(ecx, src, esDatum.toNumber(ecx).intVerified());
	}

	public static EsPrimitiveNumberInteger array_push(EsExecutionContext ecx, boolean expand)
			throws InterruptedException {
		final EsObject esThis = ecx.thisObject();
		final EsArguments args = ecx.activation().arguments();
		final int argc = args.length();
		final int length = UNeon.length(ecx, esThis);
		int neoLength = length;
		for (int i = 0; i < argc; i++) {
			final IEsOperand value = args.operand(i);
			final boolean deep = expand && (value instanceof EsIntrinsicArray);
			if (deep) {
				final EsIntrinsicArray arrayOperand = (EsIntrinsicArray) value;
				final int arrayCount = arrayOperand.length();
				for (int im = 0; im < arrayCount; im++) {
					final IEsOperand member = arrayOperand.getByIndex(im);
					if (member.esType() != EsType.TUndefined) {
						esThis.put(UNeon.toPropertyName(neoLength), member);
						neoLength++;
					}
				}
			} else {
				esThis.put(UNeon.toPropertyName(neoLength), value);
				neoLength++;
			}
		}
		esThis.putLength(neoLength, false);
		esThis.cascadeLengthUpdate(neoLength);
		return new EsPrimitiveNumberInteger(neoLength);
	}

	public static IEsOperand array_reduce(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final EsObject esThis = ac.esThis;
		final EsFunction callbackFn = ac.esFunction(0);
		final IEsOperand oInitialValue = ac.defaulted(1) ? null : ac.esOperand(1);
		final int length = UNeon.length(ecx, esThis);
		IEsOperand oPreviousValue = oInitialValue;
		int start = 0;
		while (oPreviousValue == null && start < length) {
			final IEsOperand currentValue = UNeon.espropertyByIndex(esThis, start);
			start++;
			if (currentValue.esType() != EsType.TUndefined) {
				oPreviousValue = currentValue;
			}
		}
		if (oPreviousValue == null) throw new EsApiCodeException("Cannot reduce an array with no defined members");
		IEsOperand resultValue = oPreviousValue;
		for (int i = start; i < length; i++) {
			final IEsOperand currentValue = UNeon.espropertyByIndex(esThis, i);
			if (currentValue.esType() != EsType.TUndefined) {
				resultValue = UNeonDml.reduce(ecx, resultValue, currentValue, i, esThis, callbackFn);
			}
		}
		return resultValue;
	}

	public static EsIntrinsicArray array_slice(EsExecutionContext ecx)
			throws InterruptedException {
		final EsObject esThis = ecx.thisObject();
		final EsArguments args = ecx.activation().arguments();
		final EsIntrinsicArray array = ecx.global().newIntrinsicArray();
		final int argCount = args.length();
		final int length = UNeon.length(ecx, esThis);
		final int start = argCount == 0 ? 0 : args.primitiveNumber(ecx, 0).intVerified();
		final int k = start < 0 ? Math.max(0, length + start) : Math.min(length, start);
		final int end = argCount <= 1 ? length : args.primitiveNumber(ecx, 1).intVerified();
		final int m = end < 0 ? Math.max(0, length + end) : Math.min(length, end);
		if (k >= m) return array;

		final int neoLength = m - k;
		array.setLength(neoLength);
		for (int i = 0; i < neoLength; i++) {
			final IEsOperand p = UNeon.espropertyByIndex(esThis, k + i);
			if (p.esType() != EsType.TUndefined) {
				array.putByIndex(i, p);
			}
		}
		return array;
	}

	public static EsObject array_sort(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final EsObject esThis = ac.esThis;
		final int length = UNeon.length(ecx, esThis);
		if (length < 2) return esThis;

		final IEsOperand[] aux = new IEsOperand[length];
		for (int i = 0; i < length; i++) {
			final String pname = UNeon.toPropertyName(i);
			aux[i] = esThis.esGet(pname);
			esThis.delete(pname);
		}
		final IEsOperand[] target = new IEsOperand[length];
		System.arraycopy(aux, 0, target, 0, length);
		final EsFunction oCompareFn = ac.defaulted(0) ? null : ac.esFunction(0);
		mergeSort(ecx, aux, target, 0, length, 0, oCompareFn);
		for (int i = 0; i < length; i++) {
			final String pname = UNeon.toPropertyName(i);
			esThis.put(pname, target[i]);
		}
		return esThis;
	}

	public static EsIntrinsicArray array_subtract(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final EsObject rhsObject = ac.esObject(0);
		final EsArgumentAccessor oaKeyNames = ac.find(1);
		final String[] ozptKeyNames = oaKeyNames == null ? null : oaKeyNames.ozptqtwStringValuesEvery();
		final IEsOperand[] zptMembersLhs = UNeon.zptOperandsEvery(ecx, ac.esThis);
		final IEsOperand[] zptMembersRhs = UNeon.zptOperandsEvery(ecx, rhsObject);
		final EsObject[] lhs = UNeon.zptEsObjectEvery(ecx, "this", zptMembersLhs);
		final EsObject[] rhs = UNeon.zptEsObjectEvery(ecx, "rhs", zptMembersRhs);
		return UNeonDml.subtract(ecx, lhs, rhs, ozptKeyNames);
	}

	public static EsIntrinsicArray array_tail(EsExecutionContext ecx)
			throws InterruptedException {
		final EsObject esThis = ecx.thisObject();
		final EsIntrinsicArray array = ecx.global().newIntrinsicArray();
		final int length = UNeon.length(ecx, esThis);
		if (length < 2) return array;
		final int neoLength = length - 1;
		array.setLength(neoLength);
		for (int i = 0; i < neoLength; i++) {
			final IEsOperand p = UNeon.espropertyByIndex(esThis, i + 1);
			if (p.esType() != EsType.TUndefined) {
				array.putByIndex(i, p);
			}
		}
		return array;
	}

	public static EsObject array_toMap(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final String ozErrorSuffix = ac.defaulted(0) ? null : ac.zStringValue(0);
		return UNeonCsvDecode.newMap(ecx, ac.esThis, ozErrorSuffix);
	}

	private UNeonArray() {
	}

}
