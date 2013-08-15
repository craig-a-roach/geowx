/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Elapsed;
import com.metservice.argon.ElapsedFactory;

/**
 * @author roach
 */
public class TestUnitProduct {

	private static Script newBlockingScript(BoronSpaceCfg cfg, Elapsed timeout) {
		final boolean isWin = cfg.isWinOS();
		final Script script = new Script(timeout, null, isWin);
		script.line("java -version");
		if (isWin) {
			script.line("pause");
		} else {
			script.line("read answer");
		}
		return script;
	}

	private static Script newFastScript(BoronSpaceCfg cfg) {
		final boolean isWin = cfg.isWinOS();
		final Script script = new Script(null, null, isWin);
		script.line("java -version");
		return script;
	}

	private static Script newNSLookupScript(BoronSpaceCfg cfg) {
		final boolean isWin = cfg.isWinOS();
		final Script script = new Script(ElapsedFactory.newElapsedConstant("50s"), BoronStdioPrompt.newStartOfLine("> "), isWin);
		script.line("nslookup");
		return script;
	}

	@Test
	public void t12()
			throws BoronException, InterruptedException {
		final BoronSpaceId id = BoronSpaceId.newInstance("testproduct");
		final BoronSpaceCfg cfg = new BoronSpaceCfg();
		cfg.setWorkHistoryDepth(3);
		cfg.setCooldownSecs(20);
		final BoronSpace space = new BoronSpace(id, cfg);
		space.start();

		final Script script = newNSLookupScript(cfg);

		final String[] names = { "www.google.co.nz", "www.metservice.com" };
		final String[] matches = { "google", "metservice" };
		int nameIndex = 0;
		String zTarget = names[nameIndex];
		String zMatch = matches[nameIndex];
		final BoronProductIterator iProduct = space.newProcessProductIterator(script);
		while (iProduct.hasNext()) {
			if (zTarget.equals("exit")) {
				zTarget = "";
			}
			final List<IBoronProduct> zlLines = iProduct.nextPrompt();
			for (final IBoronProduct p : zlLines) {
				if (p instanceof BoronProductStreamLine) {
					final String zValue = ((BoronProductStreamLine) p).zLine();
					if (zValue.contains("Name:") && zValue.contains(zMatch)) {
						nameIndex++;
						if (nameIndex < names.length) {
							zTarget = names[nameIndex];
							zMatch = matches[nameIndex];
						} else {
							zTarget = "exit";
							zMatch = ".";
						}
					}
				}
				System.out.println(p);
			}
			if (zTarget.length() > 0) {
				final List<BoronFeedUnit> zlFeeds1 = new ArrayList<BoronFeedUnit>();
				zlFeeds1.add(BoronFeedUnit.newStringInstance(zTarget));
				zlFeeds1.add(BoronFeedUnit.FeedUnitLineTerminator);
				iProduct.put(zlFeeds1);
			}
		}

		space.shutdown();
		Assert.assertTrue(nameIndex == names.length);
	}

	@Test(timeout = 10000)
	public void t40()
			throws BoronException, InterruptedException {
		final BoronSpaceId id = BoronSpaceId.newInstance("testproduct");
		final BoronSpaceCfg cfg = new BoronSpaceCfg();
		cfg.setWorkHistoryDepth(3);
		cfg.setCooldownSecs(20);
		final BoronSpace space = new BoronSpace(id, cfg);
		space.start();
		final Script script = newFastScript(cfg);
		final BoronProductIterator iProduct = space.newProcessProductIterator(script);
		int productCount = 0;
		while (iProduct.hasNext()) {
			final IBoronProduct product = iProduct.next();
			System.out.println(product);
			productCount++;
		}
		Assert.assertTrue("productCount=" + productCount, productCount >= 3);

		space.shutdown();
	}

	@Test(timeout = 12000)
	public void t50()
			throws BoronException, InterruptedException {
		final Elapsed timeout = ElapsedFactory.newElapsedConstant("7s");
		final BoronSpaceId id = BoronSpaceId.newInstance("testproduct");
		final BoronSpaceCfg cfg = new BoronSpaceCfg();
		cfg.setWorkHistoryDepth(3);
		cfg.setCooldownSecs(20);
		final BoronSpace space = new BoronSpace(id, cfg);
		space.start();
		final Script script = newBlockingScript(cfg, timeout);
		final BoronProductIterator iProduct = space.newProcessProductIterator(script);
		int productCount = 0;
		IBoronProduct oLastProduct = null;
		while (iProduct.hasNext()) {
			final IBoronProduct product = iProduct.next();
			System.out.println(product);
			oLastProduct = product;
			productCount++;
		}
		Assert.assertTrue("productCount=" + productCount, productCount >= 3);
		Assert.assertTrue("Cancelled", oLastProduct == BoronProductCancellation.Instance);

		space.shutdown();
	}

	@Test(timeout = 12000)
	public void t52()
			throws BoronException, InterruptedException {
		final Elapsed timeout = ElapsedFactory.newElapsedConstant("20s");
		final BoronSpaceId id = BoronSpaceId.newInstance("testproduct");
		final BoronSpaceCfg cfg = new BoronSpaceCfg();
		cfg.setWorkHistoryDepth(3);
		cfg.setCooldownSecs(20);
		final BoronSpace space = new BoronSpace(id, cfg);
		space.start();
		final Script script = newBlockingScript(cfg, timeout);
		final BoronProductIterator iProduct = space.newProcessProductIterator(script);
		int productCount = 0;
		IBoronProduct oLastProduct = null;
		while (iProduct.hasNext()) {
			final IBoronProduct product = iProduct.next(7, TimeUnit.SECONDS);
			System.out.println(product);
			oLastProduct = product;
			productCount++;
		}
		Assert.assertTrue("productCount=" + productCount, productCount >= 3);
		Assert.assertTrue("Cancelled", oLastProduct == BoronProductCancellation.Instance);

		space.shutdown();
	}

	@Test(timeout = 12000)
	public void t55()
			throws BoronException, ArgonApiException, InterruptedException {
		final Elapsed timeout = ElapsedFactory.newElapsedConstant("60s");
		final BoronSpaceId id = BoronSpaceId.newInstance("testproduct");
		final BoronSpaceCfg cfg = new BoronSpaceCfg();
		cfg.setFilterPatternConsole("live=.*,info=.*,warn=.*,fail=.*");
		cfg.setWorkHistoryDepth(3);
		cfg.setCooldownSecs(7);
		final BoronSpace space = new BoronSpace(id, cfg);
		space.start();
		final Script script = newBlockingScript(cfg, timeout);
		final BoronProductIterator iProduct = space.newProcessProductIterator(script);
		int productCount = 0;
		IBoronProduct oLastProduct = null;
		while (iProduct.hasNext()) {
			final IBoronProduct product = iProduct.next();
			System.out.println(product);
			oLastProduct = product;
			productCount++;
			if (productCount == 1) {
				Executors.newFixedThreadPool(1).execute(new Runnable() {

					@Override
					public void run() {
						space.shutdown();
					}

				});
			}
			if (productCount == 2) {
				Thread.sleep(3000);
			}
			if (productCount == 3) {
				try {
					space.newProcessProductIterator(script);
					Assert.fail("Should not be allowed to start new process");
				} catch (final BoronApiException ex) {
					System.out.println("Good exception: " + ex.getMessage());
					Assert.assertFalse(ex.toString().isEmpty());
				}
			}
		}
		Assert.assertTrue("productCount=" + productCount, productCount >= 3);
		Assert.assertTrue("Cancelled", oLastProduct == BoronProductCancellation.Instance);
	}

	private static class Script implements IBoronScript {

		@Override
		public int bcBufferStdErr() {
			return 1024;
		}

		@Override
		public int bcBufferStdIn() {
			return 512;
		}

		@Override
		public int bcBufferStdOut() {
			return 1024;
		}

		@Override
		public Elapsed getExitTimeout() {
			return oExitTimeout;
		}

		@Override
		public BoronStdioPrompt getStdioPrompt() {
			return m_oPrompt;
		}

		@Override
		public BoronInterpreterId interpreterId() {
			return interpreterId;
		}

		public Script line(String zLine) {
			zlLines.add(zLine);
			return this;
		}

		@Override
		public int maxFeedQueueDepth() {
			return 3;
		}

		@Override
		public int maxProductQueueDepth() {
			return 3;
		}

		@Override
		public boolean redirectStdErrToOut() {
			return true;
		}

		@Override
		public Charset stdioEncoding() {
			return ArgonText.UTF8;
		}

		@Override
		public byte[] stdioLineTerminator() {
			return lineTerminator;
		}

		@Override
		public List<String> zlLines() {
			return zlLines;
		}

		@Override
		public List<IBoronScriptResource> zlResources() {
			return Collections.emptyList();
		}

		public Script(Elapsed oExitTimeout, BoronStdioPrompt oPrompt, boolean isWin) {
			if (isWin) {
				this.interpreterId = BoronInterpreterId.IntrinsicWinCmd;
			} else {
				this.interpreterId = BoronInterpreterId.IntrinsicBash;
			}
			this.lineTerminator = BoronLineTerminator.select("*");
			this.oExitTimeout = oExitTimeout;
			m_oPrompt = oPrompt;
		}

		final BoronInterpreterId interpreterId;
		final Elapsed oExitTimeout;
		final byte[] lineTerminator;
		final BoronStdioPrompt m_oPrompt;

		final List<String> zlLines = new ArrayList<String>();
	}

}
