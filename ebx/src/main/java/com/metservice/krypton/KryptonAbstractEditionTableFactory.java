/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.io.File;
import java.util.concurrent.Callable;

import com.metservice.cobalt.CobaltDimensionException;

/**
 * @author roach
 */
public abstract class KryptonAbstractEditionTableFactory<T extends IKryptonEditionTable<T>> implements
		Callable<KryptonEditionResult<T>> {

	protected abstract T newEmptyTable();

	protected abstract void putTable(IKryptonFileProbe probe, T editionTable, KryptonDataRecord dataRecord)
			throws CobaltDimensionException, KryptonUnpackException, KryptonUnsupportedException;

	@Override
	public KryptonEditionResult<T> call()
			throws Exception {
		return newFileEditionTable();
	}

	public final KryptonEditionResult<T> newFileEditionTable() {
		final T table = newEmptyTable();
		final KryptonManifestCounter counter = new KryptonManifestCounter();
		final KryptonFileReader oFileReader = KryptonFileReader.createInstance(m_decoder, m_gridFile);
		if (oFileReader == null) {
			m_probe.gridFileRead(m_gridFile, "No found or empty");
			counter.skipFile();
		}
		final long tsStart = System.currentTimeMillis();
		try {
			int skippedRecordsinFile = 0;
			while (oFileReader.hasNextRecordReader()) {
				try {
					final KryptonRecordReader rr = oFileReader.nextRecordReader();
					final int recordIndex = rr.recordIndex();
					final int subRecordIndex = rr.subRecordIndex();
					try {
						final KryptonDataRecord dataRecord = rr.newDataRecord();
						putTable(m_probe, table, dataRecord);
						counter.goodRecord();
					} catch (final CobaltDimensionException ex) {
						m_probe.gridFileDimension(m_gridFile, recordIndex, subRecordIndex, ex);
						skippedRecordsinFile++;
						counter.skipRecord();
					} catch (final KryptonCodeException ex) {
						m_probe.gridFileCode(m_gridFile, recordIndex, subRecordIndex, ex);
						skippedRecordsinFile++;
						counter.skipRecord();
					} catch (final KryptonTableException ex) {
						m_probe.gridFileTable(m_gridFile, recordIndex, subRecordIndex, ex);
						skippedRecordsinFile++;
						counter.skipRecord();
					} catch (final KryptonUnpackException ex) {
						m_probe.gridFileUnpack(m_gridFile, recordIndex, subRecordIndex, ex);
						skippedRecordsinFile++;
						counter.skipRecord();
					} catch (final KryptonUnsupportedException ex) {
						m_probe.gridFileUnsupported(m_gridFile, recordIndex, subRecordIndex, ex);
						skippedRecordsinFile++;
						counter.skipRecord();
					}
				} catch (final KryptonRecordException ex) {
					m_probe.gridFileRecord(m_gridFile, ex);
					skippedRecordsinFile++;
					counter.skipRecord();
				}
			}
			if (skippedRecordsinFile == 0) {
				counter.goodFile();
			} else {
				counter.partialFile();
			}
		} catch (final KryptonReadException ex) {
			m_probe.gridFileRead(m_gridFile, ex);
			counter.skipFile();
		} finally {
			oFileReader.close();
		}
		counter.fileSumMs(System.currentTimeMillis() - tsStart);
		return new KryptonEditionResult<T>(table, counter);
	}

	protected KryptonAbstractEditionTableFactory(IKryptonFileProbe probe, KryptonDecoder decoder, File gridFile) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (decoder == null) throw new IllegalArgumentException("object is null");
		if (gridFile == null) throw new IllegalArgumentException("object is null");
		m_probe = probe;
		m_decoder = decoder;
		m_gridFile = gridFile;
	}
	private final IKryptonFileProbe m_probe;
	private final KryptonDecoder m_decoder;
	private final File m_gridFile;
}
