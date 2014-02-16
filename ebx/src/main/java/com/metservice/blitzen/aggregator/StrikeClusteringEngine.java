/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import java.util.List;

/**
 * @author roach
 */
class StrikeClusteringEngine {

	public void solve(List<Strike> strikeList) {
		if (strikeList == null) throw new IllegalArgumentException("object is null");
		final int strikeCount = strikeList.size();
		if (strikeCount == 0) return;
		final Strike[] strikes = strikeList.toArray(new Strike[strikeCount]);

		final StrikeTree tree = StrikeTree.newInstance(strikes);

	}

}
