/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
interface ISectionGD1ThinGridReader extends ISectionGD1Reader {

	public double DX();

	public double DY();

	public double longitude2();

	public int scanningMode();
}
