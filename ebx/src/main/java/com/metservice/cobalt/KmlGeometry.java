package com.metservice.cobalt;

class KmlGeometry {

	private static double clampLat(double lat) {
		if (lat < -90.0) return -90.0;
		if (lat > +90.0) return +90.0;
		return lat;
	}

	private static double normLon(double lon) {
		double n = lon;
		if (lon < -180.0) {
			while (n < -180.0) {
				n += 360.0;
			}
		} else if (lon > 180.0) {
			while (n > 180.0) {
				n -= 360.0;
			}
		}
		return n;
	}

	private static String qDeg(double deg) {
		return Long.toString(Math.round(deg));
	}

	private static String qLat(double lat) {
		if (lat > 0.0) return qDeg(lat) + "N";
		if (lat < 0.0) return qDeg(-lat) + "S";
		return "0";
	}

	private static String qLon(double lon) {
		if (lon > 0.0 && lon < 180.0) return qDeg(lon) + "E";
		if (lon < 0.0 && lon > -180.0) return qDeg(-lon) + "W";
		return qDeg(lon);
	}

	private static void xmlValue(StringBuilder dst, String tag, boolean flag) {
		dst.append("<").append(tag).append(">");
		dst.append(flag ? "1" : "0");
		dst.append("</").append(tag).append(">");
	}

	private static void xmlValue(StringBuilder dst, String tag, String z) {
		dst.append("<").append(tag).append(">");
		dst.append(z);
		dst.append("</").append(tag).append(">");
	}

	public static KmlGeometry newClampedToGround() {
		return new KmlGeometry(true, false, true);
	}

	public static KmlGeometry newRelativeToGround(boolean extrude) {
		return new KmlGeometry(false, extrude, false);
	}

	private StringBuilder appendLonLat(StringBuilder sb, double lon, double lat) {
		sb.append(lon).append(',').append(lat);
		if (!m_ground) {
			sb.append(',');
			sb.append(m_alt);
		}
		return sb;
	}

	private void xmlPolygonCommon(StringBuilder sb) {
		xmlValue(sb, "extrude", m_extrude);
		xmlValue(sb, "tesselate", m_tesselate);
		xmlValue(sb, "altitudeMode", (m_ground ? "clampedToGround" : "relativeToGround"));
	}

	public void setAltitude(double metres) {
		m_alt = metres;
	}

	public void setCoordinates(double lat1, double lon1, double lat2, double lon2) {
		m_lonL = normLon(lon1);
		m_lonR = normLon(lon2);
		m_latT = clampLat(lat1);
		m_latB = clampLat(lat2);
	}

	public String toInnerXml() {
		final StringBuilder sb = new StringBuilder(512);
		if (m_lonL < m_lonR) {
			sb.append("\n<Polygon>\n");
			xmlPolygonCommon(sb);
			sb.append("\n<outerBoundaryIs><LinearRing><coordinates>\n");
			appendLonLat(sb, m_lonL, m_latT).append(' ');
			appendLonLat(sb, m_lonL, m_latB).append(' ');
			appendLonLat(sb, m_lonR, m_latB).append(' ');
			appendLonLat(sb, m_lonR, m_latT).append(' ');
			appendLonLat(sb, m_lonL, m_latT);
			sb.append("\n</coordinates></LinearRing></outerBoundaryIs>");
			sb.append("\n</Polygon>");
		} else {
			sb.append("\n<MultiGeometry>");
			sb.append("\n<Polygon>\n");
			xmlPolygonCommon(sb);
			sb.append("\n<outerBoundaryIs><LinearRing><coordinates>\n");
			appendLonLat(sb, m_lonL, m_latT).append(' ');
			appendLonLat(sb, m_lonL, m_latB).append(' ');
			appendLonLat(sb, 180.0, m_latB).append(' ');
			appendLonLat(sb, 180.0, m_latT).append(' ');
			appendLonLat(sb, m_lonL, m_latT);
			sb.append("\n</coordinates></LinearRing></outerBoundaryIs>");
			sb.append("\n</Polygon>");
			sb.append("\n<Polygon>\n");
			xmlPolygonCommon(sb);
			sb.append("\n<outerBoundaryIs><LinearRing><coordinates>\n");
			appendLonLat(sb, -180.0, m_latT).append(' ');
			appendLonLat(sb, -180.0, m_latB).append(' ');
			appendLonLat(sb, m_lonR, m_latB).append(' ');
			appendLonLat(sb, m_lonR, m_latT).append(' ');
			appendLonLat(sb, -180.0, m_latT);
			sb.append("\n</coordinates></LinearRing></outerBoundaryIs>");
			sb.append("\n</Polygon>");
			sb.append("\n</MultiGeometry>");
		}
		return sb.toString();
	}

	public String toName() {
		final StringBuilder sb = new StringBuilder();
		sb.append(qLat(m_latT));
		sb.append(",");
		sb.append(qLon(m_lonL));
		sb.append(" ");
		sb.append(qLat(m_latB));
		sb.append(",");
		sb.append(qLon(m_lonR));
		return sb.toString();
	}

	@Override
	public String toString() {
		return toInnerXml();
	}

	private KmlGeometry(boolean ground, boolean extrude, boolean tesselate) {
		m_ground = ground;
		m_extrude = extrude;
		m_tesselate = tesselate;
	}
	private final boolean m_ground;
	private final boolean m_extrude;
	private final boolean m_tesselate;
	private double m_lonL;
	private double m_latT;
	private double m_lonR;
	private double m_latB;
	private double m_alt;
}
