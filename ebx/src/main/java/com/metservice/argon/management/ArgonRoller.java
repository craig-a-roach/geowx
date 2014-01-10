/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.DateFormatter;
import com.metservice.argon.IArgonSpaceId;

/**
 * @author roach
 */
public class ArgonRoller {

	private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
	private static final String LogSubDir = "logs";
	private static final String LogFileSuffix = ".log";

	private static final ArgonRoller DefaultInstance = new ArgonRoller("argon-roller", TimeUnit.DAYS, 31);

	public PrintStream printStreamInstance(ArgonServiceId sid, ArgonRecordType recType, IArgonSpaceId idSpace)
			throws ArgonPermissionException {
		if (sid == null) throw new IllegalArgumentException("object is null");
		if (recType == null) throw new IllegalArgumentException("object is null");
		if (idSpace == null) throw new IllegalArgumentException("object is null");
		return m_task.declareRolling(sid, recType, idSpace).printStream;
	}

	public void shutdownInstance(String qReason) {
		if (qReason == null || qReason.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_timer.cancel();
		m_task.shutdown(qReason);
	}

	static Date advance(Date pre, TimeUnit unit) {
		assert pre != null;
		assert unit != null;
		final Calendar cal = Calendar.getInstance(GMT);
		cal.setTime(pre);
		switch (unit) {
			case MINUTES:
				cal.add(Calendar.MINUTE, 1);
			break;
			case HOURS:
				cal.add(Calendar.HOUR_OF_DAY, 1);
			break;
			default:
				cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		return cal.getTime();
	}

	static File cndirServiceHome(ArgonServiceId sid, ArgonRecordType recType)
			throws ArgonPermissionException {
		assert sid != null;
		assert recType != null;
		final File vendor = new File(UArgonManagement.qUserHome(), sid.qtwVendor);
		final File service = new File(vendor, sid.qtwService);
		final File log = new File(service, LogSubDir);
		final File home = new File(log, recType.format());
		try {
			final File cndirHome = home.getCanonicalFile();
			cndirHome.mkdirs();
			if (!cndirHome.canWrite()) {
				final String u = " ...Process user is " + UArgonManagement.qUserName();
				final String msg = "Cannot create directory '" + cndirHome + u;
				throw new ArgonPermissionException(msg);
			}
			return cndirHome;
		} catch (final IOException ex) {
			final String u = " ...Process user is " + UArgonManagement.qUserName();
			final String c = " (" + ex.getMessage() + ")";
			final String m = "Cannot resolve path to home directory '" + home + "' " + c + u;
			throw new ArgonPermissionException(m);
		}
	}

	static Date firstFire(Date dateNow, TimeUnit unit) {
		assert dateNow != null;
		assert unit != null;
		final Calendar cal = Calendar.getInstance(GMT);
		cal.setTime(dateNow);
		cal.set(Calendar.MILLISECOND, 999);
		cal.set(Calendar.SECOND, 59);
		if (unit == TimeUnit.HOURS || unit == TimeUnit.DAYS) {
			cal.set(Calendar.MINUTE, 59);
		}
		if (unit == TimeUnit.DAYS) {
			cal.set(Calendar.HOUR_OF_DAY, 23);
		}
		return cal.getTime();
	}

	static String qccDirKey(ArgonServiceId sid, ArgonRecordType recType) {
		return sid + "/" + recType.format();
	}

	static String qccSeriesKey(ArgonServiceId sid, ArgonRecordType recType, String qtwSpaceId) {
		return qccDirKey(sid, recType) + ":" + qtwSpaceId;
	}

	static String qSuffix(Date ref, TimeUnit unit) {
		assert ref != null;
		assert unit != null;
		final Calendar cal = Calendar.getInstance(GMT);
		cal.setTime(ref);
		final int year = cal.get(Calendar.YEAR);
		final int moy = cal.get(Calendar.MONTH) + 1;
		final int dom = cal.get(Calendar.DAY_OF_MONTH);
		final StringBuilder sb = new StringBuilder();
		UArgonManagement.zeroLeft(sb, Integer.toString(year), 4);
		sb.append('-');
		UArgonManagement.zeroLeft(sb, Integer.toString(moy), 2);
		sb.append('-');
		UArgonManagement.zeroLeft(sb, Integer.toString(dom), 2);
		if (unit == TimeUnit.HOURS || unit == TimeUnit.MINUTES) {
			final int h24 = cal.get(Calendar.HOUR_OF_DAY);
			sb.append('-');
			UArgonManagement.zeroLeft(sb, Integer.toString(h24), 2);
		}
		if (unit == TimeUnit.MINUTES) {
			final int moh = cal.get(Calendar.MINUTE);
			UArgonManagement.zeroLeft(sb, Integer.toString(moh), 2);
		}

		return sb.toString();
	}

	public static PrintStream printStream(ArgonServiceId sid, ArgonRecordType recType, IArgonSpaceId idSpace)
			throws ArgonPermissionException {
		return DefaultInstance.printStreamInstance(sid, recType, idSpace);
	}

	public static void shutdown(String qReason) {
		DefaultInstance.shutdownInstance(qReason);
	}

	public ArgonRoller(String qTimerThreadName, TimeUnit unit, int retention) {
		if (qTimerThreadName == null || qTimerThreadName.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (unit == null) throw new IllegalArgumentException("object is null");
		if (unit != TimeUnit.DAYS && unit != TimeUnit.HOURS && unit != TimeUnit.MINUTES)
			throw new IllegalArgumentException("unsupported unit>" + unit + "<");
		m_timer = new Timer(qTimerThreadName, true);
		final long msTimerPeriod = unit.toMillis(1);
		final long msPurgeAfter = msTimerPeriod * retention;
		final Date dateNow = new Date(System.currentTimeMillis());
		final Date firstFire = firstFire(dateNow, unit);
		m_task = new RollTask(dateNow, unit, msPurgeAfter);
		m_timer.scheduleAtFixedRate(m_task, firstFire, msTimerPeriod);
		final Thread hook = new Thread(new Shutdown(this));
		Runtime.getRuntime().addShutdownHook(hook);
	}

	private final RollTask m_task;
	private final Timer m_timer;

	private static class RollingOutputStream extends OutputStream {

		private void closeStreamLocked() {
			if (m_oStream != null) {
				try {
					m_oStream.close();
				} catch (final IOException ex) {
				} finally {
					m_oStream = null;
				}
			}
		}

		private FileOutputStream getFileOutputStream() {
			m_lock.lock();
			try {
				openStreamLocked();
				return m_oStream;
			} finally {
				m_lock.unlock();
			}
		}

		private File newDestFile(String qSuffix) {
			final StringBuilder bfile = new StringBuilder();
			bfile.append(qccBaseName);
			bfile.append('.');
			bfile.append(qSuffix);
			bfile.append(LogFileSuffix);
			return new File(cndirHome, bfile.toString());
		}

		private void openStreamLocked() {
			if (m_oStream == null) {
				try {
					m_oStream = new FileOutputStream(m_destFile, true);
				} catch (final IOException ex) {
				}
			}
		}

		@Override
		public void close()
				throws IOException {
			m_lock.lock();
			try {
				closeStreamLocked();
			} finally {
				m_lock.unlock();
			}
		}

		public void complete() {
			m_lock.lock();
			try {
				closeStreamLocked();
			} finally {
				m_lock.unlock();
			}
		}

		@Override
		public void flush()
				throws IOException {
			final FileOutputStream ofos = getFileOutputStream();
			if (ofos != null) {
				ofos.flush();
			}
		}

		public void roll(String qSuffix) {
			assert qSuffix != null && qSuffix.length() > 0;
			m_lock.lock();
			try {
				closeStreamLocked();
				m_destFile = newDestFile(qSuffix);
			} finally {
				m_lock.unlock();
			}
		}

		@Override
		public void write(byte[] b)
				throws IOException {
			final FileOutputStream ofos = getFileOutputStream();
			if (ofos != null) {
				ofos.write(b);
			}
		}

		@Override
		public void write(byte[] b, int off, int len)
				throws IOException {
			final FileOutputStream ofos = getFileOutputStream();
			if (ofos != null) {
				ofos.write(b, off, len);
			}
		}

		@Override
		public void write(int b)
				throws IOException {
			final FileOutputStream ofos = getFileOutputStream();
			if (ofos != null) {
				ofos.write(b);
			}
		}

		RollingOutputStream(File cndirHome, String qccBaseName, String qSuffix) {
			assert cndirHome != null;
			assert qccBaseName != null && qccBaseName.length() > 0;
			assert qSuffix != null;
			this.printStream = new PrintStream(this, true);
			this.cndirHome = cndirHome;
			this.qccBaseName = qccBaseName;
			m_destFile = newDestFile(qSuffix);
		}

		final PrintStream printStream;
		final File cndirHome;
		final String qccBaseName;
		final Lock m_lock = new ReentrantLock();
		File m_destFile;
		FileOutputStream m_oStream;
	}

	private static class RollTask extends TimerTask {

		private File declareHomeLocked(ArgonServiceId sid, ArgonRecordType recType)
				throws ArgonPermissionException {
			final String qccDirKey = qccDirKey(sid, recType);
			File vcndirHome = m_mapHome.get(qccDirKey);
			if (vcndirHome == null) {
				vcndirHome = cndirServiceHome(sid, recType);
				m_mapHome.put(qccDirKey, vcndirHome);
			}
			return vcndirHome;
		}

		private String newShutdownMessage(String qReason) {
			final long tsNow = System.currentTimeMillis();
			final StringBuilder bmsg = new StringBuilder();
			bmsg.append("SHUTDOWN ");
			bmsg.append(DateFormatter.newT8PlatformDHMFromTs(tsNow));
			bmsg.append(" ");
			bmsg.append(qReason);
			final int len = bmsg.length();
			for (int i = len; i < 100; i++) {
				bmsg.append('-');
			}
			return bmsg.toString();
		}

		private void purge(long tsPurgeRef, File cndirHome) {
			assert cndirHome != null;
			final long tsPurge = tsPurgeRef - m_msPurgeAfter;
			final File[] ozptFiles = cndirHome.listFiles();
			if (ozptFiles == null || ozptFiles.length == 0) return;

			for (int i = 0; i < ozptFiles.length; i++) {
				final File f = ozptFiles[i];
				if (f.isFile()) {
					final String fname = f.getName();
					if (fname.endsWith(LogFileSuffix)) {
						if (f.lastModified() <= tsPurge) {
							f.delete();
						}
					}
				}
			}
		}

		public RollingOutputStream declareRolling(ArgonServiceId sid, ArgonRecordType recType, IArgonSpaceId idSpace)
				throws ArgonPermissionException {
			assert sid != null;
			assert recType != null;
			assert idSpace != null;
			final String qtwSpaceId = idSpace.format();
			if (qtwSpaceId == null || qtwSpaceId.length() == 0) {
				final String m = "Empty id for " + idSpace.getClass();
				throw new IllegalStateException(m);
			}
			final String qccSeriesKey = qccSeriesKey(sid, recType, qtwSpaceId);
			m_lock.lock();
			try {
				final File cndirHome = declareHomeLocked(sid, recType);
				RollingOutputStream vRolling = m_mapRolling.get(qccSeriesKey);
				if (vRolling == null) {
					vRolling = new RollingOutputStream(cndirHome, qtwSpaceId, m_qSuffix);
					m_mapRolling.put(qccSeriesKey, vRolling);
				}
				return vRolling;
			} finally {
				m_lock.unlock();
			}
		}

		@Override
		public void run() {
			final Set<File> zsHome;
			final long tsPurgeRef;
			m_lock.lock();
			try {
				m_ref = advance(m_ref, m_unit);
				m_qSuffix = qSuffix(m_ref, m_unit);
				final List<String> zlKeysAsc = new ArrayList<String>(m_mapRolling.keySet());
				Collections.sort(zlKeysAsc);
				for (final String qccKey : zlKeysAsc) {
					final RollingOutputStream oros = m_mapRolling.get(qccKey);
					if (oros == null) {
						continue;
					}
					oros.roll(m_qSuffix);
				}
				zsHome = new HashSet<File>(m_mapHome.values());
				tsPurgeRef = m_ref.getTime();
			} finally {
				m_lock.unlock();
			}
			final List<File> zlHomeAsc = new ArrayList<File>(zsHome);
			Collections.sort(zlHomeAsc);
			for (final File cndirHome : zlHomeAsc) {
				purge(tsPurgeRef, cndirHome);
			}
		}

		public void shutdown(String qReason) {
			final String msg = newShutdownMessage(qReason);
			m_lock.lock();
			try {
				final List<String> zlKeysAsc = new ArrayList<String>(m_mapRolling.keySet());
				Collections.sort(zlKeysAsc);
				for (final String qccKey : zlKeysAsc) {
					final RollingOutputStream oros = m_mapRolling.get(qccKey);
					if (oros == null) {
						continue;
					}
					oros.printStream.println(msg);
					oros.complete();
				}
				m_mapRolling.clear();
			} finally {
				m_lock.unlock();
			}
		}

		public RollTask(Date dateNow, TimeUnit unit, long msPurgeAfter) {
			assert dateNow != null;
			assert unit != null;
			m_msPurgeAfter = msPurgeAfter;
			m_unit = unit;
			m_ref = dateNow;
			m_qSuffix = qSuffix(dateNow, unit);
		}

		final long m_msPurgeAfter;
		final TimeUnit m_unit;
		final Map<String, File> m_mapHome = new HashMap<String, File>(16);
		final Map<String, RollingOutputStream> m_mapRolling = new HashMap<String, RollingOutputStream>(32);
		final ReentrantLock m_lock = new ReentrantLock();

		Date m_ref;
		String m_qSuffix;
	}

	private static class Shutdown implements Runnable {

		@Override
		public void run() {
			m_roller.shutdownInstance("Auto");
		}

		public Shutdown(ArgonRoller roller) {
			assert roller != null;
			m_roller = roller;
		}

		final ArgonRoller m_roller;
	}
}
