package com.metservice.cobalt;

class KmlDocument {

	public static final String Namespace = "http://www.opengis.net/kml/2.2";
	private static final int Cap = 1024;

	private void addBoolean(String qTag, boolean flag) {
		m_body.append("\n<").append(qTag).append(">");
		m_body.append(flag ? "1" : "0");
		m_body.append("</").append(qTag).append(">");
	}

	public void add(KmlFeatureDescription fd) {
		if (fd == null) throw new IllegalArgumentException("object is null");
		m_body.append("\n<description>");
		m_body.append(fd.toCDATA());
		m_body.append("\n</description>");
	}

	public void add(KmlFeatureName fn) {
		if (fn == null) throw new IllegalArgumentException("object is null");
		m_body.append("\n<name>");
		m_body.append(fn.toInnerXml());
		m_body.append("</name>");
	}

	public void add(KmlGeometry geo) {
		if (geo == null) throw new IllegalArgumentException("object is null");
		m_body.append(geo.toInnerXml());
	}

	public void addOpen(boolean flag) {
		addBoolean("open", flag);
	}

	public void addSnippet(String qtwText, int maxLines) {
		m_body.append("\n<Snippet maxLines=\"").append(maxLines).append("\">");
		m_body.append(qtwText);
		m_body.append("</Snippet>");
	}

	public void addStyle() {
		m_body.append(m_styleFactory.toInnerXml(m_styleIndex));
		m_styleIndex++;
	}

	public void beginFolder() {
		m_body.append("\n<Folder>");
	}

	public void beginPlacemark() {
		m_body.append("\n<Placemark>");
	}

	public void endFolder() {
		m_body.append("\n</Folder>");
	}

	public void endPlacemark() {
		m_body.append("\n</Placemark>");
	}

	public boolean isColumnSenseBalloon() {
		return m_styleFactory.isColumnSenseBalloon();
	}

	public String toInnerXml() {
		final String qBody = m_body.toString();
		final StringBuilder sb = new StringBuilder(qBody.length() + 128);
		sb.append("<Document>");
		sb.append(qBody);
		sb.append("\n</Document>");
		return sb.toString();
	}

	@Override
	public String toString() {
		return toInnerXml();
	}

	public String toXml() {
		final String qInner = toInnerXml();
		final StringBuilder sb = new StringBuilder(qInner.length() + 256);
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("\n<kml xmlns=\"").append(Namespace).append("\">");
		sb.append("\n").append(qInner);
		sb.append("\n</kml>");
		return sb.toString();
	}

	public KmlDocument(KmlStyleFactory styleFactory) {
		m_body = new StringBuilder(Cap);
		m_styleFactory = styleFactory;
	}

	private final StringBuilder m_body;
	private final KmlStyleFactory m_styleFactory;
	private int m_styleIndex;
}
