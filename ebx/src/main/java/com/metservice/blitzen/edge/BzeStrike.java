/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
public class BzeStrike {

	@Override
	public String toString() {
		return "y" + y + ", x" + x + " @" + t + ":" + qty + "," + type;
	}

	public BzeStrike(long t, float y, float x, float qty, BzeStrikeType type) {
		this.t = t;
		this.y = y;
		this.x = x;
		this.qty = qty;
		this.type = type;
	}
	public final long t;
	public final float y;
	public final float x;
	public final float qty;
	public final BzeStrikeType type;
}
