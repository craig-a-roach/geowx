/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.Binary;
import com.metservice.argon.json.DefaultSaxJsonFactory;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.ISaxJsonFactory;
import com.metservice.argon.json.JsonAccessor;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonNumberInteger;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;
import com.metservice.argon.json.JsonString;
import com.metservice.argon.json.SaxJsonDecoder;
import com.metservice.argon.text.ArgonNumber;
import com.metservice.cobalt.CobaltParameter;

/**
 * @author roach
 */
class ParameterDecoder extends ResourceFolderDecoder<ParameterTable> {

	public static final String PName_quantities = "quantities";
	public static final String PName_quantity = "quantity";
	public static final String PName_sid = "sid";
	public static final String PName_number = "number";
	public static final String PName_unit = "unit";
	public static final String PName_description = "description";

	private static final ISaxJsonFactory QuantitiesFactory = newQuantitiesFactory();

	private static ISaxJsonFactory newQuantitiesFactory() {
		final DefaultSaxJsonFactory f = new DefaultSaxJsonFactory() {

			@Override
			public IJsonNative createAttribute(String aname, String zValue, String ename)
					throws NumberFormatException, ArgonFormatException, JsonSchemaException {
				final String ztwValue = zValue.trim();
				final boolean isEmpty = ztwValue.length() == 0;
				if (aname.equals(PName_sid)) {
					if (isEmpty) throw new JsonSchemaException("Empty");
					return JsonString.newInstance(ztwValue);
				}
				if (aname.equals(PName_number)) {
					if (isEmpty) throw new JsonSchemaException("Empty");
					return JsonNumberInteger.newInstance(Integer.parseInt(ztwValue));
				}
				if (aname.equals(PName_unit) || aname.equals(PName_description)) return JsonString.newInstance(ztwValue);
				return null;
			}

		};
		f.declareRequiredAttributes(PName_quantity, PName_sid, PName_number, PName_unit);
		return f;
	}

	private static String resourceTableKeyG1(short centre, short subCentre, short table) {
		final StringBuilder rkb = new StringBuilder();
		rkb.append(CKrypton.FilePrefix_GRIB1);
		if (centre >= 1 && centre <= 254) {
			rkb.append(CKrypton.FilePrefix_Centre);
			rkb.append(ArgonNumber.intToDec3(centre));
		}
		if (subCentre >= 1 && subCentre <= 254) {
			rkb.append(CKrypton.FilePrefix_SubCentre);
			rkb.append(ArgonNumber.intToDec3(subCentre));
		}
		if (table >= 1 && table <= 254) {
			rkb.append(CKrypton.FilePrefix_Table);
			rkb.append(ArgonNumber.intToDec3(table));
		}
		return rkb.toString();
	}

	private static String resourceTableKeyG2(short discipline, short category) {
		final StringBuilder rkb = new StringBuilder();
		rkb.append(CKrypton.FilePrefix_GRIB2);
		if (discipline >= 0 && discipline <= 254) {
			rkb.append(CKrypton.FilePrefix_Discipline);
			rkb.append(ArgonNumber.intToDec3(discipline));
		}
		if (category >= 0 && category <= 254) {
			rkb.append(CKrypton.FilePrefix_Category);
			rkb.append(ArgonNumber.intToDec3(category));
		}
		return rkb.toString();
	}

	private boolean parseResourceTable(ParameterTable dst, JsonObject root) {
		assert root != null;
		try {
			final JsonAccessor aQuantities = root.accessor(PName_quantities);
			if (!aQuantities.isDefinedNonNull()) return false;
			final JsonObject quantities = aQuantities.datumObject();
			final JsonAccessor aQuantityArray = quantities.accessor(PName_quantity);
			if (!aQuantityArray.isDefinedNonNull()) return false;
			final JsonArray quantityArray = aQuantityArray.datumArray();
			final int qcount = quantityArray.size();
			for (int i = 0; i < qcount; i++) {
				final JsonObject quantity = quantityArray.accessor(i).datumObject();
				final int parameter = quantity.accessor(PName_number).datumInteger();
				final String qccId = quantity.accessor(PName_sid).datumQtwString();
				final String zccUnit = quantity.accessor(PName_unit).datumZtwString();
				final String zDescription = quantity.accessor(PName_description).datumZtwString();
				final CobaltParameter p = new CobaltParameter(qccId, zccUnit, zDescription);
				dst.put(parameter, p);
			}
			return true;
		} catch (final JsonSchemaException ex) {
			probe.parameterResourceParse(dst.qccKey(), ex);
		}
		return false;
	}

	@Override
	ParameterTable newTable(String qccTableKey) {
		return new ParameterTable(qccTableKey);
	}

	@Override
	protected boolean parseTableSource(ParameterTable dst, Binary source) {
		assert source != null;
		final JsonObject root = SaxJsonDecoder.parseCompact(source, QuantitiesFactory);
		final JsonObject oRootError = SaxJsonDecoder.getError(root);
		if (oRootError == null) return parseResourceTable(dst, root);
		probe.parameterResourceParse(dst.qccKey(), oRootError);
		return false;
	}

	public CobaltParameter selectG1(String source, short centre, short subCentre, short table, short parameter)
			throws KryptonTableException, KryptonCodeException {
		final String qccTableKey = resourceTableKeyG1(centre, subCentre, table);
		final ParameterTable ptable = loadResourceTable(qccTableKey);
		return ptable.select(probe, source, parameter);
	}

	public CobaltParameter selectG2(String source, short discipline, short category, short parameter)
			throws KryptonTableException, KryptonCodeException {
		final String qccTableKey = resourceTableKeyG2(discipline, category);
		final ParameterTable ptable = loadResourceTable(qccTableKey);
		return ptable.select(probe, source, parameter);
	}

	public ParameterDecoder(IKryptonProbe probe) {
		super(probe, "parameter", "decode/parameter/", CKrypton.FileSuffix_XML);
	}
}
