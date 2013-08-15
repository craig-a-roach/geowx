/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.File;
import java.util.Properties;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonArgs;
import com.metservice.argon.ArgonArgsException;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonProperties;
import com.metservice.argon.ArgonPropertiesAttribute;
import com.metservice.argon.CArgon;
import com.metservice.argon.file.ArgonDirectoryManagement;

/**
 * @author roach
 */
public class NeonStartup {

	public static NeonStartup newInstanceFromArgs(String[] args)
			throws NeonCfgSyntaxException {
		try {
			final ArgonArgs aa = new ArgonArgs(args);
			final ArgonProperties.BuilderFromArgs b = ArgonProperties.newBuilder(aa);
			b.putFiles(CNeonCmdArg.propertyFile);
			b.putAssignments();
			b.putMappedArg(CNeonCmdArg.shellPort, CNeonCmdStartupProp.ShellPort);
			b.putMappedArg(CNeonCmdArg.sourcePath, CNeonCmdStartupProp.SourcePath);
			b.printlnUnsupportedMessage();
			return new NeonStartup(b.newProperties());
		} catch (final ArgonArgsException ex) {
			throw new NeonCfgSyntaxException(ex.getMessage());
		}
	}

	public static NeonStartup newInstanceFromProperties(Properties... ozptSources) {
		return new NeonStartup(ArgonProperties.newBuilderFromProperties().add(ozptSources).newProperties());
	}

	private void autoDebugPattern(NeonSpaceCfg cfg)
			throws ArgonFormatException {
		final ArgonPropertiesAttribute oatt = m_props.find(CNeonCmdStartupProp.AutoDebugPattern);
		if (oatt == null) return;
		cfg.setAutoDebugPattern(oatt.opattern());
	}

	private void callableCacheLineBudget(NeonSpaceCfg cfg)
			throws ArgonFormatException {
		final ArgonPropertiesAttribute oatt = m_props.find(CNeonCmdStartupProp.CallableCacheLineBudget);
		if (oatt == null) return;
		cfg.setCallableCacheLineBudget(oatt.count(0, 100 * CArgon.M));
	}

	private void filterPatternConsole(NeonSpaceCfg cfg)
			throws NeonCfgSyntaxException {
		final ArgonPropertiesAttribute oatt = m_props.find(CNeonCmdStartupProp.FilterPatternConsole);
		if (oatt == null) return;
		try {
			cfg.setFilterPatternConsole(oatt.qtwValue);
		} catch (final ArgonApiException ex) {
			throw new NeonCfgSyntaxException(oatt.invalid(ex));
		}
	}

	private void filterPatternJmx(NeonSpaceCfg cfg)
			throws NeonCfgSyntaxException {
		final ArgonPropertiesAttribute oatt = m_props.find(CNeonCmdStartupProp.FilterPatternJmx);
		if (oatt == null) return;
		try {
			cfg.setFilterPatternJmx(oatt.qtwValue);
		} catch (final ArgonApiException ex) {
			throw new NeonCfgSyntaxException(oatt.invalid(ex));
		}
	}

	private void filterPatternLog(NeonSpaceCfg cfg)
			throws NeonCfgSyntaxException {
		final ArgonPropertiesAttribute oatt = m_props.find(CNeonCmdStartupProp.FilterPatternLog);
		if (oatt == null) return;
		try {
			cfg.setFilterPatternLog(oatt.qtwValue);
		} catch (final ArgonApiException ex) {
			throw new NeonCfgSyntaxException(oatt.invalid(ex));
		}
	}

	private void shellConsoleQuota(NeonSpaceCfg cfg)
			throws ArgonFormatException {
		final ArgonPropertiesAttribute oatt = m_props.find(CNeonCmdStartupProp.ShellConsoleQuota);
		if (oatt == null) return;
		cfg.setShellConsoleQuota(oatt.count(100, 100 * CArgon.M));
	}

	private void shellSessionMaxIdleSec(NeonSpaceCfg cfg)
			throws ArgonFormatException {
		final ArgonPropertiesAttribute oatt = m_props.find(CNeonCmdStartupProp.ShellSessionMaxIdle);
		if (oatt == null) return;
		cfg.setShellSessionMaxIdleSec(oatt.ms(1 * CArgon.MIN_TO_MS, 24 * CArgon.HR_TO_MS) / 1000);
	}

	public INeonSourceProvider newSourceProvider()
			throws NeonCfgSyntaxException {
		try {
			File cndirHome = ArgonDirectoryManagement.CnDirUserHome;
			final ArgonPropertiesAttribute oatt = m_props.find(CNeonCmdStartupProp.SourcePath);
			if (oatt != null) {
				cndirHome = oatt.cndirUserWriteable();
			}
			return new NeonSourceProviderDefaultFile(cndirHome);
		} catch (final ArgonFormatException ex) {
			throw new NeonCfgSyntaxException(ex);
		}
	}

	public NeonSpaceCfg newSpaceCfg()
			throws NeonCfgSyntaxException {
		try {
			final NeonSpaceCfg cfg = new NeonSpaceCfg();
			shellSessionMaxIdleSec(cfg);
			shellConsoleQuota(cfg);
			callableCacheLineBudget(cfg);
			autoDebugPattern(cfg);
			filterPatternConsole(cfg);
			filterPatternJmx(cfg);
			filterPatternLog(cfg);
			cfg.setShellProcess(true);
			return cfg;
		} catch (final ArgonFormatException ex) {
			throw new NeonCfgSyntaxException(ex);
		}
	}

	public NeonSpaceId newSpaceId()
			throws NeonCfgSyntaxException {
		try {
			int shellPort = CNeonCmdDefault.ShellPort;
			final ArgonPropertiesAttribute oatt = m_props.find(CNeonCmdStartupProp.ShellPort);
			if (oatt != null) {
				shellPort = oatt.port();
			}
			return NeonSpaceId.newInstance(shellPort);
		} catch (final ArgonFormatException ex) {
			throw new NeonCfgSyntaxException(ex);
		}
	}

	private NeonStartup(ArgonProperties props) {
		m_props = props;
	}

	private final ArgonProperties m_props;
}
