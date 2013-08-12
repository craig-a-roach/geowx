/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.shapefile;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.metservice.gallium.GalliumBoundingBoxD;
import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
public class CmdDump {

	/*
	 * "C:\Program Files\Java\jre7\bin\java.exe" -classpath C:\Users\roach\git\geowx\ebx\target\classes;C:\Users
	 * \roach\git\geowx\ebx\target\test-classes com.metservice.gallium.shapefile.CmdDump
	 * C:\Users\roach\shp_samples\GSHHS_i_L1. shp
	 */

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Missing path");
			return;
		}
		final Path path = Paths.get(args[0]);
		final ShapeReader r = new ShapeReader(path);
		final Handler h = new Handler();
		try {
			r.scan(h);
		} catch (GalliumShapefileFormatException | GalliumShapefileReadException ex) {
			ex.printStackTrace();
		}
	}

	private static class Handler implements IGalliumShapefileHandler {

		@Override
		public boolean acceptFile(Path path, long bcPayload) {
			return true;
		}

		@Override
		public boolean acceptHeader(GalliumShapefileHeader header) {
			System.out.println(header);
			return true;
		}

		@Override
		public boolean acceptPolygon(int recNo, GalliumBoundingBoxD box, int partCount, int pointCount) {
			System.out.println("Accept polygon " + recNo + " box " + box + " parts=" + partCount + ", points=" + pointCount);
			return true;
		}

		@Override
		public void point(int recNo, GalliumPointD pt) {
			System.out.println("Point " + recNo + ":" + pt);
		}

		@Override
		public void polygonClose(int recNo, int partIndex) {
			System.out.println("Rec " + recNo + " part " + partIndex + " CLOSE");
		}

		@Override
		public void polygonVertex(int recNo, int partIndex, int vertexIndex, GalliumPointD pt) {
			System.out.println("Rec " + recNo + " part " + partIndex + " point " + vertexIndex + ":" + pt);
		}

		public Handler() {
		}
	}

}
