/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.metservice.argon.Ds;

public class EsIntrinsicJvmSingleton extends EsIntrinsicSingleton {

	public static final String Name = "Jvm";

	public static final EsIntrinsicMethod[] Methods = { method_callStatic(), method_sleep() };

	private static Object createJavaBoolean(EsExecutionContext ecx, int jpindex, IEsOperand datum)
			throws InterruptedException {
		if (!datum.esType().isDatum) return Boolean.FALSE;
		return Boolean.valueOf(datum.toCanonicalBoolean());
	}

	private static Object createJavaDouble(EsExecutionContext ecx, int jpindex, IEsOperand datum)
			throws InterruptedException {
		if (!datum.esType().isDatum) return new Double(0.0);
		return new Double(datum.toNumber(ecx).doubleValue());
	}

	private static Object createJavaInteger(EsExecutionContext ecx, int jpindex, IEsOperand datum)
			throws InterruptedException {
		if (!datum.esType().isDatum) return new Integer(0);
		return new Integer(datum.toNumber(ecx).intVerified());
	}

	private static Object createJavaLong(EsExecutionContext ecx, int jpindex, IEsOperand datum)
			throws InterruptedException {
		if (!datum.esType().isDatum) return new Long(0L);
		return new Long(datum.toNumber(ecx).longValue());
	}

	private static Object createJavaObject(EsExecutionContext ecx, Method method, int jpindex, Class<?> pclass, IEsOperand datum)
			throws InterruptedException {
		if (pclass.equals(String[].class)) return createJavaStringArray(ecx, method, jpindex, datum);
		if (pclass.equals(int.class) || pclass.equals(Integer.class)) return createJavaInteger(ecx, jpindex, datum);
		if (pclass.equals(boolean.class) || pclass.equals(Boolean.class)) return createJavaBoolean(ecx, jpindex, datum);
		if (pclass.equals(String.class)) return createJavaString(ecx, jpindex, datum);
		if (pclass.equals(long.class) || pclass.equals(Long.class)) return createJavaLong(ecx, jpindex, datum);
		if (pclass.equals(double.class) || pclass.equals(Double.class)) return createJavaDouble(ecx, jpindex, datum);
		throw new UnsupportedOperationException("Conversion to " + pclass + " not yet implemented");
	}

	private static Object createJavaString(EsExecutionContext ecx, int jpindex, IEsOperand datum)
			throws InterruptedException {
		if (!datum.esType().isDatum) return null;
		return datum.toCanonicalString(ecx);
	}

	private static Object createJavaStringArray(EsExecutionContext ecx, Method method, int jpindex, IEsOperand datum)
			throws InterruptedException {
		if (!datum.esType().isDatum) return null;
		final EsObject esobject = datum.toObject(ecx);
		if (esobject instanceof EsIntrinsicArray) {
			final EsIntrinsicArray esarray = (EsIntrinsicArray) esobject;
			final int length = esarray.length();
			final String[] zpt = new String[length];
			for (int i = 0; i < length; i++) {
				final IEsOperand ese = esarray.getByIndex(i);
				zpt[i] = ese.esType().isDatum ? ese.toCanonicalString(ecx) : "";
			}
			return zpt;
		}
		final String m = "Argument " + jpindex + " passed to " + method + " must be an array";
		throw new EsApiCodeException(m);
	}

	private static EsIntrinsicMethod method_callStatic() {
		return new EsIntrinsicMethod("callStatic", new String[] { "className", "methodName" }, 2) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ma = new EsMethodAccessor(ecx);
				final String qtwClassName = ma.qtwStringValue(0);
				final Class<?> jclass = selectClass(ecx, qtwClassName);
				final String qtwMethodName = ma.qtwStringValue(1);
				final Class<?>[] zptSignature = zptSignature(ma);
				final Method method = selectMethod(jclass, qtwMethodName, zptSignature);
				final Object[] javaArgs = zptJavaArgs(ecx, method, ma);
				final InvokeResult invokeResult = invoke(jclass, method, null, javaArgs);
				if (invokeResult.oqErm == null) return invokeResult.result;
				throw new EsApiCodeException(invokeResult.oqErm);
			}
		};
	}

	private static EsIntrinsicMethod method_sleep() {
		return new EsIntrinsicMethod("sleep", new String[] { "interval" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ma = new EsMethodAccessor(ecx);
				final long msSleep = Math.max(0L, ma.longValue(0));
				Thread.sleep(msSleep);
				return EsPrimitiveUndefined.Instance;
			}
		};
	}

	private static IEsOperand newEsBoolean(Object result) {
		if (result instanceof Boolean) {
			final Boolean r = (Boolean) result;
			return EsPrimitiveBoolean.instance(r.booleanValue());
		}
		return EsPrimitiveUndefined.Instance;
	}

	private static IEsOperand newEsDouble(Object result) {
		if (result instanceof Double) {
			final Double r = (Double) result;
			return new EsPrimitiveNumberDouble(r.doubleValue());
		}
		return EsPrimitiveUndefined.Instance;
	}

	private static IEsOperand newEsInteger(Object result) {
		if (result instanceof Long) {
			final Long r = (Long) result;
			return new EsPrimitiveNumberInteger(r.longValue());
		}
		if (result instanceof Integer) {
			final Integer r = (Integer) result;
			return new EsPrimitiveNumberInteger(r.intValue());
		}
		return EsPrimitiveUndefined.Instance;
	}

	private static IEsOperand newEsString(Object result) {
		return new EsPrimitiveString(result);
	}

	private static InvokeResult newInvokeResult(Class<?> classReturn, Object result) {
		IEsOperand oOperand = null;
		if (classReturn.equals(int.class) || classReturn.equals(Integer.class)) {
			oOperand = newEsInteger(result);
		} else if (classReturn.equals(boolean.class) || classReturn.equals(Boolean.class)) {
			oOperand = newEsBoolean(result);
		} else if (classReturn.equals(long.class) || classReturn.equals(Long.class)) {
			oOperand = newEsInteger(result);
		} else if (classReturn.equals(double.class) || classReturn.equals(Double.class)) {
			oOperand = newEsDouble(result);
		} else if (classReturn.equals(String.class)) {
			oOperand = newEsString(result);
		}
		if (oOperand == null) {
			final String m = "The method return type (" + classReturn.getSimpleName() + " is not supported";
			return new InvokeResult(m);
		}
		return new InvokeResult(oOperand);
	}

	static Class<?> findClass(EsExecutionContext ecx, String className) {
		try {
			return Class.forName(className, true, ecx.getClass().getClassLoader());
		} catch (final ClassNotFoundException ex) {
		}
		return null;
	}

	static Method findMethod(Class<?> jclass, String qtwMethodName, Class<?>[] zptSignature) {
		try {
			return jclass.getMethod(qtwMethodName, zptSignature);
		} catch (final SecurityException ex) {
		} catch (final NoSuchMethodException ex) {
		}
		return null;
	}

	static InvokeResult invoke(Class<?> jclass, Method method, Object oInstance, Object[] javaArgs) {
		final String mn = method.getName();
		try {
			final Object oResult = method.invoke(oInstance, javaArgs);
			final Class<?> oClassReturn = method.getReturnType();
			final boolean isVoid = (oClassReturn == null || oClassReturn.equals(void.class));
			if (isVoid) return new InvokeResult(EsPrimitiveUndefined.Instance);
			if (oResult == null) return new InvokeResult(EsPrimitiveNull.Instance);
			return newInvokeResult(oClassReturn, oResult);
		} catch (final IllegalArgumentException ex) {
			final String erm = "Parameter mismatch when attempting to call '" + mn + "'.";
			return new InvokeResult(erm, method, ex);
		} catch (final IllegalAccessException ex) {
			final String erm = "Not permitted to call java method '" + mn + "'.";
			return new InvokeResult(erm, method, ex);
		} catch (final InvocationTargetException ex) {
			final String erm = "The java method '" + mn + "' was called, but the implementation threw an exception.";
			return new InvokeResult(erm, method, ex.getCause());
		}
	}

	static String qtwSignature(Class<?>[] zptSignature) {
		final StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (int i = 0; i < zptSignature.length; i++) {
			final Class<?> aclass = zptSignature[i];
			if (sb.length() > 1) {
				sb.append(", ");
			}
			sb.append(aclass.getSimpleName());
		}
		sb.append(')');
		return sb.toString();
	}

	static Class<?> selectClass(EsExecutionContext ecx, String className) {
		final Class<?> oClass = findClass(ecx, className);
		if (oClass == null) {
			final String cp = System.getProperty("java.class.path");
			final String m = "Could not find class '" + className + "' on class path '" + cp + "'";
			throw new EsApiCodeException(m);
		}
		return oClass;
	}

	static Method selectMethod(Class<?> jclass, String qtwMethodName, Class<?>[] zptSignature) {
		final Method oMethod = findMethod(jclass, qtwMethodName, zptSignature);
		if (oMethod == null) {
			final String cn = jclass.getCanonicalName();
			final String ms = qtwMethodName + qtwSignature(zptSignature);
			final String m = "Java class '" + cn + "' does not declare an accessible method '" + ms + "'";
			throw new EsApiCodeException(m);
		}
		return oMethod;
	}

	static Object[] zptJavaArgs(EsExecutionContext ecx, Method method, EsMethodAccessor ma)
			throws InterruptedException {
		final Class<?>[] zptParameterTypes = method.getParameterTypes();
		final int cp = zptParameterTypes.length;
		final Object[] ztargs = new Object[cp];
		for (int ip = 0, ima = 3; ip < cp; ip++, ima += 2) {
			final Class<?> pclass = zptParameterTypes[ip];
			final IEsOperand value = ma.esOperand(ima, true, false, false);
			ztargs[ip] = createJavaObject(ecx, method, ip, pclass, value);
		}
		return ztargs;
	}

	static Class<?>[] zptSignature(EsMethodAccessor ma)
			throws InterruptedException {
		final int cma = ma.argc;
		final int csig = (cma - 2) / 2;
		final Class<?>[] zptSignature = new Class<?>[csig];
		for (int ima = 2, isig = 0; ima < cma; ima += 2) {
			final String qtwDecl = ma.qtwStringValue(ima);
			Class<?> oClassArg = null;
			if (qtwDecl.equals("String")) {
				oClassArg = String.class;
			} else if (qtwDecl.equals("String[]")) {
				oClassArg = String[].class;
			} else if (qtwDecl.equals("int")) {
				oClassArg = int.class;
			} else if (qtwDecl.equals("Integer")) {
				oClassArg = Integer.class;
			} else if (qtwDecl.equals("boolean")) {
				oClassArg = boolean.class;
			} else if (qtwDecl.equals("Boolean")) {
				oClassArg = Boolean.class;
			} else if (qtwDecl.equals("long")) {
				oClassArg = long.class;
			} else if (qtwDecl.equals("Long")) {
				oClassArg = Long.class;
			} else if (qtwDecl.equals("Double")) {
				oClassArg = Double.class;
			} else if (qtwDecl.equals("double")) {
				oClassArg = double.class;
			}
			if (oClassArg == null) {
				final String m = "Unsupported Java parameter type '" + qtwDecl + "'";
				throw new EsApiCodeException(m);
			}
			zptSignature[isig] = oClassArg;
			isig++;
		}
		return zptSignature;
	}

	public static EsIntrinsicJvmSingleton declare(EsIntrinsicObject prototype) {
		final EsIntrinsicJvmSingleton self = new EsIntrinsicJvmSingleton(prototype);
		return self;
	}

	private EsIntrinsicJvmSingleton(EsIntrinsicObject prototype) {
		super(prototype, Name);
	}

	private static class InvokeResult {

		@Override
		public String toString() {
			return oqErm == null ? result.toString() : oqErm;
		}

		public InvokeResult(IEsOperand result) {
			assert result != null;
			this.oqErm = null;
			this.result = result;
		}

		public InvokeResult(String qErm) {
			assert qErm != null && qErm.length() > 0;
			this.oqErm = qErm;
			this.result = EsPrimitiveUndefined.Instance;
		}

		public InvokeResult(String msg, Method method, Throwable cause) {
			this.oqErm = msg + "\nJava Method: " + method + "\nJava Exception...\n" + Ds.format(cause);
			this.result = EsPrimitiveUndefined.Instance;
		}

		final String oqErm;
		final IEsOperand result;
	}
}
