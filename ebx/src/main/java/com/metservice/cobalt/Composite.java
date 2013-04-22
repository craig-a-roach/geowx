package com.metservice.cobalt;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.metservice.argon.ArgonText;
import com.metservice.argon.HashCoder;

class Composite implements ICobaltProduct {

	private static final Comparator<CobaltSequence> ByDimSet = new Comparator<CobaltSequence>() {

		@Override
		public int compare(CobaltSequence lhs, CobaltSequence rhs) {
			final CobaltDimensionSet lhsDs = lhs.dimensionSet();
			final CobaltDimensionSet rhsDs = rhs.dimensionSet();
			return lhsDs.compareTo(rhsDs);
		}
	};

	private CobaltDimensionSet newDimSet(CobaltSequence[] xptSequenceAsc) {
		final int count = xptSequenceAsc.length;
		final ICobaltDimensionExpression[] xptExprAsc = new ICobaltDimensionExpression[count];
		for (int i = 0; i < count; i++) {
			xptExprAsc[i] = xptSequenceAsc[i].dimensionSet();
		}
		return CobaltDimensionSet.newInstance(xptExprAsc);
	}

	void addToDC(KmlFeatureDescription kfd, boolean isOuter) {
		if (kfd == null) throw new IllegalArgumentException("object is null");
		kfd.beginCompositeTable(isOuter);
		final int count = m_xptSequenceAscDimSet.length;
		if (isOuter) {
			kfd.beginElement("tr");
			for (int i = 0; i < count; i++) {
				final CobaltSequence sequence = m_xptSequenceAscDimSet[i];
				kfd.beginElement("th");
				sequence.dimensionSet().addTo(kfd);
				kfd.endElement("th");
			}
		}
		kfd.endElement("tr");
		kfd.beginElement("tr");
		for (int i = 0; i < count; i++) {
			final CobaltSequence sequence = m_xptSequenceAscDimSet[i];
			kfd.beginElement("td");
			sequence.addInnerToDC(kfd);
			kfd.endElement("td");
		}
		kfd.endElement("tr");
		kfd.endCompositeTable();
	}

	void addToDR(KmlFeatureDescription kfd, boolean isOuter) {
		if (kfd == null) throw new IllegalArgumentException("object is null");
		kfd.beginCompositeTable(isOuter);
		final int count = m_xptSequenceAscDimSet.length;
		for (int i = 0; i < count; i++) {
			final CobaltSequence sequence = m_xptSequenceAscDimSet[i];
			kfd.beginElement("tr");
			if (isOuter) {
				kfd.beginElement("th");
				sequence.dimensionSet().addTo(kfd);
				kfd.endElement("th");
			}
			kfd.beginElement("td");
			sequence.addInnerToDR(kfd);
			kfd.endElement("td");
			kfd.endElement("tr");
		}
		kfd.endCompositeTable();
	}

	CobaltSequence[] xptSequenceAscDimSet() {
		return m_xptSequenceAscDimSet;
	}

	@Override
	public int compareTo(ICobaltProduct rhs) {
		if (rhs instanceof Composite) {
			final Composite r = (Composite) rhs;
			final int clhs = m_xptSequenceAscDimSet.length;
			final int crhs = r.m_xptSequenceAscDimSet.length;
			if (clhs < crhs) return -1;
			if (clhs > crhs) return +1;
			for (int i = 0; i < clhs; i++) {
				final CobaltSequence sqlhs = m_xptSequenceAscDimSet[i];
				final CobaltSequence sqrhs = r.m_xptSequenceAscDimSet[i];
				final int cmp = sqlhs.compareTo(sqrhs);
				if (cmp != 0) return cmp;
			}
			return 0;
		}
		throw new IllegalArgumentException("invalid rhs>" + rhs + "<");
	}

	@Override
	public CobaltDimensionSet dimensionSet() {
		return m_dimensionSet;
	}

	public boolean equals(Composite rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		final int clhs = m_xptSequenceAscDimSet.length;
		final int crhs = rhs.m_xptSequenceAscDimSet.length;
		if (clhs != crhs) return false;
		for (int i = 0; i < clhs; i++) {
			final CobaltSequence sqlhs = m_xptSequenceAscDimSet[i];
			final CobaltSequence sqrhs = rhs.m_xptSequenceAscDimSet[i];
			if (!sqlhs.equals(sqrhs)) return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof Composite)) return false;
		return equals((Composite) o);
	}

	@Override
	public int hashCode() {
		return m_hashCode;
	}

	public Composite newExtract(Permutation permutation) {
		if (permutation == null) throw new IllegalArgumentException("object is null");
		final int[] xptIndices = permutation.xptIndices;
		final int count = xptIndices.length;
		final CobaltSequence[] xptSequenceAscDimSet = new CobaltSequence[count];
		for (int i = 0; i < count; i++) {
			final int index = xptIndices[i];
			final CobaltSequence sel = m_xptSequenceAscDimSet[index];
			xptSequenceAscDimSet[i] = sel;
		}
		Arrays.sort(xptSequenceAscDimSet, ByDimSet);
		return new Composite(xptSequenceAscDimSet);
	}

	public Composite newMerged(CobaltSequence sequence) {
		if (sequence == null) throw new IllegalArgumentException("object is null");
		final int exCount = m_xptSequenceAscDimSet.length;
		final CobaltSequence[] xptNeoAscDimSet = new CobaltSequence[exCount + 1];
		for (int i = 0; i < exCount; i++) {
			xptNeoAscDimSet[i] = m_xptSequenceAscDimSet[i];
		}
		xptNeoAscDimSet[exCount] = sequence;
		Arrays.sort(xptNeoAscDimSet, ByDimSet);
		return new Composite(xptNeoAscDimSet);
	}

	public int recordCount() {
		int recordCount = 1;
		final int cardinality = m_xptSequenceAscDimSet.length;
		for (int isq = 0; isq < cardinality; isq++) {
			final int dd = m_xptSequenceAscDimSet[isq].dimensionDepth();
			recordCount = recordCount * dd;
		}
		return recordCount;
	}

	public String show(int depth) {
		final StringBuilder sb = new StringBuilder();
		final int count = m_xptSequenceAscDimSet.length;
		for (int i = 0; i < count; i++) {
			final CobaltSequence sq = m_xptSequenceAscDimSet[i];
			final String s = sq.show(depth + 1);
			if (i > 0) {
				sb.append('\n');
				ArgonText.appendSpace(sb, depth * CobaltNCube.ShowIndent);
			}
			sb.append(i == 0 ? '(' : '.');
			sb.append(s);
		}
		sb.append('\n');
		ArgonText.appendSpace(sb, depth * CobaltNCube.ShowIndent);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String toString() {
		return m_dimensionSet + "\n" + show(0);
	}

	private static int hc(CobaltSequence[] xptSequenceAsc) {
		int hc = HashCoder.INIT;
		for (int i = 0; i < xptSequenceAsc.length; i++) {
			hc = HashCoder.and(hc, xptSequenceAsc[i]);
		}
		return hc;
	}

	static Composite newInstance(List<SequenceBuilder> xlBuilders) {
		if (xlBuilders == null) throw new IllegalArgumentException("object is null");
		final int cardinality = xlBuilders.size();
		if (cardinality == 0) throw new IllegalArgumentException("empty sequence builder list");
		final CobaltSequence[] xptSequenceAscDimSet = new CobaltSequence[cardinality];
		for (int i = 0; i < cardinality; i++) {
			final SequenceBuilder builder = xlBuilders.get(i);
			xptSequenceAscDimSet[i] = CobaltSequence.newInstance(builder);
		}
		Arrays.sort(xptSequenceAscDimSet, ByDimSet);
		return new Composite(xptSequenceAscDimSet);
	}

	public static Composite newInstance(Map<CobaltDimensionName, ICobaltCoordinate> name_coordinate) {
		if (name_coordinate == null) throw new IllegalArgumentException("object is null");
		final int cardinality = name_coordinate.size();
		if (cardinality == 0) throw new IllegalArgumentException("empty name coordinate map");
		final CobaltSequence[] xptSequenceAscDimSet = new CobaltSequence[cardinality];
		int w = 0;
		for (final Map.Entry<CobaltDimensionName, ICobaltCoordinate> e : name_coordinate.entrySet()) {
			xptSequenceAscDimSet[w] = CobaltSequence.newInstance(e.getValue());
			w++;
		}
		Arrays.sort(xptSequenceAscDimSet, ByDimSet);
		return new Composite(xptSequenceAscDimSet);
	}

	private Composite(CobaltSequence[] xptSequenceAscDimSet) {
		assert xptSequenceAscDimSet != null;
		m_xptSequenceAscDimSet = xptSequenceAscDimSet;
		m_dimensionSet = newDimSet(xptSequenceAscDimSet);
		m_hashCode = hc(xptSequenceAscDimSet);
	}
	private final CobaltSequence[] m_xptSequenceAscDimSet;

	private final CobaltDimensionSet m_dimensionSet;

	private final int m_hashCode;
}
