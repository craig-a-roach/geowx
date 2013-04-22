/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

/**
 * @author roach
 */
abstract class KmlFeatureText {

	private static final int Cap = 128;

	protected final void addAttribute(String qName, String zValue) {
		m_body.append(" ").append(qName).append('=').append('\"');
		m_body.append(zValue);
		m_body.append('\"');
	}

	protected final void addText(boolean value) {
		m_body.append(value ? "1" : "0");
	}

	protected final void addText(String z) {
		assert z != null;
		final int sl = z.length();
		for (int i = 0; i < sl; i++) {
			final char ch = z.charAt(i);
			final int ic = ch;
			final boolean markup = (ch == '<' || ch == '>' || ch == '&');
			final boolean noEntity = !markup && (ic >= 32 && ic <= 126);
			if (noEntity) {
				m_body.append(ch);
			} else {
				m_body.append("&#");
				m_body.append(ic);
				m_body.append(';');
			}
		}
	}

	protected final void addTextDouble(double value) {
		m_body.append(value);
	}

	protected final void addTextInt(int value) {
		m_body.append(value);
	}

	protected final void beginElement(String qTag) {
		m_body.append("<").append(qTag).append(">");
	}

	protected final void beginElementClose() {
		m_body.append(">");
	}

	protected final void beginElementOpen(String qTag) {
		m_body.append("<").append(qTag);
	}

	protected final void endElement(String qTag) {
		m_body.append("</").append(qTag).append(">");
	}

	protected final String format() {
		return m_body.toString();
	}

	@Override
	public String toString() {
		return format();
	}

	protected KmlFeatureText() {
		m_body = new StringBuilder(Cap);
	}
	private final StringBuilder m_body;
}
