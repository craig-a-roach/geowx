/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
public interface IBoronProbeControl {
	public IBoronLogger createLogger(BoronSpaceId id, BoronSpaceCfg cfg);

	public void jmxRegister(BoronSpace space, BoronSpaceId id);

	public void jmxUnregister(BoronSpaceId id);

	public IBoronProbeFormatter newFormatter(BoronSpaceId id, BoronSpaceCfg cfg);
}
