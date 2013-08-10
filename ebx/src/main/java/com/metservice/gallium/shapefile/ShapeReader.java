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
import com.metservice.gallium.GalliumBoundingBoxD;

/**
 * @author roach
 */
class ShapeReader {

	public static final int FileCode = 9994;
	private static final int BufferCap = CArgon.K;

	private void newFileHeader(State state)
			throws GalliumShapefileFormatException, IOException {
		final long bcFileActual = state.bcFile();
		try {
			final int fileCode = state.intBE("File Code");
			if (fileCode != FileCode) {
				final String d = "Not a shapefile (file code=" + fileCode + ")";
				throw new GalliumShapefileFormatException(m_srcPath, d);
			}
			for (int i = 0; i < 5; i++) {
				state.advance4("Unused Integer");
			}
			final int bcFileHeader = state.intBE("File Length") * 2;
			if (bcFileActual != bcFileHeader) {
				final String d = "Incomplete; header specifies " + bcFileHeader + " bytes but file is " + bcFileActual;
				throw new GalliumShapefileFormatException(m_srcPath, d);
			}
			final int version = state.intLE("Version");
			final int shapeType = state.intLE("Shape Type");
			final double xMin = state.doubleLE("Xmin");
			final double yMin = state.doubleLE("Ymin");
			final double xMax = state.doubleLE("Xmax");
			final double yMax = state.doubleLE("Ymax");
			final GalliumBoundingBoxD bb = GalliumBoundingBoxD.newCorners(yMin, xMin, yMax, xMax);
			final double zMin = state.doubleLE("Zmin");
			final double zMax = state.doubleLE("Zmax");
			final double mMin = state.doubleLE("Mmin");
			final double mMax = state.doubleLE("Mmax");
			System.out.println(shapeType);

		} catch (final FormatException ex) {
			final String m = "Malformed main file header..." + ex.getMessage();
			throw new GalliumShapefileFormatException(m_srcPath, m);
		}
	}

	public void scan()
			throws GalliumShapefileFormatException, GalliumShapefileReadException {

		State oState = null;
		try {
			oState = new State(m_srcPath, m_bufferOps);
			newFileHeader(oState);

		} catch (final IOException ex) {
			throw new GalliumShapefileReadException(m_srcPath, ex);
		} finally {
			if (oState != null) {
				oState.close();
			}
		}
	}

	public ShapeReader(Path srcPath) {
		this(srcPath, BufferCap);
	}

	public ShapeReader(Path srcPath, int bufferOps) {
		if (srcPath == null) throw new IllegalArgumentException("object is null");
		m_srcPath = srcPath;
		m_bufferOps = Math.max(1, bufferOps);
	}
	private final Path m_srcPath;
	private final int m_bufferOps;

	private static class State {

		private String diag() {
			final StringBuilder sb = new StringBuilder();
			sb.append("pos=").append(m_buffer.position());
			sb.append(" lim=").append(m_buffer.limit());
			sb.append(" rem=").append(m_buffer.remaining());
			sb.append(" file=").append(m_bcFile);
			return sb.toString();
		}

		private void fillOperand(int bi, int bc) {
			if (bc == 0) return;
			final int pos = m_buffer.position();
			for (int i = 0, iw = bi, ir = pos; i < bc; i++, iw++, ir++) {
				m_operand[iw] = m_backing[ir];
			}
			m_buffer.position(pos + bc);
		}

		private void loadOperand(int bcReqd, String desc)
				throws FormatException, IOException {
			final int bcRem = m_buffer.remaining();
			if (bcReqd <= bcRem) {
				fillOperand(0, bcReqd);
				return;
			}
			fillOperand(0, bcRem);
			final int bcLoad = bcReqd - bcRem;
			m_buffer.position(0);
			m_channel.read(m_buffer);
			m_buffer.flip();
			final int bcAvail = m_buffer.remaining();
			if (bcAvail < bcLoad) {
				final String r = desc + " (" + bcReqd + " bytes)";
				throw FormatException.eof(r, diag());
			}
			fillOperand(bcRem, bcLoad);
		}

		private double makeDoubleLE() {
			return Double.longBitsToDouble(makeLongLE());
		}

		private int makeIntBE() {
			final int b3 = m_operand[0] << 24;
			final int b2 = (m_operand[1] & 0xff) << 16;
			final int b1 = (m_operand[2] & 0xff) << 8;
			final int b0 = (m_operand[3] & 0xff);
			return (b3 | b2 | b1 | b0);
		}

		private int makeIntLE() {
			final int b3 = m_operand[3] << 24;
			final int b2 = (m_operand[2] & 0xff) << 16;
			final int b1 = (m_operand[1] & 0xff) << 8;
			final int b0 = (m_operand[0] & 0xff);
			return (b3 | b2 | b1 | b0);
		}

		private long makeLongLE() {
			final long b7 = ((long) m_operand[7]) << 56;
			final long b6 = ((long) m_operand[6] & 0xff) << 48;
			final long b5 = ((long) m_operand[5] & 0xff) << 40;
			final long b4 = ((long) m_operand[4] & 0xff) << 32;
			final long b3 = ((long) m_operand[3] & 0xff) << 24;
			final long b2 = ((long) m_operand[2] & 0xff) << 16;
			final long b1 = ((long) m_operand[1] & 0xff) << 8;
			final long b0 = ((long) m_operand[0] & 0xff);
			return (b7 | b6 | b5 | b4 | b3 | b2 | b1 | b0);
		}

		public void advance4(String desc)
				throws FormatException, IOException {
			loadOperand(4, desc);
		}

		public long bcFile() {
			return m_bcFile;
		}

		public void close() {
			try {
				m_channel.close();
			} catch (final IOException ex) {
			}
		}

		public double doubleLE(String desc)
				throws FormatException, IOException {
			loadOperand(8, desc);
			return makeDoubleLE();
		}

		public int intBE(String desc)
				throws FormatException, IOException {
			loadOperand(4, desc);
			return makeIntBE();
		}

		public int intLE(String desc)
				throws FormatException, IOException {
			loadOperand(4, desc);
			return makeIntLE();
		}

		@Override
		public String toString() {
			return diag();
		}

		State(Path srcPath, int bufferOps) throws IOException {
			assert srcPath != null;
			m_bcFile = Files.size(srcPath);
			m_channel = Files.newByteChannel(srcPath, StandardOpenOption.READ);
			m_backing = new byte[8 * bufferOps];
			m_operand = new byte[8];
			m_buffer = ByteBuffer.wrap(m_backing);
			m_channel.read(m_buffer);
			m_buffer.flip();
		}
		private final long m_bcFile;
		private final SeekableByteChannel m_channel;
		private final byte[] m_backing;
		private final byte[] m_operand;
		private final ByteBuffer m_buffer;
	}
}
