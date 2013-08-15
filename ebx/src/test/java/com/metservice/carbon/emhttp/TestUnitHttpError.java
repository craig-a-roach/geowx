/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnitHttpError {

	@Test
	public void t50() {

		Assert.assertEquals("HTTP404 m1", HttpError.newInstance("HTTP404 m1", 200).toString());
		Assert.assertEquals("HTTP404", HttpError.newInstance("HTTP404", 200).toString());
		Assert.assertEquals("HTTP400 non-json", HttpError.newInstance("non-json", 400).toString());
		Assert.assertEquals("HTTP200 ok", HttpError.newInstance("ok", 200).toString());
		Assert.assertEquals("HTTP200 HTTP604 m1", HttpError.newInstance("HTTP604 m1", 200).toString());
		Assert.assertEquals("HTTP200 HTTP4044 m1", HttpError.newInstance("HTTP4044 m1", 200).toString());

	}

}
