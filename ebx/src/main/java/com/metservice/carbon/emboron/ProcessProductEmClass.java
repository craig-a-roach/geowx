/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import com.metservice.neon.EmClass;
import com.metservice.neon.EmMethod;
import com.metservice.neon.EsArgumentAccessor;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsPrimitiveString;
import com.metservice.neon.IEsOperand;

/**
 * @author roach
 */
class ProcessProductEmClass extends EmClass {

	private static final String Name = CClass.ProcessProduct;

	static final EmMethod[] Methods = { new method_toString() };
	static final ProcessProductEmClass Instance = new ProcessProductEmClass(Name, Methods);

	static ProcessProductEm arg(EsArgumentAccessor aa)
			throws InterruptedException {
		return aa.esObject(Name, ProcessProductEm.class);
	}

	static ProcessProductEm self(EsExecutionContext ecx) {
		return ecx.thisObject(Name, ProcessProductEm.class);
	}

	public ProcessProductEmClass(String qccClassName, EmMethod[] ozptMethods) {
		super(qccClassName, ozptMethods);
	}

	static class method_toString extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return new EsPrimitiveString(self(ecx).bproduct());
		}

		public method_toString() {
			super(StdName_toString);
		}
	}
}
