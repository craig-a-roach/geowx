/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.text.ArgonNumber;

/**
 * @author roach
 */
public class TestUnit1Text {

	@Test
	public void t50_number() {
		Assert.assertEquals("005", ArgonNumber.intToDec3(5));
		Assert.assertEquals("-05", ArgonNumber.intToDec3(-5));
		Assert.assertEquals("000", ArgonNumber.intToDec3(0));
		Assert.assertEquals("999", ArgonNumber.intToDec3(999));
		Assert.assertEquals("-99", ArgonNumber.intToDec3(-99));
		Assert.assertEquals("1000", ArgonNumber.intToDec3(1000));
		Assert.assertEquals("-100", ArgonNumber.intToDec3(-100));
	}

	@Test
	public void t60_posixGood() {
		try {
			ArgonText.qtwPosixName("ab.c-d_e12");
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t61_posixBad() {
		try {
			ArgonText.qtwPosixName(".b");
			Assert.fail("Expecting rejection");
		} catch (final ArgonApiException ex) {
			System.out.println("Good: " + ex.getMessage());
		}
		try {
			ArgonText.qtwPosixName("a b");
			Assert.fail("Expecting rejection");
		} catch (final ArgonApiException ex) {
		}
		try {
			ArgonText.qtwPosixName("a/b");
			Assert.fail("Expecting rejection");
		} catch (final ArgonApiException ex) {
		}
		try {
			ArgonText.qtwPosixName("a\nb");
			Assert.fail("Expecting rejection");
		} catch (final ArgonApiException ex) {
		}
	}
}
