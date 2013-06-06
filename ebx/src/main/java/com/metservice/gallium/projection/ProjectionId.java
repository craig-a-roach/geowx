/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class ProjectionId {

	public static ProjectionId newEpsg(int code, String title) {
		return new ProjectionId(Authority.newEPSG(code), Title.newInstance(title));
	}

	public static ProjectionId newInstance(String title, Authority oAuthority) {
		return new ProjectionId(oAuthority, Title.newInstance(title));
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(title);
		if (oAuthority != null) {
			sb.append(" authority ").append(oAuthority);
		}
		return sb.toString();
	}

	private ProjectionId(Authority oAuthority, Title title) {
		assert title != null;
		this.oAuthority = oAuthority;
		this.title = title;
	}
	public final Authority oAuthority;
	public final Title title;
}
