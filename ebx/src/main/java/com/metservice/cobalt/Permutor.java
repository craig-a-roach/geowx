package com.metservice.cobalt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Permutor {

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < xptPermutationsAsc.length; i++) {
			sb.append('(');
			sb.append(xptPermutationsAsc[i]);
			sb.append(')');
		}
		return sb.toString();
	}

	public static Permutor newInstance(int n, int k) {
		if (n <= 0) throw new IllegalArgumentException("invalid n>" + n + "<");
		if (k <= 0) throw new IllegalArgumentException("invalid k>" + k + "<");
		if (k > n) throw new IllegalArgumentException("invalid n,k pair>" + n + "," + k + "<");
		final List<Permutation> xl = new ArrayList<Permutation>();
		final Factory factory = new Factory(n, k);
		boolean more = true;
		while (more) {
			final Permutation oNeo = factory.create();
			if (oNeo == null) {
				more = false;
			} else {
				xl.add(oNeo);
			}
		}
		Collections.sort(xl);
		final int permCount = xl.size();
		if (permCount == 0) throw new IllegalStateException("zero permutations from (n,k)" + n + "," + k);
		return new Permutor(xl.toArray(new Permutation[permCount]));
	}

	private Permutor(Permutation[] xptPermutationsAsc) {
		assert xptPermutationsAsc != null && xptPermutationsAsc.length > 0;
		this.xptPermutationsAsc = xptPermutationsAsc;
	}

	public final Permutation[] xptPermutationsAsc;

	private static class Factory {

		private void initState() {
			m_state = new int[k];
			for (int i = 0; i < k; i++) {
				m_state[i] = i;
			}
		}

		private Permutation newPermutation() {
			final int[] xptIndices = new int[k];
			for (int i = 0; i < k; i++) {
				xptIndices[i] = m_state[i];
			}
			return new Permutation(xptIndices);
		}

		private int rippleState() {
			int carry = 1;
			for (int i = k - 1, limit = n - 1; i >= 0 && carry > 0; i--, limit--) {
				final int sn = m_state[i] + carry;
				if (sn <= limit) {
					m_state[i] = sn;
					carry = 0;
				} else {
					m_state[i] = -1;
				}
			}
			if (carry == 0) {
				for (int i = 1; i < k; i++) {
					final int sp = m_state[i - 1];
					final int s = m_state[i];
					if (s < 0) {
						m_state[i] = sp + 1;
					}
				}
			}
			return carry;
		}

		public Permutation create() {
			final int carry;
			if (m_state == null) {
				initState();
				carry = 0;
			} else {
				carry = rippleState();
			}
			return carry > 0 ? null : newPermutation();
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < m_state.length; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append(m_state[i]);
			}
			return sb.toString();
		}

		Factory(int n, int k) {
			this.n = n;
			this.k = k;
		}
		private final int n;
		private final int k;
		private int[] m_state;
	}

}
