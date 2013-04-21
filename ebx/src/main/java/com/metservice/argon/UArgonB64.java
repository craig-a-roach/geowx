/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
class UArgonB64 {

	private static final char pad = '=';
	private static final char[] nibble2code = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
			'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
			'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', '+', '/' };

	private static final UArgonB64 Instance = new UArgonB64();

	private byte[] decode(char[] zptch)
			throws ArgonFormatException {
		if (zptch == null) throw new IllegalArgumentException("object is null");
		final int bLen = zptch.length;
		if (bLen % 4 != 0) {
			final String m = "Malformed B64 string; input block size is not 4";
			throw new ArgonFormatException(m);
		}
		int li = bLen - 1;
		while (li >= 0 && zptch[li] == (byte) pad) {
			li--;
		}
		if (li < 0) return new byte[0];

		final int rLen = ((li + 1) * 3) / 4;
		final byte r[] = new byte[rLen];
		int ri = 0;
		int bi = 0;
		final int stop = (rLen / 3) * 3;
		byte b0, b1, b2, b3;
		try {
			while (ri < stop) {
				b0 = code2nibble[zptch[bi++]];
				b1 = code2nibble[zptch[bi++]];
				b2 = code2nibble[zptch[bi++]];
				b3 = code2nibble[zptch[bi++]];
				if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
					final String m = "Malformed B64 string; invalid encoding";
					throw new IllegalArgumentException(m);
				}
				r[ri++] = (byte) (b0 << 2 | b1 >>> 4);
				r[ri++] = (byte) (b1 << 4 | b2 >>> 2);
				r[ri++] = (byte) (b2 << 6 | b3);
			}
			if (rLen != ri) {
				switch (rLen % 3) {
					case 2:
						b0 = code2nibble[zptch[bi++]];
						b1 = code2nibble[zptch[bi++]];
						b2 = code2nibble[zptch[bi++]];
						if (b0 < 0 || b1 < 0 || b2 < 0) {
							final String m = "Malformed B64 string; invalid encoding";
							throw new IllegalArgumentException(m);
						}
						r[ri++] = (byte) (b0 << 2 | b1 >>> 4);
						r[ri++] = (byte) (b1 << 4 | b2 >>> 2);
					break;
					case 1:
						b0 = code2nibble[zptch[bi++]];
						b1 = code2nibble[zptch[bi++]];
						if (b0 < 0 || b1 < 0) {
							final String m = "Malformed B64 string; invalid encoding";
							throw new IllegalArgumentException(m);
						}
						r[ri++] = (byte) (b0 << 2 | b1 >>> 4);
					break;
					default:
					break;
				}
			}
		} catch (final RuntimeException ex) {
			final String m = "Malformed B64 string; character " + bi + " was not correctly encoded";
			throw new IllegalArgumentException(m);
		}
		return r;
	}

	private static char[] encode(byte[] zpt) {
		if (zpt == null) throw new IllegalArgumentException("object is null");
		final int bLen = zpt.length;
		final int cap = ((bLen + 2) / 3) * 4;
		final char r[] = new char[cap];
		int ri = 0;
		int bi = 0;
		byte b0, b1, b2;
		final int stop = (bLen / 3) * 3;
		while (bi < stop) {
			b0 = zpt[bi++];
			b1 = zpt[bi++];
			b2 = zpt[bi++];
			r[ri++] = nibble2code[(b0 >>> 2) & 0x3f];
			r[ri++] = nibble2code[(b0 << 4) & 0x3f | (b1 >>> 4) & 0x0f];
			r[ri++] = nibble2code[(b1 << 2) & 0x3f | (b2 >>> 6) & 0x03];
			r[ri++] = nibble2code[b2 & 077];
		}
		if (bLen != bi) {
			switch (bLen % 3) {
				case 2:
					b0 = zpt[bi++];
					b1 = zpt[bi++];
					r[ri++] = nibble2code[(b0 >>> 2) & 0x3f];
					r[ri++] = nibble2code[(b0 << 4) & 0x3f | (b1 >>> 4) & 0x0f];
					r[ri++] = nibble2code[(b1 << 2) & 0x3f];
					r[ri++] = pad;
				break;
				case 1:
					b0 = zpt[bi++];
					r[ri++] = nibble2code[(b0 >>> 2) & 0x3f];
					r[ri++] = nibble2code[(b0 << 4) & 0x3f];
					r[ri++] = pad;
					r[ri++] = pad;
				break;
				default:
				break;
			}
		}
		return r;
	}

	public static char[] new_zptchB64(Binary src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		return encode(src.zptReadOnly);
	}

	public static String newB64(Binary src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		return new String(encode(src.zptReadOnly));
	}

	public static Binary newBinaryFromB64(char[] zptchB64)
			throws ArgonFormatException {
		if (zptchB64 == null) throw new IllegalArgumentException("object is null");
		return Binary.newFromTransient(Instance.decode(zptchB64));
	}

	public static Binary newBinaryFromB64(String zB64)
			throws ArgonFormatException {
		if (zB64 == null) throw new IllegalArgumentException("object is null");
		return newBinaryFromB64(zB64.toCharArray());
	}

	private UArgonB64() {
		code2nibble = new byte[256];
		for (int i = 0; i < 256; i++) {
			code2nibble[i] = -1;
		}
		for (byte b = 0; b < 64; b++) {
			code2nibble[(byte) nibble2code[b]] = b;
		}
		code2nibble[(byte) pad] = 0;
	}
	private final byte[] code2nibble;

}
