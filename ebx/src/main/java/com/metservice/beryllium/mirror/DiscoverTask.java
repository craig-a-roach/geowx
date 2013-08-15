/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

/**
 * @author roach
 */
class DiscoverTask implements IBerylliumMirrorTask {

	public static final DiscoverTask Instance = new DiscoverTask();

	private DiscoverTask() {
	}
}