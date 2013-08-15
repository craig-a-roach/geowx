/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import java.nio.file.Path;

/**
 * @author roach
 */
public class GalliumShapefileFormatException extends Exception {

	private static String message(Path opath, String diagnostic) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Malformed shape file");
		if (opath != null) {
			sb.append(" '").append(opath).append("'");
		}
		sb.append(" ...");
		sb.append(diagnostic);
		return sb.toString();
	}

	public GalliumShapefileFormatException(Path opath, String diagnostic) {
		super(message(opath, diagnostic));
	}
}
