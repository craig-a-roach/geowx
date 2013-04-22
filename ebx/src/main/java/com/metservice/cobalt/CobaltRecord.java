package com.metservice.cobalt;

import java.util.HashMap;
import java.util.Map;

import com.metservice.argon.Ds;

public class CobaltRecord {

	public static CobaltRecord newInstance(ICobaltCoordinate... coordinates) {
		final CobaltRecord neo = new CobaltRecord();
		for (int i = 0; i < coordinates.length; i++) {
			neo.put(coordinates[i]);
		}
		return neo;
	}

	private <C extends ICobaltCoordinate> C checkedCast(Class<C> coordClass, ICobaltCoordinate value)
			throws CobaltDimensionException {
		if (coordClass.isInstance(value)) return coordClass.cast(value);
		final String qdn = value.dimensionName().format();
		final String qac = value.getClass().getName();
		final String qec = coordClass.getName();
		final String m = qdn + " coordinate (" + value + ") is of type " + qac + ", but expected type " + qec;
		throw new CobaltDimensionException(m);
	}

	private <C extends ICobaltCoordinate> C find(CobaltDimensionName dn, Class<C> coordClass)
			throws CobaltDimensionException {
		final ICobaltCoordinate oValue = get(dn);
		if (oValue == null) return null;
		return checkedCast(coordClass, oValue);
	}

	private Map<CobaltDimensionName, ICobaltCoordinate> newMapCopy() {
		return new HashMap<CobaltDimensionName, ICobaltCoordinate>(m_name_coordinate);
	}

	private <C extends ICobaltCoordinate> C select(CobaltDimensionName dn, Class<C> coordClass)
			throws CobaltDimensionException {
		final ICobaltCoordinate oValue = get(dn);
		if (oValue == null) {
			final String m = "Record does not have a '" + dn + "' dimension...\n" + toString();
			throw new CobaltDimensionException(m);
		}
		return checkedCast(coordClass, oValue);
	}

	Composite newComposite() {
		if (m_name_coordinate.isEmpty()) throw new IllegalStateException("cardinality is zero");
		return Composite.newInstance(m_name_coordinate);
	}

	public CobaltAnalysis analysis()
			throws CobaltDimensionException {
		return select(CobaltDimensionName.Analysis, CobaltAnalysis.class);
	}

	public int cardinality() {
		return m_name_coordinate.size();
	}

	public ICobaltGeography geography()
			throws CobaltDimensionException {
		return select(CobaltDimensionName.Geography, ICobaltGeography.class);
	}

	public ICobaltCoordinate get(CobaltDimensionName dimensionName) {
		if (dimensionName == null) throw new IllegalArgumentException("object is null");
		return m_name_coordinate.get(dimensionName);
	}

	public CobaltMember getMember()
			throws CobaltDimensionException {
		return find(CobaltDimensionName.Member, CobaltMember.class);
	}

	public CobaltMember member()
			throws CobaltDimensionException {
		return select(CobaltDimensionName.Member, CobaltMember.class);
	}

	public CobaltParameter parameter()
			throws CobaltDimensionException {
		return select(CobaltDimensionName.Parameter, CobaltParameter.class);
	}

	public ICobaltPrognosis prognosis()
			throws CobaltDimensionException {
		return select(CobaltDimensionName.Prognosis, ICobaltPrognosis.class);
	}

	public void put(CobaltRecord subRecord) {
		if (subRecord == null) throw new IllegalArgumentException("object is null");
		m_name_coordinate.putAll(subRecord.m_name_coordinate);
	}

	public ICobaltCoordinate put(ICobaltCoordinate coordinate) {
		if (coordinate == null) throw new IllegalArgumentException("object is null");
		final CobaltDimensionName dimensionName = coordinate.dimensionName();
		return m_name_coordinate.put(dimensionName, coordinate);
	}

	public ICobaltCoordinate remove(CobaltDimensionName dimensionName) {
		if (dimensionName == null) throw new IllegalArgumentException("object is null");
		return m_name_coordinate.remove(dimensionName);
	}

	public CobaltResolution resolution()
			throws CobaltDimensionException {
		return select(CobaltDimensionName.Resolution, CobaltResolution.class);
	}

	public CobaltRecord subtract(CobaltDimensionName... dimensionNames) {
		if (dimensionNames == null) throw new IllegalArgumentException("object is null");
		final Map<CobaltDimensionName, ICobaltCoordinate> neo = newMapCopy();
		final int removeCount = dimensionNames.length;
		for (int i = 0; i < removeCount; i++) {
			neo.remove(dimensionNames[i]);
		}
		return new CobaltRecord(neo);
	}

	public ICobaltSurface surface()
			throws CobaltDimensionException {
		return select(CobaltDimensionName.Surface, ICobaltSurface.class);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("coord", m_name_coordinate);
		return ds.s();
	}

	private CobaltRecord(Map<CobaltDimensionName, ICobaltCoordinate> name_coordinate) {
		assert name_coordinate != null;
		m_name_coordinate = name_coordinate;
	}

	public CobaltRecord() {
		m_name_coordinate = new HashMap<CobaltDimensionName, ICobaltCoordinate>();
	}

	private final Map<CobaltDimensionName, ICobaltCoordinate> m_name_coordinate;
}
