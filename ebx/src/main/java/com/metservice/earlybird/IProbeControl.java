/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import javax.management.InstanceAlreadyExistsException;

/**
 * @author roach
 */
interface IProbeControl {

	public ILogger createLogger(SpaceId id, SpaceCfg cfg);

	public void jmxRegister(EarlybirdSpace space, SpaceId id)
			throws InstanceAlreadyExistsException;

	public void jmxUnregister(SpaceId id);

	public IProbeFormatter newFormatter(SpaceId id, SpaceCfg cfg);
}
