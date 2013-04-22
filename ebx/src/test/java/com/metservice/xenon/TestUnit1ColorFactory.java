/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.xenon;

import java.awt.Color;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.json.JsonAccessor;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;
import com.metservice.argon.json.JsonString;

/**
 * @author roach
 */
public class TestUnit1ColorFactory {

	private static JsonAccessor newArrayDouble(String pname, String model, double... spec) {
		final JsonObject neo = JsonObject.newMutable(1);
		final JsonArray array = JsonArray.newMutable();
		array.addString(model);
		for (int i = 0; i < spec.length; i++) {
			array.addDouble(spec[i]);
		}
		neo.put(pname, array);
		return neo.accessor(pname);
	}

	private static JsonAccessor newArrayInteger(String pname, int... spec) {
		final JsonObject neo = JsonObject.newMutable(1);
		final JsonArray array = JsonArray.newMutable();
		for (int i = 0; i < spec.length; i++) {
			array.addInteger(spec[i]);
		}
		neo.put(pname, array);
		return neo.accessor(pname);
	}

	private static JsonAccessor newString(String pname, String value) {
		final JsonObject neo = JsonObject.newMutable(1);
		neo.put(pname, JsonString.newInstance(value));
		return neo.accessor(pname);
	}

	@Test
	public void t10_rgb() {
		final JsonAccessor acc = newArrayInteger("bg", 250, 200, 150);
		try {
			final Color color = ColorFactory.selectColor(acc, Color.gray);
			Assert.assertEquals("r", 250, color.getRed());
			Assert.assertEquals("g", 200, color.getGreen());
			Assert.assertEquals("b", 150, color.getBlue());
		} catch (XenonApiException | JsonSchemaException ex) {
			Assert.fail("Api|Schema Exception.." + ex.getMessage());
		}
	}

	@Test
	public void t20_rgba() {
		final JsonAccessor acc = newArrayDouble("bg", "rgb", 150, 100, 50, 90);
		try {
			final Color color = ColorFactory.selectColor(acc, Color.gray);
			Assert.assertEquals("r", 150, color.getRed());
			Assert.assertEquals("g", 100, color.getGreen());
			Assert.assertEquals("b", 50, color.getBlue());
			Assert.assertEquals("a", 90, color.getAlpha());
		} catch (XenonApiException | JsonSchemaException ex) {
			Assert.fail("Api|Schema Exception.." + ex.getMessage());
		}
	}

	@Test
	public void t30_name() {
		final JsonAccessor acc = newString("bg", "Red");
		try {
			final Color color = ColorFactory.selectColor(acc, Color.gray);
			Assert.assertEquals("r", 255, color.getRed());
			Assert.assertEquals("g", 0, color.getGreen());
			Assert.assertEquals("b", 0, color.getBlue());
			Assert.assertEquals("a", 255, color.getAlpha());
		} catch (XenonApiException | JsonSchemaException ex) {
			Assert.fail("Api|Schema Exception.." + ex.getMessage());
		}
	}

	@Test
	public void t40_hsba() {
		final JsonAccessor acc = newArrayDouble("bg", "hsb", 0.6, 0.8, 0.7, 0.2);
		try {
			final Color color = ColorFactory.selectColor(acc, Color.gray);
			Assert.assertEquals("r", 36, color.getRed());
			Assert.assertEquals("g", 93, color.getGreen());
			Assert.assertEquals("b", 179, color.getBlue());
			Assert.assertEquals("a", 51, color.getAlpha());
		} catch (XenonApiException | JsonSchemaException ex) {
			Assert.fail("Api|Schema Exception.." + ex.getMessage());
		}
	}

	@Test
	public void t90_badColorModel() {
		final JsonAccessor acc = newArrayDouble("bg", "bogus", 150);
		try {
			ColorFactory.selectColor(acc, Color.gray);
			Assert.fail("Should have detected bad color model");
		} catch (XenonApiException | JsonSchemaException ex) {
			System.out.println("Good exception: " + ex.getMessage());
		}
	}

	@Test
	public void t90_badColorValue() {
		final JsonAccessor acc = newArrayDouble("bg", "rgb", 150, 100e+199, 50, 90);
		try {
			ColorFactory.selectColor(acc, Color.gray);
			Assert.fail("Should have detected bad color value");
		} catch (XenonApiException | JsonSchemaException ex) {
			System.out.println("Good exception: " + ex.getMessage());
		}
	}

}
