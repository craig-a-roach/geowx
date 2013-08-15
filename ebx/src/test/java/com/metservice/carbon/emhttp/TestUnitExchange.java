/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.DateFactory;
import com.metservice.beryllium.BerylliumQuery;
import com.metservice.carbon.TestCarbon;
import com.metservice.carbon.TestImpHttpExchange;

/**
 * @author roach
 */
public class TestUnitExchange extends TestCarbon {

	@Test
	public void t40_postForm()
			throws InterruptedException {
		final Resource r = new Resource("exchange_postForm.txt");
		final BerylliumQuery q = BerylliumQuery.newConstant("pa", "a0", "pb", "b1", "pb", "b2");
		final TestImpHttpExchange out = sendLocalPOSTForm(q, "exchange_postForm");
		out.waitForDone();
		Assert.assertEquals(r.toString(), out.toString());
	}

	@Test
	public void t50_postJson()
			throws InterruptedException {
		final Resource r = new Resource("exchange_postJson.txt");
		final String json = "{\"pa\":\"a0\",\"pb\":[\"b1\",\"b2\"]}";
		final TestImpHttpExchange out = sendLocalPOSTJson(json, "exchange_postJson");
		out.waitForDone();
		Assert.assertEquals(r.toString(), out.toString());
	}

	@Test
	public void t60_get()
			throws InterruptedException {
		final Resource r = new Resource("exchange_get.txt");
		final Date ifMod = DateFactory.newDateConstantFromT8("20100701T1200Z00M000");
		final BerylliumQuery q = BerylliumQuery.newConstant("pa", "a0", "pb", "b1", "pb", "b2", "pb", "null", "pn", "null");
		final TestImpHttpExchange out = sendLocalGET(q, "exchange_get", ifMod);
		out.waitForDone();
		System.out.println(out);
		Assert.assertEquals(r.toString(), out.toString());
	}

	@Test
	public void t80_postFormBad()
			throws InterruptedException {
		final String json = "{\"pa\":\"a0\" \"pb\":[\"b1\",\"b2\"]}";
		final TestImpHttpExchange out = sendLocalPOSTJson(json, "exchange_postJson");
		out.waitForDone();
		Assert.assertEquals(400, out.httpStatusCode());
	}

}
