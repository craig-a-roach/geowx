/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author roach
 */
public class EsExecutionContext {

	private static final int ATTMASK_SELF = EsProperty.ATT_DONTDELETE | EsProperty.ATT_READONLY;
	private static final int ATTMASK_VAROBJ_FORMAL_PARAMETERS = EsProperty.ATT_DONTDELETE;
	private static final int ATTMASK_VAROBJ_FUNCTIONS = EsProperty.ATT_DONTDELETE;
	private static final int ATTMASK_VAROBJ_VARIABLES = EsProperty.ATT_DONTDELETE;

	public EsActivation activation() {
		if (m_variableObject instanceof EsActivation) return (EsActivation) m_variableObject;
		throw new EsInterpreterException("Activation object not available here");
	}

	public EsFunction callee() {
		if (m_oCallee == null) throw new EsInterpreterException("No callee");
		return m_oCallee;
	}

	public int depth() {
		return m_depth;
	}

	public EsExecutionContext getCalling() {
		return m_oCalling;
	}

	public EsGlobal global() {
		return m_global;
	}

	/**
	 * Test non-strict equality of operands according to ECMA 11.9.3 or strict equality of operands according to ECMA
	 * 11.9.6
	 * 
	 * @param lhs
	 *              [<i>non null</i>]
	 * @param rhs
	 *              [<i>non null</i>]
	 * @return true if equal
	 * @throws EsCodeException
	 * @throws EsInterpreterException
	 */
	public boolean isEqual(IEsOperand lhs, IEsOperand rhs, boolean strict)
			throws InterruptedException {
		if (lhs == null) throw new IllegalArgumentException("object is null");
		if (rhs == null) throw new IllegalArgumentException("object is null");
		final EsType lhsType = lhs.esType();
		final EsType rhsType = rhs.esType();
		if (lhsType == rhsType) {
			if (lhsType == EsType.TUndefined || lhsType == EsType.TNull) return true;
			if (lhsType == EsType.TNumber) {
				final EsPrimitiveNumber lhsNumber = (EsPrimitiveNumber) lhs;
				final EsPrimitiveNumber rhsNumber = (EsPrimitiveNumber) rhs;
				return EsPrimitiveNumber.canRelate(lhsNumber, rhsNumber) ? lhsNumber.sameNumberValue(rhsNumber) : false;
			} else if (lhsType == EsType.TString) {
				final EsPrimitiveString lhsString = (EsPrimitiveString) lhs;
				final EsPrimitiveString rhsString = (EsPrimitiveString) rhs;
				return lhsString.sameStringValue(rhsString);
			} else if (lhsType == EsType.TBoolean) {
				final EsPrimitiveBoolean lhsBoolean = (EsPrimitiveBoolean) lhs;
				final EsPrimitiveBoolean rhsBoolean = (EsPrimitiveBoolean) rhs;
				return lhsBoolean.sameBooleanValue(rhsBoolean);
			} else if (lhsType == EsType.TObject) return lhs == rhs;
			return false;
		}
		if (strict) return false;
		if (lhsType == EsType.TNull && rhsType == EsType.TUndefined) return true;
		if (lhsType == EsType.TUndefined && rhsType == EsType.TNull) return true;
		if (lhsType == EsType.TNumber && rhsType == EsType.TString) return isEqual(lhs, rhs.toNumber(this), false);
		if (lhsType == EsType.TString && rhsType == EsType.TNumber) return isEqual(lhs.toNumber(this), rhs, false);
		if (lhsType == EsType.TBoolean && rhsType == EsType.TNumber) return isEqual(lhs.toNumber(this), rhs, false);
		if (lhsType == EsType.TNumber && rhsType == EsType.TBoolean) return isEqual(lhs, rhs.toNumber(this), false);
		if ((lhsType == EsType.TString || lhsType == EsType.TNumber) && rhsType == EsType.TObject)
			return isEqual(lhs, rhs.toPrimitive(this, lhsType), false);
		if (lhsType == EsType.TObject && (rhsType == EsType.TString || rhsType == EsType.TNumber))
			return isEqual(lhs.toPrimitive(this, rhsType), rhs, false);
		return false;
	}

	/**
	 * Relational comparison of operands according to ECMA 11.8.5
	 * 
	 * @param lhs
	 *              [<i>non null</i>]
	 * @param rhs
	 *              [<i>non null</i>]
	 * @return [<i>possibly null</i>] TRUE if lhs is less than rhs, null if comparison is undefined
	 * @throws EsCodeException
	 * @throws EsInterpreterException
	 */
	public Boolean isLessThan(IEsOperand lhs, IEsOperand rhs)
			throws InterruptedException {
		final EsPrimitive lhsPrimitive = lhs.toPrimitive(this, EsType.TNumber);
		final EsPrimitive rhsPrimitive = rhs.toPrimitive(this, EsType.TNumber);
		final EsType lhsPrimitiveType = lhsPrimitive.esType();
		final EsType rhsPrimitiveType = rhsPrimitive.esType();
		if (lhsPrimitiveType == EsType.TString && rhsPrimitiveType == EsType.TString) {
			final EsPrimitiveString lhsString = (EsPrimitiveString) lhs;
			final EsPrimitiveString rhsString = (EsPrimitiveString) rhs;
			return Boolean.valueOf(lhsString.isLessThan(rhsString));
		}
		final EsPrimitiveNumber lhsNumber = lhs.toNumber(this);
		final EsPrimitiveNumber rhsNumber = rhs.toNumber(this);
		return EsPrimitiveNumber.canRelate(lhsNumber, rhsNumber) ? Boolean.valueOf(lhsNumber.isLessThan(rhsNumber)) : null;
	}

	/**
	 * Create a new function
	 * 
	 * @see ECMA 13.2
	 * @param callable
	 *              [<i>non null</i>] implementation
	 * @return [<i>never null</i>]
	 */
	public EsFunction newFunction(IEsCallable callable) {
		if (callable == null) throw new IllegalArgumentException("callable is null");
		final boolean isDeclared = callable.isDeclared();
		final String oqccName = callable.oqccName();
		final EsIntrinsicObject oSelfResolver;
		final EsScopeChain scope;
		if (isDeclared || oqccName == null) {
			oSelfResolver = null;
			scope = m_scopeChain;
		} else {
			oSelfResolver = m_global.newIntrinsicObject();
			scope = new EsScopeChain(m_scopeChain, oSelfResolver);
		}

		final EsFunction f = new EsFunction(m_global.prototypeFunction, scope, callable);
		if (oSelfResolver != null) {
			oSelfResolver.add(oqccName, EsProperty.newDefined(f, ATTMASK_SELF));
		}

		return f;
	}

	public EsExecutionContext newInstance(EsFunction callee, EsActivation activation, EsObject oThis) {
		return new EsExecutionContext(this, callee, activation, oThis);
	}

	public void populateVariableObject(List<String> ozlFormalParameters, Map<String, IEsCallable> ozmCallables,
			Set<String> ozsVariables) {
		if (ozlFormalParameters != null) {
			for (final String qccFormalParameter : ozlFormalParameters) {
				if (!m_variableObject.esHasProperty(qccFormalParameter)) {
					final EsProperty propFormalParameter = EsProperty.newUndefined(ATTMASK_VAROBJ_FORMAL_PARAMETERS);
					m_variableObject.add(qccFormalParameter, propFormalParameter);
				}
			}
		}

		if (ozmCallables != null) {
			for (final Map.Entry<String, IEsCallable> entry : ozmCallables.entrySet()) {
				final String qccFunctionName = entry.getKey();
				final IEsCallable callable = entry.getValue();
				final EsFunction function = newFunction(callable);
				function.enableConstruction(this);
				final EsProperty propFunction = EsProperty.newDefined(function, ATTMASK_VAROBJ_FUNCTIONS);
				m_variableObject.add(qccFunctionName, propFunction);
			}
		}

		if (ozsVariables != null) {
			for (final String qccVariable : ozsVariables) {
				if (!m_variableObject.esHasProperty(qccVariable)) {
					final EsProperty propVariable = EsProperty.newUndefined(ATTMASK_VAROBJ_VARIABLES);
					m_variableObject.add(qccVariable, propVariable);
				}
			}
		}
	}

	public EsScopeChain scopeChain() {
		return m_scopeChain;
	}

	public String showGlobal(int depth) {
		return m_global.show(depth);
	}

	public String showStackVariables(int depth) {
		final StringBuffer b = new StringBuffer();
		if (m_variableObject != m_global) {
			b.append(m_variableObject.show(depth));
			b.append('\n');
		}
		if (m_thisObject != m_global) {
			b.append("this:\n");
			b.append(m_thisObject.show(depth));
			b.append('\n');
		}
		return b.toString();
	}

	public EsObject thisObject() {
		return m_thisObject;
	}

	public <T extends EsObject> T thisObject(String esClass, Class<T> objectClass) {
		if (esClass == null || esClass.length() == 0) throw new IllegalArgumentException("esClass is empty");
		if (objectClass == null) throw new IllegalArgumentException("objectClass is null");

		if (objectClass.isInstance(m_thisObject)) return objectClass.cast(m_thisObject);
		throw new EsTypeCodeException("This is a '" + m_thisObject.esClass() + "' object, not a '" + esClass + "' object");
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(showStackVariables(1));
		sb.append(showGlobal(1));
		return sb.toString();
	}

	public EsObject variableObject() {
		return m_variableObject;
	}

	private EsExecutionContext(EsExecutionContext src, EsFunction callee, EsActivation activation, EsObject oThis) {
		assert src != null;
		assert callee != null;
		assert activation != null;

		m_oCalling = src;
		m_oCallee = callee;
		m_global = src.m_global;
		m_variableObject = activation;
		m_thisObject = (oThis == null) ? m_global : oThis;
		m_depth = src.m_depth + 1;
		EsScopeChain vScopeChain = callee.getScope();
		if (vScopeChain == null) {
			vScopeChain = new EsScopeChain(m_global);
		}
		m_scopeChain = new EsScopeChain(vScopeChain, activation);
	}

	EsExecutionContext(EsGlobal global) {
		assert global != null;
		m_oCalling = null;
		m_oCallee = null;
		m_global = global;
		m_variableObject = global;
		m_thisObject = global;
		m_depth = 0;
		m_scopeChain = new EsScopeChain(global);
	}

	private final EsExecutionContext m_oCalling;
	private final EsFunction m_oCallee;
	private final EsGlobal m_global;
	private final EsObject m_variableObject;
	private final EsObject m_thisObject;
	private final int m_depth;
	private final EsScopeChain m_scopeChain;
}
