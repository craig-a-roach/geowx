/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;


/**
 * @author roach
 */
class TitleParameter {

	public static TitleParameter newInstance(String title, double value) {
		return new TitleParameter(Title.newInstance(title), value);
	}

	@Override
	public String toString() {
		return title + "=" + value;
	}

	private TitleParameter(Title title, double value) {
		assert title != null;
		this.title = title;
		this.value = value;
	}
	public final Title title;
	public final double value;
}
