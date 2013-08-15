/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
public interface IGalliumShapefilePolyLinePart {

	public void end();

	public void vertex(int vertexIndex, GalliumPointD pt);
}
