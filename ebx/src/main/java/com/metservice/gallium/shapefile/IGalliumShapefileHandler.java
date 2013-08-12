/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.shapefile;

import java.nio.file.Path;

import com.metservice.gallium.GalliumBoundingBoxD;
import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
public interface IGalliumShapefileHandler {

	public boolean acceptFile(Path path, long bcPayload);

	public boolean acceptHeader(GalliumShapefileHeader header);

	public boolean acceptPolygon(int recNo, GalliumBoundingBoxD box, int partCount, int pointCount);

	public void point(int recNo, GalliumPointD pt);

	public void polygonClose(int recNo, int partIndex);

	public void polygonVertex(int recNo, int partIndex, int vertexIndex, GalliumPointD pt);
}
