/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emmail;

import com.metservice.beryllium.BerylliumApiException;
import com.metservice.beryllium.BerylliumSmtpEnvelope;
import com.metservice.beryllium.BerylliumSmtpManager;
import com.metservice.beryllium.BerylliumSmtpRatePolicy;
import com.metservice.beryllium.IBerylliumSmtpContent;
import com.metservice.neon.EmClass;
import com.metservice.neon.EmMethod;
import com.metservice.neon.EsApiCodeException;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsMethodAccessor;
import com.metservice.neon.EsObject;
import com.metservice.neon.EsObjectAccessor;
import com.metservice.neon.EsPrimitiveString;
import com.metservice.neon.IEsOperand;

/**
 * @author roach
 */
class MailSenderEmClass extends EmClass {

	private static final String Name = CClass.MailSender;
	static final EmMethod[] Methods = { new method_setFrom(), new method_from(), new method_setToList(), new method_toList(),
			new method_setCcList(), new method_ccList(), new method_setRatePolicy(), new method_ratePolicy(),
			new method_push(), new method_toString() };

	static final MailSenderEmClass Instance = new MailSenderEmClass(Name, Methods);

	static MailSenderEm self(EsExecutionContext ecx) {
		return ecx.thisObject(Name, MailSenderEm.class);
	}

	private MailSenderEmClass(String qccClassName, EmMethod[] ozptMethods) {
		super(qccClassName, ozptMethods);
	}

	static class method_ccList extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return new EsPrimitiveString(self(ecx).ztwCcList());
		}

		public method_ccList() {
			super("ccList");
		}
	}

	static class method_from extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return new EsPrimitiveString(self(ecx).qlcFromAddress());
		}

		public method_from() {
			super("from");
		}
	}

	static class method_push extends EmMethod {

		static final int arg_sbj = 0;
		static final int arg_bod = 1;
		static final int arg_to = 2;
		static final int arg_cc = 3;
		static final int arg_pol = 4;

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final MailSenderEm self = self(ecx);
			final String qlcFrom = self.qlcFromAddress();
			final String ztwBaseToList = self.ztwToList();
			final String ztwBaseCcList = self.ztwCcList();
			final BerylliumSmtpRatePolicy policyDefault = self.policy();
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final String qtwSubject = ac.qtwStringValue(arg_sbj);
			final EsObject esBody = ac.esObject(arg_bod);
			final String ztwExtToList = ac.defaulted(arg_to) ? "" : ac.ztwStringValue(arg_to);
			final String ztwExtCcList = ac.defaulted(arg_cc) ? "" : ac.ztwStringValue(arg_cc);
			final EsObjectAccessor oaPolicy = ac.defaulted(arg_pol) ? null : ac.select(arg_pol).createObjectAccessor();
			final String ztwToList = BerylliumSmtpEnvelope.ztwComposeAddressList(ztwBaseToList, ztwExtToList);
			final String ztwCcList = BerylliumSmtpEnvelope.ztwComposeAddressList(ztwBaseCcList, ztwExtCcList);
			final BerylliumSmtpRatePolicy policy = oaPolicy == null ? policyDefault : RatePolicyFactory.newPolicy(oaPolicy);
			try {
				final BerylliumSmtpEnvelope env = BerylliumSmtpEnvelope.newInstance(qtwSubject, qlcFrom, ztwToList,
						ztwCcList);
				final IBerylliumSmtpContent content = BodyFactory.newContent(ecx, esBody);
				final BerylliumSmtpManager imp = self.imp();
				final String qccId = self.qccId();
				imp.send(qccId, env, content, policy);
				return self;
			} catch (final BerylliumApiException ex) {
				throw new EsApiCodeException(ex);
			}
		}

		public method_push() {
			super("push", 2, CArg.subject, CArg.messageBody, CArg.toRecipients, CArg.ccRecipients, CArg.ratePolicy);
		}
	}

	static class method_ratePolicy extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return RatePolicyFactory.newEsObject(ecx, self(ecx).policy());
		}

		public method_ratePolicy() {
			super("ratePolicy");
		}
	}

	static class method_setCcList extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final MailSenderEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			self.setCcList(ac.ztwStringValue(0));
			return self;
		}

		public method_setCcList() {
			super("setCcList", 1, CArg.recipientList);
		}
	}

	static class method_setFrom extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final MailSenderEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			self.setFromAddress(ac.qtwStringValue(0).toLowerCase());
			return self;
		}

		public method_setFrom() {
			super("setFrom", 1, CArg.address);
		}
	}

	static class method_setRatePolicy extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final MailSenderEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final EsObjectAccessor aPolicy = ac.select(0).newObjectAccessor();
			final BerylliumSmtpRatePolicy policy = RatePolicyFactory.newPolicy(aPolicy);
			self.setPolicy(policy);
			return self;
		}

		public method_setRatePolicy() {
			super("setRatePolicy", 1, CArg.ratePolicy);
		}
	}

	static class method_setToList extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final MailSenderEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			self.setToList(ac.ztwStringValue(0));
			return self;
		}

		public method_setToList() {
			super("setToList", 1, CArg.recipientList);
		}
	}

	static class method_toList extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return new EsPrimitiveString(self(ecx).ztwToList());
		}

		public method_toList() {
			super("toList");
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
