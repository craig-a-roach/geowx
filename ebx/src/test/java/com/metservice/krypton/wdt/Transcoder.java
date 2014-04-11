/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.wdt;

import java.io.File;
import java.io.IOException;

import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;

import com.metservice.argon.ArgonProperties;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
class Transcoder {

	private static final String DIM_Latitude = "Lat";
	private static final String DIM_Longitude = "Lon";
	private static final String DIM_pixel = "pixel";

	private Result newResult(NetcdfFile ncfile) {
		if (ncfile == null) throw new IllegalArgumentException("object is null");
		final String fileTypeDescription = ncfile.getFileTypeDescription();
		final Dimension oDimLatitude = ncfile.findDimension(DIM_Latitude);
		final Dimension oDimLongitude = ncfile.findDimension(DIM_Longitude);
		return null;
	}

	public Result newResult() {
		NetcdfFile oNCFile = null;
		try {
			oNCFile = NetcdfFile.open(m_inFile.getPath());
			if (oNCFile == null) return new Result("NetCDF open returned null");
			return newResult(oNCFile);
		} catch (final IOException ex) {
			return new Result("Cannot open NetCDF file..." + Ds.message(ex));
		} finally {
			if (oNCFile != null) {
				try {
					oNCFile.close();
				} catch (final IOException ex) {
				}
			}
		}
	}

	public Transcoder(ArgonProperties props, File inFile) {
		if (props == null) throw new IllegalArgumentException("object is null");
		if (inFile == null) throw new IllegalArgumentException("object is null");
		m_props = props;
		m_inFile = inFile;
		m_tsStart = System.currentTimeMillis();
	}
	private final ArgonProperties m_props;
	private final File m_inFile;
	private final long m_tsStart;
}
