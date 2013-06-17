/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class ParameterValue {

	public AccessorAngle angle() {
		return new AccessorAngle(this);
	}

	public AccessorLinear linear() {
		return new AccessorLinear(this);
	}

	@Override
	public String toString() {
		return definition + "=" + value;
	}

	public ParameterValue(ParameterDefinition def, double value) {
		if (def == null) throw new IllegalArgumentException("object is null");
		this.definition = def;
		this.value = value;
	}
	public final ParameterDefinition definition;
	public final double value;
}
