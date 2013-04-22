/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonArgs;
import com.metservice.argon.ArgonArgsException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.Ds;
import com.metservice.argon.file.ArgonFileManifest;

/**
 * @author roach
 */
public class KryptonDecoderConfig {

	private static void makeFileManifest(Map<String, ArgonFileManifest> dst, ArgonArgs in, String qTagSpec, Pattern oAcceptName)
			throws ArgonArgsException, ArgonPermissionException {
		assert dst != null;
		assert in != null;
		assert qTagSpec != null && qTagSpec.length() > 0;
		final String oqtwPaths = in.consumeAllTagValuePairs(qTagSpec).oqtwValue();
		if (oqtwPaths == null) return;
		final ArgonFileManifest fm = ArgonFileManifest.newInstance(oqtwPaths, CKryptonArg.PathDelimiter, oAcceptName, null);
		dst.put(qTagSpec, fm);
	}

	public static KryptonDecoderConfig newDefaultInstance()
			throws ArgonArgsException, ArgonPermissionException {
		final Map<String, ArgonFileManifest> map = Collections.emptyMap();
		return new KryptonDecoderConfig(map);
	}

	public static KryptonDecoderConfig newInstance(ArgonArgs in)
			throws ArgonArgsException, ArgonPermissionException {
		if (in == null) throw new IllegalArgumentException("object is null");
		final Map<String, ArgonFileManifest> map = new HashMap<String, ArgonFileManifest>();
		makeFileManifest(map, in, CKryptonArg.DecodePaths_Centre, CKryptonArg.AcceptPattern_Text);
		makeFileManifest(map, in, CKryptonArg.DecodePaths_SubCentre, CKryptonArg.AcceptPattern_Text);
		makeFileManifest(map, in, CKryptonArg.DecodePaths_GeneratingProcess, CKryptonArg.AcceptPattern_Text);
		makeFileManifest(map, in, CKryptonArg.DecodePaths_Parameter, CKryptonArg.AcceptPattern_Parameter);
		return new KryptonDecoderConfig(map);
	}

	private void makeDecoder(IKryptonProbe probe, String qTagSpec, ResourceFolderDecoder<?> dst) {
		final ArgonFileManifest oFileManifest = m_mapArg_Manifest.get(qTagSpec);
		if (oFileManifest != null) {
			dst.setFileMap(oFileManifest.newFileNameMap());
		}
	}

	public CentreDecoder newDecoderCentre(IKryptonProbe probe) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		final CentreDecoder neo = new CentreDecoder(probe);
		makeDecoder(probe, CKryptonArg.DecodePaths_Centre, neo);
		return neo;
	}

	public ParameterDecoder newDecoderParameter(IKryptonProbe probe) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		final ParameterDecoder neo = new ParameterDecoder(probe);
		makeDecoder(probe, CKryptonArg.DecodePaths_Parameter, neo);
		return neo;
	}

	public CentreDecoder newDecoderSubCentre(IKryptonProbe probe) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		final CentreDecoder neo = new CentreDecoder(probe);
		makeDecoder(probe, CKryptonArg.DecodePaths_SubCentre, neo);
		return neo;
	}

	public GeneratingProcessDecoder newGeneratingProcess(IKryptonProbe probe) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		final GeneratingProcessDecoder neo = new GeneratingProcessDecoder(probe);
		makeDecoder(probe, CKryptonArg.DecodePaths_GeneratingProcess, neo);
		return neo;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("map", m_mapArg_Manifest);
		return ds.s();
	}

	private KryptonDecoderConfig(Map<String, ArgonFileManifest> mapArg_Manifest) {
		assert mapArg_Manifest != null;
		m_mapArg_Manifest = mapArg_Manifest;
	}

	private final Map<String, ArgonFileManifest> m_mapArg_Manifest;
}
