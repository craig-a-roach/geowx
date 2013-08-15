/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public interface IBerylliumNetProbe {

	void failNet(Ds diagnostic);

	void infoNet(String message);

	boolean isLiveNet();

	void liveNet(String message, Object... args);

	void warnNet(Ds diagnostic);
}
