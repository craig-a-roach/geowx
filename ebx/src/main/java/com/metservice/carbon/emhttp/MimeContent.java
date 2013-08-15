/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import java.nio.charset.Charset;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.Ds;
import com.metservice.argon.json.JsonEncoder;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsIntrinsicArray;
import com.metservice.neon.EsIntrinsicBinary;
import com.metservice.neon.EsIntrinsicHtmlEncoder;
import com.metservice.neon.EsIntrinsicJsonEncoder;
import com.metservice.neon.EsIntrinsicObject;
import com.metservice.neon.EsIntrinsicXmlEncoder;
import com.metservice.neon.EsObject;
import com.metservice.neon.IEsOperand;

/**
 * @author roach
 */
class MimeContent {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("contentType", qtwContentType);
		ds.a("content", content);
		return ds.s();
	}

	private static String qlctwContentType(String major, String minor, Charset oCharset) {
		final StringBuilder sb = new StringBuilder();
		sb.append(major.toLowerCase().trim());
		sb.append('/');
		sb.append(minor.toLowerCase().trim());
		if (oCharset != null) {
			sb.append(";charset=");
			sb.append(oCharset.name().toLowerCase().trim());
		}
		return sb.toString();
	}

	private static String qlctwContentTypeHtml(Charset charset) {
		assert charset != null;
		return qlctwContentType("text", "html", charset);
	}

	private static String qlctwContentTypeJson(Charset charset) {
		assert charset != null;
		return qlctwContentType("text", "plain", charset);
	}

	private static String qlctwContentTypePlain(Charset charset) {
		assert charset != null;
		return qlctwContentType("text", "plain", charset);
	}

	private static String qlctwContentTypeXml(Charset charset) {
		assert charset != null;
		return qlctwContentType("application", "xml", charset);
	}

	public static MimeContent newExplicit(EsExecutionContext ecx, IEsOperand content, String qtwContentType)
			throws InterruptedException {
		if (content == null) throw new IllegalArgumentException("object is null");
		if (qtwContentType == null || qtwContentType.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		final String qlctwContentType = qtwContentType.toLowerCase();
		if (content instanceof EsIntrinsicBinary) {
			final EsIntrinsicBinary bc = (EsIntrinsicBinary) content;
			final Binary binaryContent = bc.value();
			return new MimeContent(qlctwContentType, binaryContent);
		}

		final String zValue = content.toCanonicalString(ecx);
		final Charset charset = ArgonText.UTF8;
		final Binary binaryContent = Binary.newFromString(charset, zValue);
		return new MimeContent(qlctwContentType, binaryContent);
	}

	public static MimeContent newImplicit(EsExecutionContext ecx, IEsOperand content)
			throws InterruptedException {
		if (content == null) throw new IllegalArgumentException("object is null");
		final EsObject contentObject = content.toObject(ecx);
		if (contentObject instanceof EsIntrinsicJsonEncoder) {
			final EsIntrinsicJsonEncoder je = (EsIntrinsicJsonEncoder) contentObject;
			final String qlctwContentType = je.qlctwContentType(ecx);
			final Binary binaryContent = je.newBinary(ecx, true);
			return new MimeContent(qlctwContentType, binaryContent);
		}
		if (contentObject instanceof EsIntrinsicHtmlEncoder) {
			final EsIntrinsicHtmlEncoder he = (EsIntrinsicHtmlEncoder) contentObject;
			final Binary binaryContent = he.newBinary(ecx);
			final Charset charset = he.charset(ecx);
			final String qlctwContentType = qlctwContentTypeHtml(charset);
			return new MimeContent(qlctwContentType, binaryContent);
		}
		if (contentObject instanceof EsIntrinsicXmlEncoder) {
			final EsIntrinsicXmlEncoder xe = (EsIntrinsicXmlEncoder) contentObject;
			final Binary binaryContent = xe.newBinary(ecx);
			final Charset charset = xe.charset(ecx);
			final String qlctwContentType = qlctwContentTypeXml(charset);
			return new MimeContent(qlctwContentType, binaryContent);
		}
		if (contentObject instanceof EsIntrinsicObject) {
			final EsIntrinsicObject x = (EsIntrinsicObject) contentObject;
			final String qenc = JsonEncoder.Standard.encode(x);
			final Charset charset = ArgonText.ASCII;
			final Binary binaryContent = Binary.newFromString(charset, qenc);
			final String qlctwContentType = qlctwContentTypeJson(charset);
			return new MimeContent(qlctwContentType, binaryContent);
		}
		if (contentObject instanceof EsIntrinsicArray) {
			final EsIntrinsicArray a = (EsIntrinsicArray) contentObject;
			final String qenc = JsonEncoder.Standard.encode(a);
			final Charset charset = ArgonText.ASCII;
			final Binary binaryContent = Binary.newFromString(charset, qenc);
			final String qlctwContentType = qlctwContentTypeJson(charset);
			return new MimeContent(qlctwContentType, binaryContent);
		}

		final String zValue = contentObject.toCanonicalString(ecx);
		final Charset charset = ArgonText.UTF8;
		final Binary binaryContent = Binary.newFromString(charset, zValue);
		final String qlctwContentType = qlctwContentTypePlain(charset);
		return new MimeContent(qlctwContentType, binaryContent);
	}

	private MimeContent(String qtwContentType, Binary content) {
		assert qtwContentType != null && qtwContentType.length() > 0;
		assert content != null;
		this.qtwContentType = qtwContentType;
		this.content = content;
	}

	public final String qtwContentType;
	public final Binary content;

}
