/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.shapefile;

import java.io.IOException;
import java.nio.file.Path;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class GalliumShapefileReadException extends Exception {

	private static String message(Path opath, IOException ex) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Cannot read file");
		if (opath != null) {
			sb.append(" '").append(opath).append("'");
		}
		sb.append(" ...");
		sb.append(Ds.format(ex));
		return sb.toString();
	}

	public GalliumShapefileReadException(Path path, IOException ex) {
		super(message(path, ex));
	}

}
