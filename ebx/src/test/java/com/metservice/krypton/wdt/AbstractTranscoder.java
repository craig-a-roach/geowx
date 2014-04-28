/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.wdt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import com.metservice.argon.ArgonJoiner;
import com.metservice.argon.ArgonText;
import com.metservice.argon.DateFormatter;
import com.metservice.krypton.KryptonBuildException;
import com.metservice.krypton.KryptonRecord2Builder;

/**
 * @author roach
 */
abstract class AbstractTranscoder {

	private NetcdfFile newNCFile()
			throws TranscodeException {
		try {
			final NetcdfFile oNCFile = NetcdfFile.open(m_inFile.getPath());
			if (oNCFile == null) throw new TranscodeException("NetCDF open returned null");
			return oNCFile;
		} catch (final IOException ex) {
			throw new TranscodeException("Cannot open NetCDF file", ex);
		}
	}

	protected Date dateAttribute(Group root, String integral, String ofrac)
			throws TranscodeException {
		final Attribute attIntegral = selectAttribute(root, integral);
		final Number oIntegral = attIntegral.getNumericValue();
		if (oIntegral == null) throw new TranscodeException("Numeric value of attribute '" + integral + "' is null");
		final Attribute oAttFrac = ofrac == null ? null : root.findAttribute(ofrac);
		final Number oFrac = oAttFrac == null ? null : oAttFrac.getNumericValue();
		long ts = oIntegral.longValue() * 1000L;
		if (oFrac != null) {
			ts += Math.round(oFrac.floatValue() * 1000L);
		}
		final Date d = new Date(ts);
		trace.add(integral + "=" + DateFormatter.newT8FromDate(d) + "");
		return d;
	}

	protected float floatAttribute(Group root, String name)
			throws TranscodeException {
		final Attribute att = selectAttribute(root, name);
		final Number oN = att.getNumericValue();
		if (oN == null) throw new TranscodeException("Numeric value of attribute '" + name + "' is null");
		trace.add(name + "=" + oN + "");
		return oN.floatValue();
	}

	protected String qtwAttribute(Group root, String name)
			throws TranscodeException {
		final Attribute att = selectAttribute(root, name);
		final String ztw = ArgonText.ztw(att.getStringValue());
		if (ztw.length() == 0) throw new TranscodeException("String value of attribute '" + name + "' is empty");
		trace.add(name + "='" + ztw + "'");
		return ztw;
	}

	protected void saveGRIB(KryptonRecord2Builder record)
			throws TranscodeException, IOException {
		if (record == null) throw new IllegalArgumentException("object is null");
		final String inPath = m_inFile.getPath();
		final File dst = new File(inPath + ".grib");
		BufferedOutputStream obos = null;
		try {
			obos = new BufferedOutputStream(new FileOutputStream(dst));
			record.save(obos);
		} catch (final FileNotFoundException ex) {
			throw new TranscodeException("Cannot save " + dst, ex);
		} catch (final KryptonBuildException ex) {
			throw new TranscodeException(ex, "saving " + dst);
		} finally {
			if (obos != null) {
				try {
					obos.close();
				} catch (final IOException ex) {
				}
			}

		}
	}

	protected Attribute selectAttribute(Group root, String name)
			throws TranscodeException {
		if (root == null) throw new IllegalArgumentException("object is null");
		final Attribute oAtt = root.findAttribute(name);
		if (oAtt == null) throw new TranscodeException("Missing required attribute '" + name + "'");
		return oAtt;
	}

	protected Dimension selectDimension(Group root, String name)
			throws TranscodeException {
		final Dimension oDim = root.findDimension(name);
		if (oDim == null) throw new TranscodeException("Missing required dimension '" + name + "'");
		trace.add("Dimension '" + name + "' length=" + oDim.getLength());
		return oDim;
	}

	protected Variable selectVariable(Group root, String name)
			throws TranscodeException {
		final Variable oVar = root.findVariable(name);
		if (oVar == null) throw new TranscodeException("Missing required variable '" + name + "'");
		trace.add("Variable '" + name + "' length=" + oVar);
		return oVar;
	}

	protected abstract void transcode(NetcdfFile ncFile)
			throws TranscodeException, IOException;

	public String fileName() {
		return m_inFile.getName();
	}

	public long msElapsed() {
		return System.currentTimeMillis() - m_tsStart;
	}

	public String traceText() {
		return trace.toString();
	}

	public final void transcode()
			throws TranscodeException {
		final NetcdfFile ncFile = newNCFile();
		final String fileTypeDescription = ncFile.getFileTypeDescription();
		try {
			transcode(ncFile);
		} catch (final IOException ex) {
			throw new TranscodeException("Read operation on " + fileTypeDescription + " failed", ex);
		} catch (final RuntimeException ex) {
			throw new TranscodeException("Operation on " + fileTypeDescription + " failed", ex);
		} finally {
			try {
				ncFile.close();
			} catch (final IOException ex) {
			}
		}
	}

	protected AbstractTranscoder(File inFile) {
		if (inFile == null) throw new IllegalArgumentException("object is null");
		m_inFile = inFile;
		trace = new Trace();
		m_tsStart = System.currentTimeMillis();
	}

	public final Trace trace;
	private final File m_inFile;
	private final long m_tsStart;

	class Trace {

		public void add(Object... messages) {
			for (int i = 0; i < messages.length; i++) {
				final Object o = messages[i];
				final String s = o == null ? "" : o.toString().trim();
				if (s.length() > 0) {
					m_lines.add(s);
				}
			}
		}

		@Override
		public String toString() {
			return ArgonJoiner.zJoin(m_lines, "\n");
		}

		public Trace() {
		}
		private final List<String> m_lines = new ArrayList<>();
	}
}
