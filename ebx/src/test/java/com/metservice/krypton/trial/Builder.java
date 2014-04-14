/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.trial;

import java.io.File;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonArgs;
import com.metservice.argon.ArgonArgsException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.file.ArgonFileManifest;
import com.metservice.krypton.KryptonCentre;
import com.metservice.krypton.KryptonCodeException;
import com.metservice.krypton.KryptonDataRecord;
import com.metservice.krypton.KryptonDecoder;
import com.metservice.krypton.KryptonDecoderConfig;
import com.metservice.krypton.KryptonFileReader;
import com.metservice.krypton.KryptonMetaRecord;
import com.metservice.krypton.KryptonReadException;
import com.metservice.krypton.KryptonRecordException;
import com.metservice.krypton.KryptonRecordReader;
import com.metservice.krypton.KryptonTableException;

/**
 * @author roach
 */
public class Builder {

	public static Builder newInstance(ArgonArgs args, Probe probe)
			throws ArgonArgsException, ArgonPermissionException, ArgonQuotaException, ArgonStreamReadException {
		assert args != null;
		assert probe != null;
		final KryptonDecoderConfig decoderConfig = KryptonDecoderConfig.newInstance(args);
		final ParsedArgs pa = new ParsedArgs(args);
		final ArgonFileManifest gridManifest = pa.newGridManifest();
		final KryptonDecoder decoder = new KryptonDecoder(probe, decoderConfig);
		args.verifyUnsupported();
		return new Builder(probe, pa, gridManifest, decoder);
	}

	private void build(Outcome outcome, File gridFile) {
		assert outcome != null;
		assert gridFile != null;
		final KryptonFileReader oFileReader = KryptonFileReader.createInstance(m_decoder, gridFile);
		if (oFileReader == null) return;
		try {
			int skippedRecordsinFile = 0;
			while (oFileReader.hasNextRecordReader()) {
				try {
					final KryptonRecordReader rr = oFileReader.nextRecordReader();
					final int recordIndex = rr.recordIndex();
					try {
						final KryptonDataRecord data = rr.newDataRecord();
						final KryptonMetaRecord meta = data.meta();
						final KryptonCentre recordCentre = meta.centre();
						final String oqErrRecordCentre = outcome.validateRecordCentre(recordCentre);
						if (oqErrRecordCentre == null) {
							outcome.goodRecord();
						} else {
							m_probe.gridFileCode(gridFile, recordIndex, oqErrRecordCentre);
							skippedRecordsinFile++;
							outcome.skipRecord();
						}
					} catch (final KryptonCodeException ex) {
						m_probe.gridFileCode(gridFile, recordIndex, ex);
						skippedRecordsinFile++;
						outcome.skipRecord();
					} catch (final KryptonTableException ex) {
						m_probe.gridFileTable(gridFile, recordIndex, ex);
						skippedRecordsinFile++;
						outcome.skipRecord();
					}
				} catch (final KryptonRecordException ex) {
					m_probe.gridFileRecord(gridFile, ex);
					skippedRecordsinFile++;
					outcome.skipRecord();
				}
			}
			if (skippedRecordsinFile == 0) {
				outcome.goodFile();
			} else {
				outcome.partialFile();
			}
		} catch (final KryptonReadException ex) {
			m_probe.gridFileRead(gridFile, ex);
			outcome.skipFile();
		} finally {
			oFileReader.close();
		}
	}

	public void build(Outcome outcome)
			throws ArgonPermissionException, ArgonStreamWriteException {
		assert outcome != null;
		final File[] zptGridFilesAscPath = m_gridManifest.zptFilesAscPath();
		final int gridFileCount = zptGridFilesAscPath.length;
		for (int i = 0; i < gridFileCount; i++) {
			final File gridFile = zptGridFilesAscPath[i];
			build(outcome, gridFile);
		}
	}

	private Builder(Probe probe, ParsedArgs pa, ArgonFileManifest gm, KryptonDecoder de) {
		assert probe != null;
		assert pa != null;
		assert gm != null;
		m_probe = probe;
		m_gridManifest = gm;
		m_decoder = de;
	}
	private final Probe m_probe;
	private final ArgonFileManifest m_gridManifest;
	private final KryptonDecoder m_decoder;

	private static class ParsedArgs {

		public ArgonFileManifest newGridManifest()
				throws ArgonPermissionException {
			return ArgonFileManifest.newInstance(xptqtwGribPaths, oAcceptPattern, oRejectPattern);
		}

		public ParsedArgs(ArgonArgs in) throws ArgonArgsException {
			oAcceptPattern = in.consumeAllTagValuePairs(CArg.AcceptPattern).getPatternValue();
			oRejectPattern = in.consumeAllTagValuePairs(CArg.RejectPattern).getPatternValue();
			xptqtwGribPaths = in.consumeAllUntaggedValues(CArg.GribPaths).xptqtwValues();
		}
		public final Pattern oAcceptPattern;
		public final Pattern oRejectPattern;
		public final String[] xptqtwGribPaths;
	}
}
