/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import java.io.PrintStream;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonServiceId;

/**
 * @author roach
 * A console logger
 */
public class ArgonLogger {

	public static final ArgonRecordType RecordTypeProbe = new ArgonRecordType("probe");

	public void failure(String message) {
		if (m_ops == null) {
			System.err.println(message);
		} else {
			m_ops.println(message);
		}
	}

	public void information(String message) {
		if (m_ops != null) {
			m_ops.println(message);
		}
	}

	public void live(String message) {
		if (m_ops != null) {
			m_ops.println(message);
		}
	}

	public void warning(String message) {
		if (m_ops == null) {
			System.err.println(message);
		} else {
			m_ops.println(message);
		}
	}

	public ArgonLogger(ArgonServiceId sid, ArgonRecordType recType, IArgonSpaceId idSpace) {
		PrintStream ops = null;
		try {
			ops = ArgonRoller.printStream(sid, recType, idSpace);
		} catch (final ArgonPermissionException ex) {
			System.err.println("Cannot enable rolling log; using stderr for failures and warnings (" + ex.getMessage() + ")");
		}
		m_ops = ops;
	}

	public ArgonLogger(ArgonServiceId sid, IArgonSpaceId idSpace) {
		this(sid, RecordTypeProbe, idSpace);
	}

	private final PrintStream m_ops;
}
