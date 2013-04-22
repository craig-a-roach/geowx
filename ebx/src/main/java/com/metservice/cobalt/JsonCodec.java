/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
class JsonCodec {

	public static final String PName_shapes = "shapes";
	public static final String PName_coordinate = "_c";
	public static final String CoordGrid = "grid";
	public static final String CoordGrid_LatitudeLongitude = CoordGrid + "LL";
	public static final String CoordGrid_Mercator = CoordGrid + "MC";
	public static final String CoordPrognosis = "prog";
	public static final String CoordPrognosis_Point = CoordPrognosis + "P";
	public static final String CoordPrognosis_Aggregate = CoordPrognosis + "A";
	public static final String CoordParameter = "parm";
	public static final String CoordSurface = "surf";
	public static final String CoordSurface_Zero = CoordSurface + "Z";
	public static final String CoordSurface_Unary = CoordSurface + "U";
	public static final String CoordSurface_TopBottom = CoordSurface + "TB";
	public static final String CoordMember = "mbr";
	public static final String CoordAnalysis = "analysis";
	public static final String CoordResolution = "res";

	private static ICobaltCoordinate newCoordAnalysis(String qtwCoord, JsonObject src)
			throws JsonSchemaException {
		return CobaltAnalysis.newInstance(src);
	}

	private static ICobaltCoordinate newCoordGrid(String qtwCoord, JsonObject src)
			throws JsonSchemaException {
		if (qtwCoord.equals(CoordGrid_LatitudeLongitude)) return CobaltGeoLatitudeLongitude.newInstance(src);
		if (qtwCoord.equals(CoordGrid_Mercator)) return CobaltGeoMercator.newInstance(src);
		throw new JsonSchemaException("Unsupported grid type '" + qtwCoord + "' in " + src);
	}

	private static ICobaltCoordinate newCoordMember(String qtwCoord, JsonObject src)
			throws JsonSchemaException {
		return CobaltMember.newInstance(src);
	}

	private static ICobaltCoordinate newCoordParameter(String qtwCoord, JsonObject src)
			throws JsonSchemaException {
		return CobaltParameter.newInstance(src);
	}

	private static ICobaltCoordinate newCoordPrognosis(String qtwCoord, JsonObject src)
			throws JsonSchemaException {
		if (qtwCoord.equals(CoordPrognosis_Point)) return CobaltPrognosisPoint.newInstance(src);
		if (qtwCoord.equals(CoordPrognosis_Aggregate)) return CobaltPrognosisAggregate.newInstance(src);
		throw new JsonSchemaException("Unsupported prognosis type '" + qtwCoord + "' in " + src);
	}

	private static ICobaltCoordinate newCoordResolution(String qtwCoord, JsonObject src)
			throws JsonSchemaException {
		return CobaltResolution.newInstance(src);
	}

	private static ICobaltCoordinate newCoordSurface(String qtwCoord, JsonObject src)
			throws JsonSchemaException {
		if (qtwCoord.equals(CoordSurface_Zero)) return CobaltSurfaceZero.newInstance(src);
		if (qtwCoord.equals(CoordSurface_Unary)) return CobaltSurfaceUnary.newInstance(src);
		if (qtwCoord.equals(CoordSurface_TopBottom)) return CobaltSurfaceTopBottom.newInstance(src);
		throw new JsonSchemaException("Unsupported grid type '" + qtwCoord + "' in " + src);
	}

	private static IJsonNative newJsonFromComposite(Composite composite) {
		final CobaltSequence[] xptSequenceAscDimSet = composite.xptSequenceAscDimSet();
		final int cardinality = xptSequenceAscDimSet.length;
		final JsonArray array = JsonArray.newMutable(cardinality);
		for (int i = 0; i < cardinality; i++) {
			final CobaltSequence sequence = xptSequenceAscDimSet[i];
			array.add(newJsonFromSequence(sequence));
		}
		return array;
	}

	private static JsonObject newJsonFromCoordinate(ICobaltCoordinate coord) {
		assert coord != null;
		final JsonObject dst = JsonObject.newMutable(8);
		final String qtwCoord = qtwCoord(coord);
		dst.putString(PName_coordinate, qtwCoord);
		coord.saveTo(dst);
		return dst;
	}

	private static JsonArray newJsonFromSequence(CobaltSequence sequence) {
		final ICobaltProduct[] xptProductsAsc = sequence.xptProductsAsc();
		final int productCount = xptProductsAsc.length;
		final JsonArray array = JsonArray.newMutable(productCount);
		for (int i = 0; i < productCount; i++) {
			final ICobaltProduct product = xptProductsAsc[i];
			array.add(newJson(product));
		}
		return array;
	}

	private static ICobaltProduct newProductFromArray(JsonArray src)
			throws JsonSchemaException {
		assert src != null;
		final int cardinality = src.size();
		final List<SequenceBuilder> xlBuilders = new ArrayList<SequenceBuilder>(cardinality);
		for (int i = 0; i < cardinality; i++) {
			final JsonArray srcSequence = src.accessor(i).datumArray();
			xlBuilders.add(newSequenceBuilder(srcSequence));
		}
		return Composite.newInstance(xlBuilders);
	}

	private static ICobaltProduct newProductFromObject(JsonObject src)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final String qtwCoord = src.accessor(PName_coordinate).datumQtwString();
		if (qtwCoord.startsWith(CoordPrognosis)) return newCoordPrognosis(qtwCoord, src);
		if (qtwCoord.startsWith(CoordGrid)) return newCoordGrid(qtwCoord, src);
		if (qtwCoord.startsWith(CoordParameter)) return newCoordParameter(qtwCoord, src);
		if (qtwCoord.startsWith(CoordSurface)) return newCoordSurface(qtwCoord, src);
		if (qtwCoord.startsWith(CoordMember)) return newCoordMember(qtwCoord, src);
		if (qtwCoord.startsWith(CoordAnalysis)) return newCoordAnalysis(qtwCoord, src);
		if (qtwCoord.startsWith(CoordResolution)) return newCoordResolution(qtwCoord, src);
		throw new JsonSchemaException("Unsupported coordinate type '" + qtwCoord + "' in " + src);
	}

	private static SequenceBuilder newSequenceBuilder(JsonArray src)
			throws JsonSchemaException {
		assert src != null;
		final int count = src.size();
		final SequenceBuilder builder = new SequenceBuilder(count);
		for (int i = 0; i < count; i++) {
			final ICobaltProduct product = newProduct(src.get(i));
			builder.add(product);
		}
		return builder;
	}

	private static String qtwCoord(ICobaltCoordinate coord) {
		if (coord instanceof CobaltPrognosisPoint) return CoordPrognosis_Point;
		if (coord instanceof CobaltPrognosisAggregate) return CoordPrognosis_Aggregate;
		if (coord instanceof CobaltGeoLatitudeLongitude) return CoordGrid_LatitudeLongitude;
		if (coord instanceof CobaltGeoMercator) return CoordGrid_Mercator;
		if (coord instanceof CobaltParameter) return CoordParameter;
		if (coord instanceof CobaltSurfaceZero) return CoordSurface_Zero;
		if (coord instanceof CobaltSurfaceUnary) return CoordSurface_Unary;
		if (coord instanceof CobaltSurfaceTopBottom) return CoordSurface_TopBottom;
		if (coord instanceof CobaltMember) return CoordMember;
		if (coord instanceof CobaltAnalysis) return CoordAnalysis;
		if (coord instanceof CobaltResolution) return CoordResolution;
		throw new IllegalArgumentException("invalid coord>" + coord + "<");
	}

	public static JsonObject newJson(CobaltNCube ncube) {
		if (ncube == null) throw new IllegalArgumentException("object is null");
		final CobaltSequence[] xptShapeAsc = ncube.xptShapeAsc();
		final int shapeCount = xptShapeAsc.length;
		final JsonArray array = JsonArray.newMutable(shapeCount);
		for (int i = 0; i < shapeCount; i++) {
			final CobaltSequence sequence = xptShapeAsc[i];
			array.add(newJsonFromSequence(sequence));
		}
		final Map<String, IJsonNative> map = new HashMap<String, IJsonNative>(2);
		map.put(PName_shapes, array);
		return JsonObject.newImmutable(map);
	}

	public static IJsonNative newJson(ICobaltProduct product) {
		if (product == null) throw new IllegalArgumentException("object is null");
		if (product instanceof ICobaltCoordinate) return newJsonFromCoordinate((ICobaltCoordinate) product);
		if (product instanceof Composite) return newJsonFromComposite((Composite) product);
		throw new IllegalArgumentException("unsupported product>" + product + "<");
	}

	public static CobaltNCube newNCube(JsonObject src)
			throws JsonSchemaException {
		final JsonArray shapes = src.accessor(PName_shapes).datumArray();
		final int shapeCount = shapes.size();
		final List<CobaltSequence> xlShapes = new ArrayList<CobaltSequence>(shapeCount);
		for (int i = 0; i < shapeCount; i++) {
			final JsonArray shapeSequence = shapes.accessor(i).datumArray();
			final SequenceBuilder builder = newSequenceBuilder(shapeSequence);
			xlShapes.add(CobaltSequence.newInstance(builder));
		}
		return CobaltNCube.newInstance(xlShapes);
	}

	public static ICobaltProduct newProduct(IJsonNative src)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		if (src instanceof JsonObject) return newProductFromObject((JsonObject) src);
		if (src instanceof JsonArray) return newProductFromArray((JsonArray) src);
		throw new JsonSchemaException("Expecting Object or Array, but read " + src.getJsonType() + "..." + src);
	}

	private JsonCodec() {
	}
}
