/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.trial;

import com.metservice.argon.ArgonArgsAccessor;
import com.metservice.argon.ArgonArgsException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.nickel.NickelAbstractCommand;

/**
 * @author roach
 */
public class Command extends NickelAbstractCommand {

	public static void main(String[] args) {
		System.exit(new Command(args).runMain());
	}

	private Probe newProbe()
			throws ArgonArgsException {
		final ArgonArgsAccessor aReportLimit = args.consumeAllTagValuePairs(CArg.ReportLimit);
		final int reportLimit = aReportLimit.integerValue(CDefault.ReportLimit);
		final Probe probe = new Probe(reportLimit);
		return probe;
	}

	@Override
	protected int runCommand()
			throws ArgonArgsException, ArgonPermissionException, ArgonQuotaException, ArgonStreamReadException,
			ArgonStreamWriteException {
		final Probe probe = newProbe();
		final Builder b = Builder.newInstance(args, probe);
		final Outcome outcome = new Outcome();
		b.build(outcome);
		probe.emitEpilogue();
		outcome.emitReport();
		return outcome.exitCode();
	}

	private Command(String[] zptArgs) {
		super(zptArgs);
	}
}
