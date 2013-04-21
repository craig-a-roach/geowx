/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium;

/**
 * @author roach
 */
class MarchingSquareContourEngine implements IGalliumContourEngine {

	private static final int TERMINAL = -1;
	private static final int CONSUMED = -2;

	private static float avg(float bottomLeft, float bottomRight, float topLeft, float topRight) {
		return (bottomLeft + bottomRight + topLeft + topRight) / 4.0f;
	}

	private static boolean bit(float datum, float threshold) {
		return datum >= threshold;
	}

	private static int disambiguatedConnectivity(int connectivity, boolean saddle) {
		if (!saddle) {
			if (connectivity == 5) return 10;
			if (connectivity == 10) return 5;
		}
		return connectivity;
	}

	private static void formatIndex(StringBuilder sb, String prefix, int index) {
		sb.append(prefix);
		if (index == TERMINAL) {
			sb.append("x");
		} else if (index == CONSUMED) {
			sb.append("-");
		} else {
			sb.append(index);
		}
	}

	private static DatumRange newDatumRange(IGalliumContourable src) {
		assert src != null;
		final int yPointCount = src.pointCountY();
		final int xPointCount = src.pointCountX();
		float lo = Float.MAX_VALUE;
		float hi = -Float.MAX_VALUE;
		for (int y = 0; y < yPointCount; y++) {
			for (int x = 0; x < xPointCount; x++) {
				final float datum = src.datum(y, x);
				if (datum > hi) {
					hi = datum;
				}
				if (datum < lo) {
					lo = datum;
				}
			}
		}
		return new DatumRange(lo, hi);
	}

	private boolean accept(int y, int x) {
		return y >= 0 && y < m_nyCell && x >= 0 && x < m_nxCell;
	}

	private Cell addCell(int iSelf, Cell neo) {
		m_cells[iSelf] = neo;
		m_consumableCount++;
		return neo;
	}

	private int cellIndex(int y, int x) {
		return (y * m_nxCell) + x;
	}

	private void connect(int y, int x, Cell neo, int iSelf, Sense sense) {
		assert neo != null;
		assert sense != null;
		final int yAdjacent = sense.yNew(y);
		final int xAdjacent = sense.xNew(x);
		if (accept(yAdjacent, xAdjacent)) {
			final int iAdjacent = cellIndex(yAdjacent, xAdjacent);
			final Cell oAdjacent = m_cells[iAdjacent];
			if (oAdjacent != null) {
				neo.setAttachment(iAdjacent, sense);
				oAdjacent.setAttachment(iSelf, sense.inverse());
			}
		}
	}

	private int consumeAttach(Cell cell, int index, Sense sense) {
		assert cell != null;
		assert sense != null;
		final int nextIndex = cell.consume(sense);
		final Cell oReduced = cell.createReduced();
		m_cells[index] = oReduced;
		if (oReduced == null) {
			m_consumableCount--;
		}
		return nextIndex;
	}

	private void emitPolyEdge(GalliumPoly.Builder pb, int cellIndex, Sense edge, float threshold) {
		assert pb != null;
		assert edge != null;
		final int yB = yIndex(cellIndex);
		final int xL = xIndex(cellIndex);

		switch (edge) {
			case Left:
				emitPolyEdgeLon(pb, xL, yB, yB + 1, threshold);
			break;
			case Right:
				emitPolyEdgeLon(pb, xL + 1, yB, yB + 1, threshold);
			break;
			case Below:
				emitPolyEdgeLat(pb, yB, xL, xL + 1, threshold);
			break;
			case Above:
				emitPolyEdgeLat(pb, yB + 1, xL, xL + 1, threshold);
			break;
		}
	}

	private void emitPolyEdgeLat(GalliumPoly.Builder pb, int y, int xL, int xR, float threshold) {
		final float norm = normalInterpolate(m_src.datum(y, xL), m_src.datum(y, xR), threshold);
		if (Float.isNaN(norm)) return;
		final float lat = m_src.latitude(y);
		final float lonL = m_src.longitude(xL);
		final float lonStep = m_src.longitude(xR) - lonL;
		final float lon = lonL + (norm * lonStep);
		pb.addTail(lat, lon);
	}

	private void emitPolyEdgeLon(GalliumPoly.Builder pb, int x, int yB, int yT, float threshold) {
		final float norm = normalInterpolate(m_src.datum(yB, x), m_src.datum(yT, x), threshold);
		if (Float.isNaN(norm)) return;
		final float lon = m_src.longitude(x);
		final float latB = m_src.latitude(yB);
		final float latStep = m_src.latitude(yT) - latB;
		final float lat = latB + (norm * latStep);
		pb.addTail(lat, lon);
	}

	private void makeCellArray(int nyPoint, int nxPoint, float threshold) {
		for (int yBottom = 0, yTop = 1; yTop < nyPoint; yTop++, yBottom++) {
			for (int xLeft = 0, xRight = 1; xRight < nxPoint; xRight++, xLeft++) {
				final float bottomLeft = m_src.datum(yBottom, xLeft);
				final float bottomRight = m_src.datum(yBottom, xRight);
				final float topRight = m_src.datum(yTop, xRight);
				final float topLeft = m_src.datum(yTop, xLeft);
				final int b0 = bit(bottomLeft, threshold) ? 0x1 : 0x0;
				final int b1 = bit(bottomRight, threshold) ? 0x2 : 0x0;
				final int b2 = bit(topRight, threshold) ? 0x4 : 0x0;
				final int b3 = bit(topLeft, threshold) ? 0x8 : 0x0;
				final int connectivity = b3 | b2 | b1 | b0;
				final int disambiguatedConnectivity;
				if (connectivity == 5 || connectivity == 10) {
					final float avg = avg(bottomLeft, bottomRight, topLeft, topRight);
					final boolean saddle = bit(avg, threshold);
					disambiguatedConnectivity = disambiguatedConnectivity(connectivity, saddle);
				} else {
					disambiguatedConnectivity = connectivity;
				}
				putCell(yBottom, xLeft, disambiguatedConnectivity);
			}
		}
	}

	private void makeLevel(GalliumTopologyLevel.Builder dst) {
		final int nyPoint = m_src.pointCountY();
		final int nxPoint = m_src.pointCountX();
		if (nyPoint > 0 && nxPoint > 0) {
			final float threshold = dst.threshold();
			if (m_range.contains(threshold)) {
				makeCellArray(nyPoint, nxPoint, threshold);
				scanCellArray(dst);
			}
		}
	}

	private GalliumPoly.Builder newPolyBuilder(Cell origin) {
		if (origin.isIsolated()) return GalliumPoly.newBuilder(1);
		return GalliumPoly.newBuilder();
	}

	private float normalInterpolate(float datumFrom, float datumToex, float threshold) {
		if (datumFrom <= threshold && threshold < datumToex) return (threshold - datumFrom) / (datumToex - datumFrom);
		if (datumFrom >= threshold && threshold > datumToex) return (datumFrom - threshold) / (datumFrom - datumToex);
		return Float.NaN;
	}

	private void putCell(int y, int x, int connectivity) {
		if (connectivity == 0 || connectivity == 15) return;
		final int iSelf = cellIndex(y, x);
		switch (connectivity) {
			case 1:
			case 14: {
				final Cell neo = addCell(iSelf, new Cell2LB());
				connect(y, x, neo, iSelf, Sense.Left);
				connect(y, x, neo, iSelf, Sense.Below);
			}
			break;
			case 2:
			case 13: {
				final Cell neo = addCell(iSelf, new Cell2BR());
				connect(y, x, neo, iSelf, Sense.Below);
			}
			break;
			case 3:
			case 12: {
				final Cell neo = addCell(iSelf, new Cell2LR());
				connect(y, x, neo, iSelf, Sense.Left);
			}
			break;
			case 4:
			case 11:
				addCell(iSelf, new Cell2AR());
			break;
			case 6:
			case 9: {
				final Cell neo = addCell(iSelf, new Cell2AB());
				connect(y, x, neo, iSelf, Sense.Below);
			}
			break;
			case 7:
			case 8: {
				final Cell neo = addCell(iSelf, new Cell2LA());
				connect(y, x, neo, iSelf, Sense.Left);
			}
			break;
			case 5: {
				final Cell neo = addCell(iSelf, new Cell4LABR());
				connect(y, x, neo, iSelf, Sense.Left);
				connect(y, x, neo, iSelf, Sense.Below);
			}
			break;
			case 10: {
				final Cell neo = addCell(iSelf, new Cell4LBAR());
				connect(y, x, neo, iSelf, Sense.Left);
				connect(y, x, neo, iSelf, Sense.Below);
			}
			break;
			default:
		}
	}

	private void scanCellArray(GalliumTopologyLevel.Builder dst) {
		assert dst != null;
		final float threshold = dst.threshold();
		final int cellCount = m_cells.length;
		for (int cellIndex = 0; cellIndex < cellCount && m_consumableCount > 0; cellIndex++) {
			final Cell oCell = m_cells[cellIndex];
			if (oCell != null) {
				traverse(dst, oCell, cellIndex, threshold);
			}
		}
		if (m_consumableCount > 0) {
			final String m = "Untraversed cells =" + m_consumableCount + " for threshold " + threshold;
			throw new IllegalStateException(m);
		}
	}

	private void traverse(GalliumTopologyLevel.Builder dst, Cell cell, int cellIndex, float threshold) {
		assert cell != null;
		final int card = cell.card();
		for (int i = 0; i < card; i += 2) {
			final Sense senseA = cell.sense(i);
			final GalliumPoly.Builder pbA = traverseArm(cell, cellIndex, senseA, threshold);
			if (!pbA.isClosed()) {
				final Sense senseB = cell.sense(i + 1);
				final GalliumPoly.Builder pbB = traverseArm(cell, cellIndex, senseB, threshold);
				if (!pbB.isClosed()) {
					pbA.reverse();
					pbA.addTail(pbB);
				}
			}
			dst.add(GalliumPoly.newInstance(pbA));
		}
	}

	private GalliumPoly.Builder traverseArm(Cell originCell, int originIndex, Sense originSense, float threshold) {
		final GalliumPoly.Builder pb = newPolyBuilder(originCell);
		Cell hereCell = originCell;
		int hereIndex = originIndex;
		Sense departSense = originSense;
		boolean more = true;
		while (more) {
			emitPolyEdge(pb, hereIndex, departSense, threshold);
			final int nextIndex = consumeAttach(hereCell, hereIndex, departSense);
			final Cell oNextCell = (nextIndex < 0) ? null : m_cells[nextIndex];
			if (oNextCell == null) {
				more = false;
				continue;
			}
			hereCell = oNextCell;
			hereIndex = nextIndex;
			final Sense arriveSense = departSense.inverse();
			departSense = oNextCell.complementSense(arriveSense);
			consumeAttach(hereCell, hereIndex, arriveSense);
			if (hereIndex == originIndex && departSense == originSense) {
				pb.setClosed();
				more = false;
			}
		}
		return pb;
	}

	private int xIndex(int cellIndex) {
		return cellIndex % m_nxCell;
	}

	private int yIndex(int cellIndex) {
		return cellIndex / m_nxCell;
	}

	@Override
	public GalliumTopology newTopology(float[] thresholdArray) {
		if (thresholdArray == null) throw new IllegalArgumentException("object is null");
		final int thresholdCount = thresholdArray.length;
		final GalliumTopology.Builder tb = GalliumTopology.newBuilder(thresholdCount);
		for (int tIndex = 0; tIndex < thresholdCount; tIndex++) {
			final float threshold = thresholdArray[tIndex];
			final GalliumTopologyLevel.Builder tlb = GalliumTopologyLevel.newBuilder(threshold);
			makeLevel(tlb);
			tb.add(GalliumTopologyLevel.newInstance(tlb));
		}
		return GalliumTopology.newInstance(tb);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int y = m_nyCell - 1; y >= 0; y--) {
			for (int x = 0; x < m_nxCell; x++) {
				final int cellIndex = cellIndex(y, x);
				sb.append(cellIndex);
				final Cell oCell = m_cells[cellIndex];
				sb.append(oCell == null ? "." : oCell.toString());
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public MarchingSquareContourEngine(IGalliumContourable src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		final int yPointCount = src.pointCountY();
		final int xPointCount = src.pointCountX();
		m_src = src;
		m_nyCell = Math.max(0, yPointCount - 1);
		m_nxCell = Math.max(0, xPointCount - 1);
		m_range = newDatumRange(src);
		final int countCell = m_nyCell * m_nxCell;
		m_cells = new Cell[countCell];
	}
	private final IGalliumContourable m_src;
	private final int m_nyCell;
	private final int m_nxCell;
	private final DatumRange m_range;
	private final Cell[] m_cells;
	private int m_consumableCount;

	private static abstract class Cell {

		protected abstract int complement(int attachId);

		protected abstract int index(int attachId);

		protected String invalid(int id) {
			return "Unexpected cell" + card() + " id: " + id;
		}

		protected String invalid(Sense sense) {
			return "Unexpected cell" + card() + " sense: " + sense;
		}

		protected abstract void setIndex(int attachId, int index);

		public abstract int attachId(Sense sense);

		public abstract int card();

		public final Sense complementSense(Sense arrival) {
			assert arrival != null;
			final int attachId = attachId(arrival);
			final int cattachId = complement(attachId);
			return sense(cattachId);
		}

		public final int consume(Sense sense) {
			assert sense != null;
			final int attachId = attachId(sense);
			final int index = index(attachId);
			setConsumed(attachId);
			return index;
		}

		public abstract Cell createReduced();

		public abstract boolean isIsolated();

		public abstract Sense sense(int attachId);

		public final void setAttachment(int index, Sense sense) {
			setIndex(attachId(sense), index);
		}

		public void setConsumed(int attachId) {
			setIndex(attachId, CONSUMED);
		}
	}

	private static abstract class Cell2 extends Cell {

		@Override
		protected final int complement(int attachId) {
			switch (attachId) {
				case 0:
					return 1;
				case 1:
					return 0;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}

		@Override
		protected final int index(int attachId) {
			switch (attachId) {
				case 0:
					return m_index0;
				case 1:
					return m_index1;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}

		@Override
		protected final void setIndex(int attachId, int index) {
			switch (attachId) {
				case 0:
					m_index0 = index;
				break;
				case 1:
					m_index1 = index;
				break;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}

		@Override
		public final int card() {
			return 2;
		}

		@Override
		public final Cell createReduced() {
			if (m_index0 == CONSUMED && m_index1 == CONSUMED) return null;
			return this;
		}

		@Override
		public final boolean isIsolated() {
			return m_index0 < 0 && m_index1 < 0;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append('(');
			formatIndex(sb, sense(0).code(), m_index0);
			formatIndex(sb, sense(1).code(), m_index1);
			sb.append(')');
			return sb.toString();
		}

		protected Cell2() {
		}
		protected int m_index0 = TERMINAL;
		protected int m_index1 = TERMINAL;
	}

	private static class Cell2AB extends Cell2 {

		@Override
		public int attachId(Sense sense) {
			switch (sense) {
				case Above:
					return 0;
				case Below:
					return 1;
				default:
					throw new IllegalStateException(invalid(sense));
			}
		}

		@Override
		public Sense sense(int attachId) {
			switch (attachId) {
				case 0:
					return Sense.Above;
				case 1:
					return Sense.Below;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}
	}

	private static class Cell2AR extends Cell2 {

		@Override
		public int attachId(Sense sense) {
			switch (sense) {
				case Above:
					return 0;
				case Right:
					return 1;
				default:
					throw new IllegalStateException(invalid(sense));
			}
		}

		@Override
		public Sense sense(int attachId) {
			switch (attachId) {
				case 0:
					return Sense.Above;
				case 1:
					return Sense.Right;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}
	}

	private static class Cell2BR extends Cell2 {

		@Override
		public int attachId(Sense sense) {
			switch (sense) {
				case Below:
					return 0;
				case Right:
					return 1;
				default:
					throw new IllegalStateException(invalid(sense));
			}
		}

		@Override
		public Sense sense(int attachId) {
			switch (attachId) {
				case 0:
					return Sense.Below;
				case 1:
					return Sense.Right;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}
	}

	private static class Cell2LA extends Cell2 {

		@Override
		public int attachId(Sense sense) {
			switch (sense) {
				case Left:
					return 0;
				case Above:
					return 1;
				default:
					throw new IllegalStateException(invalid(sense));
			}
		}

		@Override
		public Sense sense(int attachId) {
			switch (attachId) {
				case 0:
					return Sense.Left;
				case 1:
					return Sense.Above;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}
	}

	private static class Cell2LB extends Cell2 {

		@Override
		public int attachId(Sense sense) {
			switch (sense) {
				case Left:
					return 0;
				case Below:
					return 1;
				default:
					throw new IllegalStateException(invalid(sense));
			}
		}

		@Override
		public Sense sense(int attachId) {
			switch (attachId) {
				case 0:
					return Sense.Left;
				case 1:
					return Sense.Below;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}
	}

	private static class Cell2LR extends Cell2 {

		@Override
		public int attachId(Sense sense) {
			switch (sense) {
				case Left:
					return 0;
				case Right:
					return 1;
				default:
					throw new IllegalStateException(invalid(sense));
			}
		}

		@Override
		public Sense sense(int attachId) {
			switch (attachId) {
				case 0:
					return Sense.Left;
				case 1:
					return Sense.Right;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}
	}

	private static abstract class Cell4 extends Cell {

		@Override
		protected final int complement(int attachId) {
			switch (attachId) {
				case 0:
					return 1;
				case 1:
					return 0;
				case 2:
					return 3;
				case 3:
					return 2;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}

		@Override
		protected final int index(int attachId) {
			switch (attachId) {
				case 0:
					return m_index0;
				case 1:
					return m_index1;
				case 2:
					return m_index2;
				case 3:
					return m_index3;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}

		protected abstract Cell2 newL();

		protected abstract Cell2 newR();

		@Override
		protected final void setIndex(int attachId, int index) {
			switch (attachId) {
				case 0:
					m_index0 = index;
				break;
				case 1:
					m_index1 = index;
				break;
				case 2:
					m_index2 = index;
				break;
				case 3:
					m_index3 = index;
				break;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}

		@Override
		public final int card() {
			return 4;
		}

		@Override
		public final Cell createReduced() {
			final boolean consumedL = (m_index0 == CONSUMED && m_index1 == CONSUMED);
			final boolean consumedR = (m_index2 == CONSUMED && m_index3 == CONSUMED);
			if (consumedL && consumedR) return null;
			if (consumedL) {
				final Cell2 neo = newR();
				neo.setIndex(0, m_index2);
				neo.setIndex(1, m_index3);
				return neo;
			}
			if (consumedR) {
				final Cell2 neo = newL();
				neo.setIndex(0, m_index0);
				neo.setIndex(1, m_index1);
				return neo;
			}
			return this;
		}

		@Override
		public final boolean isIsolated() {
			return m_index0 < 0 && m_index1 < 0 && m_index2 < 0 && m_index3 < 0;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append('(');
			formatIndex(sb, sense(0).code(), m_index0);
			formatIndex(sb, sense(1).code(), m_index1);
			sb.append('+');
			formatIndex(sb, sense(2).code(), m_index2);
			formatIndex(sb, sense(3).code(), m_index3);
			sb.append(')');
			return sb.toString();
		}

		protected Cell4() {
		}
		protected int m_index0 = TERMINAL;
		protected int m_index1 = TERMINAL;
		protected int m_index2 = TERMINAL;
		protected int m_index3 = TERMINAL;
	}

	private static class Cell4LABR extends Cell4 {

		@Override
		protected Cell2 newL() {
			return new Cell2LA();
		}

		@Override
		protected Cell2 newR() {
			return new Cell2BR();
		}

		@Override
		public int attachId(Sense sense) {
			switch (sense) {
				case Left:
					return 0;
				case Above:
					return 1;
				case Below:
					return 2;
				case Right:
					return 3;
				default:
					throw new IllegalStateException(invalid(sense));
			}
		}

		@Override
		public Sense sense(int attachId) {
			switch (attachId) {
				case 0:
					return Sense.Left;
				case 1:
					return Sense.Above;
				case 2:
					return Sense.Below;
				case 3:
					return Sense.Right;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}
	}

	private static class Cell4LBAR extends Cell4 {

		@Override
		protected Cell2 newL() {
			return new Cell2LB();
		}

		@Override
		protected Cell2 newR() {
			return new Cell2AR();
		}

		@Override
		public int attachId(Sense sense) {
			switch (sense) {
				case Left:
					return 0;
				case Below:
					return 1;
				case Above:
					return 2;
				case Right:
					return 3;
				default:
					throw new IllegalStateException(invalid(sense));
			}
		}

		@Override
		public Sense sense(int attachId) {
			switch (attachId) {
				case 0:
					return Sense.Left;
				case 1:
					return Sense.Below;
				case 2:
					return Sense.Above;
				case 3:
					return Sense.Right;
				default:
					throw new IllegalStateException(invalid(attachId));
			}
		}
	}

	private static class DatumRange {

		public boolean contains(float threshold) {
			return lo < threshold && threshold < hi;
		};

		@Override
		public String toString() {
			return "lo=" + lo + " hi=" + hi;
		}

		DatumRange(float lo, float hi) {
			this.lo = lo;
			this.hi = hi;
		}
		public final float lo;
		public final float hi;
	}

	private static enum Sense {
		Left(0, -1, "L"), Right(0, 1, "R"), Above(1, 0, "A"), Below(-1, 0, "B");

		public String code() {
			return m_code;
		}

		public Sense inverse() {
			if (m_yStep == 0) return m_xStep < 0 ? Right : Left;
			return m_yStep < 0 ? Above : Below;
		}

		public int xNew(int xRef) {
			return xRef + m_xStep;
		}

		public int yNew(int yRef) {
			return yRef + m_yStep;
		}

		private Sense(int yStep, int xStep, String code) {
			m_yStep = yStep;
			m_xStep = xStep;
			m_code = code;
		}
		private int m_yStep;
		private int m_xStep;
		private final String m_code;
	}

}
