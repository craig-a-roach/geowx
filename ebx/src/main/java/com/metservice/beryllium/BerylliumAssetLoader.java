/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonSplitter;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Binary;
import com.metservice.argon.DateFactory;
import com.metservice.argon.Elapsed;
import com.metservice.argon.ElapsedFactory;
import com.metservice.argon.ElapsedFormatter;
import com.metservice.argon.ElapsedUnit;

/**
 * @author roach
 */
public class BerylliumAssetLoader {

	private static final Pattern RevEntrySplitter = Pattern.compile("\\s+");

	private RevEntry createRevEntry(String qtwLine) {
		final String[] zptqtwFields = ArgonSplitter.zptqtwSplit(qtwLine, RevEntrySplitter);
		if (zptqtwFields.length != 3) return null;
		try {
			final Pattern tailPattern = Pattern.compile(zptqtwFields[0]);
			final Date lastModified = DateFactory.newDateFromT8(zptqtwFields[1]);
			final Elapsed maxAge = ElapsedFactory.newElapsed(zptqtwFields[2]);
			return new RevEntry(tailPattern, lastModified, maxAge);
		} catch (final PatternSyntaxException ex) {
		} catch (final ArgonFormatException ex) {
		}
		return null;
	}

	private RevPolicy declareRevPolicy(BerylliumPath rpath) {
		if (m_oResourceRefClass == null) return NilPolicy;
		final String qtwAssetHome = rpath.qtwNode(0) + "/" + CBeryllium.RevResource;
		RevPolicy vRevPolicy = m_assetHome_Policy.get(qtwAssetHome);
		if (vRevPolicy == null) {
			vRevPolicy = newRevPolicy(qtwAssetHome);
			m_assetHome_Policy.put(qtwAssetHome, vRevPolicy);
		}
		return vRevPolicy;
	}

	private RevEntry find(BerylliumPath rpath) {
		return declareRevPolicy(rpath).find(rpath);
	}

	private boolean handleFile(BerylliumPath rpath, Request rq, HttpServletResponse rp, BerylliumPathMime pathMime)
			throws IOException {
		if (m_ocndirAssetHome == null) return false;
		final File srcFile = new File(m_ocndirAssetHome, rpath.qtwPath());
		if (srcFile.canRead()) {
			BerylliumIO.writeStream(rq, rp, pathMime, srcFile);
			return true;
		}
		return false;
	}

	private boolean handleResource(BerylliumPath rpath, Request rq, HttpServletResponse rp, BerylliumPathMime pathMime)
			throws IOException {
		if (m_oResourceRefClass == null) return false;
		final String qPath = rpath.qtwPath();
		final RevEntry oRevEntry = find(rpath);
		Date oLastModified = null;
		Elapsed maxAge = CBeryllium.DefaultResourceMaxAge;
		if (oRevEntry != null) {
			oLastModified = oRevEntry.lastModified;
			maxAge = oRevEntry.maxAge;
		}
		final String cacheControl = "max-age=" + ElapsedFormatter.formatUnitNoSuffix(maxAge.sms, ElapsedUnit.Seconds, true);
		return BerylliumIO.writeStream(rq, rp, cacheControl, pathMime, m_oResourceRefClass, qPath, oLastModified);
	}

	private RevPolicy newRevPolicy(String qtwAssetHome) {
		if (m_oResourceRefClass == null) return NilPolicy;
		final InputStream oins = m_oResourceRefClass.getResourceAsStream(qtwAssetHome);
		if (oins == null) return NilPolicy;
		final int bcEst = CBeryllium.ResourceBufferBc;
		final int bcQuota = CBeryllium.ResourceQuotaBc;
		try {
			final Binary binary = Binary.newFromInputStream(oins, bcEst, qtwAssetHome, bcQuota);
			final String srcLines = binary.newStringASCII();
			final String[] zptqtwLines = ArgonSplitter.zptzLines(srcLines, true, false);
			final int lineCount = zptqtwLines.length;
			final List<RevEntry> zlEntries = new ArrayList<RevEntry>(lineCount);
			for (int i = 0; i < lineCount; i++) {
				final String qtwLine = zptqtwLines[i];
				final RevEntry oRevEntry = createRevEntry(qtwLine);
				if (oRevEntry != null) {
					zlEntries.add(oRevEntry);
				}
			}
			return new RevPolicy(zlEntries);
		} catch (final ArgonQuotaException ex) {
		} catch (final ArgonStreamReadException e) {
		}
		return NilPolicy;
	}

	public void handle(BerylliumPath path, Request rq, HttpServletResponse rp)
			throws IOException {
		if (path == null) throw new IllegalArgumentException("object is null");
		if (rq == null) throw new IllegalArgumentException("object is null");
		if (rp == null) throw new IllegalArgumentException("object is null");
		final BerylliumPath rpath = path.relative();
		final BerylliumPathMime pathMime = m_mimeTable.mimeTypeByExtension(rpath);
		if (handleFile(rpath, rq, rp, pathMime)) return;
		if (handleResource(rpath, rq, rp, pathMime)) return;
		rp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	public BerylliumAssetLoader(File ocndirAssetHome, Class<?> oResourceRefClass) {
		m_ocndirAssetHome = ocndirAssetHome;
		m_oResourceRefClass = oResourceRefClass;
		m_mimeTable = new BerylliumMimeTypeTable();
	}

	private final File m_ocndirAssetHome;
	private final Class<?> m_oResourceRefClass;
	private final BerylliumMimeTypeTable m_mimeTable;
	private final Map<String, RevPolicy> m_assetHome_Policy = new HashMap<String, RevPolicy>(16);

	private static final RevPolicy NilPolicy = new RevPolicy();

	private static class RevEntry {

		@Override
		public String toString() {
			return tailPattern.toString() + " " + lastModified + " " + maxAge;
		}

		public RevEntry(Pattern tailPattern, Date lastModified, Elapsed maxAge) {
			this.tailPattern = tailPattern;
			this.lastModified = lastModified;
			this.maxAge = maxAge;
		}
		final Pattern tailPattern;
		final Date lastModified;
		final Elapsed maxAge;
	}

	private static class RevPolicy {

		public RevEntry find(BerylliumPath rpath) {
			if (zptEntries.length == 0) return null;
			if (rpath.depth < 2) return null;
			final String qPath = rpath.subPathTail(1).qtwPath();
			for (int i = 0; i < zptEntries.length; i++) {
				final RevEntry e = zptEntries[i];
				if (e.tailPattern.matcher(qPath).matches()) return e;
			}
			return null;
		}

		RevPolicy() {
			this.zptEntries = new RevEntry[0];
		}

		public RevPolicy(List<RevEntry> zlEntries) {
			assert zlEntries != null;
			this.zptEntries = zlEntries.toArray(new RevEntry[zlEntries.size()]);
		}
		final RevEntry[] zptEntries;
	}
}
