/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.metservice.argon.ArgonCompare;

/**
 * @author roach
 */
class ReductionEngine {

	private static Composite extract(ICobaltProduct src, Permutation permutation) {
		if (src instanceof Composite) {
			final Composite composite = (Composite) src;
			return composite.newExtract(permutation);
		}
		throw new IllegalArgumentException("expecting a composite>" + src + "<");
	}

	private static CobaltSequence newReduced(CobaltSequence src, int keyCardinality) {
		assert src != null;
		final int productCount = src.productCount();
		if (productCount < 2) return src;
		final int srcCardinality = src.cardinality();
		if (srcCardinality < 2 || keyCardinality < 1) return src;
		final Permutor permutor = Permutor.newInstance(srcCardinality, keyCardinality);
		final Permutation[] xptPermutationsAsc = permutor.xptPermutationsAsc;
		final int permutationCount = xptPermutationsAsc.length;
		final List<SubPopulation> zlSubPopulations = new ArrayList<SubPopulation>(permutationCount);
		for (int i = 0; i < permutationCount; i++) {
			final SubPopulation subPopulation = newSubPopulation(src, xptPermutationsAsc[i]);
			if (subPopulation.population < productCount) {
				zlSubPopulations.add(subPopulation);
			}
		}
		Collections.sort(zlSubPopulations);
		if (zlSubPopulations.isEmpty()) return src;
		final SubPopulation mostDense = zlSubPopulations.get(0);
		return reduce(src, mostDense.permutation);
	}

	private static SubPopulation newSubPopulation(CobaltSequence src, Permutation permutation) {
		final ICobaltProduct[] xptProductsAsc = src.xptProductsAsc();
		final int productCount = xptProductsAsc.length;
		final Set<ICobaltProduct> xsSub = new HashSet<ICobaltProduct>(productCount);
		for (int i = 0; i < productCount; i++) {
			final Composite key = extract(xptProductsAsc[i], permutation);
			xsSub.add(key);
		}
		final int population = xsSub.size();
		return new SubPopulation(permutation, population);
	}

	private static CobaltSequence reduce(CobaltSequence in) {
		assert in != null;
		final int inProductCount = in.productCount();
		final int inCardinality = in.cardinality();
		if (inProductCount < 2 || inCardinality < 2) return in;
		CobaltSequence src = in;
		for (int keyCardinality = inCardinality - 1; keyCardinality >= 1; keyCardinality--) {
			int srcProductCount = src.productCount();
			boolean reduce = srcProductCount > 1;
			while (reduce) {
				src = newReduced(src, keyCardinality);
				final int neoProductCount = src.productCount();
				reduce = (neoProductCount > 1) && (neoProductCount < srcProductCount);
				srcProductCount = neoProductCount;
			}
		}
		return src;
	}

	private static CobaltSequence reduce(CobaltSequence src, Permutation keyPermutation) {
		assert src != null;
		assert keyPermutation != null;
		final int srcCardinality = src.cardinality();
		final Permutation complementPermutation = keyPermutation.complement(srcCardinality);
		final ICobaltProduct[] xptProductsAsc = src.xptProductsAsc();
		final int productCount = xptProductsAsc.length;
		final Map<Composite, SequenceBuilder> xmBuilder = new HashMap<Composite, SequenceBuilder>(productCount);
		for (int i = 0; i < productCount; i++) {
			final Composite key = extract(xptProductsAsc[i], keyPermutation);
			final Composite complement = extract(xptProductsAsc[i], complementPermutation);
			SequenceBuilder vComplementAccumulator = xmBuilder.get(key);
			if (vComplementAccumulator == null) {
				vComplementAccumulator = new SequenceBuilder();
				xmBuilder.put(key, vComplementAccumulator);
			}
			vComplementAccumulator.add(complement);
		}
		final int neoSequenceCount = xmBuilder.size();
		final SequenceBuilder neoBuilder = new SequenceBuilder(neoSequenceCount);
		for (final Map.Entry<Composite, SequenceBuilder> e : xmBuilder.entrySet()) {
			final Composite key = e.getKey();
			final CobaltSequence complements = CobaltSequence.newInstance(e.getValue());
			final Composite merged = key.newMerged(complements);
			neoBuilder.add(merged);
		}
		return CobaltSequence.newInstance(neoBuilder);
	}

	public static CobaltSequence reduce(SequenceBuilder builder) {
		return reduce(CobaltSequence.newInstance(builder));
	}

	private ReductionEngine() {
	}

	private static class SubPopulation implements Comparable<SubPopulation> {

		@Override
		public int compareTo(SubPopulation rhs) {
			final int c0 = ArgonCompare.fwd(population, rhs.population);
			if (c0 != 0) return c0;
			final int c1 = permutation.compareTo(rhs.permutation);
			return c1;
		}

		@Override
		public String toString() {
			return permutation + ": " + population;
		}

		SubPopulation(Permutation permutation, int population) {
			assert permutation != null;
			this.permutation = permutation;
			this.population = population;
		}
		public final Permutation permutation;
		public final int population;
	}
}
