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
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(title);
		if (oAuthority != null) {
			sb.append(" (");
			sb.append(oAuthority);
			sb.append(")");
		}
		return sb.toString();
	}

	protected AbstractProjectionFactory() {
	}
	protected Authority oAuthority;
	protected Title title = TitlePending;
}
