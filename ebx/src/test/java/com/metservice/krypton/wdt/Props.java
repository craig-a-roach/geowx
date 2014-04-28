/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.wdt;

import java.util.TimeZone;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonArgs;
import com.metservice.argon.ArgonArgsException;
import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonProperties;
import com.metservice.argon.ArgonPropertiesAttribute;
import com.metservice.argon.Elapsed;
import com.metservice.argon.ElapsedFactory;
import com.metservice.argon.TimeZoneFactory;
import com.metservice.krypton.KryptonProduct2Builder;

/**
 * @author roach
 */
class Props {

	private static final String PFILE = "pfile:p";
	private static final String VERBOSE = "verbose:v";
	private static final String TIMING = "timing:t";
	private static final String NCPATH = "ncpath:n";
	private static final String NCPATTERN = "ncpattern";
	private static final String NOW = "now";
	private static final String PARAMETER_CATEGORY = "parameterCategory";
	private static final String PARAMETER_NO = "parameterNo";
	private static final String SURFACE_ID = "surfaceId";
	private static final String ANALYSIS_TIME_PATTERN = "analysisTimePattern";
	private static final String ISSUING_CENTRE_ID = "issuingCentreId";
	private static final String GENERATING_PROCESS_ID = "generatingProcessId";
	private static final String DATA_DECIMAL_SCALE = "dataDecimalScale";
	private static final String UNIT_PREFIX = "unit";

	private static void setDefaults(ArgonProperties.BuilderFromArgs b) {
		b.putProperty(NCPATTERN, ".+[.]nc");
		b.putProperty(TIMING, false);
		b.putProperty(VERBOSE, false);
		b.putProperty(PARAMETER_CATEGORY, 16);
		b.putProperty(PARAMETER_NO, 4);
		b.putProperty(SURFACE_ID, KryptonProduct2Builder.Table4_5.EntireAtmosphereConsideredSingleLayer);
		b.putProperty(ANALYSIS_TIME_PATTERN, "maple_composite_\\d+m_(\\d+)_\\d+[.]nc");
		b.putProperty(ISSUING_CENTRE_ID, 70);
		b.putProperty(GENERATING_PROCESS_ID, KryptonProduct2Builder.Table4_3.Forecast);
		b.putProperty(DATA_DECIMAL_SCALE, 1);
		b.putProperty(unitQualName("dBZ"), "1.0");
	}

	private static String unitQualName(String unitName) {
		return UNIT_PREFIX + "." + unitName;
	}

	public static Props newInstance(String[] args)
			throws ArgonArgsException, ArgonFormatException {
		final ArgonArgs aa = new ArgonArgs(args);
		final ArgonProperties.BuilderFromArgs b = ArgonProperties.newBuilder(aa);
		setDefaults(b);
		b.putFiles(PFILE);
		b.putMappedArg(NCPATH);
		b.putMappedFlag(TIMING);
		b.putMappedFlag(VERBOSE);
		b.putAssignments();
		b.printlnUnsupportedMessage();
		final ArgonProperties ap = b.newProperties();
		return new Props(ap);
	}

	public Elapsed analysisTimeInterval() {
		return ElapsedFactory.newElapsedConstant("10m");
	}

	public boolean dataEncodeByteAligned() {
		return false;
	}

	public int dataEncodeDecimalScale()
			throws ArgonFormatException, ArgonArgsException {
		return src.select(DATA_DECIMAL_SCALE).intValue();
	}

	public int dataEncodeMaxBitDepth() {
		return 16;
	}

	public TimeZone fileNameTimezone() {
		return TimeZoneFactory.GMT;
	}

	public int generatingProcessId()
			throws ArgonFormatException, ArgonArgsException {
		return src.select(GENERATING_PROCESS_ID).intValue();
	}

	public ArgonPropertiesAttribute getUnitConverter(String unitName) {
		return src.find(unitQualName(unitName));
	}

	public int issuingCentreId()
			throws ArgonFormatException, ArgonArgsException {
		return src.select(ISSUING_CENTRE_ID).intValue();
	}

	public Pattern oAnalysisTimePattern()
			throws ArgonFormatException, ArgonArgsException {
		return src.select(ANALYSIS_TIME_PATTERN).opattern();
	}

	public Pattern oNCPattern()
			throws ArgonFormatException, ArgonArgsException {
		return src.select(NCPATTERN).opattern();
	}

	public int parameterCategory()
			throws ArgonFormatException, ArgonArgsException {
		return src.select(PARAMETER_CATEGORY).intValue();
	}

	public int parameterNo()
			throws ArgonFormatException, ArgonArgsException {
		return src.select(PARAMETER_NO).intValue();
	}

	public String qtwNCPath()
			throws ArgonArgsException {
		return src.select(NCPATH).qtwValue;
	}

	public int surfaceId()
			throws ArgonFormatException, ArgonArgsException {
		return src.select(SURFACE_ID).intValue();
	}

	public boolean timing()
			throws ArgonArgsException {
		return src.select(TIMING).flag();
	}

	public boolean verbose()
			throws ArgonArgsException {
		return src.select(VERBOSE).flag();
	}

	private Props(ArgonProperties src) throws ArgonFormatException {
		assert src != null;
		this.src = src;
		final ArgonPropertiesAttribute oAttNow = src.find(NOW);
		if (oAttNow != null) {
			ArgonClock.simulatedNow(oAttNow.ts());
		}
	}
	final ArgonProperties src;
}
