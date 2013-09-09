/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.DateFactory;
import com.metservice.argon.Elapsed;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class TestUnit1Reduce {

	CobaltAnalysis A1() {
		return new CobaltAnalysis(DateFactory.newDateConstantFromTX("20110715T1200Z"));
	}

	CobaltAnalysis A2() {
		return new CobaltAnalysis(DateFactory.newDateConstantFromTX("20110715T1800Z"));
	}

	ICobaltGeography G1() {
		return CobaltGeoLatitudeLongitude.newInstance(81.0, -53.0, 24.0, 51.0);
	}

	ICobaltGeography G2() {
		return CobaltGeoLatitudeLongitude.newInstance(65.0, -21.0, 35.0, 45.0);
	}

	ICobaltGeography G3() {
		return CobaltGeoLatitudeLongitude.newInstance(15.0, 105.0, -5.0, 125.0);
	}

	ICobaltGeography G4() {
		return CobaltGeoLatitudeLongitude.newInstance(-25.0, 160.0, -55.0, -175.0);
	}

	ICobaltGeography G5() {
		return CobaltGeoMercator.newInstance(+30.0, 0.0, -30.0, -0.5, 0.0);
	}

	CobaltMember M(int id) {
		return CobaltMember.newInstance(id);
	}

	CobaltParameter Pg_10U() {
		return new CobaltParameter("10U", "m s-1", "N10 metre U wind component");
	}

	CobaltParameter Pg_ACPCP() {
		return new CobaltParameter("ACPCP", "kg/m^2", "Convective precipitation");
	}

	CobaltParameter Pg_SP() {
		return new CobaltParameter("SP", "Pa", "Surface pressure");
	}

	CobaltParameter Pp_R() {
		return new CobaltParameter("R", "%", "Relative Humidity");
	}

	CobaltParameter Pp_T() {
		return new CobaltParameter("T", "K", "Temperature");
	}

	CobaltParameter Pp_U() {
		return new CobaltParameter("U", "m s-1", "U velocity");
	}

	CobaltParameter Pp_V() {
		return new CobaltParameter("V", "m s-1", "V velocity");
	}

	CobaltResolution R_025() {
		return CobaltResolution.newDegrees(0.25, 0.25);
	}

	CobaltResolution R_150() {
		return CobaltResolution.newDegrees(1.50, 1.50);
	}

	CobaltPrognosisPoint T(int hrs) {
		return CobaltPrognosisPoint.newAt(new Elapsed(hrs * CArgon.HR_TO_MS));
	}

	CobaltPrognosisAggregate TACC6(int hrs) {
		final int ssecsToex = hrs * 3600;
		final int ssecsFrom = ssecsToex - (6 * 3600);
		return CobaltPrognosisAggregate.newAccumulation(ssecsFrom, ssecsToex);
	}

	@Test
	public void t00_tutorial() {
		final ICobaltGeography G = G1();
		final CobaltSurfaceUnary S_p850 = CobaltSurfaceUnary.newIsobaric(850.0);
		final CobaltSurfaceUnary S_p500 = CobaltSurfaceUnary.newIsobaric(500.0);
		final CobaltSurfaceZero S_g = CobaltSurfaceZero.Ground;
		final CobaltReducer r = new CobaltReducer();
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_R(), T(0)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_R(), T(6)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_R(), T(12)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_R(), T(18)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_T(), T(0)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_T(), T(6)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_T(), T(12)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_T(), T(18)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_R(), T(0)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_R(), T(6)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_R(), T(12)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_R(), T(18)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_T(), T(0)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_T(), T(6)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_T(), T(12)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_T(), T(18)));
		r.add(CobaltRecord.newInstance(G, S_g, Pg_SP(), T(0)));
		r.add(CobaltRecord.newInstance(G, S_g, Pg_SP(), T(6)));
		r.add(CobaltRecord.newInstance(G, S_g, Pg_SP(), T(12)));
		final CobaltNCube cube = r.reduce();
		System.out.println(cube);
	}

	@Test
	public void t00_tutorialA() {
		final ICobaltGeography G = G1();
		final CobaltSurfaceUnary S_p850 = CobaltSurfaceUnary.newIsobaric(850.0);
		final CobaltSurfaceUnary S_p500 = CobaltSurfaceUnary.newIsobaric(500.0);
		final CobaltReducer r = new CobaltReducer();
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_R(), T(0)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_R(), T(6)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_R(), T(12)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_R(), T(18)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_T(), T(0)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_T(), T(6)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_T(), T(12)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_T(), T(18)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_V(), T(0)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_V(), T(6)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_V(), T(12)));
		r.add(CobaltRecord.newInstance(G, S_p850, Pp_V(), T(18)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_R(), T(0)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_R(), T(6)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_R(), T(12)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_R(), T(18)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_T(), T(0)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_T(), T(6)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_T(), T(12)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_T(), T(18)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_V(), T(0)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_V(), T(6)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_V(), T(12)));
		r.add(CobaltRecord.newInstance(G, S_p500, Pp_V(), T(18)));
		final CobaltNCube cube = r.reduce();
		System.out.println(cube);
	}

	@Test
	public void t30_A()
			throws ArgonApiException, ArgonPermissionException, ArgonStreamWriteException {
		final CobaltProviderName ProviderT30 = CobaltProviderName.newInstance("ECMWF-t30");

		CobaltNCube oPreNcube = null;

		for (int cycle = 0; cycle < 3; cycle++) {

			final CobaltSurfaceUnary S_p100 = CobaltSurfaceUnary.newIsobaric(100.0);
			final CobaltSurfaceUnary S_p250 = CobaltSurfaceUnary.newIsobaric(250.0);
			final CobaltSurfaceZero S_g = CobaltSurfaceZero.Ground;
			final CobaltSurfaceTopBottom S_e = CobaltSurfaceTopBottom.newIsentropic(250.0, 270.0);
			final CobaltReducer r = new CobaltReducer();
			if (oPreNcube != null) {
				r.add(oPreNcube);
			}

			int hstart;
			int hend;
			if (cycle == 0) {
				hstart = 0;
				hend = 12;
			} else if (cycle == 1) {
				hstart = 18;
				hend = 24;
			} else {
				hstart = 30;
				hend = 36;
			}

			for (int hrs = hstart; hrs <= hend; hrs += 6) {
				final CobaltPrognosisPoint T = T(hrs);
				final ICobaltGeography[] GX = { G1(), G2(), G3(), G4() };
				for (int ig = 0; ig < GX.length; ig++) {
					final ICobaltGeography G = GX[ig];
					if (cycle == 0 || cycle == 1) {
						r.add(CobaltRecord.newInstance(G, S_p100, Pp_R(), T, A1(), R_025()));
						r.add(CobaltRecord.newInstance(G, S_p100, Pp_U(), T, A1(), R_025()));
						r.add(CobaltRecord.newInstance(G, S_p100, Pp_T(), T, A1(), R_025()));
						r.add(CobaltRecord.newInstance(G, S_p250, Pp_R(), T, A1(), R_025()));
						r.add(CobaltRecord.newInstance(G, S_p250, Pp_U(), T, A1(), R_025()));
						r.add(CobaltRecord.newInstance(G, S_p250, Pp_T(), T, A1(), R_025()));
						r.add(CobaltRecord.newInstance(G, S_g, Pg_SP(), T, A1(), R_025()));
						r.add(CobaltRecord.newInstance(G, S_g, Pg_10U(), T, A1(), R_025()));
					} else {
						r.add(CobaltRecord.newInstance(G, S_p100, Pp_R(), T, A1(), R_025(), M(0)));
						r.add(CobaltRecord.newInstance(G, S_p100, Pp_R(), T, A1(), R_025(), M(1)));
						r.add(CobaltRecord.newInstance(G, S_p100, Pp_R(), T, A1(), R_025(), M(2)));
						if (hrs == hstart && ig == 0) {
							r.add(CobaltRecord.newInstance(G5(), S_e, Pp_V(), T(3), A2(), R_150(), M(3)));
						}
					}
				}
			}
			final CobaltNCube ncube = r.reduce();

			final String[] xA = new TestResource("ncubeA" + cycle + ".txt").lines(false);
			final String[] aA = TestResource.lines(ncube.toString(), false);
			Assert.assertArrayEquals("ncubeA" + cycle, xA, aA);

			final JsonObject jncube = ncube.newJsonObject();
			try {
				final CobaltNCube ncube1 = CobaltNCube.newInstance(jncube);
				Assert.assertTrue("encode/decodeA" + cycle, ncube.equals(ncube1));
				oPreNcube = ncube1;
			} catch (final JsonSchemaException ex) {
				Assert.fail(ex.getMessage());
			}
		}
		Assert.assertNotNull(oPreNcube);
		Assert.assertEquals(185, oPreNcube.recordCount());

		final CobaltKmlStyle style = new CobaltKmlStyle();
		try {
			final CobaltKmlRenderer kr = new CobaltKmlRenderer(style);
			final Binary newXml = kr.newXml(ProviderT30, oPreNcube);
			final String[] aAKml = TestResource.lines(newXml, false);
			final String[] aXKml = new TestResource("ncubeA2.kml").lines(false);
			Assert.assertArrayEquals("ncubeA2.kml", aXKml, aAKml);

			// newXml.save(new File("C:\\Documents and Settings\\roach\\Desktop\\t30.kml"), false);
		} catch (final CobaltDimensionException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t40_B() {

		final CobaltSurfaceZero S_g = CobaltSurfaceZero.Ground;
		final CobaltSurfaceUnary S_p250 = CobaltSurfaceUnary.newIsobaric(250.0);

		CobaltNCube oNCube = null;
		{
			final CobaltReducer r = new CobaltReducer();
			r.add(CobaltRecord.newInstance(G1(), S_p250, Pp_R(), T(6), A1(), R_025()));
			r.add(CobaltRecord.newInstance(G2(), S_p250, Pp_R(), T(6), A1(), R_025()));
			r.add(CobaltRecord.newInstance(G1(), S_g, Pg_ACPCP(), TACC6(6), A1(), R_025()));
			r.add(CobaltRecord.newInstance(G2(), S_g, Pg_ACPCP(), TACC6(6), A1(), R_025()));
			final CobaltNCube ncube = r.reduce();
			oNCube = ncube;
		}

		{
			final CobaltReducer r = new CobaltReducer();
			r.add(CobaltRecord.newInstance(G1(), S_p250, Pp_R(), T(12), A1(), R_025()));
			r.add(CobaltRecord.newInstance(G2(), S_p250, Pp_R(), T(12), A1(), R_025()));
			r.add(CobaltRecord.newInstance(G1(), S_g, Pg_ACPCP(), TACC6(12), A1(), R_025()));
			r.add(CobaltRecord.newInstance(G2(), S_g, Pg_ACPCP(), TACC6(12), A1(), R_025()));
			final CobaltNCube ncube = r.reduce();
			final CobaltReducer ru = new CobaltReducer();
			ru.add(oNCube);
			ru.add(ncube);
			oNCube = ru.reduce();
		}
		Assert.assertNotNull(oNCube);
		final String[] x = new TestResource("ncubeB0.txt").lines(false);
		final String[] a = TestResource.lines(oNCube.toString(), false);
		Assert.assertArrayEquals("ncubeB0", x, a);
	}
}
