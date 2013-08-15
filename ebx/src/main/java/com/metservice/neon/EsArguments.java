/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.List;

import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsArguments extends EsObject {

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	@Override
	public IJsonNative createJsonNative() {
		return null;
	}

	@Override
	public EsObject createObject() {
		return null;
	}

	@Override
	public String esClass() {
		return "Arguments";
	}

	public int formalParameterCount() {
		return m_zlFormalParameterNames.size();
	}

	public String formalParameterName(int index) {
		final int fpc = m_zlFormalParameterNames.size();
		return (index >= 0 && index < fpc) ? m_zlFormalParameterNames.get(index) : ("param" + index);
	}

	@Override
	public JsonType getJsonType() {
		return null;
	}

	public int length() {
		if (m_ozptArgumentValues == null) {
			final IEsOperand lengthOperand = esGet("length");
			if (lengthOperand instanceof EsPrimitiveNumberInteger)
				return ((EsPrimitiveNumberInteger) lengthOperand).intVerified();
			return -1;
		}
		return m_ozptArgumentValues.length;
	}

	public IEsOperand operand(int index) {
		if (m_ozptArgumentValues == null) return esGet(Integer.toString(index));
		if (index < 0 || index >= m_ozptArgumentValues.length) return EsPrimitiveUndefined.Instance;
		return m_ozptArgumentValues[index];
	}

	public EsPrimitive primitive(EsExecutionContext ecx, int index)
			throws InterruptedException {
		return operand(index).toPrimitive(ecx, null);
	}

	public EsPrimitiveNumber primitiveNumber(EsExecutionContext ecx, int index)
			throws InterruptedException {
		return operand(index).toNumber(ecx);
	}

	public EsPrimitiveString primitiveString(EsExecutionContext ecx, int index)
			throws InterruptedException {
		return new EsPrimitiveString(operand(index).toCanonicalString(ecx));
	}

	public EsPrimitiveBoolean primivitiveBoolean(EsExecutionContext ecx, int index) {
		return EsPrimitiveBoolean.instance(operand(index).toCanonicalBoolean());
	}

	public void setArgumentValues(IEsOperand[] ozptArgumentValues) {
		m_ozptArgumentValues = ozptArgumentValues;
	}

	@Override
	public String show(int depth) {
		final int argc = length();
		final int fpc = formalParameterCount();
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < argc && i < fpc; i++) {
			if (i > 0) {
				b.append(", ");
			}
			b.append(formalParameterName(i));
			if (depth > 0) {
				b.append("=");
				b.append(operand(i).show(depth - 1));
			}
		}
		return b.toString();
	}

	public IEsOperand[] zptValues() {
		if (m_ozptArgumentValues == null) {
			final IEsOperand[] zptArgumentValues = new IEsOperand[length()];
			for (int index = 0; index < zptArgumentValues.length; index++) {
				zptArgumentValues[index] = operand(index);
			}
			return zptArgumentValues;
		}

		return m_ozptArgumentValues;
	}

	public String ztwFormalParameterJoin(EsExecutionContext ecx)
			throws InterruptedException {
		return ztwFormalParameterJoin(ecx, ", ");
	}

	public String ztwFormalParameterJoin(EsExecutionContext ecx, String zSeparator)
			throws InterruptedException {
		final int argc = length();
		final int fpc = formalParameterCount();
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < argc && i < fpc; i++) {
			if (i > 0) {
				b.append(zSeparator);
			}
			b.append(formalParameterName(i)).append("='");
			b.append(operand(i).toCanonicalString(ecx));
			b.append("'");
		}
		return b.toString();
	}

	public EsArguments(EsObject prototype, List<String> zlFormalParameterNames) {
		super(prototype);
		if (zlFormalParameterNames == null) throw new IllegalArgumentException("zlFormalParameterNames is null");
		m_zlFormalParameterNames = zlFormalParameterNames;
	}
	private final List<String> m_zlFormalParameterNames;
	private IEsOperand[] m_ozptArgumentValues;
}
