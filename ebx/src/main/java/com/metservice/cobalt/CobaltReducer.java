package com.metservice.cobalt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CobaltReducer {

	public void add(CobaltNCube ncube) {
		if (ncube == null) throw new IllegalArgumentException("object is null");
		for (final CobaltRecord record : ncube) {
			m_shapeMap.add(record.newComposite());
		}
	}

	public void add(CobaltRecord record) {
		if (record == null) throw new IllegalArgumentException("object is null");
		if (record.cardinality() == 0) return;
		m_shapeMap.add(record.newComposite());
	}

	public boolean isEmpty() {
		return m_shapeMap.isEmpty();
	}

	public CobaltNCube reduce() {
		if (isEmpty()) throw new IllegalStateException("No records");
		return reduce(m_shapeMap);
	}

	private static CobaltNCube reduce(ShapeMap inMap) {
		assert inMap != null;
		final List<SequenceBuilder> xlBuilders = inMap.new_xlBuilders();
		final int builderCount = xlBuilders.size();
		final List<CobaltSequence> xlSequence = new ArrayList<CobaltSequence>(builderCount);
		final Set<CobaltDimensionSet> xsNeoDimSet = new HashSet<CobaltDimensionSet>(builderCount);
		for (final SequenceBuilder sequenceBuilder : xlBuilders) {
			final CobaltSequence reducedSequence = ReductionEngine.reduce(sequenceBuilder);
			xsNeoDimSet.add(reducedSequence.dimensionSet());
			xlSequence.add(reducedSequence);
		}
		final int neoBuilderCount = xsNeoDimSet.size();
		if (builderCount == neoBuilderCount) return CobaltNCube.newInstance(xlSequence);
		final ShapeMap neoMap = new ShapeMap();
		neoMap.add(xlSequence);
		final CobaltNCube neoCube = reduce(neoMap);
		return neoCube;
	}

	public CobaltReducer() {
	}
	private final ShapeMap m_shapeMap = new ShapeMap();

	private static class ShapeMap {

		public void add(CobaltSequence sequence) {
			assert sequence != null;
			if (sequence.productCount() == 0) return;
			final CobaltDimensionSet dimensionSet = sequence.dimensionSet();
			final SequenceBuilder builder = declareSequenceBuilder(dimensionSet);
			final ICobaltProduct[] xptProductsAsc = sequence.xptProductsAsc();
			for (int i = 0; i < xptProductsAsc.length; i++) {
				builder.add(xptProductsAsc[i]);
			}
		}

		public void add(Composite composite) {
			assert composite != null;
			final CobaltDimensionSet dimensionSet = composite.dimensionSet();
			final SequenceBuilder builder = declareSequenceBuilder(dimensionSet);
			builder.add(composite);
		}

		public void add(List<CobaltSequence> zlSequences) {
			assert zlSequences != null;
			final int count = zlSequences.size();
			for (int i = 0; i < count; i++) {
				add(zlSequences.get(i));
			}
		}

		public SequenceBuilder declareSequenceBuilder(CobaltDimensionSet ds) {
			SequenceBuilder vBuilder = m_zmDmSet_Builder.get(ds);
			if (vBuilder == null) {
				vBuilder = new SequenceBuilder();
				m_zmDmSet_Builder.put(ds, vBuilder);
			}
			return vBuilder;
		}

		public boolean isEmpty() {
			return m_zmDmSet_Builder.isEmpty();
		}

		public List<SequenceBuilder> new_xlBuilders() {
			return new ArrayList<SequenceBuilder>(m_zmDmSet_Builder.values());
		}

		public ShapeMap() {
			m_zmDmSet_Builder = new HashMap<CobaltDimensionSet, SequenceBuilder>(16);
		}
		private final Map<CobaltDimensionSet, SequenceBuilder> m_zmDmSet_Builder;
	}
}
