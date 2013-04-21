/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1Args {

	@Test
	public void t30() {
		final String[] args1 = { "--director", "-debug", "--pfile", "a.props", "-pfile", "b.props", "-Xp", "c.props", "x=vx",
				"y=vy" };
		final ArgonArgs aa1 = new ArgonArgs(args1);
		final boolean have_director = aa1.consumeFlag("director:d");
		Assert.assertTrue(have_director);
		final boolean have_debug = aa1.consumeFlag("debug");
		Assert.assertTrue(have_debug);
		final boolean have_X = aa1.consumeFlag(":X");
		Assert.assertTrue(have_X);
		final String[] pfile = aa1.consumeAllTagValuePairs("pfile:p").zptqtwValues();
		Assert.assertArrayEquals(new String[] { "a.props", "b.props", "c.props" }, pfile);
		final String[] array = aa1.consumeAllUntaggedValues();
		Assert.assertArrayEquals(new String[] { "x=vx", "y=vy" }, array);
		final String[] rem = aa1.consumeRemainder();
		Assert.assertArrayEquals(new String[] {}, rem);
	}

	@Test
	public void t40() {
		final String[] args = { "--accept", "\\w+[.]txt", "--timeout", "3m", "-t" };
		final ArgonArgs aa = new ArgonArgs(args);
		final boolean enable_text = aa.consumeFlag("text:t");
		Assert.assertTrue(enable_text);
		final ArgonArgsAccessor aAccept = aa.consumeAllTagValuePairs("accept:A");
		final ArgonArgsAccessor aTimeout = aa.consumeAllTagValuePairs("timeout:t");
		try {
			final Pattern accept = aAccept.patternValue();
			Assert.assertTrue(accept.matcher("a.txt").matches());
			final Elapsed timeout = aTimeout.elapsedValue();
			Assert.assertEquals(3 * 60 * 1000L, timeout.sms);
			aa.verifyUnsupported();
		} catch (final ArgonArgsException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t50() {
		final String[] args1 = { "-f", "", "a", "-d", "y", "-f", "b", "-g", "z", "x" };
		final ArgonArgs aa1 = new ArgonArgs(args1);
		final boolean have_d = aa1.consumeFlag("d");
		Assert.assertTrue(have_d);
		final ArgonArgs aa2 = aa1.newRemainder();
		final String[] zptf = aa2.consumeAllTagValuePairs("f").zptqtwValues();
		Assert.assertArrayEquals(new String[] { "a", "b" }, zptf);
		final String[] zptyx = aa2.consumeAllUntaggedValues();
		Assert.assertArrayEquals(new String[] { "y", "x" }, zptyx);
		final String unsup = aa2.toString();
		Assert.assertEquals("-g z", unsup);
	}

	@Test
	public void t60() {
		final String[] args1 = { "-ABC", "--alpha", "--beta", "--gamma", "ga", "fx", "fy" };
		final ArgonArgs aa1 = new ArgonArgs(args1);
		final boolean have_B = aa1.consumeFlag(":B");
		Assert.assertTrue(have_B);
		final boolean have_C = aa1.consumeFlag(":C");
		Assert.assertTrue(have_C);
		final boolean have_D = aa1.consumeFlag(":D");
		Assert.assertFalse(have_D);
		final boolean have_A = aa1.consumeFlag(":A");
		Assert.assertTrue(have_A);
		final boolean have_alpha = aa1.consumeFlag("alpha");
		Assert.assertTrue(have_alpha);
		final boolean have_beta = aa1.consumeFlag("beta");
		Assert.assertTrue(have_beta);
		final String[] gamma = aa1.consumeAllTagValuePairs("gamma").zptqtwValues();
		Assert.assertArrayEquals(new String[] { "ga" }, gamma);
		final String[] farray = aa1.consumeAllUntaggedValues();
		Assert.assertArrayEquals(new String[] { "fx", "fy" }, farray);
		final String[] unsep = aa1.consumeRemainder();
		Assert.assertArrayEquals(new String[] {}, unsep);
	}

}
