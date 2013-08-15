/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.CSystemExit;
import com.metservice.argon.Elapsed;
import com.metservice.boron.BoronApiException;
import com.metservice.boron.BoronExitCode;
import com.metservice.boron.BoronImpException;
import com.metservice.boron.BoronProcessId;
import com.metservice.boron.BoronProductCancellation;
import com.metservice.boron.BoronProductExitCode;
import com.metservice.boron.BoronProductInterpreterFailure;
import com.metservice.boron.BoronProductIterator;
import com.metservice.boron.BoronProductManagementFailure;
import com.metservice.boron.BoronProductStreamLine;
import com.metservice.boron.BoronSpace;
import com.metservice.boron.IBoronProduct;
import com.metservice.neon.EmClass;
import com.metservice.neon.EmException;
import com.metservice.neon.EmMethod;
import com.metservice.neon.EsApiCodeException;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsMethodAccessor;
import com.metservice.neon.EsObjectAccessor;
import com.metservice.neon.EsPrimitiveString;
import com.metservice.neon.IEsOperand;

/**
 * @author roach
 */
class ProcessSpaceEmClass extends EmClass {

	private static final String Name = CClass.ProcessSpace;
	static final EmMethod[] Methods = { new method_executeCommand(), new method_executeDaemon(), new method_newProductIterator(),
			new method_toString() };

	static final ProcessSpaceEmClass Instance = new ProcessSpaceEmClass(Name, Methods);

	static ProcessSpaceEm self(EsExecutionContext ecx) {
		return ecx.thisObject(Name, ProcessSpaceEm.class);
	}

	private ProcessSpaceEmClass(String qccClassName, EmMethod[] ozptMethods) {
		super(qccClassName, ozptMethods);
	}

	static abstract class amethod_execute extends EmMethod {

		protected static final String[] BaseParams = { CArg.script, CArg.stdioCapture };

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final ProcessSpaceEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final ScriptEm emScript = ScriptEmClass.arg(ac.select(0));
			final EsObjectAccessor oStdioCapture = ac.defaulted(1) ? null : ac.createObjectAccessor(1);
			final EsObjectAccessor oRestartPolicy = (!m_isDaemon || ac.defaulted(1)) ? null : ac.createObjectAccessor(2);
			boolean stdErrEmit = true;
			boolean stdErrReport = !m_isDaemon;
			boolean stdOutEmit = true;
			boolean stdOutReport = false;
			String zReportTerm = CProp.stdReportLineTerminator_default;
			Elapsed oAssumeHealthyAfter = m_isDaemon ? CProp.assumeHealthyAfter_default : null;
			int restartLimit = m_isDaemon ? CProp.restartLimit_default : 0;
			if (oStdioCapture != null) {
				stdErrEmit = oStdioCapture.booleanValue(CProp.stdErrEmit);
				stdErrReport = oStdioCapture.booleanValue(CProp.stdErrReport);
				stdOutEmit = oStdioCapture.booleanValue(CProp.stdOutEmit);
				stdOutReport = oStdioCapture.booleanValue(CProp.stdOutReport);
				if (!oStdioCapture.defaulted(CProp.stdReportLineTerminator)) {
					zReportTerm = oStdioCapture.zStringValue(CProp.stdReportLineTerminator);
				}
			}
			if (oRestartPolicy != null) {
				if (!oRestartPolicy.defaulted(CProp.assumeHealthlyAfter)) {
					oAssumeHealthyAfter = oRestartPolicy.elapsedValue(CProp.assumeHealthlyAfter);
				}
				if (!oRestartPolicy.defaulted(CProp.restartLimit)) {
					restartLimit = oRestartPolicy.intValue(CProp.restartLimit);
				}
			}
			final BoronSpace bspace = self.bspace();
			final ScriptTuple script = emScript.newTuple(ecx);
			try {
				boolean isComplete = false;
				boolean cancelled = false;
				BoronExitCode oExitCode = null;
				int restartCount = 0;
				String oqIncomplete = null;
				final StringBuilder oStdErrReport = stdErrReport ? new StringBuilder(256) : null;
				final StringBuilder oStdOutReport = stdOutReport ? new StringBuilder(512) : null;
				BoronProductIterator biterator = bspace.newProcessProductIterator(script);
				while (!isComplete) {
					final long tsProcessLaunch = ArgonClock.tsNow();
					while (biterator.hasNext()) {
						final IBoronProduct product = biterator.next();
						if (product instanceof BoronProductStreamLine) {
							final BoronProductStreamLine streamLine = (BoronProductStreamLine) product;
							if (streamLine.isStdErr()) {
								if (stdErrEmit) {
									ecx.global().emitFail(streamLine.zLine());
								}
								if (oStdErrReport != null) {
									if (oStdErrReport.length() > 0) {
										oStdErrReport.append(zReportTerm);
									}
									oStdErrReport.append(streamLine.zLine());
								}
							} else {
								if (stdOutEmit) {
									ecx.global().emitTrace(streamLine.zLine());
								}
								if (oStdOutReport != null) {
									if (oStdOutReport.length() > 0) {
										oStdOutReport.append(zReportTerm);
									}
									oStdOutReport.append(streamLine.zLine());
								}
							}
							continue;
						}
						if (product instanceof BoronProductExitCode) {
							oExitCode = ((BoronProductExitCode) product).exitCode();
							continue;
						}
						if (product instanceof BoronProductCancellation) {
							cancelled = true;
							oqIncomplete = "Cancelled";
							continue;
						}
						if (product instanceof BoronProductManagementFailure) {
							oqIncomplete = "Process Management Failure";
							continue;
						}
						if (product instanceof BoronProductInterpreterFailure) {
							final BoronProductInterpreterFailure pif = (BoronProductInterpreterFailure) product;
							final String ztwReason = pif.diagnostic().ztwReason();
							oqIncomplete = "Interpreter Failure" + (ztwReason.length() == 0 ? "" : "..." + ztwReason);
							continue;
						}
					}
					isComplete = true;
					final long msProcessLife = ArgonClock.tsNow() - tsProcessLaunch;
					if (m_isDaemon) {
						if (oExitCode != null && oAssumeHealthyAfter != null && restartLimit > 0) {
							final boolean terminalExit = CSystemExit.isDaemonComplete(oExitCode.value());
							final boolean wasHealthy = (msProcessLife >= oAssumeHealthyAfter.sms);
							final boolean restartAllowed = restartCount < restartLimit;
							if (!terminalExit && wasHealthy && restartAllowed) {
								restartCount++;
								biterator = bspace.newProcessProductIterator(script);
								isComplete = false;
							}
						}
					}
				}

				final BoronProcessId processId = biterator.processId();
				final String ozStdErrReport = oStdErrReport == null ? null : oStdErrReport.toString();
				final String ozStdOutReport = oStdOutReport == null ? null : oStdOutReport.toString();
				return new ProcessCompletionEm(bspace, processId, oqIncomplete, oExitCode, cancelled, ozStdErrReport,
						ozStdOutReport, restartCount);
			} catch (final BoronApiException ex) {
				throw new EsApiCodeException(ex);
			} catch (final BoronImpException ex) {
				throw new EmException(ex);
			}
		}

		public amethod_execute(String qccName, int requiredArgumentCount, String[] ozptFormalParameterNames0, boolean isDaemon,
				String... ozptFormalParameterNames1) {
			super(qccName, requiredArgumentCount, ozptFormalParameterNames0, ozptFormalParameterNames1);
			m_isDaemon = isDaemon;
		}
		private final boolean m_isDaemon;
	}

	static class method_executeCommand extends amethod_execute {

		public method_executeCommand() {
			super("executeCommand", 1, BaseParams, false);
		}
	}

	static class method_executeDaemon extends amethod_execute {

		public method_executeDaemon() {
			super("executeDaemon", 1, BaseParams, true, CArg.restartPolicy);
		}
	}

	static class method_newProductIterator extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final ProcessSpaceEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final ScriptEm emScript = ScriptEmClass.arg(ac.select(0));
			final BoronSpace bspace = self.bspace();
			try {
				final ScriptTuple script = emScript.newTuple(ecx);
				final BoronProductIterator biterator = bspace.newProcessProductIterator(script);
				return new ProcessProductIteratorEm(biterator);
			} catch (final BoronApiException ex) {
				throw new EsApiCodeException(ex);
			} catch (final BoronImpException ex) {
				throw new EmException(ex);
			}
		}

		public method_newProductIterator() {
			super("newProductIterator", 1, CArg.script);
		}
	}

	static class method_toString extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return new EsPrimitiveString(self(ecx).bspace());
		}

		public method_toString() {
			super(StdName_toString);
		}
	}
}
