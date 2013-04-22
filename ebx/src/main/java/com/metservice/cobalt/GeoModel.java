/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class GeoModel {

	public void addTo(KmlDocument doc) {
		if (doc == null) throw new IllegalArgumentException("object is null");
		final List<RFolder> xlRFoldersAsc = new ArrayList<RFolder>(xmR_Folder.values());
		Collections.sort(xlRFoldersAsc);
		for (final RFolder rf : xlRFoldersAsc) {
			doc.beginFolder();
			rf.addTo(doc);
			doc.endFolder();
		}
	}

	public int placemarkCount() {
		return placemarkCount;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("resolutionFolder", xmR_Folder);
		return ds.s();
	}

	private static Map<CoordinateK, CobaltReducer> newRag_Reducer(CobaltNCube src)
			throws CobaltDimensionException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final CobaltDimensionName[] keyNames = { CobaltDimensionName.Resolution, CobaltDimensionName.Analysis,
				CobaltDimensionName.Geography };
		final Map<CoordinateK, CobaltReducer> xmRAG_Reducer = new HashMap<CoordinateK, CobaltReducer>(128);
		for (final CobaltRecord record : src) {
			final CobaltResolution resolution = record.resolution();
			final CobaltAnalysis analysis = record.analysis();
			final ICobaltGeography geography = record.geography();
			final CoordinateK rag = new CoordinateK(resolution, analysis, geography);
			CobaltReducer vReducer = xmRAG_Reducer.get(rag);
			if (vReducer == null) {
				vReducer = new CobaltReducer();
				xmRAG_Reducer.put(rag, vReducer);
			}
			final CobaltRecord neoRecord = record.subtract(keyNames);
			vReducer.add(neoRecord);
		}
		return xmRAG_Reducer;
	}

	public static GeoModel newInstance(CobaltNCube src)
			throws CobaltDimensionException {
		if (src == null) throw new IllegalArgumentException("object is null");

		final Map<CoordinateK, CobaltReducer> xmRAG_Reducer = newRag_Reducer(src);
		final int ragCount = xmRAG_Reducer.size();
		final Map<CobaltResolution, RFolder> xmR_Folder = new HashMap<CobaltResolution, RFolder>(8);
		for (final Map.Entry<CoordinateK, CobaltReducer> e : xmRAG_Reducer.entrySet()) {
			final CoordinateK rag = e.getKey();
			final CobaltResolution resolution = rag.coordinate(0, CobaltResolution.class);
			final CobaltAnalysis analysis = rag.coordinate(1, CobaltAnalysis.class);
			final ICobaltGeography grid = rag.coordinate(2, ICobaltGeography.class);
			final CobaltReducer placemarkReducer = e.getValue();
			final CobaltNCube placemarkNCube = placemarkReducer.reduce();
			RFolder vRFolder = xmR_Folder.get(resolution);
			if (vRFolder == null) {
				vRFolder = new RFolder(resolution);
				xmR_Folder.put(resolution, vRFolder);
			}
			vRFolder.put(analysis, grid, placemarkNCube);
		}
		return new GeoModel(xmR_Folder, ragCount);
	}

	private GeoModel(Map<CobaltResolution, RFolder> xmR_Folder, int placemarkCount) {
		assert xmR_Folder != null;
		this.xmR_Folder = xmR_Folder;
		this.placemarkCount = placemarkCount;

	}
	private final Map<CobaltResolution, RFolder> xmR_Folder;
	private final int placemarkCount;

	static class AFolder implements Comparable<AFolder> {

		public void add(ICobaltGeography grid, CobaltNCube placemarkNCube) {
			assert grid != null;
			xlPlacemarks.add(new Placemark(grid, placemarkNCube));
		}

		public void addTo(KmlDocument doc) {
			final KmlFeatureName fn = new KmlFeatureName();
			analysis.addTo(fn);
			doc.add(fn);
			final List<Placemark> xlPlacemarksAsc = new ArrayList<Placemark>(xlPlacemarks);
			Collections.sort(xlPlacemarksAsc);
			for (final Placemark pm : xlPlacemarksAsc) {
				doc.beginPlacemark();
				pm.addTo(doc);
				doc.endPlacemark();
			}
		}

		@Override
		public int compareTo(AFolder rhs) {
			return analysis.compareTo(rhs.analysis);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o(getClass());
			ds.a("analysis", analysis);
			ds.a("placemarks", xlPlacemarks);
			return ds.s();
		}

		AFolder(CobaltAnalysis analysis) {
			assert analysis != null;
			this.analysis = analysis;
		}
		private final CobaltAnalysis analysis;
		private final List<Placemark> xlPlacemarks = new ArrayList<Placemark>(32);
	}

	static class Placemark implements Comparable<Placemark> {

		public void addTo(KmlDocument doc) {
			final KmlGeometry geo = KmlGeometry.newClampedToGround();
			grid.addTo(geo);
			final String geoName = geo.toName();
			final KmlFeatureName fn = new KmlFeatureName();
			fn.addText(geoName);
			final boolean isColumnSense = doc.isColumnSenseBalloon();
			final KmlFeatureDescription fd = ncube.newKmlFeatureDescription(isColumnSense);
			final int recordCount = ncube.recordCount();
			final String snippet = "Records=" + recordCount;
			doc.add(fn);
			doc.add(fd);
			doc.addSnippet(snippet, 1);
			doc.addStyle();
			doc.add(geo);
		}

		@Override
		public int compareTo(Placemark rhs) {
			return grid.compareTo(rhs.grid);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o(getClass());
			ds.a("grid", grid);
			ds.a("ncube", ncube);
			return ds.s();
		}

		Placemark(ICobaltGeography grid, CobaltNCube ncube) {
			assert grid != null;
			this.grid = grid;
			this.ncube = ncube;
		}
		private final ICobaltGeography grid;
		private final CobaltNCube ncube;
	}

	static class RFolder implements Comparable<RFolder> {

		public void addTo(KmlDocument doc) {
			final KmlFeatureName fn = new KmlFeatureName();
			resolution.addTo(fn);
			doc.add(fn);
			final List<AFolder> xlAFoldersAsc = new ArrayList<AFolder>(xmA_Folder.values());
			Collections.sort(xlAFoldersAsc);
			for (final AFolder af : xlAFoldersAsc) {
				doc.beginFolder();
				af.addTo(doc);
				doc.endFolder();
			}
		}

		@Override
		public int compareTo(RFolder rhs) {
			return resolution.compareTo(rhs.resolution);
		}

		public void put(CobaltAnalysis analysis, ICobaltGeography grid, CobaltNCube placemarkNCube) {
			assert analysis != null;
			assert grid != null;
			assert placemarkNCube != null;
			AFolder vAFolder = xmA_Folder.get(analysis);
			if (vAFolder == null) {
				vAFolder = new AFolder(analysis);
				xmA_Folder.put(analysis, vAFolder);
			}
			vAFolder.add(grid, placemarkNCube);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o(getClass());
			ds.a("resolution", resolution);
			ds.a("analysisFolder", xmA_Folder);
			return ds.s();
		}

		RFolder(CobaltResolution resolution) {
			assert resolution != null;
			this.resolution = resolution;
			xmA_Folder = new HashMap<CobaltAnalysis, GeoModel.AFolder>(16);
		}
		private final CobaltResolution resolution;
		private final Map<CobaltAnalysis, AFolder> xmA_Folder;
	}

}
