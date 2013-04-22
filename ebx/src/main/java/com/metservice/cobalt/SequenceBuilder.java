package com.metservice.cobalt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.metservice.argon.Ds;

class SequenceBuilder {

	private void addProduct(ICobaltProduct product) {
		if (product == null) throw new IllegalArgumentException("object is null");
		final CobaltDimensionSet dsNeo = product.dimensionSet();
		if (m_oDimensionSet == null) {
			m_oDimensionSet = dsNeo;
			m_zsProductAsc.add(product);
			return;
		}
		if (m_oDimensionSet.equals(dsNeo)) {
			m_zsProductAsc.add(product);
			return;
		}
		String m = "Cannot add product to sequence...";
		m += "\nSequence Dimensions=" + m_oDimensionSet;
		m += "\nProduct Dimensions=" + dsNeo;
		m += "\n" + product;
		throw new IllegalArgumentException(m);
	}

	public void add(ICobaltProduct product) {
		if (product == null) throw new IllegalArgumentException("object is null");
		if (product instanceof ICobaltCoordinate) {
			addProduct(product);
			return;
		}
		if (product instanceof Composite) {
			final Composite composite = (Composite) product;
			if (composite.dimensionSet().cardinality() == 1) {
				final CobaltSequence sequence = composite.xptSequenceAscDimSet()[0];
				final ICobaltProduct[] xptProductsAsc = sequence.xptProductsAsc();
				for (int i = 0; i < xptProductsAsc.length; i++) {
					addProduct(xptProductsAsc[i]);
				}
			} else {
				addProduct(product);
			}
			return;
		}
		throw new IllegalArgumentException("Unsupported product " + product);
	}

	public CobaltDimensionSet dimensionSet() {
		if (m_oDimensionSet == null) throw new IllegalStateException("No products");
		return m_oDimensionSet;
	}

	public boolean isEmpty() {
		return m_zsProductAsc.isEmpty();
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("dimensionSet", m_oDimensionSet);
		ds.a("products", m_zsProductAsc);
		return ds.s();
	}

	public ICobaltProduct[] xptProductsAsc() {
		final int count = m_zsProductAsc.size();
		if (count == 0) throw new IllegalStateException("No products");
		final ICobaltProduct[] xptAsc = m_zsProductAsc.toArray(new ICobaltProduct[count]);
		Arrays.sort(xptAsc);
		return xptAsc;
	}

	public SequenceBuilder() {
		this(16);
	}

	public SequenceBuilder(int initialCapacity) {
		m_zsProductAsc = new HashSet<ICobaltProduct>(initialCapacity);
	}
	private final Set<ICobaltProduct> m_zsProductAsc;
	private CobaltDimensionSet m_oDimensionSet;
}
