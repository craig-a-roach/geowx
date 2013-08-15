/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
class KernelCfg {

	public KernelCfg(ISpaceProbe probe, NeonSpaceId id, INeonSourceProvider nsp, NeonSpaceCfg cfg) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (id == null) throw new IllegalArgumentException("object is null");
		if (nsp == null) throw new IllegalArgumentException("object is null");
		if (cfg == null) throw new IllegalArgumentException("object is null");
		this.probe = probe;
		this.id = id;
		this.sourceProvider = nsp;
		this.cfg = cfg;
	}

	public final ISpaceProbe probe;
	public final NeonSpaceId id;
	public final INeonSourceProvider sourceProvider;
	public final NeonSpaceCfg cfg;
}
