/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.File;
import java.util.Properties;

import com.metservice.argon.file.ArgonCompactLoader;
import com.metservice.argon.file.ArgonDirectoryManagement;

/**
 * @author roach
 */
public class ArgonProperties {

	static final ArgonProperties Empty = new ArgonProperties(null);

	public static BuilderFromArgs newBuilder(ArgonArgs source) {
		if (source == null) throw new IllegalArgumentException("object is null");
		return new BuilderFromArgs(source);
	}

	public static BuilderFromProps newBuilderFromProperties() {
		return new BuilderFromProps();
	}

	private String oqtwValue(String pname) {
		final String ozValue = ozValue(pname);
		if (ozValue == null) return null;
		final String ztwValue = ozValue.trim();
		return ztwValue.length() == 0 ? null : ztwValue;
	}

	private String ozValue(String pname) {
		assert pname != null && pname.length() > 0;
		if (m_oProperties == null) return null;
		return m_oProperties.getProperty(pname);
	}

	public ArgonPropertiesAttribute find(String pname) {
		if (pname == null || pname.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String oqtwValue = oqtwValue(pname);
		if (oqtwValue == null) return null;
		return new ArgonPropertiesAttribute(pname, oqtwValue);
	}

	public ArgonPropertiesAttribute select(String pname)
			throws ArgonArgsException {
		final ArgonPropertiesAttribute oatt = find(pname);
		if (oatt != null) return oatt;
		final String m = "Missing '" + pname + "'; a value is required";
		throw new ArgonArgsException(m);
	}

	@Override
	public String toString() {
		return m_oProperties == null ? "" : m_oProperties.toString();
	}

	ArgonProperties(Properties oProperties) {
		m_oProperties = oProperties;
	}
	private final Properties m_oProperties;

	public static class BuilderFromArgs {

		public boolean consumeFlag(String qTagSpec) {
			return m_source.consumeFlag(qTagSpec);
		}

		public ArgonProperties newProperties() {
			if (m_dest.isEmpty()) return Empty;
			return new ArgonProperties(m_dest);
		}

		public void printlnUnsupportedMessage() {
			final String zUnsupported = m_source.toString();
			if (zUnsupported.length() == 0) return;
			final String m = "Ignoring unsupported command line options '" + zUnsupported + "'";
			System.err.println(m);
		}

		public BuilderFromArgs putAssignments() {
			m_dest.putAll(ArgonTransformer.newPropertiesFromAssignments(m_source.consumeAllUntaggedValues()));
			return this;
		}

		public BuilderFromArgs putFiles(String tagSpec) {
			if (tagSpec == null || tagSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");
			final String[] zptqtwFilePaths = m_source.consumeAllTagValuePairs(tagSpec).zptqtwValues();
			for (int i = 0; i < zptqtwFilePaths.length; i++) {
				putPropertiesFile(new File(zptqtwFilePaths[i]));
			}
			return this;
		}

		public BuilderFromArgs putMappedArg(String destPropertyName)
				throws ArgonArgsException {
			return putMappedArg(destPropertyName, destPropertyName);
		}

		public BuilderFromArgs putMappedArg(String sourceArgName, String destPropertyName)
				throws ArgonArgsException {
			if (sourceArgName == null || sourceArgName.length() == 0)
				throw new IllegalArgumentException("string is null or empty");
			if (destPropertyName == null || destPropertyName.length() == 0)
				throw new IllegalArgumentException("string is null or empty");
			final String oqtwValue = m_source.consumeAllTagValuePairs(sourceArgName).oqtwValue();
			if (oqtwValue != null) {
				m_dest.put(destPropertyName, oqtwValue);
			}
			return this;
		}

		public BuilderFromArgs putPropertiesFile(File path) {
			if (path == null) throw new IllegalArgumentException("object is null");
			try {
				final Properties oProperties = ArgonCompactLoader.findProperties(path);
				if (oProperties != null) {
					m_dest.putAll(oProperties);
				}
			} catch (final ArgonStreamReadException ex) {
				final String m = "Cannot access properties in '" + path + "'...skipping (" + ex.getMessage() + ")";
				System.err.println(m);
			}
			return this;
		}

		public BuilderFromArgs putPropertiesUserFile(String qccFileName) {
			if (qccFileName == null || qccFileName.length() == 0)
				throw new IllegalArgumentException("string is null or empty");
			return putPropertiesFile(new File(ArgonDirectoryManagement.CnDirUserHome, qccFileName));
		}

		public BuilderFromArgs putProperty(String propertyName, boolean value) {
			return putProperty(propertyName, Boolean.toString(value));
		}

		public BuilderFromArgs putProperty(String propertyName, int value) {
			return putProperty(propertyName, Integer.toString(value));
		}

		public BuilderFromArgs putProperty(String propertyName, String zValue) {
			if (propertyName == null || propertyName.length() == 0)
				throw new IllegalArgumentException("string is null or empty");
			if (zValue == null) throw new IllegalArgumentException("object is null");
			m_dest.put(propertyName, zValue);
			return this;
		}

		BuilderFromArgs(ArgonArgs source) {
			if (source == null) throw new IllegalArgumentException("object is null");
			m_source = source;
		}
		private final ArgonArgs m_source;
		private final Properties m_dest = new Properties();
	}

	public static class BuilderFromProps {

		public BuilderFromProps add(Properties... ozptSources) {
			if (ozptSources == null || ozptSources.length == 0) return this;
			for (int i = 0; i < ozptSources.length; i++) {
				final Properties oSource = ozptSources[i];
				if (oSource != null) {
					m_dest.putAll(oSource);
				}
			}
			return this;
		}

		public ArgonProperties newProperties() {
			if (m_dest.isEmpty()) return Empty;
			return new ArgonProperties(m_dest);
		}

		BuilderFromProps() {

		}
		private final Properties m_dest = new Properties();
	}
}
