/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class TestUnit1Fetcher {

	@Test
	public void t10_clean() {
		final ExecutorService xc = Executors.newFixedThreadPool(5);

		final Fetcher f = new Fetcher();
		final Query qA = new Query("A", 3000, "");
		final Future<Response> fA1 = xc.submit(new Exchange(f, qA));
		final Future<Response> fA2 = xc.submit(new Exchange(f, qA));
		final Future<Response> fA3 = xc.submit(new Exchange(f, qA));
		try {
			final Response r = fA1.get();
			Assert.assertEquals(1, r.from());
		} catch (final ExecutionException | InterruptedException ex) {
			Assert.fail(ex.getMessage());
		}
		try {
			final Response r = fA2.get();
			Assert.assertEquals(1, r.from());
		} catch (final ExecutionException | InterruptedException ex) {
			Assert.fail(ex.getMessage());
		}
		try {
			final Response r = fA3.get();
			Assert.assertEquals(1, r.from());
		} catch (final ExecutionException | InterruptedException ex) {
			Assert.fail(ex.getMessage());
		}
		final Query qAA = new Query("A", 2000, "");
		final Future<Response> fAA1 = xc.submit(new Exchange(f, qAA));
		final Future<Response> fAA2 = xc.submit(new Exchange(f, qAA));
		try {
			final Response r = fAA1.get();
			Assert.assertEquals(2, r.from());
		} catch (final ExecutionException | InterruptedException ex) {
			Assert.fail(ex.getMessage());
		}
		try {
			final Response r = fAA2.get();
			Assert.assertEquals(2, r.from());
		} catch (final ExecutionException | InterruptedException ex) {
			Assert.fail(ex.getMessage());
		}
		final Query qB = new Query("B", 2000, "");
		final Future<Response> fB1 = xc.submit(new Exchange(f, qB));
		final Future<Response> fB2 = xc.submit(new Exchange(f, qB));
		try {
			final Response r = fB1.get();
			Assert.assertEquals(3, r.from());
		} catch (final ExecutionException | InterruptedException ex) {
			Assert.fail(ex.getMessage());
		}
		try {
			final Response r = fB2.get();
			Assert.assertEquals(3, r.from());
		} catch (final ExecutionException | InterruptedException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t20_fail() {
		final ExecutorService xc = Executors.newFixedThreadPool(5);

		final Fetcher f = new Fetcher();
		final Query qA = new Query("A", 3000, "Malformed");
		final Future<Response> fA1 = xc.submit(new Exchange(f, qA));
		final Future<Response> fA2 = xc.submit(new Exchange(f, qA));
		final Future<Response> fA3 = xc.submit(new Exchange(f, qA));
		try {
			final Response r = fA1.get();
			final String oM = r.getThrowableMessage();
			Assert.assertNotNull(oM);
			Assert.assertEquals("Malformed", oM);
		} catch (final ExecutionException | InterruptedException ex) {
			Assert.fail(ex.getMessage());
		}
		try {
			final Response r = fA2.get();
			final String oM = r.getThrowableMessage();
			Assert.assertNotNull(oM);
		} catch (final ExecutionException | InterruptedException ex) {
			Assert.fail(ex.getMessage());
		}
		try {
			final Response r = fA3.get();
			final String oM = r.getThrowableMessage();
			Assert.assertNotNull(oM);
		} catch (final ExecutionException | InterruptedException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	private static class Exchange implements Callable<Response> {

		@Override
		public Response call()
				throws Exception {
			try {
				return m_fetcher.fetch(m_query);
			} catch (final ExecutionException ex) {
				return new Response(m_query, 0, ex.getCause());
			}
		}

		public Exchange(Fetcher fetcher, Query query) {
			assert fetcher != null;
			assert query != null;
			m_fetcher = fetcher;
			m_query = query;
		}
		private final Fetcher m_fetcher;
		private final Query m_query;
	}

	private static class Fetcher extends ArgonFetcher<Query, Response> {

		@Override
		protected Response getResponse(Query query)
				throws ArgonFormatException, InterruptedException {
			final long msExec = query.msExec();
			Thread.sleep(msExec);
			final String zFail = query.zFail();
			if (zFail.length() == 0) return new Response(query, m_counter.incrementAndGet());
			throw new ArgonFormatException(zFail);
		}

		public Fetcher() {
		}
		private final AtomicInteger m_counter = new AtomicInteger();
	}

	private static class Query implements IArgonFetcherQuery {

		@Override
		public String getSignature() {
			return m_spec.toLowerCase();
		}

		public long msExec() {
			return m_ms;
		}

		@Override
		public String toString() {
			return m_spec + " " + m_ms + "ms" + (m_zFail.length() == 0 ? "" : (" fail:" + m_zFail));
		}

		public String zFail() {
			return m_zFail;
		}

		public Query(String spec, long msExec, String zFail) {
			m_spec = spec;
			m_ms = msExec;
			m_zFail = zFail;
		}
		private final String m_spec;
		private final long m_ms;
		private final String m_zFail;
	}

	private static class Response {

		public int from() {
			return m_from;
		}

		public String getThrowableMessage() {
			return m_ox.getMessage();
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("R(").append(m_q).append(") #").append(m_from);
			if (m_ox != null) {
				sb.append("\n");
				sb.append(Ds.format(m_ox));
			}
			return sb.toString();
		}

		Response(Query q, int id) {
			this(q, id, null);
		}

		Response(Query q, int id, Throwable ox) {
			assert q != null;
			m_q = q;
			m_from = id;
			m_ox = ox;
		}
		private final Query m_q;
		private final int m_from;
		private final Throwable m_ox;
	}

}
