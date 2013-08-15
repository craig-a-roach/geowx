/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import com.metservice.boron.BoronProductCancellation;
import com.metservice.boron.BoronProductExitCode;
import com.metservice.boron.BoronProductInterpreterFailure;
import com.metservice.boron.BoronProductManagementFailure;
import com.metservice.boron.BoronProductStreamEnd;
import com.metservice.boron.BoronProductStreamLine;
import com.metservice.boron.BoronProductStreamWarnDecode;
import com.metservice.boron.IBoronProduct;
import com.metservice.neon.EmViewObject;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsPrimitiveString;

/**
 * @author roach
 */
class ProcessProductEm extends EmViewObject {

	private static final String pname_isStdErr = "isStdErr";
	private static final String pname_text = "text";
	private static final String pname_code = "code";
	private static final String pname_diagnostic = "diagnostic";
	private static final String pname_type = "type";

	private static final EsPrimitiveString TypeStreamLine = new EsPrimitiveString("StreamLine");
	private static final EsPrimitiveString TypeStreamEnd = new EsPrimitiveString("StreamEnd");
	private static final EsPrimitiveString TypeExitCode = new EsPrimitiveString("ExitCode");
	private static final EsPrimitiveString TypeCancellation = new EsPrimitiveString("Cancellation");
	private static final EsPrimitiveString TypeFailure = new EsPrimitiveString("Failure");
	private static final EsPrimitiveString TypeWarning = new EsPrimitiveString("Warning");
	private static final EsPrimitiveString TextFailureManagement = new EsPrimitiveString("Management");
	private static final EsPrimitiveString TextFailureInterpreter = new EsPrimitiveString("Interpreter");
	private static final EsPrimitiveString TextWarningDecode = new EsPrimitiveString("Decode");

	public IBoronProduct bproduct() {
		return m_product;
	}

	@Override
	public void putProperties(EsExecutionContext ecx)
			throws InterruptedException {

		if (m_product instanceof BoronProductStreamLine) {
			final BoronProductStreamLine psl = (BoronProductStreamLine) m_product;
			putView(pname_type, TypeStreamLine);
			putViewBoolean(pname_isStdErr, psl.isStdErr());
			putView(pname_text, psl.zLine());
			return;
		}

		if (m_product instanceof BoronProductStreamEnd) {
			final BoronProductStreamEnd pse = (BoronProductStreamEnd) m_product;
			putView(pname_type, TypeStreamEnd);
			putViewBoolean(pname_isStdErr, pse.isStdErr());
			return;
		}

		if (m_product instanceof BoronProductExitCode) {
			final BoronProductExitCode pec = (BoronProductExitCode) m_product;
			putView(pname_type, TypeExitCode);
			putViewInteger(pname_code, pec.exitCode().value());
			return;
		}

		if (m_product instanceof BoronProductCancellation) {
			putView(pname_type, TypeCancellation);
			return;
		}

		if (m_product instanceof BoronProductManagementFailure) {
			putView(pname_type, TypeFailure);
			putView(pname_text, TextFailureManagement);
			return;
		}

		if (m_product instanceof BoronProductInterpreterFailure) {
			final BoronProductInterpreterFailure pif = (BoronProductInterpreterFailure) m_product;
			putView(pname_type, TypeFailure);
			putView(pname_text, TextFailureInterpreter);
			putView(pname_diagnostic, pif.diagnostic().ztwReason());
			return;
		}

		if (m_product instanceof BoronProductStreamWarnDecode) {
			final BoronProductStreamWarnDecode pwd = (BoronProductStreamWarnDecode) m_product;
			putView(pname_type, TypeWarning);
			putView(pname_text, TextWarningDecode);
			putViewBoolean(pname_isStdErr, pwd.isStdErr());
			return;
		}
	}

	public ProcessProductEm(IBoronProduct product) {
		super(ProcessProductEmClass.Instance);
		m_product = product;
	}

	private final IBoronProduct m_product;

}
