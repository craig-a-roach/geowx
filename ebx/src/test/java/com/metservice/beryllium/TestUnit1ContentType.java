/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1ContentType {

	@Test
	public void t50() {
		final BerylliumContentType oct1 = BerylliumContentType.createInstance("image/jpeg");
		Assert.assertNotNull(oct1);
		Assert.assertEquals("image/jpeg", oct1.toString());

		final BerylliumContentType oct2 = BerylliumContentType
				.createInstance("application/x-www-form-urlencoded  ; charset=UTF-8");
		Assert.assertNotNull(oct2);
		Assert.assertEquals("application/x-www-form-urlencoded;charset=utf-8", oct2.toString());

		final BerylliumContentType oct3 = BerylliumContentType.createInstance("application/xhtml+xml");
		Assert.assertNotNull(oct3);
		Assert.assertEquals("application/xhtml+xml", oct3.toString());

		final BerylliumContentType oct4 = BerylliumContentType.createInstance("text/html;charset=LATIN1");
		Assert.assertNotNull(oct4);
		Assert.assertEquals("text/html;charset=iso-8859-1", oct4.toString());

	}

}
