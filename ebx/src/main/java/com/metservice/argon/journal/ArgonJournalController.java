/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.journal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.ArgonZip;
import com.metservice.argon.ArgonZipItem;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.file.ArgonDirectoryManagement;
import com.metservice.argon.file.ArgonFileManagement;
import com.metservice.argon.management.IArgonSpaceId;

/**
 * 
 * @author roach
 */
public class ArgonJournalController {

	private static final String TrySave = "Save entry file to journal";
	private static final String TryReadJE = "Read journal entry file";
	private static final String TryReadJZ = "Read journal ZIP file";
	private static final String CsqPartRecovery = "Partial state recovery on restart";
	private static final String CsqSkipJE = "Skip journal entry file";
	private static final String CsqSkipJZ = "Skip journal entries in ZIP file";
	public static final String ThreadPrefix = "argon-journal-";
	public static final String SubDirJournal = "journal";
	public static final String ArchiveType = "zip";
	public static final String SuffixWip = ".wip";
	private static final int SuffixWipL = SuffixWip.length();

	static final int BcMinZip = 1024;

	static final ArgonJournalIn[] ZJE = new ArgonJournalIn[0];

	private static long nextSerial(IArgonJournalProbe probe, File cndir, boolean recoveryEnabled) {
		assert probe != null;
		assert cndir != null;
		final String[] ozpt = cndir.list();
		long maxSerial = 0L;
		if (ozpt != null) {
			for (int i = 0; i < ozpt.length; i++) {
				final String qccFileName = ozpt[i];
				if (recoveryEnabled) {
					final EntryName oEntryName = createEntryName(qccFileName);
					if (oEntryName == null) {
						final File fmal = new File(cndir, qccFileName);
						final Ds ds = Ds.invalidBecause("Malformed event file name; expecting integral",
								"Assuming redundant, so will try to delete");
						probe.warnFile(ds, fmal);
						ArgonFileManagement.deleteFile(probe, fmal);
					} else {
						maxSerial = Math.max(maxSerial, oEntryName.serial);
					}
				} else {
					final File fscrub = new File(cndir, qccFileName);
					ArgonFileManagement.deleteFile(probe, fscrub);
				}
			}
		}
		return maxSerial + 1L;
	}

	static EntryName createEntryName(String qccFileName) {
		final int lenFileName = qccFileName.length();
		final boolean wip;
		final String qccCommitFileName;
		if (qccFileName.endsWith(SuffixWip)) {
			final int lenCommit = lenFileName - SuffixWipL;
			if (lenCommit == 0) return null;
			wip = true;
			qccCommitFileName = qccFileName.substring(0, lenCommit);
		} else {
			qccCommitFileName = qccFileName;
			wip = false;
		}

		final int dotPos = qccCommitFileName.indexOf('.');
		if (dotPos <= 0) return null;
		final String zccSerial = qccCommitFileName.substring(0, dotPos);
		final String zccType = qccCommitFileName.substring(dotPos + 1);
		if (zccSerial.length() == 0 || zccType.length() == 0) return null;
		try {
			final long serial = Long.parseLong(zccSerial);
			return new EntryName(serial, zccType, wip);
		} catch (final NumberFormatException ex) {
		}
		return null;
	}

	static boolean deleteFile(IArgonJournalProbe probe, File ex) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (ex == null) throw new IllegalArgumentException("object is null");
		if (ex.delete()) return true;
		final Ds ds = Ds.invalidBecause("Could not delete file", "Will remain in journal");
		probe.warnFile(ds, ex);
		return false;
	}

	public static Config newConfig(IArgonJournalProbe probe, ArgonServiceId sid, IArgonSpaceId idSpace,
			IArgonJournalListener oLsn, IArgonJournalMirror oMirror)
			throws ArgonPermissionException {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (sid == null) throw new IllegalArgumentException("object is null");
		if (idSpace == null) throw new IllegalArgumentException("object is null");
		final File cndir = ArgonDirectoryManagement.cndirEnsureUserWriteable(sid.qtwVendor, sid.qtwService, SubDirJournal,
				idSpace.format());
		final String qccThreadName = ThreadPrefix + idSpace.format();
		return new Config(probe, oLsn, oMirror, cndir, qccThreadName);
	}

	public static ArgonJournalController newInstance(Config cfg)
			throws ArgonPermissionException {
		if (cfg == null) throw new IllegalArgumentException("object is null");
		final long nextSerial = nextSerial(cfg.probe, cfg.cndir, cfg.recoveryEnabled);
		final RollTask task = new RollTask(cfg);
		final Timer timer = new Timer(cfg.qccThreadName, true);
		timer.schedule(task, cfg.msTimerDelay, cfg.msTimerPeriod);
		return new ArgonJournalController(cfg, nextSerial, timer);
	}

	public void autoCommit(ArgonJournalOut journalOut) {
		newTransaction(journalOut, true);
	}

	public void cancel() {
		m_timer.cancel();
	}

	public boolean commit(ArgonJournalTx tx) {
		if (tx == null) throw new IllegalArgumentException("object is null");
		if (!m_persistEnabled) return true;
		if (tx.isAutoCommit()) return true;
		final File wipFile = tx.wipFile();
		final File commitFile = tx.commitFile();
		final boolean renamed = ArgonFileManagement.renameFile(m_probe, wipFile, commitFile);
		if (m_oMirror != null) {
			m_oMirror.commit(tx);
		}
		return renamed;
	}

	public ArgonJournalTx createTransaction(ArgonJournalIn journalIn) {
		if (journalIn == null) throw new IllegalArgumentException("object is null");
		if (!journalIn.isWorkInProgress()) return null;
		return ArgonJournalTx.newInstance(m_cndir, journalIn.serial(), journalIn.qccType(), false);
	}

	public ArgonJournalTx newTransaction(ArgonJournalOut journalOut) {
		return newTransaction(journalOut, false);
	}

	public ArgonJournalTx newTransaction(ArgonJournalOut journalOut, boolean autoCommit) {
		if (journalOut == null) throw new IllegalArgumentException("object is null");
		final Binary source = journalOut.source();
		final String qccType = journalOut.qccType();
		m_lock.lock();
		try {
			final long nextSerial = m_nextSerial.getAndIncrement();
			final ArgonJournalTx tx = ArgonJournalTx.newInstance(m_cndir, nextSerial, qccType, autoCommit);
			final File destFile = tx.wipFile();
			try {
				if (m_persistEnabled) {
					source.save(destFile, false);
				}
				if (m_oListener != null) {
					m_oListener.saved(qccType, nextSerial, destFile);
				}
				if (m_oMirror != null) {
					m_oMirror.saved(tx, source);
				}
				return tx;
			} catch (final ArgonPermissionException ex) {
				final Ds ds = Ds.triedTo(TrySave, ex, CsqPartRecovery);
				m_probe.failFile(ds, destFile);
			} catch (final ArgonStreamWriteException ex) {
				final Ds ds = Ds.triedTo(TrySave, ex, CsqPartRecovery);
				m_probe.failFile(ds, destFile);
			}
			return null;
		} finally {
			m_lock.unlock();
		}
	}

	public Iterator<ArgonJournalIn> recoveryIterator() {
		return new Iter(m_probe, m_cndir, m_bcMaxDeflated, m_bcMaxInflated);
	}

	private ArgonJournalController(Config config, long nextSerial, Timer timer) {
		assert config != null;
		assert timer != null;
		m_probe = config.probe;
		m_cndir = config.cndir;
		m_oListener = config.oListener;
		m_oMirror = config.oMirror;
		if (config.bcMaxDeflated == Config.BcDefault) {
			m_bcMaxDeflated = Math.max(BcMinZip, config.bcNominalArchiveInflated * 2);
		} else {
			m_bcMaxDeflated = config.bcMaxDeflated;
		}
		if (config.bcMaxInflated == Config.BcDefault) {
			m_bcMaxInflated = config.bcNominalArchiveInflated * 4;
		} else {
			m_bcMaxInflated = config.bcMaxInflated;
		}
		m_persistEnabled = config.persistEnabled;
		m_nextSerial = new AtomicLong(nextSerial);
		m_timer = timer;
	}
	private final IArgonJournalProbe m_probe;
	private final File m_cndir;
	private final IArgonJournalListener m_oListener;
	private final IArgonJournalMirror m_oMirror;
	private final Lock m_lock = new ReentrantLock();
	private final int m_bcMaxDeflated;
	private final int m_bcMaxInflated;
	private final boolean m_persistEnabled;
	private final AtomicLong m_nextSerial;
	private final Timer m_timer;

	private static class EntryName implements Comparable<EntryName> {

		@Override
		public int compareTo(EntryName rhs) {
			if (serial < rhs.serial) return -1;
			if (serial > rhs.serial) return +1;
			return 0;
		}

		@Override
		public String toString() {
			return serial + " (" + qccType + ")" + (wip ? " wip" : "");
		}

		public EntryName(long serial, String qccType, boolean wip) {
			assert qccType != null && qccType.length() > 0;
			this.serial = serial;
			this.qccType = qccType;
			this.wip = wip;
		}

		final long serial;
		final String qccType;
		final boolean wip;
	}

	private static class EntryRef implements Comparable<EntryRef> {

		public static EntryRef createInstance(File cndir, String qccFileName) {
			final EntryName oEName = createEntryName(qccFileName);
			if (oEName == null) return null;
			final File file = new File(cndir, qccFileName);
			return new EntryRef(file, oEName);
		}

		@Override
		public int compareTo(EntryRef rhs) {
			final long ls = entryName.serial;
			final long rs = rhs.entryName.serial;
			if (ls < rs) return -1;
			if (ls > rs) return +1;
			return 0;
		}

		@Override
		public String toString() {
			return file.getPath();
		}

		private EntryRef(File file, EntryName ename) {
			assert file != null;
			assert ename != null;
			this.file = file;
			this.entryName = ename;
		}

		final File file;
		final EntryName entryName;
	}

	private static class Iter implements Iterator<ArgonJournalIn> {

		private ArgonJournalIn oJournalEntry(EntryRef eref) {
			assert eref != null;
			try {
				final Binary oSource = Binary.createFromFile(eref.file, m_bcMaxInflated);
				if (oSource != null) {
					final long ts = eref.file.lastModified();
					final EntryName ename = eref.entryName;
					return new ArgonJournalIn(ename.serial, ename.qccType, ename.wip, oSource, ts);
				}
				final Ds ds = Ds.invalidBecause("Could not find journal entry file", CsqSkipJE);
				m_probe.failFile(ds, eref.file);
			} catch (final ArgonQuotaException ex) {
				m_probe.failFile(Ds.triedTo(TryReadJE, ex, CsqSkipJE), eref.file);
			} catch (final ArgonStreamReadException ex) {
				m_probe.failFile(Ds.triedTo(TryReadJE, ex, CsqSkipJE), eref.file);
			}
			return null;
		}

		private ArgonJournalIn[] ozptJournalEntry(EntryRef eref) {
			try {
				final Binary oZipSource = Binary.createFromFile(eref.file, m_bcMaxArchive);
				if (oZipSource != null) {
					final List<ArgonZipItem> zlZipItems = oZipSource.newZipDecodedAscName(m_bcMaxInflated);
					final int itemCount = zlZipItems.size();
					final List<ArgonJournalIn> zlJournalEntry = new ArrayList<ArgonJournalIn>(itemCount);
					for (int i = 0; i < itemCount; i++) {
						final ArgonZipItem item = zlZipItems.get(i);
						final EntryName oen = createEntryName(item.qccFileName);
						if (oen == null) {
							final Ds ds = Ds.invalidBecause("Malformed entry name in ZIP file", CsqSkipJE);
							ds.a("entry", item.qccFileName);
							m_probe.warnFile(ds, eref.file);
							continue;
						}
						final long tslm = item.lastModifiedAt.getTime();
						zlJournalEntry.add(new ArgonJournalIn(oen.serial, oen.qccType, oen.wip, item.content, tslm));
					}
					Collections.sort(zlJournalEntry);
					return zlJournalEntry.toArray(new ArgonJournalIn[zlJournalEntry.size()]);
				}
				final Ds ds = Ds.invalidBecause("Could not find journal ZIP file", CsqSkipJZ);
				m_probe.failFile(ds, eref.file);
			} catch (final ArgonQuotaException ex) {
				m_probe.failFile(Ds.triedTo(TryReadJZ, ex, CsqSkipJZ), eref.file);
			} catch (final ArgonStreamReadException ex) {
				m_probe.failFile(Ds.triedTo(TryReadJZ, ex, CsqSkipJZ), eref.file);
			}
			return null;
		}

		private boolean refilled() {
			boolean refilled = false;
			while (!refilled && m_erIndex < m_erCount) {
				final EntryRef eref = m_zlEntryRefAsc.get(m_erIndex);
				if (eref.entryName.qccType.equals(ArchiveType)) {
					final ArgonJournalIn[] ozptNeo = ozptJournalEntry(eref);
					if (ozptNeo != null && ozptNeo.length > 0) {
						m_zptPending = ozptNeo;
						refilled = true;
					}
				} else {
					final ArgonJournalIn oNeo = oJournalEntry(eref);
					if (oNeo != null) {
						m_zptPending = new ArgonJournalIn[1];
						m_zptPending[0] = oNeo;
						refilled = true;
					}
				}
				m_erIndex++;
			}
			return refilled;
		}

		@Override
		public boolean hasNext() {
			return m_jeIndex < m_zptPending.length;
		}

		@Override
		public ArgonJournalIn next() {
			if (m_jeIndex >= m_zptPending.length) throw new NoSuchElementException("hasNext() is false");
			final ArgonJournalIn next = m_zptPending[m_jeIndex];
			m_jeIndex++;
			if (m_jeIndex == m_zptPending.length) {
				if (refilled()) {
					m_jeIndex = 0;
				}
			}
			return next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Iterator does not support remove");
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			return sb.toString();
		}

		Iter(IArgonJournalProbe probe, File cndir, int bcMaxArchive, int bcMaxInflated) {
			assert probe != null;
			assert cndir != null;
			m_probe = probe;
			m_bcMaxArchive = bcMaxArchive;
			m_bcMaxInflated = bcMaxInflated;
			final String[] ozpt = cndir.list();
			final int fcount = ozpt == null ? 0 : ozpt.length;
			final List<EntryRef> zlEntryRefAsc = new ArrayList<EntryRef>(fcount);
			if (ozpt != null) {
				for (int i = 0; i < fcount; i++) {
					final String qccFileName = ozpt[i];
					final EntryRef oERef = EntryRef.createInstance(cndir, qccFileName);
					if (oERef != null) {
						zlEntryRefAsc.add(oERef);
					}
				}
			}
			Collections.sort(zlEntryRefAsc);
			m_zlEntryRefAsc = zlEntryRefAsc;
			m_erCount = zlEntryRefAsc.size();
			m_erIndex = 0;
			refilled();
			m_jeIndex = 0;
		}

		private final IArgonJournalProbe m_probe;
		private final int m_bcMaxArchive;
		private final int m_bcMaxInflated;
		private final List<EntryRef> m_zlEntryRefAsc;
		private final int m_erCount;
		private int m_erIndex;
		private ArgonJournalIn[] m_zptPending = ZJE;
		private int m_jeIndex;
	}

	private static class RollTask extends TimerTask {

		private static final String TryRoll = "Roll journal entry files";
		private static final String CsqRetry = "Abandon this attempt then retry at scheduled frequency";
		private static final String TryCompress = "Compress journal entry files";
		private static final String CsqRetain = "Retain uncompressed files and scrub incomplete archive";

		private long bcEstimate(File f) {
			final long bcFile = f.length();
			if (m_bcBlockEstimator <= 1) return bcFile;
			final long d = bcFile / m_bcBlockEstimator;
			final long r = bcFile % m_bcBlockEstimator;
			final long blockCount = r == 0 ? d : (d + 1);
			return blockCount * m_bcBlockEstimator;
		}

		private void compress(List<File> xlCompress, long serialLast) {
			final String qccArchiveFileName = ArgonJournalTx.newArchiveFileName(serialLast);
			final File destFile = new File(m_cndir, qccArchiveFileName);
			final int count = xlCompress.size();
			final File[] xptCompress = xlCompress.toArray(new File[count]);
			boolean cleanup = false;
			try {
				ArgonZip.encodeToFile(xptCompress, destFile, m_cndir, null, true, File.separatorChar);
				discard(xptCompress);
			} catch (final ArgonApiException ex) {
				m_probe.warnFile(Ds.triedTo(TryCompress, ex, CsqRetain), destFile);
				cleanup = true;
			} catch (final ArgonPermissionException ex) {
				m_probe.warnFile(Ds.triedTo(TryCompress, ex, CsqRetain), destFile);
				cleanup = true;
			} catch (final ArgonStreamWriteException ex) {
				m_probe.warnFile(Ds.triedTo(TryCompress, ex, CsqRetain), destFile);
				cleanup = true;
			} catch (final ArgonStreamReadException ex) {
				m_probe.warnFile(Ds.triedTo(TryCompress, ex, CsqRetain), destFile);
				cleanup = true;
			} catch (final RuntimeException ex) {
				final Ds ds = Ds.triedTo(TryCompress, ex, CsqRetain);
				ds.a("count", count);
				ds.a("destFile", destFile);
				m_probe.failSoftware(ds);
				cleanup = true;
			} finally {
				if (cleanup) {
					deleteFile(m_probe, destFile);
				}
			}
		}

		private void discard(File[] xptCompress) {
			for (int i = 0; i < xptCompress.length; i++) {
				deleteFile(m_probe, xptCompress[i]);
			}
		}

		@Override
		public void run() {
			try {
				final long tsNow = System.currentTimeMillis();
				final String[] ozpt = m_cndir.list();
				if (ozpt == null || ozpt.length == 0) return;
				final List<EntryRef> zlERefs = new ArrayList<EntryRef>(ozpt.length);
				for (int i = 0; i < ozpt.length; i++) {
					final String qccFileName = ozpt[i];
					final EntryRef oERef = EntryRef.createInstance(m_cndir, qccFileName);
					if (oERef != null) {
						zlERefs.add(oERef);
					}
				}
				Collections.sort(zlERefs);
				final int ercount = zlERefs.size();
				long bcCompress = 0L;
				final List<File> zlCompress = new ArrayList<File>(64);
				boolean compressable = true;
				for (int i = 0; i < ercount && compressable; i++) {
					final EntryRef er = zlERefs.get(i);
					final EntryName ename = er.entryName;
					if (ename.wip) {
						compressable = false;
						continue;
					}
					final long tsLastModified = er.file.lastModified();
					final long msAge = tsNow - tsLastModified;
					if (msAge < m_msCompressLag) {
						compressable = false;
						continue;
					}
					if (msAge > m_msRecoveryWindow) {
						deleteFile(m_probe, er.file);
						continue;
					}
					if (ename.qccType.equals(ArchiveType)) {
						continue;
					}
					zlCompress.add(er.file);
					final long bcFile = bcEstimate(er.file);
					bcCompress += bcFile;
					if (bcCompress >= m_bcNominalArchiveInflated) {
						compress(zlCompress, ename.serial);
						bcCompress = 0L;
						zlCompress.clear();
					}
				}
			} catch (final RuntimeException ex) {
				final Ds ds = Ds.triedTo(TryRoll, ex, CsqRetry);
				ds.a("msRecoveryWindow", m_msRecoveryWindow);
				ds.a("bcNominalArchiveInflated", m_bcNominalArchiveInflated);
				m_probe.failSoftware(ds);
			}
		}

		RollTask(Config cfg) {
			assert cfg != null;
			m_probe = cfg.probe;
			m_cndir = cfg.cndir;
			m_msRecoveryWindow = cfg.msRecoveryWindow;
			m_msCompressLag = cfg.msTimerDelay;
			m_bcNominalArchiveInflated = cfg.bcNominalArchiveInflated;
			m_bcBlockEstimator = cfg.bcBlockEstimator;
		}

		private final IArgonJournalProbe m_probe;
		private final File m_cndir;
		private final long m_msRecoveryWindow;
		private final long m_msCompressLag;
		private final int m_bcNominalArchiveInflated;
		private final int m_bcBlockEstimator;
	}

	public static class Config {

		private static final int BcDefault = -1;

		public Config blockEstimatorSize(int bc) {
			bcBlockEstimator = Math.max(1, bc);
			return this;
		}

		public Config compressionHoldoff(TimeUnit unit, int count) {
			if (unit == null) throw new IllegalArgumentException("object is null");
			msTimerDelay = unit.toMillis(count);
			return this;
		}

		public Config compressionPeriod(TimeUnit unit, int count) {
			if (unit == null) throw new IllegalArgumentException("object is null");
			msTimerPeriod = unit.toMillis(count);
			return this;
		}

		public Config maxDeflatedSize(int bc) {
			bcMaxDeflated = Math.max(16, bc);
			return this;
		}

		public Config maxInflatedSize(int bc) {
			bcMaxInflated = Math.max(16, bc);
			return this;
		}

		public Config nominalArchiveInflatedSize(int bc) {
			bcNominalArchiveInflated = Math.max(16, bc);
			return this;
		}

		public Config persistEnabled(boolean enabled) {
			persistEnabled = enabled;
			return this;
		}

		public Config recoveryEnabled(boolean enabled) {
			recoveryEnabled = enabled;
			return this;
		}

		public Config recoveryWindow(TimeUnit unit, long count) {
			if (unit == null) throw new IllegalArgumentException("object is null");
			msRecoveryWindow = unit.toMillis(count);
			return this;
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("ArgonJournalController.Config");
			ds.a("cndir", cndir);
			ds.a("msRecoveryWindow", msRecoveryWindow);
			ds.a("bcNominalArchiveInflated", bcNominalArchiveInflated);
			ds.a("msTimerDelay", msTimerDelay);
			ds.a("msTimerPeriod", msTimerPeriod);
			ds.a("persistEnabled", persistEnabled);
			ds.a("recoveryEnabled", recoveryEnabled);
			return ds.s();
		}

		Config(IArgonJournalProbe probe, IArgonJournalListener oLsn, IArgonJournalMirror oMirror, File cndir,
				String qccThreadName) {
			assert probe != null;
			assert cndir != null;
			this.probe = probe;
			this.oListener = oLsn;
			this.oMirror = oMirror;
			this.cndir = cndir;
			this.qccThreadName = qccThreadName;
		}

		final IArgonJournalProbe probe;
		final IArgonJournalListener oListener;
		final IArgonJournalMirror oMirror;
		final File cndir;
		final String qccThreadName;
		long msRecoveryWindow = 36 * CArgon.HR_TO_MS;
		int bcNominalArchiveInflated = 1 * CArgon.M;
		long msTimerDelay = 15 * CArgon.MIN_TO_MS;
		long msTimerPeriod = 1 * CArgon.HR_TO_MS;
		boolean persistEnabled = true;
		boolean recoveryEnabled = true;
		int bcMaxInflated = BcDefault;
		int bcMaxDeflated = BcDefault;
		int bcBlockEstimator = 4 * CArgon.K;
	}
}
