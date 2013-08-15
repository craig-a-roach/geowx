/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import com.metservice.argon.Binary;
import com.metservice.neon.EmClass;
import com.metservice.neon.EmConstructor;
import com.metservice.neon.EmMethod;
import com.metservice.neon.EmObject;
import com.metservice.neon.EsMethodAccessor;
import com.metservice.neon.EsArgumentAccessor;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsIntrinsicBinary;
import com.metservice.neon.EsIntrinsicXmlEncoder;
import com.metservice.neon.EsObject;
import com.metservice.neon.EsPrimitiveString;
import com.metservice.neon.IEsOperand;

/**
 * @author roach
 */
class ScriptEmClass extends EmClass {

	private static final String Name = CClass.Script;
	static final EmMethod[] Methods = { new method_addResource(), new method_toString() };

	static final ScriptEmClass Instance = new ScriptEmClass(Name, Methods);

	static final ctor Constructor = new ctor();

	static ScriptEm arg(EsArgumentAccessor aa)
			throws InterruptedException {
		return aa.esObject(Name, ScriptEm.class);
	}

	static ScriptEm self(EsExecutionContext ecx) {
		return ecx.thisObject(Name, ScriptEm.class);
	}

	private ScriptEmClass(String qccClassName, EmMethod[] ozptMethods) {
		super(qccClassName, ozptMethods);
	}

	static class ctor extends EmConstructor {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final ScriptEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			self.construct(ac.select(0).xlzStringValuesEvery());
			return self;
		}

		@Override
		public EmObject declarePrototype() {
			return new ScriptEm(Instance);
		}

		public ctor() {
			super(Name, 1, CArg.lines);
		}
	}

	static class method_addResource extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final ScriptEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final String qtwPath = ac.qtwStringValue(0);
			final EsObject content = ac.esObject(1);
			final Binary binaryContent;
			if (content instanceof EsIntrinsicXmlEncoder) {
				final EsIntrinsicXmlEncoder xmlEncoder = (EsIntrinsicXmlEncoder) content;
				binaryContent = xmlEncoder.newBinary(ecx);
			} else if (content instanceof EsIntrinsicBinary) {
				final EsIntrinsicBinary binary = (EsIntrinsicBinary) content;
				binaryContent = binary.value();
			} else {
				final String zContent = content.toCanonicalString(ecx);
				binaryContent = Binary.newFromStringUTF8(zContent);
			}
			self.addResource(qtwPath, binaryContent);
			return self;
		}

		public method_addResource() {
			super("addResource", 2, CArg.path, CArg.content);
		}
	}

	static class method_toString extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return new EsPrimitiveString(self(ecx));
		}

		public method_toString() {
			super(StdName_toString);
		}
	}

}
