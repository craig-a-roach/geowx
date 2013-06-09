/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
abstract class AbstractProjectionFactory implements IProjectionFactory {

	private static final Title TitlePending = Title.newInstance("pending");
	private static final int VariantPending = -1;

	@Override
	public void setAuthority(Authority a) {
		if (a == null) throw new IllegalArgumentException("object is null");
		oAuthority = a;
	}

	@Override
	public void setTitle(Title t) {
		if (t == null) throw new IllegalArgumentException("object is null");
		title = t;
	}

	@Override
	public void setVariant(int id) {
		if (id < 0) throw new IllegalArgumentException("invalid variant>" + id + "<");
		id = variantId;
	}

	protected AbstractProjectionFactory() {
	}
	protected Authority oAuthority;
	protected Title title = TitlePending;
	protected int variantId = VariantPending;
}
