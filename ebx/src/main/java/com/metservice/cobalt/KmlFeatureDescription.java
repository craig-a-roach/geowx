package com.metservice.cobalt;

class KmlFeatureDescription extends KmlFeatureText {

	public void addBoldText(String z) {
		beginElement("b");
		addText(z);
		endElement("b");
	}

	public void beginCompositeTable(boolean isOuter) {
		beginElementOpen("table");
		addAttribute("border", "2");
		addAttribute("padding", "2");
		beginElementClose();
	}

	public void beginProductsTable() {
		beginElementOpen("table");
		addAttribute("border", "1");
		addAttribute("padding", "1");
		beginElementClose();
	}

	//
	public void endCompositeTable() {
		endElement("table");
	}

	public void endProductsTable() {
		endElement("table");
	}

	public String toCDATA() {
		return "<![CDATA[" + format() + "]]>";
	}

	public KmlFeatureDescription() {
	}
}
