/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
abstract class AbstractProjectionCylindrical extends AbstractProjection {

	@Override
	public boolean isRectilinear() {
		return true;
	}

	protected AbstractProjectionCylindrical(Authority oAuthority, Title title, ArgCylindrical args) {
		super(oAuthority, title);
		if (args == null) throw new IllegalArgumentException("object is null");
		this.args = args;
	}
	protected final ArgCylindrical args;
}
