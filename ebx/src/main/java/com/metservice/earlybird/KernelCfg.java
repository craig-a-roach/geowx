/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

/**
 * @author roach
 */
class KernelCfg {

	public KernelCfg(ISpaceProbe probe, SpaceId id, SpaceCfg cfgS, PathSensorCfg cfgPs) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (id == null) throw new IllegalArgumentException("object is null");
		if (cfgS == null) throw new IllegalArgumentException("object is null");
		if (cfgPs == null) throw new IllegalArgumentException("object is null");
		this.probe = probe;
		this.id = id;
		this.cfgSpace = cfgS;
		this.cfgPathSensor = cfgPs;
	}
	public final ISpaceProbe probe;
	public final SpaceId id;
	public final SpaceCfg cfgSpace;
	public final PathSensorCfg cfgPathSensor;
}
