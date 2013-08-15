/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.List;

import com.metservice.argon.DateFormatter;
import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
abstract class ShellPageHub extends ShellPage {

	private static String oqccAssureNavTailNode(INeonSourceDescriptor sd) {
		assert sd != null;
		final NeonSourceDescriptorType dtype = sd.type();
		if (dtype == NeonSourceDescriptorType.Ignore) return null;
		if (dtype == NeonSourceDescriptorType.Container) return CNeonShell.Node_assure_nav;
		if (dtype == NeonSourceDescriptorType.EcmaScript) return CNeonShell.Node_control;
		return null;
	}

	private static String oqccAssureRunTailNode(INeonSourceDescriptor sd) {
		assert sd != null;
		final NeonSourceDescriptorType dtype = sd.type();
		if (dtype == NeonSourceDescriptorType.Ignore) return null;
		if (dtype == NeonSourceDescriptorType.Container) return CNeonShell.Node_assure_run_tree;
		if (dtype == NeonSourceDescriptorType.EcmaScript) return CNeonShell.Node_assure_run_js;
		return null;
	}

	private static String oqccEditTailNode(INeonSourceDescriptor sd, boolean edit) {
		assert sd != null;
		final NeonSourceDescriptorType dtype = sd.type();
		if (dtype == NeonSourceDescriptorType.Ignore) return null;
		if (dtype == NeonSourceDescriptorType.Container) return edit ? CNeonShell.Node_manage : CNeonShell.Node_index;
		final boolean ecmaScript = dtype == NeonSourceDescriptorType.EcmaScript;
		if (ecmaScript) return edit ? CNeonShell.Node_edit_js : CNeonShell.Node_control;
		return edit ? CNeonShell.Node_edit_txt : CNeonShell.Node_source_txt;
	}

	protected static String qLastModified(INeonSourceDescriptor sd) {
		assert sd != null;
		return DateFormatter.newPlatformDHMSYFromTs(sd.tsLastModified());
	}

	protected static String qVisibility(INeonSourceDescriptor sd) {
		assert sd != null;
		return sd.isWip() ? "\u2717OFFLINE" : "\u2714online";
	}

	protected BerylliumPath getAssureNavSubPath(INeonSourceDescriptor sd) {
		final String oqccTailNode = oqccAssureNavTailNode(sd);
		if (oqccTailNode == null) return null;
		final String qccNode = sd.qccNode();
		return m_pathMemberBase.newPath(qccNode, oqccTailNode);
	}

	protected BerylliumPath getAssureRunSubPath(INeonSourceDescriptor sd) {
		final String oqccTailNode = oqccAssureRunTailNode(sd);
		if (oqccTailNode == null) return null;
		final String qccNode = sd.qccNode();
		return m_pathMemberBase.newPath(qccNode, oqccTailNode);
	}

	protected BerylliumPath getEditSubPath(INeonSourceDescriptor sd, boolean edit) {
		final String oqccTailNode = oqccEditTailNode(sd, edit);
		if (oqccTailNode == null) return null;
		final String qccNode = sd.qccNode();
		return m_pathMemberBase.newPath(qccNode, oqccTailNode);
	}

	protected List<? extends INeonSourceDescriptor> zlSubDescriptors(ShellGlobal g) {
		final String zccProviderPath = m_pathProvider.ztwPath();
		final INeonSourceProvider sourceProvider = g.kc.sourceProvider;
		return sourceProvider.zlDescriptorsAsc(zccProviderPath);
	}

	public final BerylliumPath assureNavPath() {
		return m_pathMemberBase.newPath(CNeonShell.Node_assure_nav);
	}

	public final BerylliumPath indexPath() {
		return m_pathMemberBase.newPath(CNeonShell.Node_index);
	}

	public final BerylliumPath managePath() {
		return m_pathMemberBase.newPath(CNeonShell.Node_manage);
	}

	public final BerylliumPath oParentAssurePath() {
		if (m_path.depth > 2) return m_path.subPathHead(-2).newPath(CNeonShell.Node_assure_nav);
		return null;
	}

	public final BerylliumPath oParentIndexPath() {
		if (m_path.depth > 2) return m_path.subPathHead(-2).newPath(CNeonShell.Node_index);
		return null;
	}

	public final BerylliumPath oParentManagePath() {
		if (m_path.depth > 2) return m_path.subPathHead(-2).newPath(CNeonShell.Node_manage);
		return null;
	}

	public final BerylliumPath oShutdownPath(ShellGlobal g) {
		return g.isProcess() && m_path.depth <= 2 ? CNeonShell.Shutdown : null;
	}

	public BerylliumPath providerPath() {
		return m_pathProvider;
	}

	public final BerylliumPath selfPath() {
		return m_path;
	}

	public BerylliumPath subProviderPath(String qcctwNode) {
		return m_pathProvider.newPath(qcctwNode);
	}

	public ShellPageHub(BerylliumPath path) {
		if (path == null) throw new IllegalArgumentException("object is null");
		m_path = path;
		m_pathMemberBase = path.subPath(0, -1);
		m_pathProvider = path.subPath(1, -1);
	}
	private final BerylliumPath m_path;
	private final BerylliumPath m_pathMemberBase;
	private final BerylliumPath m_pathProvider;
}
