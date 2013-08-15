/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import java.nio.file.Path;

import com.metservice.gallium.GalliumBoundingBoxD;
import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
public interface IGalliumShapefileHandler {

	public boolean acceptFile(Path path, long bcPayload);

	public boolean acceptHeader(GalliumShapefileHeader header);

	public IGalliumShapefileMultiPoint createMultiPoint(int recNo, GalliumBoundingBoxD box, int pointCount);

	public IGalliumShapefilePolygon createPolygon(int recNo, GalliumBoundingBoxD box, int partCount, int pointCount);

	public IGalliumShapefilePolyLine createPolyLine(int recNo, GalliumBoundingBoxD box, int partCount, int pointCount);

	public void point(int recNo, GalliumPointD pt);
}
