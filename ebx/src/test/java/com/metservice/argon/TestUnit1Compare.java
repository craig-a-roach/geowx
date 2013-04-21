/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1Compare {

	@Test
	public void t50_similar() {
		Assert.assertTrue("2/3", ArgonCompare.similar(2.0f / 3.0f, 1.0f - 1.0f / 3.0f));
	}

}
