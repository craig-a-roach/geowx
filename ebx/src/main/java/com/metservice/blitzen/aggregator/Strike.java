/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

/**
 * @author roach
 */
class Strike {

	@Override
	public String toString() {
		return "y" + y + ", x" + x + " @" + t + ":" + qty + "," + cat;
	}

	public Strike(long t, float y, float x, float qty, int cat) {
		this.t = t;
		this.y = y;
		this.x = x;
		this.qty = qty;
		this.cat = cat;
	}
	public final long t;
	public final float y;
	public final float x;
	public final float qty;
	public final int cat;
}
