/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author roach
 */
class BerylliumMimeTypeTable {

	public static final String DefaultText = "text/plain";
	public static final String DefaultBinary = "application/octet-stream";

	private String inferMimeType(BerylliumPath path) {
		assert path != null;
		for (final String qtwNode : path) {
			final String qlc = qtwNode.toLowerCase();
			if (qlc.startsWith("image") || qlc.equals("bin")) return DefaultBinary;
		}
		return DefaultText;
	}

	public BerylliumPathMime mimeTypeByExtension(BerylliumPath path) {
		if (path == null) throw new IllegalArgumentException("object is null");
		final BerylliumPathExt oPathExt = path.createExtension();
		if (oPathExt != null) {
			String vqlctwMimeType = extmap.get(oPathExt.qlctwExt);
			if (vqlctwMimeType == null) {
				vqlctwMimeType = inferMimeType(path);
			}
			return new BerylliumPathMime(vqlctwMimeType, oPathExt);
		}

		final String qlctwMimeType = inferMimeType(path);
		return new BerylliumPathMime(qlctwMimeType, null);
	}

	private static String oqlctw(String oz) {
		if (oz == null) return null;
		final String zlctw = oz.trim().toLowerCase();
		return (zlctw.length() == 0) ? null : zlctw;
	}

	public BerylliumMimeTypeTable(String... extMimePairs) {
		try {
			final ResourceBundle bundle = ResourceBundle.getBundle("com.metservice.beryllium.mime");
			for (final String extKey : bundle.keySet()) {
				final String ozmime = bundle.getString(extKey);
				final String okey = oqlctw(extKey);
				final String omime = oqlctw(ozmime);
				if (okey != null && omime != null) {
					extmap.put(okey, omime);
				}
			}
		} catch (final MissingResourceException ex) {
		}
		final int pairCount = extMimePairs.length / 2;
		for (int i = 0, iext = 0, imime = 1; i < pairCount; i++, iext += 2, imime += 2) {
			final String okey = oqlctw(extMimePairs[iext]);
			final String omime = oqlctw(extMimePairs[imime]);
			if (okey != null && omime != null) {
				extmap.put(okey, omime);
			}
		}
	}
	private final Map<String, String> extmap = new HashMap<String, String>(256);
}
