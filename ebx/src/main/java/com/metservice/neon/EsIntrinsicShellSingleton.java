/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

public class EsIntrinsicShellSingleton extends EsIntrinsicSingleton {

	private static final String[] Args_assertEquals = { "description", "expected", "actual" };
	private static final int ArgCReqd_assertEquals = 2;

	public static final String Name = "Shell";

	public static final EsIntrinsicMethod[] Methods = { method_trace(), method_fail(), method_assertEquals(),
			method_assertEqualsStrict() };

	private static EsIntrinsicMethod method_assertEquals() {
		return new EsIntrinsicMethod("assertEquals", Args_assertEquals, ArgCReqd_assertEquals) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				assertEquals(ecx, false);
				return EsPrimitiveUndefined.Instance;
			}
		};
	}

	private static EsIntrinsicMethod method_assertEqualsStrict() {
		return new EsIntrinsicMethod("assertEqualsStrict", Args_assertEquals, ArgCReqd_assertEquals) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				assertEquals(ecx, true);
				return EsPrimitiveUndefined.Instance;
			}
		};
	}

	private static EsIntrinsicMethod method_fail() {
		return new EsIntrinsicMethod("fail", new String[] { "value" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				evalEmit(ecx, NeonShell.EmitType.Fail);
				return EsPrimitiveUndefined.Instance;
			}
		};
	}

	private static EsIntrinsicMethod method_trace() {
		return new EsIntrinsicMethod("trace", new String[] { "value" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				evalEmit(ecx, NeonShell.EmitType.Trace);
				return EsPrimitiveUndefined.Instance;
			}
		};
	}

	static void assertEquals(EsExecutionContext ecx, boolean strict)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final String zDescription = ac.zStringValue(0);
		final IEsOperand expected = ac.esOperand(1);
		final IEsOperand oActual = ac.defaulted(2) ? null : ac.esOperand(2);
		final ShellHook shellHook = ecx.global().shellHook;
		final NeonShell shell = shellHook.shell;
		final String qccSourcePath = shellHook.source.qccPath();
		final String qTypeExpected = UNeon.subTypeName(expected);
		final String zExpected = expected.toCanonicalString(ecx);
		if (oActual == null) {
			final String m = zDescription + " {" + zExpected + "}" + qTypeExpected;
			shell.emit(qccSourcePath, NeonShell.EmitType.Trace, m);
			return;
		}

		final boolean isEqual = ecx.isEqual(expected, oActual, strict);
		if (isEqual) {
			final String m = "ASSERT " + zDescription + " PASS {" + zExpected + "}" + qTypeExpected;
			shell.emit(qccSourcePath, NeonShell.EmitType.Trace, m);
			return;
		}
		final String zActual = oActual.toCanonicalString(ecx);
		final String qTypeActual = UNeon.subTypeName(oActual);
		final String me = "ASSERT " + zDescription + " FAIL {" + zExpected + "}" + qTypeExpected + " <<<EXPECTED";
		final String ma = "ASSERT " + zDescription + " FAIL {" + zActual + "}" + qTypeActual + " <<<ACTUAL";
		shell.emit(qccSourcePath, NeonShell.EmitType.Fail, me);
		shell.emit(qccSourcePath, NeonShell.EmitType.Fail, ma);
		throw new EsAssertException(zDescription, zExpected, qTypeExpected, zActual, qTypeActual);
	}

	static void evalEmit(EsExecutionContext ecx, NeonShell.EmitType emitType)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final ShellHook shellHook = ecx.global().shellHook;
		final NeonShell shell = shellHook.shell;
		final String qccSourcePath = shellHook.source.qccPath();
		for (int i = 0; i < ac.argc; i++) {
			final String zValue = ac.esOperand(i).toCanonicalString(ecx);
			shell.emit(qccSourcePath, emitType, zValue);
		}
	}

	public static EsIntrinsicShellSingleton declare(EsIntrinsicObject prototype) {
		final EsIntrinsicShellSingleton self = new EsIntrinsicShellSingleton(prototype);
		return self;
	}

	private EsIntrinsicShellSingleton(EsIntrinsicObject prototype) {
		super(prototype, Name);
	}
}
