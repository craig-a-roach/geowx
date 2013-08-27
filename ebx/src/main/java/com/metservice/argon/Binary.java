/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

/**
 * @author roach
 */
public class Binary {

	public static final Binary Empty = new Binary(new byte[0]);
	public static final int QuotaClassPath = 256 * CArgon.M;

	public static Binary createFromClassPath(Class<?> resourceRef, String qccPath) {
		return createFromClassPath(resourceRef, qccPath, QuotaClassPath);
	}

	public static Binary createFromClassPath(Class<?> resourceRef, String qccPath, int bcQuota) {
		if (resourceRef == null) throw new IllegalArgumentException("object is null");
		final InputStream oins = resourceRef.getResourceAsStream(qccPath);
		if (oins == null) return null;
		try {
			return newFromInputStream(oins, 0L, qccPath, bcQuota);
		} catch (final ArgonQuotaException ex) {
			throw new IllegalStateException("Failed to load Binary from class path; too large..." + ex.getMessage());
		} catch (final ArgonStreamReadException ex) {
			throw new IllegalStateException("Failed to load Binary from class path; reading..." + ex.getMessage());
		}
	}

	public static Binary createFromFile(File srcFile, int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException {
		FileInputStream fis;
		try {
			fis = new FileInputStream(srcFile);
		} catch (final FileNotFoundException ex) {
			return null;
		}
		final long bcSrc = srcFile.length();
		try {
			return UArgonIO.newInstance(fis, bcSrc, srcFile.getPath(), bcQuota);
		} finally {
			try {
				fis.close();
			} catch (final IOException ex) {
			}
		}
	}

	public static Binary createFromFile(IArgonFileProbe oprobe, File srcFile, int bcQuota) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(srcFile);
		} catch (final FileNotFoundException ex) {
			return null;
		}
		final long bcSrc = srcFile.length();
		try {
			return UArgonIO.newInstance(fis, bcSrc, srcFile.getPath(), bcQuota);
		} catch (final ArgonQuotaException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo("Read complete file contents into binary array", ex, "Return null");
				ds.a("bcQuota", bcQuota);
				oprobe.failFile(ds, srcFile);
			}
			return null;
		} catch (final ArgonStreamReadException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo("Read complete file contents into binary array", ex, "Return null");
				oprobe.failFile(ds, srcFile);
			}
			return null;
		} finally {
			try {
				fis.close();
			} catch (final IOException ex) {
			}
		}
	}

	public static Binary newFromB64MIME(String zsrc)
			throws ArgonFormatException {
		if (zsrc == null) throw new IllegalArgumentException("object is null");
		return UArgonB64.newBinaryFromB64MIME(zsrc);
	}

	public static Binary newFromB64URL(String zsrc)
			throws ArgonFormatException {
		if (zsrc == null) throw new IllegalArgumentException("object is null");
		return UArgonB64.newBinaryFromB64URL(zsrc);
	}

	public static Binary newFromInputStream(InputStream ins, int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException {
		return UArgonIO.newInstance(ins, 0L, null, bcQuota);
	}

	public static Binary newFromInputStream(InputStream ins, long bcEst, String ozSourceName, int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException {
		return UArgonIO.newInstance(ins, bcEst, ozSourceName, bcQuota);
	}

	public static Binary newFromString(Charset charset, String ozSource) {
		if (charset == null) throw new IllegalArgumentException("object is null");
		if (ozSource == null || ozSource.length() == 0) return Empty;
		return new Binary(ozSource.getBytes(charset));
	}

	public static Binary newFromStringASCII(String ozSource) {
		return newFromString(UArgon.ASCII, ozSource);
	}

	public static Binary newFromStringISO8859(String ozSource) {
		return newFromString(UArgon.ISO8859_1, ozSource);
	}

	public static Binary newFromStringUTF8(String ozSource) {
		return newFromString(UArgon.UTF8, ozSource);
	}

	public static Binary newFromTransient(byte[] ozptReadOnly) {
		if (ozptReadOnly == null || ozptReadOnly.length == 0) return Empty;
		return new Binary(ozptReadOnly);
	}

	public static Binary newFromTransient(byte[] ozpReadOnly, int lenReqd) {
		if (ozpReadOnly == null) return Empty;
		final int lenSrc = ozpReadOnly.length;
		if (lenReqd == 0 || lenSrc == 0) return Empty;
		final byte[] zptReadOnly;
		if (lenSrc == lenReqd) {
			zptReadOnly = ozpReadOnly;
		} else {
			zptReadOnly = new byte[lenReqd];
			System.arraycopy(ozpReadOnly, 0, zptReadOnly, 0, lenReqd);
		}
		return new Binary(zptReadOnly);
	}

	public static Binary newSnapshot(byte[] ozptMutable) {
		if (ozptMutable == null) return Empty;
		final int len = ozptMutable.length;
		if (len == 0) return Empty;
		final byte[] zptReadOnly = new byte[len];
		System.arraycopy(ozptMutable, 0, zptReadOnly, 0, len);
		return new Binary(zptReadOnly);
	}

	public int byteCount() {
		return zptReadOnly.length;
	}

	public String dump(int maxLines) {
		return Ds.dump(zptReadOnly, maxLines);
	}

	public boolean equals(Binary r) {
		if (r == this) return true;
		if (r == null) return false;
		final int llength = zptReadOnly.length;
		final int rlength = r.zptReadOnly.length;
		if (llength != rlength) return false;
		for (int i = 0; i < llength; i++) {
			if (zptReadOnly[i] != r.zptReadOnly[i]) return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof Binary)) return false;
		return equals((Binary) o);
	}

	public ImageInputStream getImageInputStream() {
		return new MemoryCacheImageInputStream(getInputStream());
	}

	public InputStream getInputStream() {
		return new In(zptReadOnly);
	}

	@Override
	public int hashCode() {
		int hash = HashCoder.INIT;
		final int c = zptReadOnly.length;
		for (int i = 0; i < c; i++) {
			hash = HashCoder.and(hash, zptReadOnly[i]);
		}
		return hash;
	}

	public boolean isEmpty() {
		return zptReadOnly.length == 0;
	}

	public String newB64MIME() {
		return UArgonB64.newB64MIME(this);
	}

	public String newB64URL() {
		return UArgonB64.newB64URL(this);
	}

	public Binary newGZipDecoded()
			throws ArgonQuotaException, ArgonStreamReadException {
		return UArgonIOGz.newDecoded(this);
	}

	public Binary newGZipDecoded(int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException {
		return UArgonIOGz.newDecoded(this, null, bcQuota);
	}

	public Binary newGZipEncoded()
			throws ArgonStreamWriteException {
		return UArgonIOGz.newEncoded(this);
	}

	public String newString(Charset charset) {
		if (charset == null) throw new IllegalArgumentException("object is null");
		if (zptReadOnly.length == 0) return "";
		return new String(zptReadOnly, charset);
	}

	public String newStringASCII() {
		return newString(UArgon.ASCII);
	}

	public String newStringISO8859() {
		return newString(UArgon.ISO8859_1);
	}

	public String newStringUTF8() {
		return newString(UArgon.UTF8);
	}

	public List<ArgonZipItem> newZipDecodedAscName(int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException {
		return UArgonIOZip.zlDecodedAscName(this, null, bcQuota);
	}

	public void save(File destFile, boolean append)
			throws ArgonPermissionException, ArgonStreamWriteException {
		final FileOutputStream fos;
		try {
			fos = new FileOutputStream(destFile, append);
		} catch (final FileNotFoundException ex) {
			final Ds ds = Ds.triedTo("Open file for saving binary array", ex, ArgonPermissionException.class);
			ds.a("destFile", destFile);
			ds.a("append", append);
			throw new ArgonPermissionException(ds.s());
		}

		try {
			fos.write(zptReadOnly, 0, zptReadOnly.length);
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Save binary array to file", ex, ArgonStreamWriteException.class);
			ds.a("destFile", destFile);
			ds.a("append", append);
			throw new ArgonStreamWriteException(ds.s());
		} finally {
			try {
				fos.close();
			} catch (final IOException ex) {
			}
		}
	}

	public boolean save(IArgonFileProbe oprobe, File destFile, boolean append) {
		final FileOutputStream fos;
		try {
			fos = new FileOutputStream(destFile, append);
		} catch (final FileNotFoundException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo("Open file for saving binary array", ex, "Return error flag");
				ds.a("append", append);
				oprobe.failFile(ds, destFile);
			}
			return false;
		}
		try {
			fos.write(zptReadOnly, 0, zptReadOnly.length);
			return true;
		} catch (final IOException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo("Save binary array to file", ex, "Return error flag");
				ds.a("append", append);
				oprobe.failFile(ds, destFile);
			}
			return false;
		} finally {
			try {
				fos.close();
			} catch (final IOException ex) {
			}
		}
	}

	@Override
	public String toString() {
		return Ds.dump(zptReadOnly);
	}

	private Binary(byte[] zptReadOnly) {
		assert zptReadOnly != null;
		this.zptReadOnly = zptReadOnly;
	}

	public final byte[] zptReadOnly;

	private static class In extends InputStream {

		@Override
		public int read() {
			return (m_pos < m_count) ? (m_zptReadOnly[m_pos++] & 0xff) : -1;
		}

		@Override
		public final int read(byte b[], int off, int len) {
			if (b == null) throw new NullPointerException("No buffer supplied by caller");
			if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
				final String m = "Invalid offset (" + off + "), length (" + len + ") arguments supplied by caller";
				throw new IndexOutOfBoundsException(m);
			}

			if (m_pos >= m_count) return -1;

			final int clen = ((m_pos + len) > m_count) ? m_count - m_pos : len;
			if (clen <= 0) return 0;

			System.arraycopy(m_zptReadOnly, m_pos, b, off, clen);
			m_pos += clen;
			return clen;
		}

		@Override
		public final synchronized void reset()
				throws IOException {
			m_pos = 0;
		}

		@Override
		public String toString() {
			return Integer.toString(m_pos) + ":" + Integer.toString(m_count);
		}

		public In(byte[] zptReadOnly) {
			assert zptReadOnly != null;
			m_zptReadOnly = zptReadOnly;
			m_count = zptReadOnly.length;
			m_pos = 0;
		}

		private final byte[] m_zptReadOnly;
		private final int m_count;
		private int m_pos;
	}// In
}
