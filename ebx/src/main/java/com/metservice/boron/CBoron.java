/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.CArgon;

/**
 * @author roach
 */
abstract class CBoron implements CArgon {

	static final String Vendor = "com.metservice";
	public static final String ServiceName = "boron";
	static final ArgonServiceId ServiceId = new ArgonServiceId(Vendor, ServiceName);
	static final String ThreadPrefix = "boron-";

	static final String FileName_Mutex = "space.mutex";
	static final String FileName_MainScript = "main";
	static final String SubDirName_Work = "work";
	static final String FileName_ProcessIdCheckpoint = "processId.cp";

	static final int DefaultWorkHistoryDepth = 1000;
	static final int DefaultCooldownSecs = 120;

	static final int AvgCharsPerTextLine = 50;
	static final int MaxTextFileBufferBc = 16 * M;
	static final int HealthYieldMs = 1000;
	static final int WorkRotateYieldMs = 1000;

	static final String CharsetDecodeErrorReplacement16 = "\uFFFD";
	static final byte[] CharsetEncodeErrorReplacement7 = { 0x3F };
}
