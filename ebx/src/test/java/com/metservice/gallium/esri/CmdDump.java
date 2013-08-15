/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.metservice.gallium.GalliumBoundingBoxD;
import com.metservice.gallium.GalliumPointD;
import com.metservice.gallium.esri.GalliumShapefileFormatException;
import com.metservice.gallium.esri.GalliumShapefileHeader;
import com.metservice.gallium.esri.GalliumShapefileReadException;
import com.metservice.gallium.esri.IGalliumShapefileHandler;
import com.metservice.gallium.esri.IGalliumShapefileMultiPoint;
import com.metservice.gallium.esri.IGalliumShapefilePolyLine;
import com.metservice.gallium.esri.IGalliumShapefilePolyLinePart;
import com.metservice.gallium.esri.IGalliumShapefilePolygon;
import com.metservice.gallium.esri.IGalliumShapefilePolygonPart;
import com.metservice.gallium.esri.ShapefileReader;

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
		final ShapefileReader r = new ShapefileReader(path);
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
		public IGalliumShapefileMultiPoint createMultiPoint(int recNo, GalliumBoundingBoxD box, int pointCount) {
			return null;
		}

		@Override
		public IGalliumShapefilePolygon createPolygon(int recNo, GalliumBoundingBoxD box, int partCount, int pointCount) {
			System.out.println("Polygon record " + recNo + " :" + box);
			return new Polygon();
		}

		@Override
		public IGalliumShapefilePolyLine createPolyLine(int recNo, GalliumBoundingBoxD box, int partCount, int pointCount) {
			System.out.println("PolyLine record " + recNo + " :" + box);
			return new PolyLine();
		}

		@Override
		public void point(int recNo, GalliumPointD pt) {
			System.out.println("Point " + recNo + ":" + pt);
		}

		public Handler() {
		}
	}

	private static class Polygon implements IGalliumShapefilePolygon {

		@Override
		public IGalliumShapefilePolygonPart createPart(int partIndex, int pointCount) {
			System.out.println("Part " + partIndex + ": " + pointCount + " vertices");
			return new PolygonPart();
		}
	}

	private static class PolygonPart implements IGalliumShapefilePolygonPart {

		@Override
		public void close() {
			System.out.println("Part CLOSE");
		}

		@Override
		public void vertex(int vertexIndex, GalliumPointD pt) {
			System.out.println("Part vertex " + vertexIndex + ":" + pt);
		}
	}

	private static class PolyLine implements IGalliumShapefilePolyLine {

		@Override
		public IGalliumShapefilePolyLinePart createPart(int partIndex, int pointCount) {
			System.out.println("Part " + partIndex + ": " + pointCount + " vertices");
			return new PolyLinePart();
		}
	}

	private static class PolyLinePart implements IGalliumShapefilePolyLinePart {

		@Override
		public void end() {
			System.out.println("Part END");
		}

		@Override
		public void vertex(int vertexIndex, GalliumPointD pt) {
			System.out.println("Part vertex " + vertexIndex + ":" + pt);
		}
	}
}
