/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium;

/**
 * @author roach
 */
public interface IGalliumContourable {

	public float datum(int y, int x);

	public float latitude(int y);

	public float longitude(int x);

	public int pointCountX();

	public int pointCountY();

}
