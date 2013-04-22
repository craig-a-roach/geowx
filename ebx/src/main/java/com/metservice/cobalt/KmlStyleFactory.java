package com.metservice.cobalt;

class KmlStyleFactory {

	private void addBalloonStyleText(StringBuilder sb, String uhtml) {
		sb.append("<text><![CDATA[").append(uhtml).append(")]]></text>");
	}

	private void addLineStyleWidth(StringBuilder sb) {
		sb.append("<width>");
		sb.append(m_style.polygonLineWidth());
		sb.append("</width>");
	}

	private void addPolyStyleColor(StringBuilder sb, int id) {
		sb.append("<color>");
		sb.append(m_style.polygonFillColor().format(id, m_polygonCount));
		sb.append("</color>");
	}

	private void beginBalloonStyle(StringBuilder sb) {
		sb.append("<BallonStyle>");
	}

	private void beginLineStyle(StringBuilder sb) {
		sb.append("<LineStyle>");
	}

	private void beginPolyStyle(StringBuilder sb) {
		sb.append("<PolyStyle>");
	}

	private void beginStyle(StringBuilder sb) {
		sb.append("\n<Style>");
	}

	private void endBalloonStyle(StringBuilder sb) {
		sb.append("</BallonStyle>");
	}

	private void endLineStyle(StringBuilder sb) {
		sb.append("</LineStyle>");
	}

	private void endPolyStyle(StringBuilder sb) {
		sb.append("</PolyStyle>");
	}

	private void endStyle(StringBuilder sb) {
		sb.append("</Style>");
	}

	private void styleItem(StringBuilder sb, int id) {
		beginStyle(sb);
		beginBalloonStyle(sb);
		addBalloonStyleText(sb, "<b>$[name]</b><br />$[description]");
		endBalloonStyle(sb);
		beginPolyStyle(sb);
		addPolyStyleColor(sb, id);
		endPolyStyle(sb);
		beginLineStyle(sb);
		addLineStyleWidth(sb);
		endLineStyle(sb);
		endStyle(sb);
	}

	public boolean isColumnSenseBalloon() {
		return m_style.isColumnSenseBalloon();
	}

	public String toInnerXml(int id) {
		final StringBuilder sb = new StringBuilder();
		styleItem(sb, id);
		return sb.toString();
	}

	public KmlStyleFactory(CobaltKmlStyle style, int polyCount) {
		if (style == null) throw new IllegalArgumentException("object is null");
		m_style = style;
		m_polygonCount = polyCount;
	}
	private final CobaltKmlStyle m_style;
	private final int m_polygonCount;
}
