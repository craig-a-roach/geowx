/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.net;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonPermissionException;

/**
 * @author roach
 */
public class TestUnit1Nic {

	@Test
	public void t40() {
		try {
			final ArgonNic oLB = ArgonNicDiscoverer.findLoopbackIPv4();
			if (oLB == null) {
				System.out.println("No loopback Noic");
			}
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t50() {
		try {
			final String spec = "?IPv4SG";
			final ArgonNic oNL = ArgonNicDiscoverer.findUnicastNonLoopback(spec);
			Assert.assertNotNull("Nic matching '" + spec + "'", oNL);
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t60() {
		try {
			final String spec = "?IPv4SG eth99";
			final ArgonNic oNL = ArgonNicDiscoverer.findUnicastNonLoopback(spec);
			Assert.assertNotNull("Nic matching '" + spec + "'", oNL);
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t70() {

		final String[] specs = { "?", "?IPv* eth2", "?IPv4S10.(10|20).+.+ eth1:eth0", "?IPv*GL", "?IPv4|+.met.co.nz" };

		try {
			for (int i = 0; i < specs.length; i++) {
				final String spec = specs[i];
				final ArgonNic oNL = ArgonNicDiscoverer.findUnicastNonLoopback(spec);
				if (oNL == null) {
					System.out.println("Info: No Nic matches '" + spec + "'");
				} else {
					System.out.println("Detected Nic " + oNL + " matching '" + spec + "'");
				}
			}
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		}
	}

}
