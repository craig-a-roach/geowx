/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.management.InstanceAlreadyExistsException;
import javax.management.NotificationBroadcasterSupport;

import com.metservice.argon.ArgonPlatformException;

/**
 * @author roach
 */
public class EarlybirdSpace extends NotificationBroadcasterSupport implements EarlybirdSpaceMBean {

	@Override
	public String alterFilterConsole(String filterPattern) {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.alterFilterConsole(filterPattern);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String alterFilterJmx(String filterPattern) {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.alterFilterJmx(filterPattern);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String alterFilterLog(String filterPattern) {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.alterFilterLog(filterPattern);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public boolean awaitShutdown(long msAwait)
			throws InterruptedException {
		m_rw.readLock().lock();
		try {
			return m_kernel.awaitShutdown(msAwait);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public void awaitShutdownRequest()
			throws InterruptedException {
		boolean wait = true;
		while (wait) {
			wait = !awaitShutdown(1000L);
			Thread.yield();
		}
		Thread.sleep(1000L);
	}

	@Override
	public String getConfigInfo() {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.getConfigInfo();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String getFilterPatternConsole() {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.getFilterPatternConsole();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String getFilterPatternJmx() {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.getFilterPatternJmx();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String getFilterPatternLog() {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.getFilterPatternLog();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String getIdInfo() {
		return m_id.toString();
	}

	@Override
	public String requestRestart() {
		m_rw.writeLock().lock();
		try {
			m_kernel = m_kernel.serviceRestart(m_spaceProbe, m_cfgSpace, m_cfgPathSensor);
			return "restarted";
		} catch (final ArgonPlatformException ex) {
			return ex.getMessage();
		} catch (final InterruptedException ex) {
			return "restart cancelled";
		} finally {
			m_rw.writeLock().unlock();
		}
	}

	@Override
	public void restoreFilterConsole() {
		m_rw.readLock().lock();
		try {
			m_spaceProbe.restoreFilterConsole();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public void restoreFilterJmx() {
		m_rw.readLock().lock();
		try {
			m_spaceProbe.restoreFilterJmx();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public void restoreFilterLog() {
		m_rw.readLock().lock();
		try {
			m_spaceProbe.restoreFilterLog();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public void shutdown() {
		m_rw.readLock().lock();
		try {
			m_kernel.serviceEnd();
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		} finally {
			m_rw.readLock().unlock();
		}
		m_probeControl.jmxUnregister(m_id);
	}

	public void start()
			throws ArgonPlatformException, InterruptedException {
		m_rw.readLock().lock();
		try {
			m_kernel.serviceStart();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		return m_id.toString();
	}

	public EarlybirdSpace(SpaceId id, SpaceCfg sc, PathSensorCfg psc) throws ArgonPlatformException,
			InstanceAlreadyExistsException {
		this(id, sc, psc, new DefaultProbeControl());
	}

	public EarlybirdSpace(SpaceId id, SpaceCfg cfgS, PathSensorCfg cfgPs, IProbeControl pc) throws ArgonPlatformException,
			InstanceAlreadyExistsException {
		if (id == null) throw new IllegalArgumentException("object is null");
		if (cfgS == null) throw new IllegalArgumentException("object is null");
		if (cfgPs == null) throw new IllegalArgumentException("object is null");
		if (pc == null) throw new IllegalArgumentException("object is null");
		m_id = id;
		m_cfgSpace = cfgS;
		m_cfgPathSensor = cfgPs;
		m_probeControl = pc;
		m_spaceProbe = new SpaceProbe(this, id, cfgS, pc);
		pc.jmxRegister(this, id);
		final KernelCfg kc = new KernelCfg(m_spaceProbe, id, cfgS, cfgPs);
		m_kernel = Kernel.newInstance(kc);
	}
	private final SpaceId m_id;
	private final ReadWriteLock m_rw = new ReentrantReadWriteLock();
	private final IProbeControl m_probeControl;
	private final SpaceCfg m_cfgSpace;
	private final PathSensorCfg m_cfgPathSensor;
	private final SpaceProbe m_spaceProbe;
	private Kernel m_kernel;
}

// @formatter:off
/*
 * 
 * public class Builder {
 * 
 * public static Builder newInstance(ArgonArgs args, Probe probe) throws ArgonArgsException, ArgonPermissionException,
 * ArgonQuotaException, ArgonStreamReadException { assert args != null; assert probe != null; final KryptonDecoderConfig
 * decoderConfig = KryptonDecoderConfig.newInstance(args); final ParsedArgs pa = new ParsedArgs(args); final
 * KryptonDecoder decoder = new KryptonDecoder(probe, decoderConfig); args.verifyUnsupported(); return new
 * Builder(probe, pa, decoder); }
 * 
 * private void build(Outcome outcome, File gridFile) { assert outcome != null; assert gridFile != null; final String
 * qccPath = gridFile.getAbsolutePath(); final KryptonFileReader oFileReader =
 * KryptonFileReader.createInstance(m_decoder, qccPath); if (oFileReader == null) return; final long tsStart =
 * System.currentTimeMillis(); try { int skippedRecordsinFile = 0; while (oFileReader.hasNextRecordReader()) { try {
 * final KryptonRecordReader rr = oFileReader.nextRecordReader(); final int recordIndex = rr.recordIndex(); try { final
 * KryptonDataRecord dataRecord = rr.newDataRecord(); emit(dataRecord, 15.0, 61.0, 15.0, 51.4, 15.1, 37.0, 20.2, 37.0,
 * 175.0, -39.0, 172.0, -43.0, 175.0, -41.0); // System.out.println(dataRecord); outcome.goodRecord(); } catch (final
 * KryptonCodeException ex) { m_probe.gridFileCode(gridFile, recordIndex, ex); skippedRecordsinFile++;
 * outcome.skipRecord(); } catch (final KryptonTableException ex) { m_probe.gridFileTable(gridFile, recordIndex, ex);
 * skippedRecordsinFile++; outcome.skipRecord(); } } catch (final KryptonRecordException ex) {
 * m_probe.gridFileRecord(gridFile, ex); skippedRecordsinFile++; outcome.skipRecord(); } } if (skippedRecordsinFile ==
 * 0) { outcome.goodFile(); } else { outcome.partialFile(); } } catch (final KryptonReadException ex) {
 * m_probe.gridFileRead(gridFile, ex); outcome.skipFile(); } finally { oFileReader.close(); } final long ms =
 * System.currentTimeMillis() - tsStart; System.out.println("Elapsed " + gridFile + "=" + ms + "ms"); }
 * 
 * private void emit(KryptonDataRecord dataRecord, double... coords) { final CobaltRecord ncubeRec =
 * dataRecord.meta().ncubeRecord(); try { final CobaltParameter pa = ncubeRec.parameter(); if
 * (!pa.qccId().equals("MX2T6")) return; // if (!pa.qccId().equals("2T")) return; final CobaltMember oMember =
 * ncubeRec.getMember(); if (oMember == null) return; final ICobaltPrognosis prognosis = ncubeRec.prognosis(); final
 * KryptonInterpolator interpolator = dataRecord.newInterpolator(); for (int c = 0; c < coords.length; c += 2) { final
 * double longitude = coords[c]; final double latitude = coords[c + 1]; final Float oValue =
 * interpolator.getBilinear(longitude, latitude); if (oValue != null) { System.out.println("Prognosis " + prognosis +
 * ", Member " + oMember.id + " " + longitude + "," + latitude + "=" + (oValue.floatValue() - 273.0f)); } } } catch
 * (final CobaltDimensionException ex) { } catch (final KryptonInterpolationException ex) { } }
 * 
 * public void build(Outcome outcome) throws ArgonPermissionException, ArgonStreamWriteException { assert outcome !=
 * null; final ArgonFileManifest gridManifest = m_parsedArgs.newGridManifest(); final File[] zptGridFilesAscPath =
 * gridManifest.zptFilesAscPath(); final int gridFileCount = zptGridFilesAscPath.length; for (int i = 0; i <
 * gridFileCount; i++) { final File gridFile = zptGridFilesAscPath[i]; build(outcome, gridFile); } }
 * 
 * private Builder(Probe probe, ParsedArgs pa, KryptonDecoder de) { assert probe != null; assert pa != null; m_probe =
 * probe; m_parsedArgs = pa; m_decoder = de; } private final Probe m_probe; private final ParsedArgs m_parsedArgs;
 * private final KryptonDecoder m_decoder;
 * 
 * private static class ParsedArgs {
 * 
 * public ArgonFileManifest newGridManifest() throws ArgonPermissionException { return
 * ArgonFileManifest.newInstance(xptqtwGribPaths, oAcceptPattern, oRejectPattern); }
 * 
 * public ParsedArgs(ArgonArgs in) throws ArgonArgsException { oAcceptPattern =
 * in.consumeAllTagValuePairs(CArg.AcceptPattern).getPatternValue(); oRejectPattern =
 * in.consumeAllTagValuePairs(CArg.RejectPattern).getPatternValue(); xptqtwGribPaths =
 * in.consumeAllUntaggedValues(CArg.GribPaths).xptqtwValues(); } public final Pattern oAcceptPattern; public final
 * Pattern oRejectPattern; public final String[] xptqtwGribPaths; } }
 */
