/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emmail;

import com.metservice.beryllium.IBerylliumSmtpContent;
import com.metservice.beryllium.IBerylliumSmtpHtml;
import com.metservice.beryllium.IBerylliumSmtpText;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsIntrinsicHtmlEncoder;
import com.metservice.neon.EsIntrinsicJsonEncoder;
import com.metservice.neon.EsIntrinsicXmlEncoder;
import com.metservice.neon.EsObject;

/**
 * @author roach
 */
class BodyFactory {

	public static IBerylliumSmtpContent newContent(EsExecutionContext ecx, EsObject bodyObject)
			throws InterruptedException {
		if (bodyObject == null) throw new IllegalArgumentException("object is null");
		if (bodyObject instanceof EsIntrinsicHtmlEncoder) {
			final EsIntrinsicHtmlEncoder he = (EsIntrinsicHtmlEncoder) bodyObject;
			return new Html(he.newString(ecx));
		}
		if (bodyObject instanceof EsIntrinsicXmlEncoder) {
			final EsIntrinsicXmlEncoder xe = (EsIntrinsicXmlEncoder) bodyObject;
			return new Text(xe.newString(ecx));
		}
		if (bodyObject instanceof EsIntrinsicJsonEncoder) {
			final EsIntrinsicJsonEncoder je = (EsIntrinsicJsonEncoder) bodyObject;
			return new Text(je.newString(ecx, false));
		}
		return new Text(bodyObject.toCanonicalString(ecx));
	}

	private BodyFactory() {
	}

	private static class Html implements IBerylliumSmtpHtml {

		@Override
		public String zeHtml() {
			return m_zeHtml;
		}

		Html(String zeHtml) {
			assert zeHtml != null;
			m_zeHtml = zeHtml;
		}
		private final String m_zeHtml;
	}

	private static class Text implements IBerylliumSmtpText {

		@Override
		public String zText() {
			return m_zText;
		}

		Text(String zText) {
			assert zText != null;
			m_zText = zText;
		}
		private final String m_zText;
	}

}
