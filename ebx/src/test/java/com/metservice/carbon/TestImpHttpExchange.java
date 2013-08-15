/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.io.Buffer;

import com.metservice.argon.Binary;
import com.metservice.argon.BinaryOutputStream;

/**
 * @author roach
 */
public class TestImpHttpExchange extends HttpExchange {

	private String fmt() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_httpStatus.get());
		sb.append("\n");
		final ArrayList<String> zlNames = Collections.list(m_headers.getFieldNames());
		final List<String> zlNamesAsc = new ArrayList<String>(zlNames);
		Collections.sort(zlNamesAsc);
		for (final String name : zlNamesAsc) {
			sb.append(name);
			sb.append(": ");
			final ArrayList<String> zlValues = Collections.list(m_headers.getValues(name));
			final int vcount = zlValues.size();
			final StringBuilder bval = new StringBuilder();
			for (int i = 0; i < vcount; i++) {
				if (bval.length() > 0) {
					bval.append(", ");
				}
				final String value = zlValues.get(i);
				if (value != null) {
					sb.append(value);
				}
			}
			sb.append(bval.toString());
			sb.append('\n');
		}
		sb.append("\n\n");
		sb.append(newBinary());
		return sb.toString();
	}

	@Override
	protected final void onResponseContent(Buffer content)
			throws IOException {
		super.onResponseContent(content);
		m_lockBufferRx.lock();
		try {
			content.writeTo(m_binaryOutputStreamRx);
		} finally {
			m_lockBufferRx.unlock();
		}
	}

	@Override
	protected synchronized final void onResponseHeader(Buffer name, Buffer value)
			throws IOException {
		super.onResponseHeader(name, value);
		m_headers.add(name, value);
	}

	@Override
	protected synchronized void onResponseStatus(Buffer version, int status, Buffer reason)
			throws IOException {
		super.onResponseStatus(version, status, reason);
		m_httpStatus.set(status);
	}

	public int httpStatusCode() {
		return m_httpStatus.get();
	}

	public Binary newBinary() {
		m_lockBufferRx.lock();
		try {
			return m_binaryOutputStreamRx.newBinary();
		} finally {
			m_lockBufferRx.unlock();
		}
	}

	@Override
	public String toString() {
		return zStripCR(fmt());
	}

	public String toStringCRLF() {
		return zEnsureCR(fmt());
	}

	static String zEnsureCR(String zIn) {
		final int len = zIn.length();
		final StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			final char ch = zIn.charAt(i);
			if (ch != '\r') {
				if (ch == '\n') {
					sb.append("\r\n");
				} else {
					sb.append(ch);
				}
			}
		}
		return sb.toString();
	}

	static String zStripCR(String zIn) {
		final int len = zIn.length();
		final StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			final char ch = zIn.charAt(i);
			if (ch != '\r') {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	public TestImpHttpExchange() {
	}
	private final AtomicInteger m_httpStatus = new AtomicInteger(0);
	private final HttpFields m_headers = new HttpFields();
	private final Lock m_lockBufferRx = new ReentrantLock();
	private final BinaryOutputStream m_binaryOutputStreamRx = new BinaryOutputStream(8192);
}
