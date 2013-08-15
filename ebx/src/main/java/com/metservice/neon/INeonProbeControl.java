/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import javax.management.InstanceAlreadyExistsException;

/**
 * @author roach
 */
public interface INeonProbeControl {

	public INeonLogger createLogger(NeonSpaceId id, NeonSpaceCfg cfg);

	public void jmxRegister(NeonSpace space, NeonSpaceId id)
			throws InstanceAlreadyExistsException;

	public void jmxUnregister(NeonSpaceId id);

	public INeonProbeFormatter newFormatter(NeonSpaceId id, NeonSpaceCfg cfg);
}
