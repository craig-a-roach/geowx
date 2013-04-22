package com.metservice.cobalt;

import com.metservice.argon.HashCoder;

class Permutation implements Comparable<Permutation> {

	private String msgNoComplement(int n) {
		return "undefined " + n + "-complement for " + format();
	}

	@Override
	public int compareTo(Permutation rhs) {
		final int clhs = xptIndices.length;
		final int crhs = rhs.xptIndices.length;
		if (clhs < crhs) return -1;
		if (clhs > crhs) return +1;
		for (int i = 0; i < clhs; i++) {
			final int xlhs = xptIndices[i];
			final int xrhs = rhs.xptIndices[i];
			if (xlhs < xrhs) return -1;
			if (xlhs > xrhs) return +1;
		}
		return 0;
	}

	public Permutation complement(int n) {
		final int rlen = xptIndices.length;
		final int clen = n - rlen;
		if (clen < 1) throw new IllegalArgumentException(msgNoComplement(n));
		final int[] xptComplement = new int[clen];
		int wc = 0;
		int r = 0;
		int xl = 0;
		while (xl < n && r < rlen) {
			final int xr = xptIndices[r];
			if (xr >= n) throw new IllegalArgumentException(msgNoComplement(n));
			if (xl < xr) {
				xptComplement[wc] = xl;
				wc++;
			} else {
				r++;
			}
			xl++;
		}
		while (xl < n) {
			xptComplement[wc] = xl;
			wc++;
			xl++;
		}
		return new Permutation(xptComplement);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof Permutation)) return false;
		return equals((Permutation) o);
	}

	public boolean equals(Permutation rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		final int clhs = xptIndices.length;
		final int crhs = rhs.xptIndices.length;
		if (clhs != crhs) return false;
		for (int i = 0; i < clhs; i++) {
			final int xlhs = xptIndices[i];
			final int xrhs = rhs.xptIndices[i];
			if (xlhs != xrhs) return false;
		}
		return true;
	}

	public String format() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < xptIndices.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(xptIndices[i]);
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		int hc = HashCoder.INIT;
		for (int i = 0; i < xptIndices.length; i++) {
			hc = HashCoder.and(hc, xptIndices[i]);
		}
		return hc;
	}

	@Override
	public String toString() {
		return format();
	}

	public Permutation(int[] xptIndices) {
		if (xptIndices == null) throw new IllegalArgumentException("object is null");
		if (xptIndices.length == 0) throw new IllegalArgumentException("empty indices array");
		this.xptIndices = xptIndices;
	}
	public final int[] xptIndices;
}
