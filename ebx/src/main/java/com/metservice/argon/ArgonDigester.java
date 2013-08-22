/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author roach
 */
public class ArgonDigester {

	private static ArgonDigester newStandardInstance(String algorithm) {
		try {
			return new ArgonDigester(MessageDigest.getInstance(algorithm));
		} catch (final NoSuchAlgorithmException ex) {
			final String m = "Standard digest algorithm '" + algorithm + "'  is not available..." + Ds.message(ex);
			throw new IllegalStateException(m);
		}
	}

	public static ArgonDigester newInstance(String algorithm)
			throws ArgonPlatformException {
		try {
			return new ArgonDigester(MessageDigest.getInstance(algorithm));
		} catch (final NoSuchAlgorithmException ex) {
			final String m = "Required digest algorithm '" + algorithm + "'  is not available..." + Ds.message(ex);
			throw new ArgonPlatformException(m);
		}
	}

	public static ArgonDigester newMD5() {
		return newStandardInstance("MD5");
	}

	public static ArgonDigester newSHA1() {
		return newStandardInstance("SHA-1");
	}

	public static ArgonDigester newSHA256() {
		return newStandardInstance("SHA-256");
	}

	public Binary digest(Binary src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		return Binary.newFromTransient(digest(src.zptReadOnly));
	}

	public byte[] digest(byte[] zptReadOnly) {
		if (zptReadOnly == null) throw new IllegalArgumentException("object is null");
		m_lock.lock();
		try {
			m_imp.reset();
			m_imp.update(zptReadOnly);
			return m_imp.digest();
		} finally {
			m_lock.unlock();
		}
	}

	public Binary digestUTF8(String src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		return Binary.newFromTransient(digest(src.getBytes(UArgon.UTF8)));
	}

	public String digestUTF8B64URL(String src) {
		return digestUTF8(src).newB64URL();
	}

	private ArgonDigester(MessageDigest imp) {
		assert imp != null;
		m_imp = imp;
	}

	private final Lock m_lock = new ReentrantLock();
	private final MessageDigest m_imp;
}
