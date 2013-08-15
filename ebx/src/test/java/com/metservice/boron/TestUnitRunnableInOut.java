/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
public class TestUnitRunnableInOut {

	@Test
	public void t40()
			throws BoronCfgSyntaxException, InterruptedException {
		final TestImpSpaceProbe probe = new TestImpSpaceProbe();
		final BoronSpaceId id = BoronSpaceId.newInstance("testRunnableIn");
		final BoronSpaceCfg cfg = new BoronSpaceCfg();
		final KernelCfg kc = new KernelCfg(probe, id, cfg);

		final Script script = new Script();
		final FeedQueue queue = new FeedQueue(script);
		final String fs1 = "AB\u00BA\u00BB\u00BC\u00BD";
		final List<BoronFeedUnit> zlFeeds = new ArrayList<BoronFeedUnit>();
		zlFeeds.add(BoronFeedUnit.newStringInstance(fs1));
		zlFeeds.add(BoronFeedUnit.FeedUnitLineTerminator);
		zlFeeds.add(BoronFeedUnit.FeedUnitStreamTerminator);
		queue.put(kc, zlFeeds, null);

		final List<BoronFeedUnit> zlOver = new ArrayList<BoronFeedUnit>();
		zlOver.add(BoronFeedUnit.newStringInstance("xyz"));
		final List<BoronFeedUnit> zlReject = queue.put(kc, zlOver, Elapsed.Zero);
		Assert.assertEquals(1, zlReject.size());

		final ByteArrayOutputStream out = new ByteArrayOutputStream(100);
		final RunnableIn rin = new RunnableIn(kc, out, queue);
		rin.run();
		final byte[] ba = out.toByteArray();
		final byte[] exp = (fs1 + "\r\n").getBytes(ArgonText.UTF8);
		Assert.assertArrayEquals(exp, ba);
	}

	@Test
	public void t50()
			throws IOException, BoronCfgSyntaxException, InterruptedException {
		final TestImpSpaceProbe probe = new TestImpSpaceProbe();
		final BoronSpaceId id = BoronSpaceId.newInstance("testRunnableOut");
		final BoronSpaceCfg cfg = new BoronSpaceCfg();
		final KernelCfg kc = new KernelCfg(probe, id, cfg);

		final Script script = new Script();
		final ProductQueue queue = new ProductQueue(script);
		final byte[] ba1 = "ABC\u00BAD\r\nXY\n\n012345678\u01C1JKL\n".getBytes(script.stdioEncoding());
		final ByteArrayInputStream in = new ByteArrayInputStream(ba1);
		final RunnableOut rout = new RunnableOut(kc, OutStreamType.StdOut, in, queue);
		rout.run();
		in.close();
		final IBoronProduct p1 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p1 instanceof BoronProductStreamLine);
		Assert.assertEquals("ABC\u00BAD", ((BoronProductStreamLine) p1).zLine());
		final IBoronProduct p2 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p2 instanceof BoronProductStreamLine);
		Assert.assertEquals("XY", ((BoronProductStreamLine) p2).zLine());
		final IBoronProduct p3 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p3 instanceof BoronProductStreamLine);
		Assert.assertEquals("", ((BoronProductStreamLine) p3).zLine());
		final IBoronProduct p4 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p4 instanceof BoronProductStreamLine);
		Assert.assertEquals("012345678\u01C1JKL", ((BoronProductStreamLine) p4).zLine());
		final IBoronProduct p5 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p5 instanceof BoronProductStreamEnd);
		final IBoronProduct p6 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p6 instanceof BoronProductCancellation);
	}

	@Test
	public void t60()
			throws BoronCfgSyntaxException, InterruptedException, IOException {
		final TestImpSpaceProbe probe = new TestImpSpaceProbe();
		final BoronSpaceId id = BoronSpaceId.newInstance("testRunnableIn");
		final BoronSpaceCfg cfg = new BoronSpaceCfg();
		final KernelCfg kc = new KernelCfg(probe, id, cfg);

		final Script script = new Script(BoronStdioPrompt.newStartOfLine(">"));
		final ProductQueue queue = new ProductQueue(script);
		final byte[] ba1 = "ABC\n>D\nE>F\n>".getBytes(script.stdioEncoding());
		final ByteArrayInputStream in = new ByteArrayInputStream(ba1);
		final RunnableOut rout = new RunnableOut(kc, OutStreamType.StdOut, in, queue);
		rout.run();
		in.close();
		final IBoronProduct p1 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p1 instanceof BoronProductStreamLine);
		Assert.assertEquals("ABC", ((BoronProductStreamLine) p1).zLine());
		final IBoronProduct p2 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p2 instanceof BoronProductStreamLine);
		Assert.assertEquals(">", ((BoronProductStreamLine) p2).zLine());
		final IBoronProduct p3 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p3 instanceof BoronProductStreamLine);
		Assert.assertEquals("D", ((BoronProductStreamLine) p3).zLine());
		final IBoronProduct p4 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p4 instanceof BoronProductStreamLine);
		Assert.assertEquals("E>F", ((BoronProductStreamLine) p4).zLine());
		final IBoronProduct p5 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p5 instanceof BoronProductStreamLine);
		Assert.assertEquals(">", ((BoronProductStreamLine) p5).zLine());
		final IBoronProduct p6 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p6 instanceof BoronProductStreamEnd);
	}

	@Test
	public void t61()
			throws BoronCfgSyntaxException, InterruptedException, IOException {
		final TestImpSpaceProbe probe = new TestImpSpaceProbe();
		final BoronSpaceId id = BoronSpaceId.newInstance("testRunnableIn");
		final BoronSpaceCfg cfg = new BoronSpaceCfg();
		final KernelCfg kc = new KernelCfg(probe, id, cfg);

		final Script script = new Script(BoronStdioPrompt.newAnywhere(">"));
		final ProductQueue queue = new ProductQueue(script);
		final byte[] ba1 = "ABC\nD>E>F\n>".getBytes(script.stdioEncoding());
		final ByteArrayInputStream in = new ByteArrayInputStream(ba1);
		final RunnableOut rout = new RunnableOut(kc, OutStreamType.StdOut, in, queue);
		rout.run();
		in.close();
		final IBoronProduct p1 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p1 instanceof BoronProductStreamLine);
		Assert.assertEquals("ABC", ((BoronProductStreamLine) p1).zLine());
		final IBoronProduct p2 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p2 instanceof BoronProductStreamLine);
		Assert.assertEquals("D>", ((BoronProductStreamLine) p2).zLine());
		final IBoronProduct p3 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p3 instanceof BoronProductStreamLine);
		Assert.assertEquals("E>", ((BoronProductStreamLine) p3).zLine());
		final IBoronProduct p4 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p4 instanceof BoronProductStreamLine);
		Assert.assertEquals("F", ((BoronProductStreamLine) p4).zLine());
		final IBoronProduct p5 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p5 instanceof BoronProductStreamLine);
		Assert.assertEquals(">", ((BoronProductStreamLine) p5).zLine());
		final IBoronProduct p6 = queue.takeProduct(Elapsed.Zero);
		Assert.assertTrue(p6 instanceof BoronProductStreamEnd);
	}

	private static class Script implements IBoronScript {

		private static final byte[] LT = { 0x0D, 0x0A };

		@Override
		public int bcBufferStdErr() {
			return 5;
		}

		@Override
		public int bcBufferStdIn() {
			return 5;
		}

		@Override
		public int bcBufferStdOut() {
			return 10;
		}

		@Override
		public Elapsed getExitTimeout() {
			return null;
		}

		@Override
		public BoronStdioPrompt getStdioPrompt() {
			return m_oPrompt;
		}

		@Override
		public BoronInterpreterId interpreterId() {
			return BoronInterpreterId.IntrinsicBash;
		}

		@Override
		public int maxFeedQueueDepth() {
			return 3;
		}

		@Override
		public int maxProductQueueDepth() {
			return 10;
		}

		@Override
		public boolean redirectStdErrToOut() {
			return false;
		}

		@Override
		public Charset stdioEncoding() {
			return ArgonText.UTF8;
		}

		@Override
		public byte[] stdioLineTerminator() {
			return LT;
		}

		@Override
		public List<String> zlLines() {
			return Collections.emptyList();
		}

		@Override
		public List<IBoronScriptResource> zlResources() {
			return Collections.emptyList();
		}

		public Script() {
			m_oPrompt = null;
		}

		public Script(BoronStdioPrompt oPrompt) {
			m_oPrompt = oPrompt;
		}
		private final BoronStdioPrompt m_oPrompt;
	}
}
