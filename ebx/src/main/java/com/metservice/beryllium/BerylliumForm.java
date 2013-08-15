/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.BinaryOutputStream;
import com.metservice.argon.file.ArgonFileManagement;

/**
 * @author roach
 */
public class BerylliumForm {

	private static final Pattern DispositionSplitter = Pattern.compile(";");

	private static boolean consumePart(InputStream in, byte[] byteBoundary, FieldTarget oFieldTarget)
			throws IOException {
		int state = -2;
		int c;
		boolean cr = false;
		boolean lf = false;

		while (true) {
			int b = 0;
			while ((c = (state != -2) ? state : in.read()) != -1) {
				state = -2;
				if (c == 13 || c == 10) {
					if (c == 13) {
						state = in.read();
					}
					break;
				}
				if (b >= 0 && b < byteBoundary.length && c == byteBoundary[b]) {
					b++;
				} else {
					if (cr) {
						write(oFieldTarget, 13);
					}
					if (lf) {
						write(oFieldTarget, 10);
					}
					cr = lf = false;
					if (b > 0) {
						write(oFieldTarget, byteBoundary, b);
					}
					b = -1;
					write(oFieldTarget, c);
				}
			}
			if ((b > 0 && b < byteBoundary.length - 2) || (b == byteBoundary.length - 1)) {
				if (cr) {
					write(oFieldTarget, 13);
				}
				if (lf) {
					write(oFieldTarget, 10);
				}
				cr = lf = false;
				write(oFieldTarget, byteBoundary, b);
				b = -1;
			}
			if (b > 0 || c == -1) {
				final boolean moreParts = b < byteBoundary.length;
				return moreParts;
			}
			if (cr) {
				write(oFieldTarget, 13);
			}
			if (lf) {
				write(oFieldTarget, 10);
			}
			cr = (c == 13);
			lf = (c == 10 || state == 10);
			if (state == 10) {
				state = -2;
			}
		}
	}

	private static FieldTarget createFieldTarget(String qtwContentDisposition, File cndirTarget)
			throws ArgonPermissionException, IOException {
		assert qtwContentDisposition != null && qtwContentDisposition.length() > 0;
		assert cndirTarget != null;
		final String[] zptParts = DispositionSplitter.split(qtwContentDisposition);
		boolean isFormData = false;
		String oztwName = null;
		String oztwFileName = null;
		for (int i = 0; i < zptParts.length; i++) {
			final String ztwPart = zptParts[i].trim();
			if (ztwPart.length() == 0) {
				continue;
			}
			final String zlctwPart = ztwPart.toLowerCase();
			if (zlctwPart.startsWith("form-data")) {
				isFormData = true;
			} else if (zlctwPart.startsWith("name=")) {
				oztwName = ztwValue(ztwPart);
			} else if (zlctwPart.startsWith("filename=")) {
				oztwFileName = ztwValue(ztwPart);
			}
		}
		if (!isFormData) return null;
		if (oztwName == null || oztwName.length() == 0) return null;

		if (oztwFileName == null) {
			final BinaryOutputStream bos = new BinaryOutputStream(CBeryllium.FormDataBufferBc);
			return new FieldTargetString(oztwName, bos);
		}
		if (oztwFileName.length() == 0) return null;
		final File wipFile = new File(cndirTarget, oztwFileName + CBeryllium.FileUploadSuffix);
		final File targetFile = new File(cndirTarget, oztwFileName);
		final FileOutputStream fos = ArgonFileManagement.newFileOutputStream(wipFile);
		final OutputStream bos = new BufferedOutputStream(fos, CBeryllium.FileUploadBufferBc);
		return new FieldTargetFile(oztwName, bos, wipFile, targetFile);
	}

	private static String findContentDisposition(InputStream in)
			throws IOException {
		String oqtwContentDisposition = null;
		boolean more = true;
		while (more) {
			final byte[] bytes = TypeUtil.readLine(in);
			if (bytes == null || bytes.length == 0) {
				more = false;
			} else {
				final String qLine = new String(bytes, ArgonText.UTF8);

				final int posColon = qLine.indexOf(':', 0);
				if (posColon > 0) {
					final String qlctwKey = qLine.substring(0, posColon).trim().toLowerCase();
					final String ztwValue = qLine.substring(posColon + 1).trim();
					if (qlctwKey.equals("content-disposition") && ztwValue.length() > 0) {
						oqtwContentDisposition = ztwValue;
					}
				}
			}
		}
		return oqtwContentDisposition;
	}

	private static Logger logger() {
		return Log.getLogger("Form");
	}

	private static BerylliumForm newFormEncoded(Request rq, String qlctwContentType) {
		assert rq != null;
		return new BerylliumForm(rq.getParameters());
	}

	private static BerylliumForm newMultipartFormData(Request rq, String qtwContentType, InputStream in, File cndirTarget)
			throws ArgonPermissionException, IOException {
		assert rq != null;
		assert qtwContentType != null && qtwContentType.length() > 0;
		assert in != null;
		assert cndirTarget != null;

		final int bpos = qtwContentType.indexOf("boundary=");
		if (bpos < 0) {
			logger().debug("Malformed content type boundary '" + qtwContentType + "'");
			throw new IOException("Malformed request");
		}
		final String boundary = "--" + ztwValue(qtwContentType.substring(bpos));
		final byte[] byteBoundary = (boundary + "--").getBytes(ArgonText.UTF8);
		final byte[] bytes = TypeUtil.readLine(in);
		final String ozLine = bytes == null ? null : new String(bytes, ArgonText.UTF8);
		if (ozLine == null) {
			logger().debug("Missing initial multi part boundary '" + boundary + "' in null line");
			throw new IOException("Malformed request; missing initial multi part boundary");
		}
		if (!ozLine.equals(boundary)) {
			logger().warn("Missing initial multi part boundary '" + boundary + "' in line '" + ozLine + "'");
			throw new IOException("Malformed request; missing initial multi part boundary");
		}

		final MultiMap<String> mapResult = rq.getParameters();
		boolean moreParts = true;
		while (moreParts) {
			final String qtwContentDisposition = selectContentDisposition(in);
			final FieldTarget oFieldTarget = createFieldTarget(qtwContentDisposition, cndirTarget);
			try {
				moreParts = consumePart(in, byteBoundary, oFieldTarget);
			} finally {
				if (oFieldTarget != null) {
					oFieldTarget.close();
					oFieldTarget.addToMap(mapResult);
				}
			}
		}
		return new BerylliumForm(mapResult);
	}

	private static BerylliumForm newNoContentType(Request rq) {
		assert rq != null;
		return new BerylliumForm(rq.getParameters());
	}

	private static String selectContentDisposition(InputStream in)
			throws IOException {
		final String oqContentDisposition = findContentDisposition(in);
		if (oqContentDisposition == null) throw new IOException("Malformed request; Missing content-disposition");
		return oqContentDisposition;
	}

	private static void write(FieldTarget oTarget, byte[] b, int len)
			throws IOException {
		if (oTarget != null) {
			oTarget.outputStream.write(b, 0, len);
		}
	}

	private static void write(FieldTarget oTarget, int b)
			throws IOException {
		if (oTarget != null) {
			oTarget.outputStream.write(b);
		}
	}

	private static String ztwValue(String nameEqualsValue)
			throws IOException {
		final int posEq = nameEqualsValue.indexOf('=');
		if (posEq < 0) {
			logger().debug("Malformed encoding '" + nameEqualsValue + "'; expecting name=value");
			throw new IOException("Malformed request");
		}
		String ztwValue = nameEqualsValue.substring(posEq + 1).trim();
		final int posSemi = ztwValue.indexOf(';');
		if (posSemi > 0) {
			ztwValue = ztwValue.substring(0, posSemi);
		}
		if (ztwValue.startsWith("\"")) {
			final int posQuote = ztwValue.indexOf('"', 1);
			if (posQuote < 1) {
				logger().debug("Malformed encoding '" + nameEqualsValue + "'; unbalanced quote");
				throw new IOException("Malformed request");
			}
			ztwValue = ztwValue.substring(1, posQuote);
		} else {
			final int posSpace = ztwValue.indexOf(' ');
			if (posSpace > 0) {
				ztwValue = ztwValue.substring(0, posSpace);
			}
		}
		return ztwValue;
	}

	public static BerylliumForm newInstance(Request rq) {
		final String ozContentType = rq.getContentType();
		final String zlctwContentType = ozContentType == null ? "" : ozContentType.trim().toLowerCase();
		if (zlctwContentType.length() == 0) return newNoContentType(rq);
		if (zlctwContentType.startsWith(MimeTypes.FORM_ENCODED)) return newFormEncoded(rq, zlctwContentType);
		logger().warn("Unexpected content-type '" + zlctwContentType + "'; using default");
		return newNoContentType(rq);
	}

	public static BerylliumForm newInstance(Request rq, File cndirTarget)
			throws ArgonPermissionException, IOException {
		if (rq == null) throw new IllegalArgumentException("object is null");
		if (cndirTarget == null) throw new IllegalArgumentException("object is null");

		final String ozContentType = rq.getContentType();
		final String ztwContentType = ozContentType == null ? "" : ozContentType.trim();
		if (ztwContentType.length() == 0) return newNoContentType(rq);

		final String zlctwContentType = ztwContentType.toLowerCase();
		if (zlctwContentType.startsWith("multipart/form-data")) {
			final BufferedInputStream in = new BufferedInputStream(rq.getInputStream());
			try {
				return newMultipartFormData(rq, ztwContentType, in, cndirTarget);
			} finally {
				try {
					in.close();
				} catch (final IOException ex) {
				}
			}
		}
		if (zlctwContentType.startsWith(MimeTypes.FORM_ENCODED)) return newFormEncoded(rq, zlctwContentType);

		logger().warn("Unsupported content-type '" + zlctwContentType + "'; using default");
		return newNoContentType(rq);
	}

	public File getFileValue(String fieldName)
			throws BerylliumApiException {
		if (fieldName == null || fieldName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final Object oValue = m_map.get(fieldName);
		if (oValue == null) return null;
		if (oValue instanceof File) return (File) oValue;
		final String m = "Unexpected value for '" + fieldName + "'; " + oValue.getClass();
		logger().debug(m);
		throw new BerylliumApiException(m);
	}

	public boolean isOn(String fieldName) {
		final Object oValue = m_map.get(fieldName);
		return oValue != null;
	}

	public String oqTextValue(String fieldName) {
		final Object oValue = m_map.get(fieldName);
		if (oValue == null) return null;
		final String zValue;
		if (oValue instanceof String[]) {
			final String[] zptValue = (String[]) oValue;
			if (zptValue.length == 0) return null;
			zValue = zptValue[0];
		} else {
			zValue = oValue.toString();
		}
		if (zValue.length() == 0) return null;
		return zValue;
	}

	private BerylliumForm(MultiMap<String> map) {
		assert map != null;
		m_map = map;
	}

	private final MultiMap<String> m_map;

	private static abstract class FieldTarget {

		public abstract void addToMap(Map<String, Object> map)
				throws ArgonPermissionException;

		public void close() {
			try {
				outputStream.close();
			} catch (final IOException ex) {
			}
		}

		public FieldTarget(String qcctwName, OutputStream outputStream) {
			assert qcctwName != null && qcctwName.length() > 0;
			assert outputStream != null;
			this.qcctwName = qcctwName;
			this.outputStream = outputStream;
		}
		final String qcctwName;
		final OutputStream outputStream;
	}

	private static class FieldTargetFile extends FieldTarget {

		@Override
		public void addToMap(Map<String, Object> map)
				throws ArgonPermissionException {
			ArgonFileManagement.renameFile(wipFile, destFile);
			map.put(qcctwName, destFile);
		}

		@Override
		public String toString() {
			return qcctwName + "=" + wipFile;
		}

		public FieldTargetFile(String qcctwName, OutputStream outputStream, File wipFile, File destFile) {
			super(qcctwName, outputStream);
			assert wipFile != null;
			assert destFile != null;
			this.wipFile = wipFile;
			this.destFile = destFile;
		}
		final File wipFile;
		final File destFile;
	}

	private static class FieldTargetString extends FieldTarget {

		@Override
		public void addToMap(Map<String, Object> map)
				throws ArgonPermissionException {
			final Binary binary = m_binaryStream.newBinary();
			final String zValue = binary.newStringUTF8();
			map.put(qcctwName, zValue);
		}

		@Override
		public String toString() {
			return qcctwName;
		}

		public FieldTargetString(String qcctwName, BinaryOutputStream bos) {
			super(qcctwName, bos);
			assert bos != null;
			m_binaryStream = bos;
		}
		private final BinaryOutputStream m_binaryStream;
	}
}
