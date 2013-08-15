/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;
import com.metservice.argon.IArgonRunProbe;
import com.metservice.argon.management.IArgonServiceProbe;
import com.metservice.beryllium.IBerylliumNetProbe;

/**
 * @author roach
 */
interface ISpaceProbe extends IArgonFileProbe, IArgonRunProbe, IArgonServiceProbe, IBerylliumNetProbe {

	public void failFileWatch(Ds diagnostic);

	public void warnFileWatch(Ds diagnostic);
}
