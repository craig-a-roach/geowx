/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import java.io.File;

import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Binary;
import com.metservice.boron.BoronApiException;
import com.metservice.neon.EmClass;
import com.metservice.neon.EmException;
import com.metservice.neon.EmMethod;
import com.metservice.neon.EsApiCodeException;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsMethodAccessor;
import com.metservice.neon.EsPrimitiveNull;
import com.metservice.neon.EsPrimitiveString;
import com.metservice.neon.IEsOperand;

/**
 * @author roach
 */
class ProcessCompletionEmClass extends EmClass {

	private static final String Name = CClass.ProcessCompletion;
	static final EmMethod[] Methods = { new method_loadFile(), new method_diagnostic(), new method_toString() };

	static final ProcessCompletionEmClass Instance = new ProcessCompletionEmClass(Name, Methods);

	static ProcessCompletionEm self(EsExecutionContext ecx) {
		return ecx.thisObject(Name, ProcessCompletionEm.class);
	}

	private ProcessCompletionEmClass(String qccClassName, EmMethod[] ozptMethods) {
		super(qccClassName, ozptMethods);
	}

	static class method_diagnostic extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return new EsPrimitiveString(self(ecx).diagnostic());
		}

		public method_diagnostic() {
			super("diagnostic");
		}
	}

	static class method_loadFile extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final ProcessCompletionEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final String qtwRelative = ac.qtwStringValue(0);
			final int bcQuota = ac.defaulted(1) ? Integer.MAX_VALUE : ac.intValue(2);
			try {
				final File cndirProcess = self.cndirProcess();
				final File srcFile = new File(cndirProcess, qtwRelative);
				final Binary oContent = Binary.createFromFile(srcFile, bcQuota);
				return oContent == null ? EsPrimitiveNull.Instance : ecx.global().newIntrinsicBinary(oContent);
			} catch (final BoronApiException ex) {
				throw new EsApiCodeException(ex);
			} catch (final ArgonQuotaException ex) {
				throw new EsApiCodeException(ex);
			} catch (final ArgonStreamReadException ex) {
				throw new EmException(ex);
			}
		}

		public method_loadFile() {
			super("loadFile", 1, CArg.path, CArg.quota);
		}
	}

	static class method_toString extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return new EsPrimitiveString(self(ecx).toString());
		}

		public method_toString() {
			super(StdName_toString);
		}
	}
}
