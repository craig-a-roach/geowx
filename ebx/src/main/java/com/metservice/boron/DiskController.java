/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Ds;
import com.metservice.argon.file.ArgonDirectoryManagement;
import com.metservice.argon.file.ArgonFileManagement;

/**
 * @author roach
 */
class DiskController {

	private static final Charset ProcessIdCheckpointCharset = ArgonText.ASCII;

	private static File cndirBaseWriteable(KernelCfg kc)
			throws BoronPlatformException {
		final String ozBase = kc.cfg.getBase();
		final String ztwBase = ozBase == null ? "" : ozBase.trim();
		try {
			final File cndirBase;
			if (ztwBase.length() == 0) {
				cndirBase = ArgonDirectoryManagement.cndirEnsureUserWriteable(CBoron.Vendor, CBoron.ServiceName);
			} else {
				cndirBase = ArgonDirectoryManagement.cndir(new File(ztwBase));
				ArgonDirectoryManagement.cndirEnsureWriteable(cndirBase);
			}
			return cndirBase;
		} catch (final ArgonPermissionException ex) {
			final String m = "Cannot create base directory '" + ztwBase + "'...";
			throw new BoronPlatformException(m + ex.getMessage());
		}
	}

	private static File cndirHomeWriteable(KernelCfg kc, File cndirBase)
			throws BoronPlatformException {
		final String qccId = kc.id.format();
		try {
			return ArgonDirectoryManagement.cndirEnsureSubWriteable(cndirBase, qccId);
		} catch (final ArgonPermissionException ex) {
			final String m = "Cannot create home directory for space '" + qccId + "'...";
			throw new BoronPlatformException(m + ex.getMessage());
		}
	}

	private static File cndirWorkWriteable(KernelCfg kc, File cndirHome)
			throws BoronPlatformException {
		final String qccId = kc.id.format();
		try {
			return ArgonDirectoryManagement.cndirEnsureSubWriteable(cndirHome, CBoron.SubDirName_Work);
		} catch (final ArgonPermissionException ex) {
			final String m = "Cannot create work directory for space '" + qccId + "'...";
			throw new BoronPlatformException(m + ex.getMessage());
		}
	}

	private static BoronProcessId nextProcessId(KernelCfg kc, File fileIdCheckpoint)
			throws BoronPlatformException {
		if (fileIdCheckpoint == null) throw new IllegalArgumentException("object is null");
		try {
			final String[] ozpt = UBoron.loadText(kc.probe, fileIdCheckpoint, ProcessIdCheckpointCharset);
			long nextProcessId = BoronProcessId.Init;
			if (ozpt != null && ozpt.length > 0) {
				final long nVal = ArgonText.parseLongB36(ozpt[0], -1L).longValue();
				if (nVal >= BoronProcessId.Init) {
					nextProcessId = nVal + 1L;
				}
			}
			return new BoronProcessId(nextProcessId);
		} catch (final DiskException ex) {
			final Ds ds = Ds.invalidBecause("Could not read process id checkpoint", BoronPlatformException.class);
			ds.a("checkpointFile", fileIdCheckpoint);
			throw new BoronPlatformException(ds);
		}
	}

	public static DiskController newInstance(KernelCfg kc, ExecutorService exec)
			throws BoronPlatformException, BoronApiException {
		if (kc == null) throw new IllegalArgumentException("object is null");
		if (exec == null) throw new IllegalArgumentException("object is null");

		final File cndirBase = cndirBaseWriteable(kc);
		final File cndirHome = cndirHomeWriteable(kc, cndirBase);
		final File fileIdCheckpoint = new File(cndirHome, CBoron.FileName_ProcessIdCheckpoint);
		final File cndirWork = cndirWorkWriteable(kc, cndirHome);
		final SpaceMutex mutex = new SpaceMutex(cndirHome);
		mutex.acquire(kc);
		final WorkRotator workRotator = WorkRotator.newInstance(kc, cndirWork);
		final BoronProcessId bpidNextWork = workRotator.nextProcessId();
		final BoronProcessId bpidNextCheckpoint = nextProcessId(kc, fileIdCheckpoint);
		final BoronProcessId bpidNext = bpidNextCheckpoint.max(bpidNextWork);
		return new DiskController(kc, exec, cndirWork, mutex, fileIdCheckpoint, workRotator, bpidNext);
	}

	private final File cndirProcessMake(BoronProcessId processId)
			throws BoronImpException {
		if (processId == null) throw new IllegalArgumentException("object is null");
		final String qPSubName = processId.qId();
		try {
			return ArgonDirectoryManagement.cndirEnsureSubWriteable(m_cndirWork, qPSubName);
		} catch (final ArgonPermissionException ex) {
			final Ds ds = Ds.triedTo("Create process working directory", ex, BoronImpException.class);
			ds.a("processId", processId);
			ds.a("workingdirectory", m_cndirWork);
			throw new BoronImpException(ds);
		}
	}

	private BoronProcessId nextProcessId()
			throws BoronImpException {
		final BoronProcessId neoProcessId = new BoronProcessId(m_nextProcessId.getAndIncrement());
		final List<String> lines = new ArrayList<String>(1);
		lines.add(neoProcessId.qId());
		try {
			UBoron.saveText(kc.probe, m_fileIdCheckpoint, ProcessIdCheckpointCharset, "\n", lines);
			return neoProcessId;
		} catch (final DiskException ex) {
			final Ds ds = Ds.invalidBecause("Could not save process id checkpoint", BoronImpException.class);
			ds.a("id", neoProcessId);
			throw new BoronImpException(ds);
		}
	}

	private File saveMainScript(IBoronScriptInterpreter interpreter, IBoronScript script, File cndirProcess)
			throws BoronImpException {
		assert interpreter != null;
		assert script != null;
		assert cndirProcess != null;
		final Charset scriptEncoding = interpreter.scriptEncoding();
		final String zLineTerm = interpreter.zScriptLineTerminator();
		final String qccMainScriptName = interpreter.qccScriptName(CBoron.FileName_MainScript);
		final File cnMainScript = new File(cndirProcess, qccMainScriptName);
		final List<String> zlLines = script.zlLines();
		try {
			UBoron.saveText(kc.probe, cnMainScript, scriptEncoding, zLineTerm, zlLines);
			return cnMainScript;
		} catch (final DiskException ex) {
			final Ds ds = Ds.invalidBecause("Could not create main script file for process", BoronImpException.class);
			ds.a("mainScriptFile", cnMainScript);
			ds.a("encoding", scriptEncoding);
			throw new BoronImpException(ds);
		}
	}

	private void saveResource(File cnResourceFile, byte[] zptContent)
			throws BoronImpException {
		assert cnResourceFile != null;
		assert zptContent != null;
		try {
			UBoron.saveBinary(kc.probe, cnResourceFile, zptContent);
		} catch (final DiskException ex) {
			final Ds ds = Ds.invalidBecause("Could not create resource file for process", BoronImpException.class);
			ds.a("resourceFile", cnResourceFile);
			throw new BoronImpException(ds);
		}
	}

	private void saveResources(IBoronScript script, File cndirProcess)
			throws BoronImpException {
		assert script != null;
		assert cndirProcess != null;
		final List<IBoronScriptResource> zlResources = script.zlResources();
		for (final IBoronScriptResource resource : zlResources) {
			final String ozccRelativePath = resource.qccRelativePath();
			final byte[] ozptContent = resource.zptContent();
			if (ozccRelativePath != null && ozccRelativePath.length() > 0 && ozptContent != null) {
				final File cnResourceFile = ArgonFileManagement.cnfile(kc.probe, new File(cndirProcess, ozccRelativePath));
				saveResource(cnResourceFile, resource.zptContent());
			}
		}
	}

	public File cndirProcess(BoronProcessId processId)
			throws BoronApiException {
		if (processId == null) throw new IllegalArgumentException("object is null");
		final String qPSubName = processId.qId();
		final File cndirProcess = new File(m_cndirWork, qPSubName);
		if (!cndirProcess.canRead()) {
			final String m = "Process " + processId + " working directory '" + cndirProcess + "' no longer exists";
			throw new BoronApiException(m);
		}
		return cndirProcess;
	}

	public MainProcessImage newMainProcessImage(IBoronScriptInterpreter interpreter, IBoronScript script)
			throws BoronImpException {
		if (interpreter == null) throw new IllegalArgumentException("object is null");
		if (script == null) throw new IllegalArgumentException("object is null");
		final BoronProcessId neoProcessId = nextProcessId();
		final File cndirProcess = cndirProcessMake(neoProcessId);
		final File cnMainScript = saveMainScript(interpreter, script, cndirProcess);
		saveResources(script, cndirProcess);
		final ProcessBuilder processBuilder = interpreter.newProcessBuilder(cnMainScript);
		processBuilder.directory(cndirProcess);
		processBuilder.redirectErrorStream(script.redirectStdErrToOut());
		return new MainProcessImage(neoProcessId, cndirProcess, processBuilder);
	}

	public void onRemoveHalted(ProcessEngine engine) {
		if (engine == null) throw new IllegalArgumentException("object is null");
		m_workRotator.add(engine.processId());
	}

	public void serviceEnd() {
		m_runnableWorkRotator.end();
		m_mutex.release(kc);
	}

	public void serviceStart()
			throws InterruptedException {
		m_execService.execute(m_runnableWorkRotator);
		m_runnableWorkRotator.awaitStart();
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("cndirWork", m_cndirWork);
		ds.a("nextProcessId", m_nextProcessId);
		return ds.s();
	}

	private DiskController(KernelCfg kc, ExecutorService exec, File cndirWork, SpaceMutex mutex, File fileIdCheckpoint,
			WorkRotator workRotator, BoronProcessId bpidNext) {
		assert kc != null;
		assert cndirWork != null;
		assert mutex != null;
		assert fileIdCheckpoint != null;
		assert workRotator != null;
		assert bpidNext != null;
		this.kc = kc;
		m_execService = exec;
		m_cndirWork = cndirWork;
		m_mutex = mutex;
		m_fileIdCheckpoint = fileIdCheckpoint;
		m_workRotator = workRotator;
		m_runnableWorkRotator = new RunnableWorkRotator(kc, workRotator);
		m_nextProcessId = new AtomicLong(bpidNext.id);
	}

	final KernelCfg kc;
	private final ExecutorService m_execService;
	private final File m_cndirWork;
	private final SpaceMutex m_mutex;
	private final File m_fileIdCheckpoint;
	private final WorkRotator m_workRotator;
	private final RunnableWorkRotator m_runnableWorkRotator;
	private final AtomicLong m_nextProcessId;
}
