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
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonArgsException;
import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonJoiner;
import com.metservice.argon.ArgonPropertiesAttribute;
import com.metservice.argon.CArgon;
import com.metservice.argon.DateFactory;
import com.metservice.argon.Elapsed;
import com.metservice.argon.TimeFactors;
import com.metservice.krypton.KryptonBitmap2Builder;
import com.metservice.krypton.KryptonData2Packer00;
import com.metservice.krypton.KryptonDataBinary2Builder;
import com.metservice.krypton.KryptonDataRepresentation2Builder;
import com.metservice.krypton.KryptonGrid2Builder;
import com.metservice.krypton.KryptonIdentification2Builder;
import com.metservice.krypton.KryptonProduct2Builder;
import com.metservice.krypton.KryptonProduct2Builder.Template4_0;
import com.metservice.krypton.KryptonRecord2Builder;

/**
 * @author roach
 */
class Transcoder extends AbstractTranscoder {

	private static final String DataType = "SparseLatLonGrid";
	private static final long MaxPrognosisMins = CArgon.MIN_PER_DAY;

	private Date analysisTime(Group root)
			throws TranscodeException {
		try {
			final Pattern oPattern = m_props.oAnalysisTimePattern();
			final TimeZone tz = m_props.fileNameTimezone();
			if (oPattern != null) {
				final Matcher matcher = oPattern.matcher(fileName());
				if (matcher.find() && matcher.groupCount() == 1) {
					final String tx = matcher.group(1);
					try {
						return DateFactory.newInstance(tx, tz);
					} catch (final ArgonFormatException ex) {
						final String msg = ex.getMessage();
						trace.add("Cannot parse analysis time '" + tx + "' in file name (" + msg + ") - will estimate");
					}
				}
			}
			final TimeFactors est = estimatedAnalysisTime();
			trace.add("Using estimated analysis time=" + est);
			return est.newDate();
		} catch (final ArgonFormatException | ArgonArgsException | ArgonApiException ex) {
			throw new TranscodeException(ex, "Cannot determine analysis time");
		}
	}

	private TimeFactors estimatedAnalysisTime()
			throws ArgonArgsException, ArgonApiException {
		final Elapsed interval = m_props.analysisTimeInterval();
		final long tsNow = ArgonClock.tsNow() - (interval.sms / 2);
		final TimeFactors tf = TimeFactors.newInstance(tsNow, m_props.fileNameTimezone());
		return tf.newAlignedInterval(interval.sms, TimeFactors.AlignSense.Floor);
	}

	private KryptonGrid2Builder newGDS(Group root, int nx, int ny)
			throws TranscodeException {
		final float dx = floatAttribute(root, "LonGridSpacing");
		final float dy = floatAttribute(root, "LatGridSpacing");

		final float La1 = floatAttribute(root, "Latitude");
		final float Lo1 = floatAttribute(root, "Longitude");
		final float La2 = La1 - (ny * dy);
		final float Lo2 = Lo1 + (nx * dx);

		final KryptonGrid2Builder gds = new KryptonGrid2Builder();
		gds.newTemplate3_0(nx, dx, ny, dy, La1, Lo1, La2, Lo2);
		return gds;
	}

	private KryptonIdentification2Builder newIDS(Group root, Date refTime)
			throws TranscodeException {
		try {
			final int centre = m_props.issuingCentreId();
			final int typeOfData = KryptonIdentification2Builder.Table1_4.Forecast_Products;
			return new KryptonIdentification2Builder(centre, typeOfData, refTime);
		} catch (ArgonArgsException | ArgonFormatException ex) {
			throw new TranscodeException(ex, "Cannot construct identification section");
		}
	}

	private KryptonData2Packer00 newPacker(float unitConverter, float[] grid)
			throws TranscodeException {
		try {
			final int decimalScale = m_props.dataEncodeDecimalScale();
			final int maxBitDepth = m_props.dataEncodeMaxBitDepth();
			final boolean dataByteAligned = m_props.dataEncodeByteAligned();
			return KryptonData2Packer00.newInstance(grid, unitConverter, decimalScale, maxBitDepth, dataByteAligned);
		} catch (ArgonArgsException | ArgonFormatException ex) {
			throw new TranscodeException(ex, "Cannot construct data representation");
		}
	}

	private KryptonProduct2Builder newPDS(Group root, Date refTime)
			throws TranscodeException {
		try {
			final int parameterCategory = m_props.parameterCategory();
			final int parameterNo = m_props.parameterNo();
			final int surfaceId = m_props.surfaceId();
			final int generatingProcessId = m_props.generatingProcessId();

			final Date validityTime = dateAttribute(root, "Time", "FractionalTime");
			final long minsPrognosisL = (validityTime.getTime() - refTime.getTime()) / 60000;
			final int minsPrognosis = (int) Math.max(0L, Math.min(MaxPrognosisMins, minsPrognosisL));

			final float height = floatAttribute(root, "Height");
			if (height > 0.0f) throw new TranscodeException("Unsupported height " + height + "...expecting zero");

			final KryptonProduct2Builder pds = new KryptonProduct2Builder();
			final Template4_0 t4_0 = pds.newTemplate4_0(parameterCategory, parameterNo);
			t4_0.typeOfGeneratingProcess(generatingProcessId);
			t4_0.unitOfTimeRange(KryptonProduct2Builder.Table4_4.Minute);
			t4_0.forecastTime(minsPrognosis);
			t4_0.horizontalLayer().level1().type(surfaceId);
			return pds;
		} catch (ArgonArgsException | ArgonFormatException ex) {
			throw new TranscodeException(ex, "Cannot construct product section");
		}
	}

	private float unitConverter(Group root)
			throws TranscodeException {
		final String unitName = qtwAttribute(root, "Unit-value");
		final ArgonPropertiesAttribute oConverter = m_props.getUnitConverter(unitName);
		if (oConverter == null) throw new TranscodeException("No conversion property for unit '" + unitName + "'");
		try {
			return oConverter.floatValue();
		} catch (final ArgonFormatException ex) {
			throw new TranscodeException(ex, "Cannot parse unit converter");
		}
	}

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
		final Date refTime = analysisTime(root);
		final float unitConverter = unitConverter(root);

		final String TypeName = qtwAttribute(root, "TypeName");

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
		Arrays.fill(grid, Float.NaN);
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

		final long tsStartEncode = System.currentTimeMillis();

		final KryptonIdentification2Builder IDS = newIDS(root, refTime);
		final KryptonGrid2Builder GDS = newGDS(root, NX, NY);
		final KryptonProduct2Builder PDS = newPDS(root, refTime);
		final KryptonData2Packer00 pk00 = newPacker(unitConverter, grid);
		final KryptonDataRepresentation2Builder DRS = new KryptonDataRepresentation2Builder(pk00);
		final KryptonBitmap2Builder BMS = new KryptonBitmap2Builder(pk00);
		final KryptonDataBinary2Builder DBS = new KryptonDataBinary2Builder(pk00);
		final KryptonRecord2Builder record = KryptonRecord2Builder.newMeteorological(IDS, GDS, PDS, DRS, BMS, DBS);
		trace.add("Encode elapsed=" + (System.currentTimeMillis() - tsStartEncode) + "ms");

		final long tsStartSave = System.currentTimeMillis();
		saveGRIB(record);
		trace.add("Write GRIB file elapsed=" + (System.currentTimeMillis() - tsStartSave) + "ms");
	}

	public Transcoder(Props props, File inFile) {
		super(inFile);
		m_props = props;
	}

	private final Props m_props;
}
