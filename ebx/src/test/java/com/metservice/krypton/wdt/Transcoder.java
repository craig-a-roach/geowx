/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.wdt;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
		final Variable varPixelNS = selectVariable(root, "pixel_x");
		final Variable varPixelWE = selectVariable(root, "pixel_y");
		final Variable varPixelRun = selectVariable(root, "pixel_count");

		final long tsStartRead = System.currentTimeMillis();
		final Array arrayDatum = varDatum.read();
		final Array arrayPixelNS = varPixelNS.read();
		final Array arrayPixelWE = varPixelWE.read();
		final Array arrayPixelRun = varPixelRun.read();
		trace.add("File read elapsed=" + (System.currentTimeMillis() - tsStartRead) + "ms");

		final int NX = dimLongitude.getLength();
		final int NY = dimLatitude.getLength();
		final int runCount = dimPixel.getLength();
		final int[] shapeDatum = arrayDatum.getShape();
		if (shapeDatum.length != 1 || shapeDatum[0] != runCount) {
			final String msg = "Unexpected datum shape [" + ArgonJoiner.zCsv(shapeDatum) + "]";
			throw new TranscodeException(msg);
		}

		final long tsStartDecode = System.currentTimeMillis();
		final int gridLength = NX * NY;
		final float[] grid = new float[gridLength];
		Arrays.fill(grid, MissingData);
		for (int irun = 0; irun < runCount; irun++) {
			final float datum = arrayDatum.getFloat(irun);
			final int pixelNS = arrayPixelNS.getInt(irun);
			final int pixelWE = arrayPixelWE.getInt(irun);
			final int pixelRun = arrayPixelRun.getInt(irun);
			final int istart = (pixelNS * NX) + pixelWE;
			final int iend = istart + pixelRun;
			if (iend >= gridLength) {
				final String msg = "Invalid grid offset " + iend + "/" + gridLength + " for run " + irun;
				throw new TranscodeException(msg);
			}
			Arrays.fill(grid, istart, iend, datum);
		}
		trace.add("Decode elapsed=" + (System.currentTimeMillis() - tsStartDecode) + "ms");
	}

	public Transcoder(ArgonProperties props, File inFile) {
		super(inFile);
		m_props = props;
	}

	private final ArgonProperties m_props;
}
