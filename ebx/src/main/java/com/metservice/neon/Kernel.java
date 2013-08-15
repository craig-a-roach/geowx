/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.List;
import java.util.Map;

import com.metservice.argon.Ds;
import com.metservice.beryllium.BerylliumSupportId;

/**
 * @author roach
 */
class Kernel {

	public static Kernel newInstance(KernelCfg kc) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		final NeonSourceLoader sl = new NeonSourceLoader(kc);
		final NeonCallableCache cc = new NeonCallableCache(kc);
		final NeonShell sh = NeonShell.newInstance(kc, sl);
		return new Kernel(kc, sl, cc, sh);
	}

	private void commit(EsCommitAgenda oCommitAgenda, boolean commitable) {
		if (oCommitAgenda == null) return;
		final ISpaceProbe probe = m_kc.probe;
		if (commitable) {
			oCommitAgenda.commit(probe);
		} else {
			oCommitAgenda.rollback(probe);
		}
	}

	private boolean commitableCallResult(IEsOperand callResult) {
		boolean rollback = false;
		if (callResult instanceof EsCompletionThrow) {
			final EsCompletionThrow ct = (EsCompletionThrow) callResult;
			rollback = ct.toCanonicalBoolean();
		}
		return !rollback;
	}

	private IEsCallable newCallable(EsSource source)
			throws NeonScriptException {
		try {
			return m_callableCache.newCallable(source);
		} catch (final EsSyntaxException ex) {
			final String diagnostic = ex.getMessage();
			m_kc.probe.failScriptCompile(source.qccPath(), diagnostic);
			throw new NeonScriptCompileException(diagnostic);
		}
	}

	private IEsOperand newCallResult(EsRequest request, IEsCallable callable, EsGlobal global, EsExecutionContext ecx)
			throws InterruptedException {
		final EsCallableEntryPoint oEntryPoint = request.getEntryPoint();
		final IEsOperand mainResult = callable.call(ecx);
		if (mainResult instanceof EsCompletionThrow) return mainResult;
		if (oEntryPoint == null) return mainResult;
		final Map<String, IEsCallable> zmCallables = callable.zmCallables();
		ecx.populateVariableObject(null, zmCallables, null);
		final String qtwFunctionName = oEntryPoint.qtwFunctionName();
		final EsReference ref = ecx.scopeChain().resolve(qtwFunctionName);
		final EsObject oBase = ref.getBase();
		EsFunction oFunction = null;
		if (oBase != null) {
			final IEsOperand esf = oBase.esGet(qtwFunctionName);
			if (esf instanceof EsFunction) {
				oFunction = (EsFunction) esf;
			}
		}
		if (oFunction == null) {
			final String m = "Required entry point function '" + qtwFunctionName + "' has not been declared";
			throw new EsApiCodeException(m);
		}
		final EsList argumentList = oEntryPoint.newArgumentList(ecx);
		final EsActivation activation = EsActivation.newInstance(global, oFunction, argumentList);
		final EsExecutionContext ecxEp = ecx.newInstance(oFunction, activation, global);
		final IEsCallable callableEp = oFunction.callable();
		return callableEp.call(ecxEp);
	}

	private EsSource newSource(String qccSourcePath)
			throws NeonScriptException {
		try {
			return m_sourceLoader.newSource(qccSourcePath);
		} catch (final EsSourceLoadException ex) {
			final String diagnostic = ex.getMessage();
			m_kc.probe.failScriptLoad(qccSourcePath, diagnostic);
			throw new NeonScriptCompileException(diagnostic);
		}
	}

	private void run(EsRequest request, EsResponse response, EsSource source, IEsCallable callable)
			throws InterruptedException, NeonScriptException, NeonImpException {
		assert request != null;
		assert source != null;
		assert callable != null;
		final ShellHook shellHook = m_shell.newHook(request, source);
		shellHook.runStart();
		boolean cleanRun = false;
		boolean commitable = false;
		EsCommitAgenda oCommitAgenda = null;
		try {
			final EsGlobal global = EsGlobal.newInstance(m_kc.probe, m_kc.sourceProvider, shellHook);
			final EsExecutionContext ecx = global.initialExecutionContext();
			final List<IEmInstaller> zlInstallers = request.zlInstallers();
			for (final IEmInstaller installer : zlInstallers) {
				installer.install(ecx);
			}
			oCommitAgenda = global.commitAgenda;
			final IEsOperand callResult = newCallResult(request, callable, global, ecx);
			response.save(request, global, ecx, callResult);
			commitable = commitableCallResult(callResult);
			cleanRun = true;
		} catch (final EsRunException ex) {
			final String qccSourcePath = source.qccPath();
			final String causeMessage = ex.causeMessage();
			final String diagnostic = ex.getMessage();
			if (ex instanceof EsAssertException) {
				m_shell.console().failScriptRun(qccSourcePath, diagnostic);
				throw new NeonScriptAssertException(causeMessage);
			}
			m_kc.probe.failScriptRun(qccSourcePath, diagnostic);
			m_shell.console().failScriptRun(qccSourcePath, diagnostic);
			throw new NeonScriptRunException(diagnostic, causeMessage);
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo("Run Ecmascript program", ex, NeonImpException.class);
			ds.a("request", request);
			ds.a("source", source);
			ds.a("callable", callable);
			m_kc.probe.failSoftware(ds);
			throw new NeonImpException(ds);
		} finally {
			commit(oCommitAgenda, commitable);
			shellHook.runEnd(cleanRun);
		}
	}

	void selfRegister(NeonSpace space) {
		if (space == null) throw new IllegalArgumentException("object is null");
		m_shell.assurance().register(space);
	}

	public boolean isShutdownInProgress() {
		return m_shell.isShutdownInProgress();
	}

	public NeonAssuranceReport newAssuranceReport() {
		return m_shell.assurance().newReport();
	}

	public void register(NeonAssuranceContext oContext) {
		m_shell.assurance().register(oContext);
	}

	public void run(EsRequest request, EsResponse response)
			throws NeonScriptException, NeonImpException, InterruptedException {
		if (request == null) throw new IllegalArgumentException("object is null");
		if (response == null) throw new IllegalArgumentException("object is null");
		final String qccSourcePath = request.qccSourcePath();
		final EsSource source = newSource(qccSourcePath);
		final IEsCallable callable = newCallable(source);
		run(request, response, source, callable);
	}

	public void serviceEnd() {
		m_shell.serviceEnd();
	}

	public Kernel serviceRestart(ISpaceProbe neoProbe, INeonSourceProvider neoSourceProvider, NeonSpaceCfg neoCfg)
			throws NeonPlatformException {
		if (neoProbe == null) throw new IllegalArgumentException("object is null");
		if (neoSourceProvider == null) throw new IllegalArgumentException("object is null");
		if (neoCfg == null) throw new IllegalArgumentException("object is null");
		serviceEnd();
		final KernelCfg neoKernelCfg = new KernelCfg(neoProbe, m_kc.id, neoSourceProvider, neoCfg);
		final Kernel neo = newInstance(neoKernelCfg);
		neo.serviceStart();
		return neo;
	}

	public void serviceStart()
			throws NeonPlatformException {
		m_shell.serviceStart();
	}

	public List<BerylliumSupportId> zlDebugSupportIdsAsc() {
		return m_shell.debugger().zlSupportIdsAsc();
	}

	public List<BerylliumSupportId> zlProfileSupportIdsAsc() {
		return m_shell.profiler().zlSupportIdsAsc();
	}

	private Kernel(KernelCfg kc, NeonSourceLoader sl, NeonCallableCache cc, NeonShell sh) {
		assert kc != null;
		assert sl != null;
		assert cc != null;
		assert sh != null;
		m_kc = kc;
		m_sourceLoader = sl;
		m_callableCache = cc;
		m_shell = sh;
	}
	private final KernelCfg m_kc;
	private final NeonSourceLoader m_sourceLoader;
	private final NeonCallableCache m_callableCache;
	private final NeonShell m_shell;
}
