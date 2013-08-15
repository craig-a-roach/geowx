/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1Url {

	@Test
	public void t30() {
		final BerylliumUrlBuilder ub = new BerylliumUrlBuilder();
		ub.setHost("aws.com");
		ub.setPath(BerylliumPath.newAbsolute("ui", "page", "help.html"));
		ub.setFragment("toc");
		Assert.assertEquals("/ui/page/help.html#toc", ub.qtwEncodedBaselined());
		Assert.assertEquals("http://aws.com/ui/page/help.html#toc", ub.qtwEncodedAbsolute());
	}

	@Test
	public void t40() {
		final BerylliumUrlBuilder ub = new BerylliumUrlBuilder();
		ub.setPath(BerylliumPath.newRelative("style", "core.css"));
		Assert.assertEquals("style/core.css", ub.qtwEncodedBaselined());
		Assert.assertEquals("http://localhost/style/core.css", ub.qtwEncodedAbsolute());
	}

	@Test
	public void t50()
			throws Exception {
		final Handler50 h = new Handler50();
		final TestHelpBrowser browser = new TestHelpBrowser(9950, h);
		try {
			final HttpExchange x1 = browser.sendGET("http://localhost:9950/ui/service/pageA");
			x1.waitForDone();
			Assert.assertNotNull(h.oB1);
			Assert.assertEquals("http://localhost:9951/ui/asset/image/logo?j=C+D&k=E%23F", h.oB1.qtwEncodedAbsolute());
		} finally {
			browser.shutdown();
		}
	}

	private static class Handler50 extends AbstractHandler {

		@Override
		public void handle(String target, Request rq, HttpServletRequest svrq, HttpServletResponse svrp)
				throws IOException, ServletException {
			oB1 = new BerylliumUrlBuilder();
			oB1.setSchemeHostPortPath(rq, 2);
			oB1.setPort(9951);
			oB1.addPath(BerylliumPath.newRelative("asset", "image", "logo"));
			oB1.setQuery(BerylliumQuery.newConstant("j", "C D", "k", "E#F"));
		}

		public Handler50() {
		}
		BerylliumUrlBuilder oB1;
	}

}
