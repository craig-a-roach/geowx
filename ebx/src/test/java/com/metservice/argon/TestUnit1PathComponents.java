/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1PathComponents {

	@Test
	public void t50() {

		final File ab = new File("/a/b");
		final File abc = new File("/a/b/c");
		final File abc_f1 = new File("/a/b/c/f1.txt");
		final File abc_f2 = new File("/a/b/c/f2.txt");
		final File ab_f1 = new File("/a/b/f1.txt");

		final PathComponents Pab = new PathComponents(ab);
		final PathComponents Pabc = new PathComponents(abc);
		final PathComponents Pabc_f1 = new PathComponents(abc_f1);
		final PathComponents Pabc_f2 = new PathComponents(abc_f2);
		final PathComponents Pab_f1 = new PathComponents(ab_f1);

		Assert.assertTrue(Pab.compareTo(Pabc_f2) < 0);
		Assert.assertTrue(Pabc_f1.compareTo(Pabc_f2) < 0);
		Assert.assertTrue(Pab.hashCode() != Pabc_f2.hashCode());

		Assert.assertTrue(Pab.contains(Pabc));
		Assert.assertFalse(Pabc.contains(Pab));
		Assert.assertTrue(Pabc_f1.newParent().equals(Pabc));
		Assert.assertEquals("f2.txt", Pabc_f1.newParent().oqccRelativePath(Pabc_f2, null, '/'));
		Assert.assertEquals("c/d/f2.txt", Pabc_f1.newParent().oqccRelativePath(Pabc_f2, new String[] { "c", "d" }, '/'));
		Assert.assertEquals("c/f2.txt", Pab_f1.newParent().oqccRelativePath(Pabc_f2, null, '/'));
	}

}
