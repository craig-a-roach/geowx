/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;


/**
 * @author roach
 */
class DemandTask implements IBerylliumMirrorTask {

	@Override
	public String toString() {
		return qccPath;
	}

	public DemandTask(String qccPath) {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		this.qccPath = qccPath;
	}
	public final String qccPath;
}
