/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.net;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1Platform {

	@Test
	public void t50_localEmail() {
		final String qccLocalEmailAddress = ArgonPlatform.qlcLocalEmailAddress();
		Assert.assertTrue("Valid email: " + qccLocalEmailAddress, qccLocalEmailAddress.length() >= 3);
	}

}
