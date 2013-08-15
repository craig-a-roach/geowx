/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import java.nio.charset.Charset;

import com.metservice.argon.ArgonText;
import com.metservice.argon.CArgon;
import com.metservice.argon.Elapsed;
import com.metservice.boron.BoronInterpreterId;
import com.metservice.neon.EsPrimitiveNull;
import com.metservice.neon.EsPrimitiveNumberInteger;
import com.metservice.neon.EsPrimitiveString;

/**
 * @author roach
 */
class CProp {

	public static final String GlobalFileSystem = "fileSystem";
	public static final String GlobalProcessSpace = "process";
	public static final String GlobalCaller = "caller";

	public static final String root = "root";
	public static final String user = "user";
	public static final String software = "software";

	public static final String interpreter = "interpreter";
	public static final String interpreter_default_string = "bash";
	public static final EsPrimitiveString interpreter_default_primitive = new EsPrimitiveString(interpreter_default_string);
	public static final BoronInterpreterId interpreter_default_id = new BoronInterpreterId(interpreter_default_string);

	public static final String exitTimeout = "exitTimeout";
	public static final EsPrimitiveNull exitTimeout_default_primitive = EsPrimitiveNull.Instance;

	public static final String assumeHealthlyAfter = "assumeHealthyAfter";
	public static final Elapsed assumeHealthyAfter_default = Elapsed.newInstance(CArgon.MIN_TO_MS);

	public static final String restartLimit = "restartLimit";
	public static final int restartLimit_default = 50;

	public static final String redirectStdErrToOut = "redirectStdErrToOut";
	public static final boolean redirectStdErrToOut_default = false;

	public static final String stdioEncoding = "stdioEncoding";
	public static final Charset stdioEncoding_default = ArgonText.UTF8;

	public static final String stdioLineTerminator = "stdioLineTerminator";
	public static final EsPrimitiveNull stdioLineTerminator_default = EsPrimitiveNull.Instance;

	public static final String maxProductQueueDepth = "maxProductQueueDepth";
	public static final EsPrimitiveNumberInteger maxProductQueueDepth_default = new EsPrimitiveNumberInteger(64);

	public static final String maxFeedQueueDepth = "maxFeedQueueDepth";
	public static final EsPrimitiveNumberInteger maxFeedQueueDepth_default = new EsPrimitiveNumberInteger(32);

	public static final String stdOutBufferSize = "stdOutBufferSize";
	public static final EsPrimitiveNumberInteger stdOutBufferSize_default = new EsPrimitiveNumberInteger(1024);

	public static final String stdErrBufferSize = "stdErrBufferSize";
	public static final EsPrimitiveNumberInteger stdErrBufferSize_default = new EsPrimitiveNumberInteger(1024);

	public static final String stdInBufferSize = "stdInBufferSize";
	public static final EsPrimitiveNumberInteger stdInBufferSize_default = new EsPrimitiveNumberInteger(512);

	public static final String isWinOS = "isWinOS";

	public static final String stdErrEmit = "stdErrEmit";
	public static final String stdOutEmit = "stdOutEmit";
	public static final String stdErrReport = "stdErrReport";
	public static final String stdOutReport = "stdOutReport";
	public static final String stdReportLineTerminator = "stdReportLineTerminator";
	public static final String stdReportLineTerminator_default = "\n";
	public static final String exitCode = "exitCode";
	public static final String restartCount = "restartCount";
	public static final String incomplete = "incomplete";
	public static final String cancelled = "cancelled";
}
