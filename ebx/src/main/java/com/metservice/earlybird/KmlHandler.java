/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
class KmlHandler extends AbstractHandler {

	@Override
	public void handle(String target, Request rq, HttpServletRequest sr, HttpServletResponse rp)
			throws IOException, ServletException {
		rq.setHandled(true);
		final BerylliumPath path = BerylliumPath.newInstance(rq);
		final String queryString = rq.getQueryString(); // TODO
		System.out.println(path + " " + queryString);
		rp.setStatus(HttpServletResponse.SC_OK);
		rp.setContentType(KmlBuilder.MimeType);
		final PrintWriter writer = rp.getWriter();

		final KmlBuilder b = new KmlBuilder();
		b.placemarkOpen();
		b.name("Wellington");
		b.description("city");
		b.pointOpen();
		b.coordinates("174.77,-41.288");
		b.pointClose();
		b.placemarkClose();
		b.save(writer);
	}

	public KmlHandler(KernelCfg kc) {
	}
}
