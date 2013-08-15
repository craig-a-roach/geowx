/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Binary;

/**
 * @author roach
 */
public class BerylliumIO {

	private static BinaryRef findBinaryRef(Class<?> refClass, String qccResourcePath) {
		final InputStream oins = refClass.getResourceAsStream(qccResourcePath);
		if (oins == null) return null;
		return new BinaryRef(qccResourcePath, oins);
	}

	private static boolean isModified(Request orq, Date oLastModified) {
		return oLastModified == null ? true : isModified(orq, oLastModified.getTime());
	}

	private static boolean isModified(Request orq, long tsLastModified) {
		if (orq == null) return true;
		if (tsLastModified <= 0L) return true;
		final long tsIfModifiedSince = orq.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
		if (tsIfModifiedSince <= 0L) return true;
		final long ms = Math.abs(tsLastModified - tsIfModifiedSince);
		return ms >= 1000L;
	}

	private static void setEntityHeaders(Request orq, HttpServletResponse rp, BerylliumPathMime pathMime) {
		final String ozAcceptEncoding = orq == null ? null : orq.getHeader(HttpHeaders.ACCEPT_ENCODING);
		final BerylliumPathMime.EntityHeaders entityHeaders = pathMime.newEntityHeaders(ozAcceptEncoding);
		rp.setContentType(entityHeaders.qlcContentType);
		if (entityHeaders.oqlcContentEncoding != null) {
			rp.setHeader(HttpHeaders.CONTENT_ENCODING, entityHeaders.oqlcContentEncoding);
		}
	}

	private static void writeStream(HttpServletResponse rp, InputStream ins, int bcBuffer)
			throws IOException {
		if (rp == null) throw new IllegalArgumentException("object is null");
		if (ins == null) throw new IllegalArgumentException("object is null");
		final ServletOutputStream sos = rp.getOutputStream();
		final byte[] buffer = new byte[bcBuffer];
		try {
			boolean more = true;
			while (more) {
				final int bcRead = ins.read(buffer);
				if (bcRead == -1) {
					more = false;
				} else {
					sos.write(buffer, 0, bcRead);
				}
			}
			sos.flush();
		} finally {
			closeSilent(ins);
			closeSilent(sos);
		}
	}

	private static void writeStream(Request orq, HttpServletResponse rp, String oqccCacheControl, BerylliumPathMime pathMime,
			Binary binary, Date oLastModified)
			throws IOException {
		if (rp == null) throw new IllegalArgumentException("object is null");
		if (pathMime == null) throw new IllegalArgumentException("object is null");
		if (binary == null) throw new IllegalArgumentException("object is null");
		rp.setStatus(HttpServletResponse.SC_OK);
		if (oqccCacheControl != null) {
			rp.setHeader(HttpHeaders.CACHE_CONTROL, oqccCacheControl);
		}
		if (oLastModified != null) {
			rp.setDateHeader(HttpHeaders.LAST_MODIFIED, oLastModified.getTime());
		}
		setEntityHeaders(orq, rp, pathMime);
		rp.setContentLength(binary.byteCount());
		final ServletOutputStream sos = rp.getOutputStream();
		try {
			sos.write(binary.zptReadOnly);
			sos.flush();
		} finally {
			closeSilent(sos);
		}
	}

	static void closeSilent(InputStream oins) {
		if (oins == null) return;
		try {
			oins.close();
		} catch (final IOException ex) {
		}
	}

	static void closeSilent(OutputStream oos) {
		if (oos == null) return;
		try {
			oos.close();
		} catch (final IOException ex) {
		}
	}

	public static void writeStream(HttpServletResponse rp, Binary content)
			throws IOException {
		if (rp == null) throw new IllegalArgumentException("object is null");
		if (content == null) throw new IllegalArgumentException("object is null");
		rp.setContentLength(content.byteCount());
		final ServletOutputStream sos = rp.getOutputStream();
		try {
			sos.write(content.zptReadOnly);
			sos.flush();
		} finally {
			closeSilent(sos);
		}
	}

	public static void writeStream(Request orq, HttpServletResponse rp, BerylliumPathMime pathMime, File srcFile)
			throws IOException {
		if (rp == null) throw new IllegalArgumentException("object is null");
		if (pathMime == null) throw new IllegalArgumentException("object is null");
		if (srcFile == null) throw new IllegalArgumentException("object is null");

		if (srcFile.canRead()) {
			final long tsLastModified = srcFile.lastModified();
			if (isModified(orq, tsLastModified)) {
				final long srcLength = srcFile.length();
				if (srcLength > CBeryllium.FileQuotaBc) {
					rp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
				} else {
					final FileInputStream fis = new FileInputStream(srcFile);
					rp.setStatus(HttpServletResponse.SC_OK);
					rp.setDateHeader(HttpHeaders.LAST_MODIFIED, tsLastModified);
					setEntityHeaders(orq, rp, pathMime);
					final int isrcLength = (int) srcLength;
					rp.setContentLength(isrcLength);
					final int bcBuffer = Math.min(isrcLength, CBeryllium.FileBufferBc);
					writeStream(rp, fis, bcBuffer);
				}
			} else {
				rp.setStatus(HttpStatus.NOT_MODIFIED_304);
			}
		} else {
			rp.setStatus(HttpStatus.NOT_FOUND_404);
		}
	}

	public static boolean writeStream(Request orq, HttpServletResponse rp, String oqccCacheControl, BerylliumPathMime pathMime,
			Class<?> refClass, String qccResourcePath, Date oLastModified)
			throws IOException {
		if (rp == null) throw new IllegalArgumentException("object is null");
		if (pathMime == null) throw new IllegalArgumentException("object is null");
		if (refClass == null) throw new IllegalArgumentException("object is null");
		if (qccResourcePath == null || qccResourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");

		final BinaryRef oBinaryRef = findBinaryRef(refClass, qccResourcePath);
		if (oBinaryRef == null) return false;

		try {
			if (isModified(orq, oLastModified)) {
				final Binary binary = oBinaryRef.newBinary();
				writeStream(orq, rp, oqccCacheControl, pathMime, binary, oLastModified);
			} else {
				rp.setStatus(HttpStatus.NOT_MODIFIED_304);
			}
			return true;
		} catch (final ArgonQuotaException ex) {
			rp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
			return true;
		} catch (final ArgonStreamReadException ex) {
			throw new IOException(ex.getMessage());
		} finally {
			oBinaryRef.discard();
		}
	}

	public static void writeStreamNoCache(HttpServletResponse rp, String qccContentType, Binary content, Date oLastModified)
			throws IOException {
		if (rp == null) throw new IllegalArgumentException("object is null");
		if (qccContentType == null || qccContentType.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (content == null) throw new IllegalArgumentException("object is null");
		rp.setStatus(HttpServletResponse.SC_OK);
		rp.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=0");
		if (oLastModified != null) {
			rp.setDateHeader(HttpHeaders.LAST_MODIFIED, oLastModified.getTime());
		}
		rp.setContentType(qccContentType);
		writeStream(rp, content);
	}

	public static void writeStreamOperatorMessage(HttpServletResponse rp, String zMessage)
			throws IOException {
		if (zMessage == null) throw new IllegalArgumentException("object is null");
		final Binary content = Binary.newFromStringUTF8(zMessage);
		writeStreamNoCache(rp, CBeryllium.OperatorContentType, content, null);
	}

	private BerylliumIO() {
	}

	static class BinaryRef {

		public void discard() {
			closeSilent(ins);
		}

		public Binary newBinary()
				throws ArgonQuotaException, ArgonStreamReadException {
			return Binary.newFromInputStream(ins, CBeryllium.ResourceBufferBc, qccResourcePath, CBeryllium.ResourceQuotaBc);
		}

		public BinaryRef(String qccResourcePath, InputStream ins) {
			assert ins != null;
			this.qccResourcePath = qccResourcePath;
			this.ins = ins;
		}
		final String qccResourcePath;
		final InputStream ins;
	}

}
