/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonStreamReadException;

/**
 * @author roach
 */
public class TestUnit1Sax {

	@Test
	public void t50()
			throws ArgonStreamReadException {
		String src = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		src += "<quantities description=\"ecmwf_2_v128.tab\">\n";
		src += "<quantity sid=\"STRF\" number=\"1\" unit=\"m2 s-1\" fid=\"Stream_function\" description=\"Stream function\"/>\n";
		src += "<quantity sid=\"VPOT\" number=\"2\" unit=\"m2 s-1\" fid=\"Velocity_potential\"><description>Velocity potential</description></quantity>\n";
		src += "<quantity sid=\"PT\" number=\"3\" unit=\"K\" fid=\"Potential_temperature\">Potential temperature</quantity>\n";
		src += "</quantities>";

		String dst = "{quantities:{description:\"ecmwf_2_v128.tab\",quantity:[";
		dst += "{description:\"Stream function\",number:1,sid:\"STRF\",unit:\"m2 s-1\"},";
		dst += "{description:\"Velocity potential\",number:2,sid:\"VPOT\",unit:\"m2 s-1\"},";
		dst += "{description:\"Potential temperature\",number:3,sid:\"PT\",unit:\"K\"}";
		dst += "]}}";

		final DefaultSaxJsonFactory factory = new DefaultSaxJsonFactory() {

			@Override
			public IJsonNative createAttribute(String aname, String zValue, String ename)
					throws NumberFormatException, ArgonFormatException, JsonSchemaException {
				final String ztwValue = zValue.trim();
				if (aname.equals("sid")) {
					if (ztwValue.length() == 0) throw new JsonSchemaException("Empty");
					return JsonString.newInstance(ztwValue);
				}
				if (aname.equals("fid")) return null;
				if (aname.equals("number")) {
					if (ztwValue.length() == 0) throw new JsonSchemaException("Empty");
					return JsonNumberInteger.newInstance(Integer.parseInt(ztwValue));
				}
				return super.createAttribute(aname, zValue, ename);
			}
		};
		factory.declareSimpleText("description");
		factory.declareSingleton("description", "quantity");
		factory.declareTextAttributeName("quantity", "description");

		final JsonObject jx = SaxJsonDecoder.parseCompact(src, factory);
		// System.out.println(JsonEncoder.Debug.encode(jx));
		final String jxe = JsonEncoder.Default.encode(jx);
		Assert.assertEquals(dst, jxe);
	}

	@Test
	public void t60() {
		String src = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		src += "<doc>\n";
		src += "<line>Line 1</line><line/><line>Line 2</line><line>  </line><line>Line 3</line>\n";
		src += "</doc>";

		final DefaultSaxJsonFactory factory = new DefaultSaxJsonFactory();
		factory.declareSimpleText("line");

		final String dst = "{doc:{line:[\"Line 1\",\"Line 2\",\"Line 3\"]}}";

		final JsonObject jx = SaxJsonDecoder.parseCompact(src, factory);
		// System.out.println(JsonEncoder.Debug.encode(jx));
		final String jxe = JsonEncoder.Default.encode(jx);
		Assert.assertEquals(dst, jxe);
	}

	@Test
	public void t70() {
		String src = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		src += "<doc>\n";
		src += "<qty id=\"a\" title=\"alpha\" cat=\"T\"><uom id=\"K\"/></qty>\n";
		src += "<qty xid=\"b\" title=\"beta\" cat=\"P\"><uom id=\"hPa\"/></qty>\n";
		src += "<qty title=\"charlie\" id=\"c\"  cat=\"T\"><uom id=\"K\"/></qty>\n";
		src += "</doc>";

		final DefaultSaxJsonFactory factory = new DefaultSaxJsonFactory();
		factory.declareSingleton("uom", "qty");
		final String[] reqd = { "id", "cat" };
		factory.declareRequiredAttributes("qty", reqd);

		final JsonObject jx = SaxJsonDecoder.parseCompact(src, factory);
		// System.out.println(JsonEncoder.Debug.encode(jx));
		final JsonObject jxError = SaxJsonDecoder.getError(jx);
		Assert.assertNotNull("detected error", jxError);
		System.out.println("Good Exception>>" + JsonEncoder.Debug.encode(jxError));
		try {
			final String charlie = jx.accessor("doc").datumObject().accessor("qty").datumArray().accessor(1).datumObject()
					.accessor("id").datumQtwString();
			Assert.assertEquals("c", charlie);
		} catch (final JsonSchemaException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t80() {
		String src = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		src += "<doc xmlns=\"WMO\" xmlns:iso=\"ISO-UNIT\" xmlns:ncep=\"NCEP-GRIB\">\n";
		src += "<qty id=\"a\" title=\"alpha\" ncep:cat=\"T\"><iso:uom id=\"K\"/></qty>\n";
		src += "<qty title=\"charlie\" id=\"c\"  ncep:cat=\"T\"><iso:uom id=\"K\"/></qty>\n";
		src += "</doc>";

		final String dst = "{wmo_doc:{wmo_qty:[{id:\"a\",iso_uom:{id:\"K\"},nc_cat:\"T\",title:\"alpha\"},{id:\"c\",iso_uom:{id:\"K\"},nc_cat:\"T\",title:\"charlie\"}]}}";

		final DefaultSaxJsonFactory factory = new DefaultSaxJsonFactory();
		factory.declareUriPrefix("WMO", "wmo_");
		factory.declareUriPrefix("ISO-UNIT", "iso_");
		factory.declareUriPrefix("NCEP-GRIB", "nc_");
		factory.declareSingleton("iso_uom", "wmo_qty");
		factory.declareRequiredAttributes("wmo_qty", "id", "nc_cat");

		final JsonObject jx = SaxJsonDecoder.parseCompact(src, factory);
		// System.out.println(JsonEncoder.Debug.encode(jx));
		final String jxe = JsonEncoder.Default.encode(jx);
		Assert.assertEquals(dst, jxe);
	}

	@Test
	public void t90() {
		// Not well supported by compact
		String src = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		src += "<doc>\n";
		src += "<line>abc<b>d</b>ef</line>\n";
		src += "<line><b>x</b>y<b>z</b></line>\n";
		src += "</doc>";

		final DefaultSaxJsonFactory factory = new DefaultSaxJsonFactory();
		final JsonObject jx = SaxJsonDecoder.parseCompact(src, factory);
		// System.out.println(JsonEncoder.Debug.encode(jx));
		Assert.assertNull("detected error", SaxJsonDecoder.getError(jx));
	}
}
