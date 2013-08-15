/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import javax.servlet.http.HttpServletResponse;

import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.Binary;
import com.metservice.neon.EmClass;
import com.metservice.neon.EmConstructor;
import com.metservice.neon.EmMethod;
import com.metservice.neon.EmMutableAccessor;
import com.metservice.neon.EmMutablePropertyAccessor;
import com.metservice.neon.EmObject;
import com.metservice.neon.EsApiCodeException;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsMethodAccessor;
import com.metservice.neon.EsType;
import com.metservice.neon.IEsOperand;

/**
 * @author roach
 */
class HttpResponseEmClass extends EmClass {

	private static final String Name = CClass.HttpResponse;
	static final EmMethod[] Methods = { new method_encodeContent(), new method_redirect(false), new method_redirect(true),
			new method_setExpires(), new method_setLastModified() };

	static final HttpResponseEmClass Instance = new HttpResponseEmClass(Name, Methods);

	static final ctor Constructor = new ctor();

	static HttpResponseEm self(EsExecutionContext ecx) {
		return ecx.thisObject(Name, HttpResponseEm.class);
	}

	private HttpResponseEmClass(String qccClassName, EmMethod[] ozptMethods) {
		super(qccClassName, ozptMethods);
	}

	static class ctor extends EmConstructor {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final HttpResponseEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			if (ac.defaulted(0)) {
				self.constructEmpty();
			} else {
				final String ztwContentType = ac.defaulted(1) ? "" : ac.ztwStringValue(1);
				final IEsOperand operand = ac.esOperandDatum(0);
				if (ztwContentType.length() == 0) {
					if (operand.esType() == EsType.TNumber) {
						self.constructStatus(operand.toNumber(ecx).intVerified());
					} else {
						self.constructContent(MimeContent.newImplicit(ecx, operand));
					}
				} else {
					self.constructContent(MimeContent.newExplicit(ecx, operand, ztwContentType));
				}
			}
			return self;
		}

		@Override
		public EmObject declarePrototype() {
			return new HttpResponseEm(Instance);
		}

		public ctor() {
			super(Name, 0, CArg.content, CArg.contentType);
		}
	}

	static class method_encodeContent extends EmMethod {

		private void encodeGZip(EsExecutionContext ecx, HttpResponseEm self)
				throws InterruptedException {
			final Binary src = srcContent(ecx, self);
			try {
				self.putUpdate(ecx, CProp.content, src.newGZipEncoded());
				self.putUpdate(CProp.contentEncoding, "gzip");
			} catch (final ArgonStreamWriteException ex) {
				throw new EsApiCodeException(ex);
			}
		}

		private Binary srcContent(EsExecutionContext ecx, HttpResponseEm self)
				throws InterruptedException {
			final EmMutableAccessor ma = new EmMutableAccessor(ecx, self);
			final EmMutablePropertyAccessor aContent = ma.newPropertyAccessor(CProp.content);
			if (aContent.esType.isDatum) return aContent.binaryValue();
			throw new EsApiCodeException("No content to encode; set content first");
		}

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final HttpResponseEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final String qlctwEncoding = ac.qtwStringValue(0).toLowerCase();
			if (qlctwEncoding.equals("gz") || qlctwEncoding.equals("gzip")) {
				encodeGZip(ecx, self);
			} else {
				final String m = "Unsupported encoding scheme '" + qlctwEncoding + "'";
				throw new EsApiCodeException(m);
			}
			return self;
		}

		public method_encodeContent() {
			super("encodeContent", 1, CArg.encoding);
		}
	}

	static class method_redirect extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final HttpResponseEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final String qtwLocation = ac.qtwStringValue(0);
			final int statusCode = m_permanent ? HttpServletResponse.SC_MOVED_PERMANENTLY
					: HttpServletResponse.SC_MOVED_TEMPORARILY;
			self.putUpdateInteger(CProp.statusCode, statusCode);
			self.putUpdate(CProp.location, qtwLocation);
			return self;
		}

		public method_redirect(boolean permanent) {
			super(permanent ? "redirectPermanent" : "redirectTemporary", 1, CArg.location);
			m_permanent = permanent;
		}

		private final boolean m_permanent;
	}

	static class method_setExpires extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final HttpResponseEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			self.putUpdateTime(CProp.expires, ac.tsTimeValue(0));
			return self;
		}

		public method_setExpires() {
			super("setExpires", 1, CArg.time);
		}
	}

	static class method_setLastModified extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final HttpResponseEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			self.putUpdateTime(CProp.lastModified, ac.tsTimeValue(0));
			return self;
		}

		public method_setLastModified() {
			super("setLastModified", 1, CArg.time);
		}
	}

}
