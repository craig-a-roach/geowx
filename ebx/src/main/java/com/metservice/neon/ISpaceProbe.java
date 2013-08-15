/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.File;

import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;

/**
 * @author roach
 */
interface ISpaceProbe extends IArgonFileProbe {

	void failFile(Ds diagnostic, File ofile);

	void failNet(Ds diagnostic);

	void failScriptCompile(String qccSourcePath, String diagnostic);

	void failScriptEmit(String qccSourcePath, String diagnostic);

	void failScriptLoad(String qccSourcePath, String diagnostic);

	void failScriptRun(String qccSourcePath, String diagnostic);

	void failSoftware(Ds diagnostic);

	void failSoftware(RuntimeException exRT);

	void infoNet(Ds diagnostic);

	void infoShell(String diagnostic);

	void liveScriptEmit(String qccSourcePath, String diagnostic);

	void warnFile(Ds diagnostic, File ofile);

	void warnNet(Ds diagnostic);

	void warnSoftware(Ds diagnostic);
}
