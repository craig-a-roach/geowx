/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import javax.management.InstanceAlreadyExistsException;

import com.metservice.argon.CSystemExit;

/**
 * @author roach
 */
public class Boot {

	public static void main(String[] args) {
		System.exit(runMain(args));
	}

	public static int runMain(String[] args) {
		NeonSpace ospace = null;
		int exitCode = CSystemExit.GeneralError;
		String oqStartFailureOperator = null;
		String oqStartFailureDeveloper = null;
		try {
			final NeonStartup startup = NeonStartup.newInstanceFromArgs(args);
			final NeonSpaceId id = startup.newSpaceId();
			final NeonSpaceCfg cfg = startup.newSpaceCfg();
			final INeonSourceProvider sp = startup.newSourceProvider();
			ospace = new NeonSpace(id, sp, cfg);
			ospace.start();
			ospace.awaitShutdownRequest();
			ospace.shutdown();
			exitCode = CSystemExit.OK;
		} catch (final NeonCfgSyntaxException ex) {
			oqStartFailureOperator = "Invalid configuration syntax";
			oqStartFailureDeveloper = ex.getMessage();
			exitCode = CSystemExit.UsageError;
		} catch (final NeonPlatformException ex) {
			oqStartFailureOperator = "Host platform is in an unexpected state";
			oqStartFailureDeveloper = ex.getMessage();
			exitCode = CSystemExit.ConfigurationError;
		} catch (final InstanceAlreadyExistsException ex) {
			oqStartFailureOperator = "Already running on host";
			oqStartFailureDeveloper = ex.getMessage();
			exitCode = CSystemExit.ConfigurationError;
		} catch (final InterruptedException ex) {
			oqStartFailureOperator = "Cancelled By Operator";
			exitCode = CSystemExit.CancelledByOperator;
		} finally {
			if (oqStartFailureOperator != null) {
				System.err.println("Neon Failed To Start");
				System.err.println(oqStartFailureOperator);
				if (oqStartFailureDeveloper != null) {
					System.err.println("Developer Diagnostic...");
					System.err.println(oqStartFailureDeveloper);
				}
			}
			if (ospace != null) {
				ospace.shutdown();
			}
		}
		return exitCode;
	}
}
