/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.beryllium.BerylliumPathMime.EntityHeaders;

/**
 * @author roach
 */
public class TestUnit1Mime {

	@Test
	public void t50() {
		final BerylliumMimeTypeTable mt = new BerylliumMimeTypeTable();
		final BerylliumPath p1 = BerylliumPath.newAbsolute("a", "b", "c.jpg");
		final BerylliumPath p2 = BerylliumPath.newAbsolute("a", "b", "c.css");
		final BerylliumPath p3 = BerylliumPath.newAbsolute("a", "image", "c.");
		Assert.assertEquals("image/jpeg", mt.mimeTypeByExtension(p1).qlctwMimeType);
		Assert.assertEquals("text/css", mt.mimeTypeByExtension(p2).qlctwMimeType);
		Assert.assertEquals("application/octet-stream", mt.mimeTypeByExtension(p3).qlctwMimeType);
	}

	@Test
	public void t60() {
		final BerylliumMimeTypeTable mt = new BerylliumMimeTypeTable();
		final BerylliumPathMime pm1 = mt.mimeTypeByExtension(BerylliumPath.newAbsolute("a", "c.js"));
		final BerylliumPathMime pm2 = mt.mimeTypeByExtension(BerylliumPath.newAbsolute("a", "c.js.gz"));
		final BerylliumPathMime pm3 = mt.mimeTypeByExtension(BerylliumPath.newAbsolute("a", "c.gz"));

		final EntityHeaders eh1_none = pm1.newEntityHeaders(null);
		final EntityHeaders eh1_gzip = pm1.newEntityHeaders("deflate, gzip, bz");

		final EntityHeaders eh2_none = pm2.newEntityHeaders(null);
		final EntityHeaders eh2_deflate = pm2.newEntityHeaders("deflate");
		final EntityHeaders eh2_gzip = pm2.newEntityHeaders("deflate, gzip, bz");

		final EntityHeaders eh3_none = pm3.newEntityHeaders(null);
		final EntityHeaders eh3_gzip = pm3.newEntityHeaders("deflate, gzip, bz");

		Assert.assertEquals("application/x-javascript", eh1_none.qlcContentType);
		Assert.assertNull("no content encoding", eh1_none.oqlcContentEncoding);
		Assert.assertEquals("application/x-javascript", eh1_gzip.qlcContentType);
		Assert.assertNull("no content encoding", eh1_gzip.oqlcContentEncoding);

		Assert.assertEquals("application/gzip", eh2_none.qlcContentType);
		Assert.assertNull("no content encoding", eh2_none.oqlcContentEncoding);
		Assert.assertEquals("application/gzip", eh2_deflate.qlcContentType);
		Assert.assertNull("no content encoding", eh2_deflate.oqlcContentEncoding);
		Assert.assertEquals("application/x-javascript", eh2_gzip.qlcContentType);
		Assert.assertEquals("gzip", eh2_gzip.oqlcContentEncoding);

		Assert.assertEquals("application/gzip", eh3_none.qlcContentType);
		Assert.assertNull("no content encoding", eh3_none.oqlcContentEncoding);
		Assert.assertEquals("application/gzip", eh3_gzip.qlcContentType);
		Assert.assertNull("no content encoding", eh3_gzip.oqlcContentEncoding);

	}

}
