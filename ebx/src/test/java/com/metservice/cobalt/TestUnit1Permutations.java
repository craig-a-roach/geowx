package com.metservice.cobalt;

import junit.framework.Assert;

import org.junit.Test;

public class TestUnit1Permutations {

	@Test
	public void t40_complement() {

		final Permutation p13 = new Permutation(new int[] { 1, 3 });
		Assert.assertEquals("0,2,4", p13.complement(5).toString());
		Assert.assertEquals("0,2", p13.complement(4).toString());

		final Permutation p01 = new Permutation(new int[] { 0, 1 });
		Assert.assertEquals("2,3", p01.complement(4).toString());
		Assert.assertEquals("2", p01.complement(3).toString());

		final Permutation p3 = new Permutation(new int[] { 3 });
		Assert.assertEquals("0,1,2,4", p3.complement(5).toString());
		Assert.assertEquals("0,1,2", p3.complement(4).toString());

	}

	@Test
	public void t50_permutor() {

		Assert.assertEquals("(0,1)(0,2)(1,2)", Permutor.newInstance(3, 2).toString());
		Assert.assertEquals("(0)(1)(2)", Permutor.newInstance(3, 1).toString());
		Assert.assertEquals("(0,1,2)", Permutor.newInstance(3, 3).toString());

		Assert.assertEquals("(0,1,2)(0,1,3)(0,2,3)(1,2,3)", Permutor.newInstance(4, 3).toString());
		Assert.assertEquals("(0,1)(0,2)(0,3)(1,2)(1,3)(2,3)", Permutor.newInstance(4, 2).toString());

		final String x54 = "(0,1,2,3)(0,1,2,4)(0,1,3,4)(0,2,3,4)(1,2,3,4)";
		final String x53 = "(0,1,2)(0,1,3)(0,1,4)(0,2,3)(0,2,4)(0,3,4)(1,2,3)(1,2,4)(1,3,4)(2,3,4)";
		final String x52 = "(0,1)(0,2)(0,3)(0,4)(1,2)(1,3)(1,4)(2,3)(2,4)(3,4)";
		final String x51 = "(0)(1)(2)(3)(4)";
		Assert.assertEquals("(5,4)", x54, Permutor.newInstance(5, 4).toString());
		Assert.assertEquals("(5,3)", x53, Permutor.newInstance(5, 3).toString());
		Assert.assertEquals("(5,2)", x52, Permutor.newInstance(5, 2).toString());
		Assert.assertEquals("(5,1)", x51, Permutor.newInstance(5, 1).toString());
	}

}
