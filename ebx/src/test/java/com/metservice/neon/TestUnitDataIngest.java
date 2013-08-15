package com.metservice.neon;

import org.junit.Test;

public class TestUnitDataIngest extends TestNeon {
	@Test
	public void createsDataIngestXml() {
		final Expectation expectedString = new Expectation("output", new Resource("XmlEncoder.DataIngest.xml"));
		jsassert(expectedString, "XmlEncoder.DataIngest");
	}
}
