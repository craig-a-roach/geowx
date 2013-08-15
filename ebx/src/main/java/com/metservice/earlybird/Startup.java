/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonArgs;
import com.metservice.argon.ArgonArgsException;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonProperties;
import com.metservice.argon.ArgonPropertiesAttribute;
import com.metservice.beryllium.BerylliumHttpConnectorType;

/**
 * @author roach
 */
class Startup {

	public static Startup newInstanceFromArgs(String[] args)
			throws CfgSyntaxException {
		try {
			final ArgonArgs aa = new ArgonArgs(args);
			final ArgonProperties.BuilderFromArgs b = ArgonProperties.newBuilder(aa);
			b.putFiles(CCmdArg.propertyFile);
			b.putAssignments();
			b.putMappedArg(CCmdArg.servicePort, CStartupProp.KmlPort);
			b.putMappedArg(CCmdArg.gridPath, CStartupProp.GridPath);
			b.putMappedArg(CCmdArg.stationUrl, CStartupProp.StationUrl);
			b.printlnUnsupportedMessage();
			return new Startup(b.newProperties());
		} catch (final ArgonArgsException ex) {
			throw new CfgSyntaxException(ex);
		}
	}

	private void filterPatternConsole(SpaceCfg cfg)
			throws CfgSyntaxException {
		final ArgonPropertiesAttribute oatt = m_props.find(CStartupProp.FilterPatternConsole);
		if (oatt == null) return;
		try {
			cfg.setFilterPatternConsole(oatt.qtwValue);
		} catch (final ArgonApiException ex) {
			throw new CfgSyntaxException(oatt.invalid(ex));
		}
	}

	private void filterPatternJmx(SpaceCfg cfg)
			throws CfgSyntaxException {
		final ArgonPropertiesAttribute oatt = m_props.find(CStartupProp.FilterPatternJmx);
		if (oatt == null) return;
		try {
			cfg.setFilterPatternJmx(oatt.qtwValue);
		} catch (final ArgonApiException ex) {
			throw new CfgSyntaxException(oatt.invalid(ex));
		}
	}

	private void filterPatternLog(SpaceCfg cfg)
			throws CfgSyntaxException {
		final ArgonPropertiesAttribute oatt = m_props.find(CStartupProp.FilterPatternLog);
		if (oatt == null) return;
		try {
			cfg.setFilterPatternLog(oatt.qtwValue);
		} catch (final ArgonApiException ex) {
			throw new CfgSyntaxException(oatt.invalid(ex));
		}
	}

	private void kmlServiceMaxThreads(SpaceCfg cfg)
			throws ArgonFormatException {
		final ArgonPropertiesAttribute oatt = m_props.find(CStartupProp.KmlServiceMaxThreads);
		if (oatt == null) return;
		cfg.setKmlServiceMaxThreads(oatt.count(4, 1024));
	}

	private void kmlServiceMinThreads(SpaceCfg cfg)
			throws ArgonFormatException {
		final ArgonPropertiesAttribute oatt = m_props.find(CStartupProp.KmlServiceMinThreads);
		if (oatt == null) return;
		cfg.setKmlServiceMinThreads(oatt.count(4, 1024));
	}

	private void serviceConnectorType(SpaceCfg cfg)
			throws CfgSyntaxException {
		final ArgonPropertiesAttribute oatt = m_props.find(CStartupProp.ServiceConnectorType);
		if (oatt == null) return;
		try {
			cfg.setServiceConnectorType(BerylliumHttpConnectorType.Table.select(oatt.qtwValue));
		} catch (final ArgonFormatException ex) {
			throw new CfgSyntaxException(oatt.invalid(ex));
		}
	}

	public PathSensorCfg newPathSensorCfg()
			throws CfgSyntaxException {
		try {
			final ArgonPropertiesAttribute att = m_props.select(CStartupProp.GridPath);
			return PathSensorCfg.newInstance(att.qtwValue);
		} catch (final ArgonArgsException ex) {
			throw new CfgSyntaxException(ex);
		}
	}

	public SpaceCfg newSpaceCfg()
			throws CfgSyntaxException {
		try {
			final SpaceCfg cfg = new SpaceCfg();
			serviceConnectorType(cfg);
			kmlServiceMinThreads(cfg);
			kmlServiceMaxThreads(cfg);
			filterPatternConsole(cfg);
			filterPatternJmx(cfg);
			filterPatternLog(cfg);
			return cfg;
		} catch (final ArgonFormatException ex) {
			throw new CfgSyntaxException(ex);
		}
	}

	public SpaceId newSpaceId()
			throws CfgSyntaxException {
		try {
			int kmlPort = CStartupDefault.KmlPort;
			final ArgonPropertiesAttribute oatt = m_props.find(CStartupProp.KmlPort);
			if (oatt != null) {
				kmlPort = oatt.port();
			}
			return SpaceId.newInstance(kmlPort);
		} catch (final ArgonFormatException ex) {
			throw new CfgSyntaxException(ex);
		}
	}

	private Startup(ArgonProperties props) {
		assert props != null;
		m_props = props;
	}

	private final ArgonProperties m_props;
}
