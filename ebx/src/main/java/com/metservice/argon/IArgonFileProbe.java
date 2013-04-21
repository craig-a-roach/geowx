/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.File;

/**
 * @author roach
 */
public interface IArgonFileProbe {

	void failFile(Ds diagnostic, File ofile);

	void warnFile(Ds diagnostic, File ofile);
}
