package com.metservice.cobalt;

import org.junit.Assert;
import org.junit.Test;

public class TestUnit1Kml {

	@Test
	public void t20() {
		final KmlGeometry ASIA = KmlGeometry.newClampedToGround();
		ASIA.setCoordinates(50.0, -170.0, 20.0, 109.5);
		Assert.assertEquals("50N,170W 20N,110E", ASIA.toName());
		final KmlGeometry NZ = KmlGeometry.newRelativeToGround(true);
		NZ.setCoordinates(-25.0, 160.0, -55.0, 180.0);
		NZ.setAltitude(5000.0);
		Assert.assertEquals("25S,160E 55S,180", NZ.toName());
	}

	@Test
	public void t30() {
		final CobaltKmlColor c1 = CobaltKmlColor.newABGR("??3F?3");
		Assert.assertEquals("aa3faaaa", c1.format(2, 3));
		Assert.assertEquals("553f5555", c1.format(1, 3));
		Assert.assertEquals("003f0000", c1.format(0, 3));
	}

}
