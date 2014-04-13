/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.wdt;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import com.metservice.argon.ArgonJoiner;
import com.metservice.argon.ArgonProperties;

/**
 * @author roach
 */
class Transcoder extends AbstractTranscoder {

	private static final String DataType = "SparseLatLonGrid";

	@Override
	protected void transcode(NetcdfFile ncFile)
			throws TranscodeException, IOException {
		trace.add("File type=" + ncFile.getFileTypeDescription());
		final Group root = ncFile.getRootGroup();
		final String qtwDataType = qtwAttribute(root, "DataType");
		if (!qtwDataType.equals(DataType)) {
			final String msg = "Unsupported data type '" + qtwDataType + "', expecting " + DataType;
			throw new TranscodeException(msg);
		}
		final String TypeName = qtwAttribute(root, "TypeName");
		final String Unit = qtwAttribute(root, "Unit-value");
		final float LatGridSpacing = floatAttribute(root, "LatGridSpacing");
		final float LonGridSpacing = floatAttribute(root, "LonGridSpacing");
		final Date Time = dateAttribute(root, "Time", "FractionalTime");
		final float Latitude = floatAttribute(root, "Latitude");
		final float Longitude = floatAttribute(root, "Longitude");
		final float Height = floatAttribute(root, "Height");
		final float MissingData = floatAttribute(root, "MissingData");

		final Dimension dimLatitude = selectDimension(root, "Lat");
		final Dimension dimLongitude = selectDimension(root, "Lon");
		final Dimension dimPixel = selectDimension(root, "pixel");

		final Variable varDatum = selectVariable(root, TypeName);
		final Variable varPixelLat = selectVariable(root, "pixel_x");
		final Variable varPixelLon = selectVariable(root, "pixel_y");
		final Variable varPixelRun = selectVariable(root, "pixel_count");

		final long tsStartRead = System.currentTimeMillis();
		final Array arrayDatum = varDatum.read();
		final Array arrayPixelLat = varPixelLat.read();
		final Array arrayPixelLon = varPixelLon.read();
		final Array arrayPixelRun = varPixelRun.read();
		final long msRead = System.currentTimeMillis() - tsStartRead;
		trace.add("File read elapsed=" + msRead + "ms");

		final int runCount = dimPixel.getLength();
		final int[] shapeDatum = arrayDatum.getShape();
		if (shapeDatum.length != 1 || shapeDatum[0] != runCount) {
			final String msg = "Unexpected datum shape [" + ArgonJoiner.zCsv(shapeDatum) + "]";
			throw new TranscodeException(msg);
		}

		for (int irun = 0; irun < runCount; irun++) {
			final float datum = arrayDatum.getFloat(irun);
			final int pixelLat = arrayPixelLat.getInt(irun);
			final int pixelLon = arrayPixelLon.getInt(irun);
			final int pixelRun = arrayPixelRun.getInt(irun);
			final float lat = Latitude + (LatGridSpacing * pixelLat);

			System.out.println(datum + "@" + pixelLat + "," + pixelLon + "*" + pixelRun);
		}

	}

	public Transcoder(ArgonProperties props, File inFile) {
		super(inFile);
		m_props = props;
	}

	private final ArgonProperties m_props;
}
