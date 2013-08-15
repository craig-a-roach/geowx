/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.concurrent.atomic.AtomicBoolean;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
class RunnableOut extends ARunnable {

	private static final String TryRead = "Read output from process";
	private static final int CR = ArgonText.CH_ASCII_CR;
	private static final int LF = ArgonText.CH_ASCII_LF;

	private void send(CharsetDecoder decoder, ByteBuffer in, CharBuffer out, StringBuilder wrap, Event event) {
		assert decoder != null;
		assert in != null;
		assert out != null;
		assert event != null;

		try {
			final boolean endOfInput = event != Event.WRAP;
			in.flip();
			boolean more = true;
			while (more) {
				final CoderResult coderResult = decoder.decode(in, out, endOfInput);
				out.flip();
				final String s = out.toString();
				out.clear();
				if (coderResult.isOverflow()) {
					send(Event.WRAP, s, wrap);
				} else if (coderResult.isUnderflow()) {
					send(event, s, wrap);
					more = false;
				} else {
					send(event, s, wrap);
					m_productQueue.putOutStreamDecodeWarning(m_type);
					more = false;
				}
			}
			if (in.hasRemaining()) {
				in.compact();
			} else {
				in.clear();
			}
			if (endOfInput) {
				decoder.reset();
			}
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	private void send(Event event, String z, StringBuilder wrap)
			throws InterruptedException {
		assert event != null;
		assert z != null;
		assert wrap != null;

		if (event == Event.WRAP) {
			wrap.append(z);
			return;
		}

		final String zLine;
		if (wrap.length() > 0) {
			wrap.append(z);
			zLine = wrap.toString();
			wrap.setLength(0);
		} else {
			zLine = z;
		}

		if (event == Event.EOL) {
			m_productQueue.putOutStreamLine(m_type, false, zLine);
			return;
		}

		if (event == Event.PROMPT) {
			m_productQueue.putOutStreamLine(m_type, true, zLine);
			return;
		}

		if (zLine.length() > 0) {
			m_productQueue.putOutStreamLine(m_type, false, zLine);
		}

		if (event == Event.CAN) {
			m_productQueue.putOutStreamCancelled(m_type);
			return;
		}

		if (event == Event.EOF) {
			m_productQueue.putOutStreamEnd(m_type);
			return;
		}

		throw new IllegalArgumentException("invalid event>" + event + "<");
	}

	@Override
	protected void doWork() {
		final int bcBuffer = m_productQueue.bcBuffer(m_type);
		final ByteBuffer in = ByteBuffer.allocate(bcBuffer);
		final CharBuffer out = CharBuffer.allocate(bcBuffer);
		final StringBuilder wrap = new StringBuilder();
		final Charset stdEncoding = m_productQueue.stdEncoding();
		final CharsetDecoder charsetDecoder = stdEncoding.newDecoder();
		charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);
		charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		charsetDecoder.replaceWith(CBoron.CharsetDecodeErrorReplacement16);
		final BoronStdioPrompt oPrompt = m_productQueue.getStdioPrompt();
		final StdioPromptMatcher oMatcher = StdioPromptMatcher.createInstance(oPrompt, stdEncoding);
		try {
			boolean more = true;
			boolean disabled = false;
			while (more) {
				if (m_cancelled.get() || Thread.interrupted()) {
					send(charsetDecoder, in, out, wrap, Event.CAN);
					disabled = true;
					more = false;
				} else {
					final int r = m_in.read();
					final boolean matchedPrompt = oMatcher != null && oMatcher.matches(r);
					if (r == -1) {
						send(charsetDecoder, in, out, wrap, Event.EOF);
						more = false;
					} else if (r == LF) {
						send(charsetDecoder, in, out, wrap, Event.EOL);
					} else if (r != CR) {
						if (!in.hasRemaining()) {
							send(charsetDecoder, in, out, wrap, Event.WRAP);
						}
						in.put((byte) r);
						if (matchedPrompt) {
							send(charsetDecoder, in, out, wrap, Event.PROMPT);
						}
					}
				}
			}
			if (disabled) {
				boolean drain = true;
				while (drain) {
					drain = m_in.read() != -1;
				}
			}
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo(TryRead, ex);
			ds.a("type", m_type).a("cancelled", m_cancelled);
			ds.a("in", in).a("out", out).a("wrap", wrap);
			ds.a("productQueue", m_productQueue);
			kc.probe.failSoftware(ds);
			m_productQueue.putOutStreamFail(m_type);
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo(TryRead, ex);
			ds.a("type", m_type).a("cancelled", m_cancelled);
			ds.a("in", in).a("out", out).a("wrap", wrap);
			ds.a("productQueue", m_productQueue);
			kc.probe.failSoftware(ds);
			m_productQueue.putOutStreamFail(m_type);
		} finally {
			try {
				m_in.close();
			} catch (final IOException exIO) {
			}
		}
	}

	public void cancel() {
		m_cancelled.set(true);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("type", m_type);
		ds.a("cancelled", m_cancelled);
		return ds.s();
	}

	public RunnableOut(KernelCfg kc, OutStreamType type, InputStream in, ProductQueue productQueue) {
		super(kc);
		assert type != null;
		assert in != null;
		assert productQueue != null;
		m_type = type;
		m_in = in;
		m_productQueue = productQueue;
		m_cancelled = new AtomicBoolean(false);
	}

	private final OutStreamType m_type;
	private final InputStream m_in;
	private final ProductQueue m_productQueue;
	private final AtomicBoolean m_cancelled;

	private static enum Event {
		EOF, EOL, CAN, WRAP, PROMPT;
	}
}
