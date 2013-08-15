/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.metservice.argon.text.ArgonJoiner;
import com.metservice.beryllium.BerylliumAssetLoader;
import com.metservice.beryllium.BerylliumPath;
import com.metservice.beryllium.BerylliumSupportId;

/**
 * @author roach
 */
class NeonShellHandler extends AbstractHandler {

	private BerylliumPath createRedirect(ShellSession session, BerylliumPath path) {
		if (path.depth < 2) return null;
		final String qtwLastNode = path.qtwNode(-1);

		if (path.depth >= 3 && qtwLastNode.equals(CNeonShell.Node_assure_run_js)) {
			final String qtwSourcePath = path.subPath(1, -1).qtwPath();
			m_global.assurance.add(session.idSupport(), qtwSourcePath);
			session.sendMessage("Submitted " + qtwSourcePath);
			return path.subPathHead(-1).newPath(CNeonShell.Node_control);
		}

		if (path.depth >= 3 && qtwLastNode.equals(CNeonShell.Node_assure_run_tree)) {
			final BerylliumPath root = path.subPath(1, -1);
			m_global.assurance.addTree(session.idSupport(), root);
			session.sendMessage("Submitted " + root.qtwPath() + " tree");
			return path.subPathHead(-1).newPath(CNeonShell.Node_assure_nav);
		}

		return null;
	}

	private ShellPage createUIPage(BerylliumPath path) {

		if (path.depth < 2) return null;

		final String qtwLastNode = path.qtwNode(-1);
		if (qtwLastNode.equals(CNeonShell.Node_index)) return new ShellPageIndex(path);
		if (qtwLastNode.equals(CNeonShell.Node_manage)) return new ShellPageManage(path);
		if (qtwLastNode.equals(CNeonShell.Node_assure_nav)) return new ShellPageAssure(path);
		if (qtwLastNode.equals(CNeonShell.Node_shutdown)) return new ShellPageShutdown();

		if (path.depth >= 3) {
			if (qtwLastNode.equals(CNeonShell.Node_control)) return new ShellPageControl(path);
			if (qtwLastNode.equals(CNeonShell.Node_console)) return new ShellPageConsole(path);
			if (qtwLastNode.equals(CNeonShell.Node_debug)) return new ShellPageDebug(path);
			if (qtwLastNode.equals(CNeonShell.Node_profile)) return new ShellPageProfile(path);
			if (qtwLastNode.equals(CNeonShell.Node_source_js)) return new ShellPageSourceJs(path);
			if (qtwLastNode.equals(CNeonShell.Node_source_txt)) return new ShellPageSourceTxt(path);
			if (qtwLastNode.equals(CNeonShell.Node_edit_js)) return new ShellPageEditJs(path);
			if (qtwLastNode.equals(CNeonShell.Node_edit_txt)) return new ShellPageEditTxt(path);
		}

		return null;
	}

	private ShellSession declareSession(Request rq) {
		assert rq != null;
		final BerylliumSupportId idSupport = BerylliumSupportId.newInstance(rq);
		m_lockSessions.lock();
		try {
			ShellSession vSession = m_sessionMap.get(idSupport);
			if (vSession == null) {
				vSession = new ShellSession(idSupport);
				m_sessionMap.put(idSupport, vSession);
			}
			return vSession;
		} finally {
			m_lockSessions.unlock();
		}
	}

	private ShellSession findSession(BerylliumSupportId idSupport) {
		assert idSupport != null;
		m_lockSessions.lock();
		try {
			return m_sessionMap.get(idSupport);
		} finally {
			m_lockSessions.unlock();
		}
	}

	private void handleUI(BerylliumPath path, Request rq, HttpServletResponse rp)
			throws IOException, ServletException {
		final ShellSession session = declareSession(rq);
		final BerylliumPath oRedirect = createRedirect(session, path);
		if (oRedirect == null) {
			final ShellPage oPage = createUIPage(path);
			if (oPage == null) {
				rp.sendError(HttpServletResponse.SC_NOT_FOUND, rq.getPathInfo());
			} else {
				session.handle(m_global, oPage, rq, rp);
			}
		} else {
			rp.sendRedirect(oRedirect.qtwEncodedPath());
		}
	}

	private ShellSession removeSession(BerylliumSupportId sid) {
		assert sid != null;
		ShellSession oExSession = null;
		m_lockSessions.lock();
		try {
			oExSession = m_sessionMap.remove(sid);
		} finally {
			m_lockSessions.unlock();
		}
		if (oExSession != null) {
			m_global.debugger.removeSession(sid);
			m_global.profiler.removeSession(sid);
		}
		return oExSession;
	}

	private List<BerylliumSupportId> zlIdleSupportIds(long msMaxIdle) {
		final long tsIdleBefore = System.currentTimeMillis() - msMaxIdle;
		m_lockSessions.lock();
		try {
			final List<BerylliumSupportId> zl = new ArrayList<BerylliumSupportId>();
			for (final Entry<BerylliumSupportId, ShellSession> e : m_sessionMap.entrySet()) {
				final long tsLastRequest = e.getValue().tsLastRequest();
				if (tsLastRequest < tsIdleBefore) {
					zl.add(e.getKey());
				}
			}
			return zl;
		} finally {
			m_lockSessions.unlock();
		}
	}

	private List<BerylliumSupportId> zlSupportIdsAsc() {
		final List<BerylliumSupportId> zl;
		m_lockSessions.lock();
		try {
			zl = new ArrayList<BerylliumSupportId>(m_sessionMap.keySet());
		} finally {
			m_lockSessions.unlock();
		}
		Collections.sort(zl);
		return zl;
	}

	@Override
	public void handle(String target, Request rq, HttpServletRequest sr, HttpServletResponse rp)
			throws IOException, ServletException {
		rq.setHandled(true);
		final BerylliumPath path = BerylliumPath.newInstance(rq);

		if (path.match(0, CNeonShell.Node_ui)) {
			handleUI(path, rq, rp);
			return;
		}
		if (path.match(0, CNeonShell.Node_asset)) {
			m_assetLoader.handle(path, rq, rp);
			return;
		}

		if (path.isFavouriteIcon()) {
			m_assetLoader.handle(CNeonShell.Favicon, rq, rp);
			return;
		}

		rp.sendRedirect(CNeonShell.Redirect);
	}

	public boolean isShutdownInProgress() {
		return m_global.isShutdownInProgress();
	}

	public void reapIdleSessions(long msMaxIdle) {
		final List<BerylliumSupportId> zlIdleSessions = zlIdleSupportIds(msMaxIdle);
		final List<String> zlRemoved = new ArrayList<String>();
		for (final BerylliumSupportId sid : zlIdleSessions) {
			final ShellSession oEx = removeSession(sid);
			if (oEx != null) {
				zlRemoved.add(sid.toString());
			}
		}
		if (!zlRemoved.isEmpty()) {
			final String qMessage = "Removed idle session(s): '" + ArgonJoiner.zComma(zlRemoved) + "'";
			m_global.kc.probe.infoShell(qMessage);
			sendMessage(qMessage);
		}
	}

	public void sendMessage(String qMessage) {
		final List<BerylliumSupportId> zlSupportIdsAsc = zlSupportIdsAsc();
		for (final BerylliumSupportId sid : zlSupportIdsAsc) {
			final ShellSession oSession = findSession(sid);
			if (oSession != null) {
				oSession.sendMessage(qMessage);
			}
		}
	}

	public NeonShellHandler(KernelCfg kc, NeonSourceLoader sl, NeonConsole con, NeonDebugger dbg, NeonProfiler prf,
			NeonAssurance asr) {
		m_global = new ShellGlobal(kc, sl, con, dbg, prf, asr);
		m_assetLoader = new BerylliumAssetLoader(null, getClass());
	}
	private final ShellGlobal m_global;
	private final Lock m_lockSessions = new ReentrantLock();
	private final Map<BerylliumSupportId, ShellSession> m_sessionMap = new HashMap<BerylliumSupportId, ShellSession>();
	private final BerylliumAssetLoader m_assetLoader;
}
