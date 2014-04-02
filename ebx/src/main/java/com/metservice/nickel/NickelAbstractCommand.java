/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.nickel;

import com.metservice.argon.ArgonArgs;
import com.metservice.argon.ArgonArgsException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.CSystemExit;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public abstract class NickelAbstractCommand {

	protected abstract int runCommand()
			throws ArgonArgsException, ArgonPermissionException, ArgonQuotaException, ArgonStreamReadException,
			ArgonStreamWriteException, InterruptedException;

	public final int runMain() {
		int exitCode = CSystemExit.GeneralError;
		String oqFailureOperator = null;
		String oqFailureDeveloper = null;
		try {
			exitCode = runCommand();
		} catch (final ArgonArgsException ex) {
			oqFailureOperator = "Command Line Syntax";
			oqFailureDeveloper = Ds.message(ex);
			exitCode = CSystemExit.UsageError;
		} catch (final ArgonPermissionException ex) {
			oqFailureOperator = "File Permission";
			oqFailureDeveloper = Ds.format(ex);
			exitCode = CSystemExit.PermissionDenied;
		} catch (final ArgonQuotaException ex) {
			oqFailureOperator = "File Quota";
			oqFailureDeveloper = Ds.format(ex);
			exitCode = CSystemExit.QuotaExceeded;
		} catch (final ArgonStreamReadException ex) {
			oqFailureOperator = "File Read";
			oqFailureDeveloper = Ds.format(ex);
			exitCode = CSystemExit.IOError;
		} catch (final ArgonStreamWriteException ex) {
			oqFailureOperator = "File Write";
			oqFailureDeveloper = Ds.format(ex);
			exitCode = CSystemExit.IOError;
		} catch (final InterruptedException ex) {
			oqFailureOperator = "Cancelled";
			exitCode = CSystemExit.CancelledByOperator;
		} catch (final RuntimeException ex) {
			oqFailureOperator = "Software";
			oqFailureDeveloper = Ds.format(ex);
			exitCode = CSystemExit.SoftwareError;
		} finally {
			if (oqFailureOperator != null) {
				System.err.println("Command Failed");
				System.err.println(oqFailureOperator);
				if (oqFailureDeveloper != null) {
					System.err.println("Developer Diagnostic...");
					System.err.println(oqFailureDeveloper);
				}
			}
		}
		return exitCode;
	}

	@Override
	public String toString() {
		return args.toString();
	}

	protected NickelAbstractCommand(String[] zptArgs) {
		if (zptArgs == null) throw new IllegalArgumentException("object is null");
		this.args = new ArgonArgs(zptArgs);
	}
	protected final ArgonArgs args;
}
