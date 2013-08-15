/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.File;

import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;

/**
 * @author roach
 */
interface ISpaceProbe extends IArgonFileProbe {

	void failFile(Ds diagnostic, File ofile);

	void failSoftware(Ds diagnostic);

	void failSoftware(RuntimeException exRT);

	void warnFile(Ds diagnostic, File ofile);

	void warnSoftware(Ds diagnostic);

}
