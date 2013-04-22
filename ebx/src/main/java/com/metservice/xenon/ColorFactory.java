/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.xenon;

import java.awt.Color;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Ds;
import com.metservice.argon.json.JsonAccessor;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonSchemaException;
import com.metservice.argon.json.JsonType;

/**
 * @author roach
 */
class ColorFactory {

	private static Color newColorFromArray(JsonAccessor acc, Color def)
			throws XenonApiException, JsonSchemaException {
		assert acc != null;
		final JsonArray array = acc.datumArray();
		final int size = array.size();
		if (size == 0) return def;
		final JsonAccessor accFirst = array.accessor(0, acc);
		final JsonType oType0 = accFirst.oJsonType;
		if (oType0 == null) return def;
		String oztwSpaceName = null;
		int pos = 0;
		if (oType0 == JsonType.TString) {
			oztwSpaceName = accFirst.datumZtwString();
			pos = 1;
		}
		final ColorModelName modelName = newModelName(acc, oztwSpaceName);
		final int componentCount = size - pos;
		final float[] components = new float[componentCount];
		for (int i = 0; i < componentCount; i++, pos++) {
			components[i] = array.accessor(pos, acc).datumFloat();
		}
		return newColorFromComponents(acc, modelName, components);
	}

	private static Color newColorFromComponents(JsonAccessor acc, ColorModelName model, float[] components)
			throws XenonApiException {
		final int ccActual = components.length;
		final int ccReqd = model.componentCount();
		final boolean supportsAlpha = model.supportsAlpha();
		if (ccActual < ccReqd) {
			final String m = "Insufficient " + model + " components for " + acc.name;
			throw new XenonApiException(m);
		}
		final int ccMax = ccReqd + (supportsAlpha ? 1 : 0);
		if (ccActual > ccMax) {
			final String m = "Too many " + model + " components for " + acc.name;
			throw new XenonApiException(m);
		}
		switch (model) {
			case rgb:
				return newRGB(acc, components);
			case hsb:
				return newHSB(acc, components);
			default: {
				final String m = acc.name + " color model '" + model + "' is unsupported";
				throw new XenonApiException(m);
			}
		}
	}

	private static Color newColorFromName(JsonAccessor acc, Color def)
			throws JsonSchemaException, XenonApiException {
		final String ztwName = acc.datumZtwString();
		if (ztwName.length() == 0) return def;
		final BasicColorName oMatch = BasicColorName.Table.find(ztwName);
		if (oMatch == null) {
			final String opts = BasicColorName.Table.qCommaValues();
			final String mm = acc.name + " color '" + ztwName + "' is undefined";
			final String m = mm + "; options are [" + opts + "']";
			throw new XenonApiException(m);
		}
		return oMatch.color();
	}

	private static Color newHSB(JsonAccessor acc, float[] components)
			throws XenonApiException {
		assert components != null;
		final int cc = components.length;
		final float hue = components[0];
		final float sat = components[1];
		final float bright = components[2];
		final float alpha = (cc < 4) ? 1.0f : components[3];
		try {
			final Color hsbColor = Color.getHSBColor(hue, sat, bright);
			final int r = hsbColor.getRed();
			final int g = hsbColor.getGreen();
			final int b = hsbColor.getBlue();
			final int a = Math.round(alpha * 255.0f);
			return new Color(r, g, b, a);
		} catch (final RuntimeException ex) {
			final String m = "Invalid color components for '" + acc.fullyQualifiedName() + "'..." + Ds.format(ex);
			throw new XenonApiException(m);
		}
	}

	private static ColorModelName newModelName(JsonAccessor acc, String oztwName)
			throws XenonApiException {
		final String oqtwName = ArgonText.oqtw(oztwName);
		if (oqtwName == null) return ColorModelName.rgb;
		final ColorModelName oName = ColorModelName.Table.find(oqtwName);
		if (oName == null) {
			final String opts = ColorModelName.Table.qCommaValues();
			final String mm = acc.name + " color model name '" + oqtwName + "'  is undefined";
			final String m = mm + "; options are [" + opts + "]";
			throw new XenonApiException(m);
		}
		return oName;
	}

	private static Color newRGB(JsonAccessor acc, float[] components)
			throws XenonApiException {
		assert components != null;
		final int cc = components.length;
		final int r = Math.round(components[0]);
		final int g = Math.round(components[1]);
		final int b = Math.round(components[2]);
		final int a = (cc < 4) ? 255 : Math.round(components[3]);
		try {
			return new Color(r, g, b, a);
		} catch (final RuntimeException ex) {
			final String m = "Invalid color components for '" + acc.name + "'..." + Ds.format(ex);
			throw new XenonApiException(m);
		}
	}

	public static Color selectColor(JsonAccessor acc, Color def)
			throws XenonApiException, JsonSchemaException {
		assert acc != null;
		if (!acc.isDefinedNonNull()) return def;
		final JsonType jsonType = acc.jsonType();
		if (jsonType == JsonType.TArray) return newColorFromArray(acc, def);
		return newColorFromName(acc, def);
	}
}
