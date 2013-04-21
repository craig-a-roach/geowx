/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.text.ArgonTransformer;

/**
 * @author roach
 */
public class TestUnit1Transformer {

	@Test
	public void t40() {
		Assert.assertEquals("ABCDE", ArgonTransformer.zNoControl("A\nB\rC\tD\fE"));
		Assert.assertEquals("A\nBC\tDE", ArgonTransformer.zNoControl("A\nB\rC\tD\fE", true, true));
		Assert.assertEquals("ABCDE", ArgonTransformer.zNoControl("ABCDE"));
		Assert.assertEquals("ABCDE", ArgonTransformer.zSanitized("AB.CD#E", ".#", ""));
		Assert.assertEquals("AB_CD_E", ArgonTransformer.zSanitized("AB.CD#E", ".#", "_"));
		Assert.assertEquals("ABCDE", ArgonTransformer.zSanitized(".ABCDE#", ".#", ""));
		Assert.assertEquals("_ABCDE_", ArgonTransformer.zSanitized(".ABCDE#", ".#", "_"));
		Assert.assertEquals("ABCDE", ArgonTransformer.zSanitized("ABCDE", ".#", "_"));
		Assert.assertEquals("Ab.1.C-D_E", ArgonTransformer.zPosixSanitized("Ab.1.C-D_E"));
		Assert.assertEquals("A_b_.1_C-D_E_", ArgonTransformer.zPosixSanitized("A(b).1 C-D_E#"));
	}

	@Test
	public void t50() {
		final String[] sa = { "a=3", "b=true", "  c.x=jk\n c.y=kj \n" };
		final Properties p = ArgonTransformer.newPropertiesFromAssignments(sa);
		final String pcx = p.getProperty("c.x");
		Assert.assertEquals("jk", pcx);
	}

	@Test
	public void t60() {
		final String[] sa = { "a", "b=true" };
		final Properties p = ArgonTransformer.newPropertiesFromAssignments(sa);
		final String pa = p.getProperty("a");
		final String pb = p.getProperty("b");
		final String pc = p.getProperty("c");
		Assert.assertEquals("", pa);
		Assert.assertEquals("true", pb);
		Assert.assertNull(pc);
	}

	@Test
	public void t70() {
		Assert.assertEquals(".", ArgonTransformer.zLeftDot3("abcd", 1));
		Assert.assertEquals("..", ArgonTransformer.zLeftDot3("abcd", 2));
		Assert.assertEquals("...", ArgonTransformer.zLeftDot3("abcd", 3));
		Assert.assertEquals("abcd", ArgonTransformer.zLeftDot3("abcd", 4));
		Assert.assertEquals("a...", ArgonTransformer.zLeftDot3("abcde", 4));
		Assert.assertEquals("abcd", ArgonTransformer.zLeftDot3("abcd", 5));
		Assert.assertEquals("abcde", ArgonTransformer.zLeftDot3("abcde", 5));
		Assert.assertEquals("ab...", ArgonTransformer.zLeftDot3("abcdef", 5));
	}

}
