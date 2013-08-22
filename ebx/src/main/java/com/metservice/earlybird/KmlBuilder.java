/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import java.io.PrintWriter;

import com.metservice.argon.ArgonTransformer;

/**
 * @author roach
 */
class KmlBuilder {

	public static final String MimeType = "application/vnd.google-earth.kml+xml";
	private static final String Prologue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\">";

	private static final String Epilogue = "</kml>";

	public void close(String tag) {
		m_sb.append("</").append(tag).append(">\n");
	}

	public void coordinates(String qValue) {
		if (qValue == null || qValue.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String tag = "coordinates";
		open(tag);
		textNode(qValue);
		close(tag);
	}

	public void description(String qValue) {
		if (qValue == null || qValue.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_sb.append("<description>");
		textNode(qValue);
		m_sb.append("</description>\n");
	}

	public void name(String qValue) {
		if (qValue == null || qValue.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String tag = "name";
		open(tag);
		textNode(qValue);
		close(tag);
	}

	public void open(String tag) {
		m_sb.append('<').append(tag).append(">\n");
	}

	public void placemarkClose() {
		close("Placemark");
	}

	public void placemarkOpen() {
		open("Placemark");
	}

	public void pointClose() {
		close("Point");
	}

	public void pointOpen() {
		open("Point");
	}

	public void save(PrintWriter writer) {
		if (writer == null) throw new IllegalArgumentException("object is null");
		writer.println(Prologue);
		writer.println(m_sb.toString());
		writer.println(Epilogue);
	}

	public void textNode(String zIn) {
		final String zxe = ArgonTransformer.zXmlEncode(zIn, false, false, false, false);
		m_sb.append(zxe);
	}

	public KmlBuilder() {
		m_sb = new StringBuilder(4096);
	}
	private final StringBuilder m_sb;
}
