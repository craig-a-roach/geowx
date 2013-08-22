/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emmail;

import com.metservice.argon.ArgonSplitter;
import com.metservice.argon.ArgonText;
import com.metservice.argon.ArgonTuple2;
import com.metservice.beryllium.BerylliumSmtpManager;
import com.metservice.beryllium.BerylliumSmtpUrl;
import com.metservice.neon.EmClass;
import com.metservice.neon.EmMethod;
import com.metservice.neon.EsApiCodeException;
import com.metservice.neon.EsArgumentAccessor;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsMethodAccessor;
import com.metservice.neon.EsPrimitiveString;
import com.metservice.neon.IEsOperand;

/**
 * @author roach
 */
class MailEmClass extends EmClass {

	private static final String Name = CClass.Mail;
	static final EmMethod[] Methods = { new method_configureSmtp(), new method_sender(), new method_toString() };

	static final MailEmClass Instance = new MailEmClass(Name, Methods);

	static ArgonTuple2<String, Integer> newHostPort(EsArgumentAccessor aHostPort)
			throws InterruptedException {
		assert aHostPort != null;
		final String qtwHostPort = aHostPort.qtwStringValue();
		final String[] zptqtwHostPort = ArgonSplitter.zptqtwSplit(qtwHostPort, ':');
		final int fc = zptqtwHostPort.length;
		if (fc == 0) throw new EsApiCodeException("Malformed " + aHostPort.qPath + ": " + qtwHostPort);
		if (fc == 1) return new ArgonTuple2<String, Integer>(zptqtwHostPort[0], CDefault.Port);
		final Integer oPort = ArgonText.parseInteger(zptqtwHostPort[1], null);
		if (oPort == null) throw new EsApiCodeException("Malformed " + aHostPort.qPath + " port field in: " + qtwHostPort);
		return new ArgonTuple2<String, Integer>(zptqtwHostPort[0], oPort);
	}

	static MailEm self(EsExecutionContext ecx) {
		return ecx.thisObject(Name, MailEm.class);
	}

	private MailEmClass(String qccClassName, EmMethod[] ozptMethods) {
		super(qccClassName, ozptMethods);
	}

	static class method_configureSmtp extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final MailEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final String qcctwId = ac.qtwStringValue(0);
			ArgonTuple2<String, Integer> hostPort = CDefault.HostPort;
			if (ac.specified(1)) {
				hostPort = newHostPort(ac.select(1));
			}
			final String qtwUserName = ac.defaulted(2) ? CDefault.UserName : ac.qtwStringValue(2);
			final String zcctwPassword = ac.defaulted(3) ? CDefault.Password : ac.ztwStringValue(3);
			final boolean secure = ac.defaulted(4) ? CDefault.Secure : ac.booleanValue(4);
			final BerylliumSmtpUrl url;
			if (hostPort.b < 0) {
				url = BerylliumSmtpUrl.newInstance(hostPort.a, qtwUserName);
			} else {
				url = BerylliumSmtpUrl.newInstance(hostPort.a, qtwUserName, hostPort.b);
			}
			final BerylliumSmtpManager imp = self.imp();
			imp.register(qcctwId, url, zcctwPassword, secure);
			return self;
		}

		public method_configureSmtp() {
			super("configureSmtp", 1, CArg.configurationId, CArg.smtpHostNamePort, CArg.userName, CArg.password, CArg.secure);
		}
	}

	static class method_sender extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final MailEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final String qcctwId = ac.defaulted(0) ? CDefault.ConfigurationId : ac.qtwStringValue(0);
			final BerylliumSmtpManager imp = self.imp();
			return new MailSenderEm(imp, qcctwId);
		}

		public method_sender() {
			super("sender", 0, CArg.configurationId);
		}
	}

	static class method_toString extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return new EsPrimitiveString(self(ecx).imp().reportConnectionStatus());
		}

		public method_toString() {
			super(StdName_toString);
		}
	}
}
