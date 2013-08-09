/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.shapefile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.metservice.argon.CArgon;

/**
 * @author roach
 */
class ShapeReader {

	private static final int BufferCap = 8 * CArgon.K;

	public void scan() {

		State oState = null;
		try {
			oState = new State(m_srcPath, BufferCap);

		} catch (final IOException ex) {
			System.err.println(ex.getMessage());
		} finally {
			try {
				if (oState != null) {
					oState.close();
				}
			} catch (final IOException ex) {
				// TODO Probe
			}
		}
	}

	public ShapeReader(Path srcPath) {
		if (srcPath == null) throw new IllegalArgumentException("object is null");
		m_srcPath = srcPath;
	}
	private final Path m_srcPath;

	private static class State {

		public void close()
				throws IOException {
			m_channel.close();
		}

		public int read()
				throws IOException {
			return m_channel.read(m_buffer);
		}

		State(Path srcPath, int cap) throws IOException {
			assert srcPath != null;
			m_channel = Files.newByteChannel(srcPath, StandardOpenOption.READ);
			m_backing = new byte[cap];
			m_buffer = ByteBuffer.wrap(m_backing);
		}
		private final SeekableByteChannel m_channel;
		private final byte[] m_backing;
		private final ByteBuffer m_buffer;
	}
}
