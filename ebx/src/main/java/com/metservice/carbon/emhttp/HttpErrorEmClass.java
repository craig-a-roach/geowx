/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import javax.servlet.http.HttpServletResponse;

import com.metservice.neon.EmClass;
import com.metservice.neon.EmConstructor;
import com.metservice.neon.EmMethod;
import com.metservice.neon.EmObject;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsMethodAccessor;
import com.metservice.neon.EsPrimitiveString;
import com.metservice.neon.IEsOperand;

/**
 * @author roach
 */
class HttpErrorEmClass extends EmClass {

	private static final String Name = CClass.HttpError;
	static final EmMethod[] Methods = { new method_toString() };

	static final HttpErrorEmClass Instance = new HttpErrorEmClass(Name, Methods);

	static final ctor Constructor = new ctor();

	static HttpErrorEm self(EsExecutionContext ecx) {
		return ecx.thisObject(Name, HttpErrorEm.class);
	}

	private HttpErrorEmClass(String qccClassName, EmMethod[] ozptMethods) {
		super(qccClassName, ozptMethods);
	}

	static class ctor extends EmConstructor {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final HttpErrorEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final String qtwStatusCode = ac.qtwStringValue(0);
			final String ztwMessage = ac.defaulted(1) ? "" : ac.ztwStringValue(1);
			final HttpError he = HttpError.newInstance(qtwStatusCode, ztwMessage, HttpServletResponse.SC_FORBIDDEN);
			self.constructError(he);
			return self;
		}

		@Override
		public EmObject declarePrototype() {
			return new HttpErrorEm(Instance);
		}

		public ctor() {
			super(Name, 1, CArg.statusCode, CArg.message);
		}
	}

	static class method_toString extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return new EsPrimitiveString(self(ecx).newTuple(ecx).format());
		}

		public method_toString() {
			super(StdName_toString);
		}
	}

}
