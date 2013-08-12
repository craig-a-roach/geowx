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
import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
class ShapeReader {

	public static final int FileCode = 9994;
	private static final int DefaultBufferCap = 8 * CArgon.K;
	private static final int FileHeaderBc = 100;

	private void emitPoint(IGalliumShapefileHandler handler, State state, int recNo)
			throws FormatException, IOException {
		final GalliumPointD pt = newPointD(state);
		handler.point(recNo, pt);
	}

	private void emitPolygon(IGalliumShapefileHandler handler, State state, int recNo)
			throws FormatException, IOException {
		final GalliumBoundingBoxD box = newBoundingBoxD(state);
		final int partCount = state.intLE("NumParts");
		final int pointCount = state.intLE("NumPoints");
		if (!handler.acceptPolygon(recNo, box, partCount, pointCount)) {
			final int bcAdvance = (partCount * 4) + (pointCount * 16);
			state.advance(bcAdvance);
			return;
		}
		final int[] zptParts = state.intLEArray(partCount, "Parts");
		for (int partIndex = 0, partNext = 1; partIndex < partCount; partIndex++, partNext++) {
			final int pointStart = zptParts[partIndex];
			final int pointEnd = partNext < partCount ? zptParts[partNext] : pointCount;
			for (int pointIndex = pointStart; pointIndex < pointEnd; pointIndex++) {
				final GalliumPointD pt = newPointD(state);
				handler.polygon(recNo, partIndex, pointIndex, pt);
			}
		}
	}

	private int emitRecord(IGalliumShapefileHandler handler, State state)
			throws FormatException, IOException {
		assert state != null;
		final int recNo = state.intBE("Record Number");
		final int bcRec = (state.intBE("Content Length") * 2) + 8;
		final int shapeType = state.intLE("Shape Type");
		switch (shapeType) {
			case 0:
			break;
			case 1:
				emitPoint(handler, state, recNo);
			break;
			case 5:
				emitPolygon(handler, state, recNo);
			break;
			default: {
				throw FormatException.unsupportedShape(shapeType, recNo, state);
			}
		}
		return bcRec;
	}

	private GalliumBoundingBoxD newBoundingBoxD(State state)
			throws FormatException, IOException {
		assert state != null;
		final double xMin = state.doubleLE("Xmin");
		final double yMin = state.doubleLE("Ymin");
		final double xMax = state.doubleLE("Xmax");
		final double yMax = state.doubleLE("Ymax");
		return GalliumBoundingBoxD.newCorners(yMin, xMin, yMax, xMax);
	}

	private GalliumShapefileHeader newFileHeader(State state)
			throws GalliumShapefileFormatException, IOException {
		assert state != null;
		final long bcFileActual = state.bcFile();
		try {
			final int fileCode = state.intBE("File Code");
			if (fileCode != FileCode) {
				final String d = "Not a shapefile (file code=" + fileCode + ")";
				throw new GalliumShapefileFormatException(m_srcPath, d);
			}
			state.advance(20);
			final int bcFileHeader = state.intBE("File Length") * 2;
			if (bcFileActual != bcFileHeader) {
				final String d = "Incomplete; header specifies " + bcFileHeader + " bytes but file is " + bcFileActual;
				throw new GalliumShapefileFormatException(m_srcPath, d);
			}
			final int version = state.intLE("Version");
			final int shapeType = state.intLE("Shape Type");
			if (!supportedShape(shapeType)) {
				final String d = "Unsupported shape type " + shapeType;
				throw new GalliumShapefileFormatException(m_srcPath, d);
			}
			final GalliumBoundingBoxD box = newBoundingBoxD(state);
			final double zMin = state.doubleLE("Zmin");
			final double zMax = state.doubleLE("Zmax");
			final double mMin = state.doubleLE("Mmin");
			final double mMax = state.doubleLE("Mmax");
			return new GalliumShapefileHeader(version, shapeType, box, zMin, zMax, mMin, mMax);
		} catch (final FormatException ex) {
			final String m = "Malformed main file header..." + ex.getMessage();
			throw new GalliumShapefileFormatException(m_srcPath, m);
		}
	}

	private GalliumPointD newPointD(State state)
			throws FormatException, IOException {
		final double x = state.doubleLE("X");
		final double y = state.doubleLE("Y");
		return new GalliumPointD(x, y);
	}

	private void scanRecords(IGalliumShapefileHandler handler, State state)
			throws GalliumShapefileFormatException, IOException {
		assert handler != null;
		assert state != null;
		final long bcPayload = state.bcPayload();
		int recIndex = 0;
		long bcScanned = 0L;
		try {
			while (bcScanned < bcPayload) {
				final int bcRec = emitRecord(handler, state);
				bcScanned += bcRec;
				recIndex++;
			}
		} catch (final FormatException ex) {
			final String exm = ex.getMessage();
			final String p = "File byte offset=" + state.biFile() + ".";
			final String s = "Scanned " + bcScanned + " bytes.";
			final String m = "Record index " + recIndex + " is malformed. " + " " + p + " " + s + " " + exm;
			throw new GalliumShapefileFormatException(m_srcPath, m);
		}
	}

	private boolean supportedShape(int t) {
		if (t < 0 || t > 31) return false;
		if (t == 0) return true;
		return (t == 1 || t == 3 || t == 5 || t == 8);
	}

	public void scan(IGalliumShapefileHandler handler)
			throws GalliumShapefileFormatException, GalliumShapefileReadException {
		if (handler == null) throw new IllegalArgumentException("object is null");
		State oState = null;
		try {
			oState = new State(m_srcPath, m_bufferOps);
			final long bcPayload = oState.bcPayload();
			if (handler.acceptFile(m_srcPath, bcPayload)) {
				final GalliumShapefileHeader header = newFileHeader(oState);
				if (handler.acceptHeader(header)) {
					scanRecords(handler, oState);
				}
			}
		} catch (final IOException ex) {
			throw new GalliumShapefileReadException(m_srcPath, ex);
		} finally {
			if (oState != null) {
				oState.close();
			}
		}
	}

	public ShapeReader(Path srcPath) {
		this(srcPath, DefaultBufferCap);
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

		public void advance(int bcAdvance)
				throws IOException {
			final int bcRem = m_buffer.remaining();
			if (bcAdvance <= bcRem) {
				final int pos = m_buffer.position();
				m_buffer.position(pos + bcAdvance);
				return;
			}
			final int bcSeek = bcAdvance - bcRem;
			final long biFileEx = m_channel.position();
			final long biFileNeo = biFileEx + bcSeek;
			m_channel.position(biFileNeo);
			m_buffer.position(0);
			m_channel.read(m_buffer);
			m_buffer.flip();
		}

		public long bcFile() {
			return m_bcFile;
		}

		public long bcPayload() {
			return Math.max(0L, m_bcFile - FileHeaderBc);
		}

		public long biFile() {
			try {
				return m_channel.position();
			} catch (final IOException ex) {
				return -1L;
			}
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

		public int[] intLEArray(int length, String desc)
				throws FormatException, IOException {
			final int[] zpt = new int[length];
			for (int i = 0; i < length; i++) {
				zpt[i] = intLE(desc);
			}
			return zpt;
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
