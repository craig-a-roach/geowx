/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

/**
 * @author roach
 */
class SaveK extends UploadK {

	@Override
	protected String trackerType() {
		return "Save";
	}

	public SaveK(IBerylliumMirrorProbe probe, IBerylliumMirrorSaveTask task) {
		super(probe);
		if (task == null) throw new IllegalArgumentException("object is null");
		this.task = task;
	}
	public final IBerylliumMirrorSaveTask task;
}
