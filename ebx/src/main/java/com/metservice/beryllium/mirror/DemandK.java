/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import com.metservice.argon.Ds;

class DemandK extends DownloadK {

	@Override
	protected String trackerType() {
		return "Demand";
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("task", task);
		ds.a("response", getResponse());
		return ds.s();
	}

	public DemandK(IBerylliumMirrorProbe probe, DemandTask task) {
		super(probe);
		if (task == null) throw new IllegalArgumentException("object is null");
		this.task = task;
	}
	public final DemandTask task;
}