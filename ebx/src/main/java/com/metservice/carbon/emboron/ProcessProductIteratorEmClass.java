/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import java.util.concurrent.TimeUnit;

import com.metservice.boron.BoronApiException;
import com.metservice.boron.BoronProductIterator;
import com.metservice.boron.IBoronProduct;
import com.metservice.neon.EmClass;
import com.metservice.neon.EmException;
import com.metservice.neon.EmMethod;
import com.metservice.neon.EsMethodAccessor;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsPrimitiveBoolean;
import com.metservice.neon.EsPrimitiveString;
import com.metservice.neon.IEsOperand;

/**
 * @author roach
 */
class ProcessProductIteratorEmClass extends EmClass {

	private static final String Name = CClass.ProcessProductIterator;
	static final EmMethod[] Methods = { new method_hasNext(), new method_next(), new method_toString() };

	static final ProcessProductIteratorEmClass Instance = new ProcessProductIteratorEmClass(Name, Methods);

	static ProcessProductIteratorEm self(EsExecutionContext ecx) {
		return ecx.thisObject(Name, ProcessProductIteratorEm.class);
	}

	private ProcessProductIteratorEmClass(String qccClassName, EmMethod[] ozptMethods) {
		super(qccClassName, ozptMethods);
	}

	static class method_hasNext extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return EsPrimitiveBoolean.instance(self(ecx).biterator().hasNext());
		}

		public method_hasNext() {
			super("hasNext");
		}
	}

	static class method_next extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			try {
				final BoronProductIterator biterator = self(ecx).biterator();
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final IBoronProduct product;
				if (ac.defaulted(0)) {
					product = biterator.next();
				} else {
					final long smsTimeout = ac.smsElapsedValue(0);
					product = biterator.next(smsTimeout, TimeUnit.MILLISECONDS);
				}
				return new ProcessProductEm(product);
			} catch (final BoronApiException ex) {
				throw new EmException(ex);
			}
		}

		public method_next() {
			super("next", 0, CArg.timeout);
		}
	}

	static class method_toString extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return new EsPrimitiveString(self(ecx).biterator());
		}

		public method_toString() {
			super(StdName_toString);
		}
	}
}
