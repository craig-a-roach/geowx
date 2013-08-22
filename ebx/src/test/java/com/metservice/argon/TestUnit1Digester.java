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
public class TestUnit1Digester {

	@Test
	public void t50() {
		final String s1 = "http://www.metservice.com/publicData";
		final String s2 = "http://www.metservice.com/publicdata";
		final String du1 = ArgonDigester.newSHA1().digestUTF8B64URL(s1);
		final String du2 = ArgonDigester.newSHA1().digestUTF8B64URL(s2);
		Assert.assertEquals("KCg5_DPIxDlcN0Q5-qJg6iiwA18", du1);
		Assert.assertEquals("cJgVuJBawkL_nczfDykAZ-0ZgBM", du2);
	}

}
