/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import org.junit.Test;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.DateFactory;
import com.metservice.argon.Real;

/**
 * @author roach
 */
public class TestUnitEsString extends TestNeon {

	@Test
	public void t50_StringRegex() {
		final Expectation x = new Expectation("ew1", false, "ew2", true);
		x.add("m1", true, "m2", false, "m3", true, "m4", true);
		x.add("a1", "a.x,b.y,c.x");
		x.add("a2", "a.x,c.x");
		x.add("a3", "b.y");
		x.add("a4", "b*y");
		x.add("t1", "a/b/", "t2", "a/b", "t3", "a/b/", "t4", "a/b", "t5", "");
		x.add("rep1", "a/b.c");
		x.add("rep2", "a/b/c");
		x.add("sa1", "006:p02");
		x.add("sa2", "006:c00");
		x.add("sb1", "a:3|b:6");
		x.add("y1", "a,b,c,d");
		x.add("#y2", 0);
		jsassert(x, "StringRegex");
	}

	@Test
	public void t50_StringTime() {
		final Expectation x = new Expectation("gf", "20100305T2030Z00M000");
		x.add("sgmt1", "2030 Friday, 05-Mar-2010 GMT", "snz", "0930 Saturday, 06-Mar-2010 NZDT");
		x.add("shmt1", "02:25:18 Thu, 08-Dec-2011 GMT", "shmt2", "10:25:18 Thu, 08-Dec-2011 GMT");
		x.add("shmt3", "13:25:18 Wed, 07-Dec-2011 GMT", "shmt4", "13:25:18 Wed, 07-Dec-2011 GMT");
		x.add("sx1mt", "0421 Friday, 05-Mar-2010 GMT");
		x.add("sx2mt", "0421 Saturday, 05-Dec-2009 GMT");
		x.add("sx3mt", "1100 Tuesday, 04-Mar-2008 GMT");
		x.add("symt", "0421 Friday, 05-Mar-2010 GMT");
		x.add("z1f", "1723 Wednesday, 15-Jun-2011 GMT");
		x.add("z2f", "1723 Wednesday, 15-Jun-2011 GMT");
		x.add("z3ft", "bad");
		jsassert(x, "StringTime");
	}

	@Test
	public void t60_StringBind() {
		final Expectation x = new Expectation("x1", "Aquebecromeo.papaBquebec${j}${k}${m}${c}Z");
		x.add("x2", "Aquebecromeo.papaBquebecmike${c}Z");
		x.add("x3", "Aquebecromeo.papaBquebecmike${p}${r}Z");
		x.add("x4", "Aquebecromeo.papaBquebecmikepaparomeoZ");
		x.add("y1", "A");
		x.add("z1", "papa");
		x.add("f1", "SL.us008001/ST.opnl/MT.ensg_CY.06/RD.20110602/PT.grid_DF");
		jsassert(x, "StringBind");
	}

	@Test
	public void t65_StringLslParse() {
		ArgonClock.simulatedNow(DateFactory.newDateConstantFromT8("20110613T1300Z00M000").getTime());
		final Expectation x = new Expectation();
		x.add("m0", "20110614T0506Z00M000");
		x.add("m1", "20110725T0321Z00M000");
		x.add("m2", "20110610T0000Z00M000");
		x.add("m3", "20100208T0000Z00M000");
		x.add("m4", "20101225T0526Z00M000");
		x.add("t4", "20110725T0321Z00M000");
		x.add("n4", "fh.0228_pa.p20_lt.press_gr.onedeg.idx");
		x.add("^z4", Real.newInstance(3205246132L));
		jsassert(x, "StringLsl");
	}

	@Test
	public void t70_StringInvoke() {
		final Expectation x = new Expectation("xj", "jkl", "xk", "???", "xp", "pqrxy");
		jsassert(x, "StringInvoke");
	}

}
