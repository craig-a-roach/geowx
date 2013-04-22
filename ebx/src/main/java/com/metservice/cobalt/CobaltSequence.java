package com.metservice.cobalt;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.metservice.argon.ArgonText;
import com.metservice.argon.HashCoder;

public class CobaltSequence implements Comparable<CobaltSequence> {

	void addInnerToDC(KmlFeatureDescription kfd) {
		if (kfd == null) throw new IllegalArgumentException("object is null");
		final int count = m_xptProductsAsc.length;
		kfd.beginProductsTable();
		for (int i = 0; i < count; i++) {
			final ICobaltProduct product = m_xptProductsAsc[i];
			kfd.beginElement("tr");
			kfd.beginElement("td");
			if (product instanceof ICobaltCoordinate) {
				final ICobaltCoordinate coordinate = (ICobaltCoordinate) product;
				coordinate.addTo(kfd);
			} else if (product instanceof Composite) {
				final Composite shape = (Composite) product;
				shape.addToDC(kfd, false);
			}
			kfd.endElement("td");
			kfd.endElement("tr");
		}
		kfd.endProductsTable();
	}

	void addInnerToDR(KmlFeatureDescription kfd) {
		if (kfd == null) throw new IllegalArgumentException("object is null");
		final int count = m_xptProductsAsc.length;
		kfd.beginProductsTable();
		kfd.beginElement("tr");
		for (int i = 0; i < count; i++) {
			final ICobaltProduct product = m_xptProductsAsc[i];
			kfd.beginElement("td");
			if (product instanceof ICobaltCoordinate) {
				final ICobaltCoordinate coordinate = (ICobaltCoordinate) product;
				coordinate.addTo(kfd);
			} else if (product instanceof Composite) {
				final Composite shape = (Composite) product;
				shape.addToDR(kfd, false);
			}
			kfd.endElement("td");
		}
		kfd.endElement("tr");
		kfd.endProductsTable();
	}

	void addShapeTo(KmlFeatureDescription kfd, boolean senseCol) {
		if (kfd == null) throw new IllegalArgumentException("object is null");
		final int count = m_xptProductsAsc.length;
		for (int i = 0; i < count; i++) {
			final ICobaltProduct product = m_xptProductsAsc[i];
			if (product instanceof Composite) {
				final Composite shape = (Composite) product;
				if (senseCol) {
					shape.addToDC(kfd, true);
				} else {
					shape.addToDR(kfd, true);
				}
			}
		}
	}

	public int cardinality() {
		return dimensionSet().cardinality();
	}

	@Override
	public int compareTo(CobaltSequence rhs) {
		final int lhsLen = m_xptProductsAsc.length;
		final int rhsLen = rhs.m_xptProductsAsc.length;
		if (lhsLen < rhsLen) return -1;
		if (lhsLen > rhsLen) return +1;
		for (int i = 0; i < lhsLen; i++) {
			final ICobaltProduct lhsProd = m_xptProductsAsc[i];
			final ICobaltProduct rhsProd = rhs.m_xptProductsAsc[i];
			final int cmp = lhsProd.compareTo(rhsProd);
			if (cmp != 0) return cmp;
		}
		return 0;
	}

	public int dimensionDepth() {
		int sum = 0;
		final int count = m_xptProductsAsc.length;
		for (int i = 0; i < count; i++) {
			final ICobaltProduct product = m_xptProductsAsc[i];
			if (product instanceof ICobaltCoordinate) {
				sum++;
			} else if (product instanceof Composite) {
				sum += ((Composite) product).recordCount();
			}
		}
		return sum;
	}

	public CobaltDimensionSet dimensionSet() {
		return m_xptProductsAsc[0].dimensionSet();
	}

	public boolean equals(CobaltSequence rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		final int lhsLen = m_xptProductsAsc.length;
		final int rhsLen = rhs.m_xptProductsAsc.length;
		if (lhsLen != rhsLen) return false;
		for (int i = 0; i < lhsLen; i++) {
			final ICobaltProduct lhsProd = m_xptProductsAsc[i];
			final ICobaltProduct rhsProd = rhs.m_xptProductsAsc[i];
			if (!lhsProd.equals(rhsProd)) return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltSequence)) return false;
		return equals((CobaltSequence) o);
	}

	@Override
	public int hashCode() {
		return m_hashCode;
	}

	public int productCount() {
		return m_xptProductsAsc.length;
	}

	public Iterator<CobaltRecord> shapeIterator() {
		return new ShapeSequenceIterator(m_xptProductsAsc);
	}

	public String show(int depth) {
		final StringBuilder sb = new StringBuilder();
		final int count = m_xptProductsAsc.length;
		if (dimensionSet().isUnaryName()) {
			sb.append(' ');
			sb.append(dimensionSet());
			sb.append(" ");
			for (int i = 0; i < count; i++) {
				final ICobaltProduct product = m_xptProductsAsc[i];
				if (product instanceof ICobaltCoordinate) {
					final ICobaltCoordinate coord = (ICobaltCoordinate) product;
					if (i > 0) {
						sb.append(" ");
					}
					sb.append(coord.show());
				}
			}
		} else {
			sb.append(dimensionSet());
			sb.append('\n');
			ArgonText.appendSpace(sb, depth * CobaltNCube.ShowIndent);
			for (int i = 0; i < count; i++) {
				final ICobaltProduct product = m_xptProductsAsc[i];
				if (product instanceof Composite) {
					final Composite composite = (Composite) product;
					if (i > 0) {
						sb.append("+\n");
						ArgonText.appendSpace(sb, depth * CobaltNCube.ShowIndent);
					}
					sb.append(composite.show(depth));
				}
			}
		}
		return sb.toString();
	}

	public String showShape() {
		final StringBuilder sb = new StringBuilder();
		final int count = m_xptProductsAsc.length;
		for (int i = 0; i < count; i++) {
			final ICobaltProduct product = m_xptProductsAsc[i];
			if (product instanceof Composite) {
				final Composite shape = (Composite) product;
				if (i > 0) {
					sb.append("+\n");
				}
				sb.append(shape.show(0));
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return show(0);
	}

	public ICobaltProduct[] xptProductsAsc() {
		return m_xptProductsAsc;
	}

	private static int hc(ICobaltProduct[] xptProductsAsc) {
		int hc = HashCoder.INIT;
		for (int i = 0; i < xptProductsAsc.length; i++) {
			hc = HashCoder.and(hc, xptProductsAsc[i]);
		}
		return hc;
	}

	public static CobaltSequence newInstance(ICobaltProduct product) {
		if (product == null) throw new IllegalArgumentException("object is null");
		return new CobaltSequence(new ICobaltProduct[] { product });
	}

	public static CobaltSequence newInstance(SequenceBuilder builder) {
		if (builder == null) throw new IllegalArgumentException("object is null");
		return new CobaltSequence(builder.xptProductsAsc());
	}

	private CobaltSequence(ICobaltProduct[] xptProductsAsc) {
		assert xptProductsAsc != null && xptProductsAsc.length > 0;
		m_xptProductsAsc = xptProductsAsc;
		m_hashCode = hc(m_xptProductsAsc);
	}
	private final ICobaltProduct[] m_xptProductsAsc;

	private final int m_hashCode;

	private static class ShapeSequenceIterator implements Iterator<CobaltRecord> {

		private Composite advanceNextComposite(int start) {
			final int count = m_xptProductsAsc.length;
			for (int i = start; i < count; i++) {
				final ICobaltProduct product = m_xptProductsAsc[i];
				if (product instanceof Composite) {
					m_index = i;
					return (Composite) product;
				}
			}
			return null;
		}

		@Override
		public boolean hasNext() {
			return m_oComposite != null;
		}

		@Override
		public CobaltRecord next() {
			if (m_oComposite == null) throw new NoSuchElementException("Exhausted at " + toString());
			if (m_oCompositeIterator == null) {
				m_oCompositeIterator = new CompositeIterator(m_oComposite);
			}
			final CobaltRecord record = m_oCompositeIterator.next();
			if (!m_oCompositeIterator.hasNext()) {
				m_oCompositeIterator = null;
				m_oComposite = advanceNextComposite(m_index + 1);
			}

			return record;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove product from shape");
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(m_index);
			if (m_oCompositeIterator != null) {
				sb.append(">");
				sb.append(m_oCompositeIterator);
			}
			return sb.toString();
		}

		ShapeSequenceIterator(ICobaltProduct[] xptProductsAsc) {
			m_xptProductsAsc = xptProductsAsc;
			m_oComposite = advanceNextComposite(0);
		}
		private final ICobaltProduct[] m_xptProductsAsc;
		private Composite m_oComposite;
		private CompositeIterator m_oCompositeIterator;
		private int m_index;
	}
}
