/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.disk.cache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import com.metservice.argon.Binary;

/**
 * @author roach
 */
public class TestUnit1Classpath {

	@Test
	public void t50() {
		final Binary b = Binary.newFromStringUTF8("ABC.DEF.GHI.KLM.no.pqR");
		try {
			final MessageDigest digester = MessageDigest.getInstance("SHA-1");
			final long tsStart = System.currentTimeMillis();
			for (int i = 0; i < 10000; i++) {
				digester.reset();
				digester.update(b.zptReadOnly);
				final Binary out = Binary.newFromTransient(digester.digest());
				if (i == 0) {
					System.out.println(out.dump(10));
				}
			}
			final long ms = System.currentTimeMillis() - tsStart;
			System.out.println("Elapsed=" + ms);
			// SHA-1 = 34ms, SHA-256=45ms, MD5=67ms,MD2=52ms,SHA-512=45ms
		} catch (final NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}

	}

}
