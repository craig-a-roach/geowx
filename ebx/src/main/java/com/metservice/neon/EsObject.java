/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonObject;

/**
 * 
 * @author roach
 */
public abstract class EsObject implements IEsOperand {

	public static final String PropertyName_length = "length";
	public static final String MethodName_toString = "toString";
	public static final String MethodName_valueOf = "valueOf";
	public static final String MethodName_equals = "equals";

	private static final int ATTMASK_LENGTH_MUTABLE = EsProperty.ATT_DONTDELETE | EsProperty.ATT_DONTENUM;
	private static final int ATTMASK_LENGTH_READONLY = EsProperty.ATT_DONTDELETE | EsProperty.ATT_DONTENUM
			| EsProperty.ATT_READONLY;

	private IEsOperand evaluateThis(EsExecutionContext ecx, IEsOperand oFunctionOperand)
			throws InterruptedException {
		if (oFunctionOperand instanceof EsFunction) {
			final EsFunction function = ((EsFunction) oFunctionOperand);
			final EsActivation activation = EsActivation.newInstance(ecx.global(), function, new EsList());
			final EsExecutionContext neoExecutionContext = ecx.newInstance(function, activation, this);
			final IEsCallable callable = function.callable();
			return callable.call(neoExecutionContext);
		}
		return null;
	}

	private EsProperty getProperty(String zccPropertyKey) {
		return m_lzyKeyToProperty == null ? null : m_lzyKeyToProperty.get(zccPropertyKey);
	}

	private Map<String, EsProperty> keyToProperty() {
		if (m_lzyKeyToProperty == null) {
			m_lzyKeyToProperty = new HashMap<String, EsProperty>(8);
		}
		return m_lzyKeyToProperty;
	}

	private boolean noProperties() {
		return m_lzyKeyToProperty == null;
	}

	private int propertyCount() {
		return m_lzyKeyToProperty == null ? 0 : m_lzyKeyToProperty.size();
	}

	protected String canonizePropertyKey(String zccPropertyKey) {
		return zccPropertyKey;
	}

	protected void cascadeLengthUpdate(int neoLength) {
	}

	protected void cascadeUpdate(String zccPropertyKey, IEsOperand neoPropertyValue) {
	}

	protected abstract void loadProperties(EsExecutionContext ecx)
			throws InterruptedException;

	public final void add(String zccPropertyKey, boolean allowUpdate, IEsOperand value) {
		if (zccPropertyKey == null) throw new IllegalArgumentException("key is null");
		if (value == null) throw new IllegalArgumentException("value is null");
		final EsProperty property = allowUpdate ? EsProperty.newDontDelete(value) : EsProperty.newReadOnlyDontDelete(value);
		keyToProperty().put(zccPropertyKey, property);
	}

	public final void add(String zccPropertyKey, EsProperty property) {
		if (zccPropertyKey == null) throw new IllegalArgumentException("key is null");
		if (property == null) throw new IllegalArgumentException("property is null");
		keyToProperty().put(zccPropertyKey, property);
	}

	public abstract EsObject createObject();

	public final boolean delete(String zccPropertyKey) {
		if (zccPropertyKey == null) throw new IllegalArgumentException("key is null");
		return keyToProperty().remove(zccPropertyKey) != null;
	}

	/**
	 * Returns a boolean value indicating whether a Put operation with property will succeed.
	 * 
	 * @see ECMA 8.6.2.3
	 * @param zccPropertyKey
	 *              [<i>1+ char</i>]
	 * @return true if Put will succeed.
	 */
	public final boolean esCanPut(String zccPropertyKey) {
		if (zccPropertyKey == null) throw new IllegalArgumentException("key is null");
		final EsProperty oProperty = getProperty(zccPropertyKey);
		return (oProperty == null) ? (m_oPrototype == null || m_oPrototype.esCanPut(zccPropertyKey)) : !oProperty.isReadOnly();
	}

	/**
	 * Returns a string value indicating the kind of object.
	 * 
	 * @see ECMA 8.6.2
	 * @return [<i>never null, always 1+ char</i>]
	 */
	public abstract String esClass();

	/**
	 * Removes the specified property from the object
	 * 
	 * @see ECMA 8.6.2.5
	 * @param zccPropertyKey
	 *              [<i>1+ char</i>]
	 * @return true if property is not a member of object
	 */
	public final boolean esDelete(String zccPropertyKey) {
		if (zccPropertyKey == null) throw new IllegalArgumentException("key is null");
		final EsProperty oProperty = getProperty(zccPropertyKey);
		if (oProperty == null) return true;
		if (oProperty.isDontDelete()) {
			final String m = "Prototype property '" + zccPropertyKey + "' cannot be deleted";
			throw new EsProtectionCodeException(m);
		}

		keyToProperty().remove(zccPropertyKey);
		return true;
	}

	/**
	 * Returns the value of the named property.
	 * 
	 * @see ECMA 8.6.2.1
	 * @param zccPropertyKey
	 *              [<i>1+ char</i>]
	 * @return [<i>never null</i>]
	 */
	public final IEsOperand esGet(String zccPropertyKey) {
		if (zccPropertyKey == null) throw new IllegalArgumentException("key is null");
		final EsProperty oProperty = getProperty(canonizePropertyKey(zccPropertyKey));
		if (oProperty == null) {
			if (m_oPrototype == null) return EsPrimitiveUndefined.Instance;
			return m_oPrototype.esGet(zccPropertyKey);
		}
		return oProperty.value;
	}

	/**
	 * Returns a boolean value indicating whether the object already has a member with the given name.
	 * 
	 * @see ECMA 8.6.2.4
	 * @param zccPropertyKey
	 *              [<i>1+ char</i>]
	 * @return true if has member with name.
	 */
	public final boolean esHasProperty(String zccPropertyKey) {
		if (zccPropertyKey == null) throw new IllegalArgumentException("key is null");
		final EsProperty oProperty = getProperty(canonizePropertyKey(zccPropertyKey));
		if (oProperty == null) {
			if (m_oPrototype == null) return false;
			return m_oPrototype.esHasProperty(zccPropertyKey);
		}
		return true;
	}

	public final List<String> esPropertyKeys(boolean includeEmptyKey) {
		if (noProperties()) return Collections.emptyList();

		final List<String> zlPropertyKeys = new ArrayList<String>(propertyCount());
		final Iterator<Map.Entry<String, EsProperty>> iEntries = keyToProperty().entrySet().iterator();
		while (iEntries.hasNext()) {
			final Map.Entry<String, EsProperty> entry = iEntries.next();
			final String zccPropertyKey = entry.getKey();
			if (includeEmptyKey || zccPropertyKey.length() > 0) {
				final EsProperty property = entry.getValue();
				if (!property.isDontEnum()) {
					zlPropertyKeys.add(zccPropertyKey);
				}
			}
		}
		Collections.sort(zlPropertyKeys);
		if (m_oPrototype != null) {
			zlPropertyKeys.addAll(m_oPrototype.esPropertyKeys(includeEmptyKey));
		}
		return zlPropertyKeys;
	}

	/**
	 * Returns a list containing the names of enumerable properties.
	 * 
	 * @return a list, possibly empty, of property names
	 */
	public final List<String> esPropertyNames() {
		return esPropertyKeys(false);
	}

	/**
	 * Returns the prototype of this object.
	 * 
	 * @see ECMA 8.6.2
	 * @return [<i>possibly null</i>] - only null for prototype of built-in object, or host objects
	 */
	public final EsObject esPrototype() {
		return m_oPrototype;
	}

	/**
	 * Sets the specified property to value.
	 * 
	 * @see ECMA 8.6.2.2
	 * @param zccPropertyKey
	 *              [<i>1+ char</i>]
	 * @param propertyValue
	 *              [<i>non null</i>]
	 * @return [<i>possibly null</i>]
	 */
	public final void esPut(String zccPropertyKey, IEsOperand propertyValue) {
		if (zccPropertyKey == null) throw new IllegalArgumentException("key is null");
		if (propertyValue == null) throw new IllegalArgumentException("propertyValue is null");

		final EsProperty oProperty = getProperty(zccPropertyKey);
		if (oProperty == null) {
			if (m_oPrototype != null && !m_oPrototype.esCanPut(zccPropertyKey)) {
				final String m = "Prototype property '" + zccPropertyKey + "' is read-only; its value cannot be modified";
				throw new EsProtectionCodeException(m);
			}
		} else {
			if (oProperty.isReadOnly()) {
				final String m = "Property '" + zccPropertyKey + "' is read-only; its value cannot be modified";
				throw new EsProtectionCodeException(m);
			}
		}

		final EsProperty property;
		if (oProperty == null) {
			property = EsProperty.newUndefined();
			keyToProperty().put(zccPropertyKey, property);
		} else {
			property = oProperty;
		}
		property.value = propertyValue;
		cascadeUpdate(zccPropertyKey, propertyValue);
	}

	public EsType esType() {
		return EsType.TObject;
	}

	public IEsOperand getByIndex(int index) {
		if (index < 0) throw new IllegalArgumentException("Invalid index:" + index);
		return esGet(UNeon.toPropertyName(index));
	}

	public final EsPrimitive internalDefaultValueHintNumber(EsExecutionContext ecx)
			throws InterruptedException {
		final IEsOperand oValueOfResult = evaluateThis(ecx, esGet(MethodName_valueOf));
		if (oValueOfResult instanceof EsPrimitive) return (EsPrimitive) oValueOfResult;
		final IEsOperand oToStringResult = evaluateThis(ecx, esGet(MethodName_toString));
		if (oToStringResult instanceof EsPrimitive) return (EsPrimitive) oToStringResult;

		throw new EsTypeCodeException(esClass() + "(" + show(1) + ") does not have a default value");
	}

	public final EsPrimitive internalDefaultValueHintString(EsExecutionContext ecx)
			throws InterruptedException {
		final IEsOperand oToStringResult = evaluateThis(ecx, esGet(MethodName_toString));
		if (oToStringResult instanceof EsPrimitive) return (EsPrimitive) oToStringResult;
		final IEsOperand oValueOfResult = evaluateThis(ecx, esGet(MethodName_valueOf));
		if (oValueOfResult instanceof EsPrimitive) return (EsPrimitive) oValueOfResult;

		throw new EsTypeCodeException(esClass() + "(" + show(1) + ") does not have a default value");
	}

	public JsonObject newJsonObject() {
		final List<String> propertyNames = esPropertyNames();
		final int pcount = propertyNames.size();
		final Map<String, IJsonNative> neoPropertyValueMap = new HashMap<String, IJsonNative>(pcount);
		for (int i = 0; i < pcount; i++) {
			final String pname = propertyNames.get(i);
			final IEsOperand esOperand = esGet(pname);
			final IJsonNative oJsonNative = esOperand.createJsonNative();
			if (oJsonNative != null) {
				neoPropertyValueMap.put(pname, oJsonNative);
			}
		}
		return JsonObject.newImmutable(neoPropertyValueMap);
	}

	public final EsProperty put(String zccPropertyKey, IEsOperand propertyValue) {
		if (zccPropertyKey == null) throw new IllegalArgumentException("key is null");
		if (propertyValue == null) throw new IllegalArgumentException("propertyValue is null");

		EsProperty vProperty = getProperty(zccPropertyKey);
		if (vProperty == null) {
			vProperty = EsProperty.newDefined(propertyValue, EsProperty.ATTMASK_DEFAULT);
			keyToProperty().put(zccPropertyKey, vProperty);
		} else {
			vProperty.value = propertyValue;
		}
		return vProperty;
	}

	public final EsProperty putLength(int value, boolean readOnly) {
		final EsProperty property = put(PropertyName_length, new EsPrimitiveNumberInteger(value));
		property.attributeMask = readOnly ? ATTMASK_LENGTH_READONLY : ATTMASK_LENGTH_MUTABLE;
		return property;
	}

	public final void putLengthMutable(int value) {
		putLength(value, false);
	}

	public final void putLengthReadOnly(int value) {
		putLength(value, true);
	}

	@Override
	public String show(int depth) {
		final StringBuilder b = new StringBuilder();
		b.append(esClass());
		if (depth > 0) {
			if (!noProperties()) {
				int pcount = 0;
				for (final Map.Entry<String, EsProperty> entry : keyToProperty().entrySet()) {
					final EsProperty property = entry.getValue();
					if (property.isDontEnum()) {
						continue;
					}
					final EsType valueType = property.value.esType();
					if (!valueType.isPublished) {
						continue;
					}
					if (pcount == 0) {
						b.append('{');
					} else {
						b.append(',');
					}
					b.append(entry.getKey());
					b.append("=");
					if (valueType == EsType.TString) {
						b.append("'");
					}
					b.append(property.value.show(depth - 1));
					if (valueType == EsType.TString) {
						b.append("'");
					}
					pcount++;
				}
				if (pcount > 0) {
					b.append('}');
				}
			}
		}
		return b.toString();
	}

	public boolean toCanonicalBoolean() {
		return true;
	}

	public String toCanonicalString(EsExecutionContext ecx)
			throws InterruptedException {
		loadProperties(ecx);
		final EsPrimitive primitive = internalDefaultValueHintString(ecx);
		return primitive.toCanonicalString(ecx);
	}

	public EsPrimitiveNumber toNumber(EsExecutionContext ecx)
			throws InterruptedException {
		loadProperties(ecx);
		final EsPrimitive primitive = internalDefaultValueHintNumber(ecx);
		return primitive.toNumber(ecx);
	}

	public EsObject toObject(EsExecutionContext ecx)
			throws InterruptedException {
		loadProperties(ecx);
		return this;
	}

	public EsPrimitive toPrimitive(EsExecutionContext ecx, EsType oPreference)
			throws InterruptedException {
		loadProperties(ecx);
		return (oPreference == EsType.TString) ? internalDefaultValueHintString(ecx) : internalDefaultValueHintNumber(ecx);
	}

	@Override
	public String toString() {
		return show(1);
	}

	protected EsObject() {
		m_oPrototype = null;
	}

	protected EsObject(EsObject prototype) {
		if (prototype == null) throw new IllegalArgumentException("prototype is null");
		m_oPrototype = prototype;
	}

	private final EsObject m_oPrototype;
	private Map<String, EsProperty> m_lzyKeyToProperty;
}
