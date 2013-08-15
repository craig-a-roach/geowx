/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;

import com.metservice.argon.Binary;
import com.metservice.beryllium.BerylliumHttpClientFactory;
import com.metservice.beryllium.BerylliumHttpServerFactory;
import com.metservice.beryllium.BerylliumPath;
import com.metservice.beryllium.BerylliumPathQuery;
import com.metservice.beryllium.BerylliumQuery;
import com.metservice.carbon.emhttp.HttpEmInstaller;
import com.metservice.carbon.emhttp.HttpNeonRouter;
import com.metservice.neon.NeonImpException;
import com.metservice.neon.NeonScriptException;

/**
 * @author roach
 */
public abstract class TestCarbon extends TestNeon {

	private static final int ServerPort = 9551;

	private static Server c_server;
	private static HttpClient c_client;

	@AfterClass
	public static void stopClient() {
		if (c_client != null) {
			try {
				c_client.stop();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
			c_client = null;
		}
	}

	@AfterClass
	public static void stopServer() {
		if (c_server != null) {
			try {
				c_server.stop();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
			c_server = null;
		}
	}

	public TestImpHttpExchange send(Address address, String method, BerylliumPathQuery uri, String oqContentType,
			Binary oContent, Date oIfModifiedSince, String jsfile) {
		if (address == null) throw new IllegalArgumentException("object is null");
		if (method == null || method.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (uri == null) throw new IllegalArgumentException("object is null");

		if (c_client == null) throw new IllegalStateException("no client");

		final TestImpHttpExchange he = new TestImpHttpExchange();
		he.setAddress(address);
		he.setScheme(HttpSchemes.HTTP_BUFFER);
		he.setMethod(method);
		he.setRequestURI(uri.ztwPathQueryEncoded());
		final HttpFields requestFields = he.getRequestFields();
		if (oqContentType != null) {
			requestFields.put(HttpHeaders.CONTENT_TYPE_BUFFER, oqContentType);
		}
		if (oContent != null) {
			requestFields.putLongField(HttpHeaders.CONTENT_LENGTH_BUFFER, oContent.byteCount());
		}
		if (oIfModifiedSince != null) {
			requestFields.putDateField(HttpHeaders.IF_MODIFIED_SINCE, oIfModifiedSince.getTime());
		}
		requestFields.put(HttpHeaders.HOST, address.getHost());
		requestFields.put(HttpHeaders.CONNECTION_BUFFER, HttpHeaderValues.KEEP_ALIVE_BUFFER);
		final InputStream oins = oContent == null ? null : oContent.getInputStream();
		try {
			if (oins != null) {
				he.setRequestContentSource(oins);
			}
			c_client.send(he);
			return he;
		} catch (final IOException ex) {
			Assert.fail(ex.getMessage());
		} finally {
			if (oins != null) {
				try {
					oins.close();
				} catch (final IOException ex) {
				}
			}
		}
		return null;
	}

	public TestImpHttpExchange sendLocalGET(BerylliumQuery query, String jsfile, Date oIfModifiedSince) {
		final Address address = new Address("localhost", ServerPort);
		final BerylliumPath path = BerylliumPath.newAbsolute("ui", "service", jsfile);
		final BerylliumPathQuery uri = new BerylliumPathQuery(path, query);
		return send(address, "GET", uri, null, null, oIfModifiedSince, jsfile);
	}

	public TestImpHttpExchange sendLocalPOSTForm(BerylliumQuery query, String jsfile) {
		final Address address = new Address("localhost", ServerPort);
		final BerylliumPath path = BerylliumPath.newAbsolute("ui", "service", jsfile);
		final BerylliumPathQuery uri = new BerylliumPathQuery(path);
		final Binary content = Binary.newFromStringISO8859(query.format());
		return send(address, "POST", uri, "application/x-www-form-urlencoded;charset=iso-8859-1", content, null, jsfile);
	}

	public TestImpHttpExchange sendLocalPOSTJson(String json, String jsfile) {
		final Address address = new Address("localhost", ServerPort);
		final BerylliumPath path = BerylliumPath.newAbsolute("ui", "service", jsfile);
		final BerylliumPathQuery uri = new BerylliumPathQuery(path);
		final Binary content = Binary.newFromStringUTF8(json);
		return send(address, "POST", uri, "text/plain;charset=utf-8", content, null, jsfile);
	}

	@Before
	public void setupClient() {
		if (c_client != null) return;
		try {
			final BerylliumHttpClientFactory.Config clientCfg = BerylliumHttpClientFactory.newConfig("browser");
			final HttpClient client = BerylliumHttpClientFactory.newClient(clientCfg);
			client.start();
			c_client = client;
		} catch (final Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Before
	public void setupServer() {
		if (c_server != null) return;
		try {
			final BerylliumHttpServerFactory.ServerConfig serverCfg = BerylliumHttpServerFactory.newServerConfig();
			final BerylliumHttpServerFactory.ConnectorConfig cxCfg = BerylliumHttpServerFactory
					.newConnectorConfig(ServerPort);
			final Server server = BerylliumHttpServerFactory.newServer(serverCfg, cxCfg);
			final CarbonHandler ch = new CarbonHandler();
			server.setHandler(ch);
			server.start();
			c_server = server;
		} catch (final Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}

	protected class CarbonHandler extends AbstractHandler {

		@Override
		public void handle(String target, Request rq, HttpServletRequest sr, HttpServletResponse rp)
				throws IOException, ServletException {
			rq.setHandled(true);
			final BerylliumPath path = BerylliumPath.newInstance(rq);
			final String qtwServiceNode = path.qtwNode(2);
			final HttpEmInstaller installer = new HttpEmInstaller(rq, 2048);
			final HttpNeonRouter router = new HttpNeonRouter();
			try {
				run(router, qtwServiceNode, null, installer);
				router.send(rp);
			} catch (final NeonScriptException ex) {
				router.sendError(rp, ex);
			} catch (final NeonImpException ex) {
				router.sendError(rp, ex);
			} catch (final InterruptedException ex) {
				router.sendError(rp, ex);
			} catch (final RuntimeException ex) {
				router.sendError(rp, ex);
			}
		}

		public CarbonHandler() {
		}
	}
}
