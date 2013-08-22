/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonTransformer;
import com.metservice.argon.Ds;
import com.metservice.beryllium.BerylliumApiException;
import com.metservice.beryllium.BerylliumSupportId;
import com.metservice.beryllium.CBeryllium;

/**
 * @author roach
 */
class ShellSession {

	private void footer(PrintWriter writer) {
		writer.println("<div id=\"footer\">");
		final String oqMessage = m_queueMessages.poll();
		final String qStatus = oqMessage == null ? "OK" : oqMessage;
		final String qheStatus = ArgonTransformer.zHtmlEncodePCDATA(qStatus);
		writer.println("<p>" + qheStatus + "</p>");
		writer.println("</div>");
		writer.println("</body>");
		writer.println("</html>");
	}

	private void header(PrintWriter writer, ShellPage page) {
		writer.println("<body>");
		writer.println("<div id=\"header\">");
		writer.println("<h1>Neon Shell</h1>");
		final String qheTitle = ArgonTransformer.zHtmlEncodePCDATA(page.qTitle());
		writer.println("<h2>" + qheTitle + "</h2>");
		writer.println("</div>");
	}

	private void meta(PrintWriter writer, ShellPage page) {
		writer.println("<head>");
		final String qheTitle = ArgonTransformer.zHtmlEncodePCDATA(page.qTitle());
		writer.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">");
		writer.println("<title>" + qheTitle + "</title>");
		writer.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + CNeonShell.CSS_ui + "\"/>");
		page.scripts(writer);
		page.styles(writer);
		writer.println("</head>");
	}

	private void prologue(PrintWriter writer) {
		writer.println(CBeryllium.Html5Prologue);
		writer.println("<html lang=\"en\">");
	}

	public void handle(ShellGlobal global, ShellPage page, Request rq, HttpServletResponse rp)
			throws IOException, ServletException {
		if (global == null) throw new IllegalArgumentException("object is null");
		if (page == null) throw new IllegalArgumentException("object is null");
		rp.setStatus(HttpServletResponse.SC_OK);
		rp.setContentType("text/html;charset=utf-8");
		final PrintWriter writer = rp.getWriter();
		prologue(writer);
		meta(writer, page);
		header(writer, page);
		try {
			page.render(this, global, rq, rp);
		} catch (final BerylliumApiException ex) {
			sendMessage(ex.getMessage());
		} catch (final InterruptedException ex) {
			sendMessage("Session Interrupted");
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo("Handle request", ex);
			ds.a("page", page);
			ds.a("request", rq);
			sendMessage(ds.s());
		}
		m_tsLastRequest.set(System.currentTimeMillis());
		footer(writer);
	}

	public BerylliumSupportId idSupport() {
		return m_idSupport;
	}

	public void sendMessage(String qMessage) {
		if (qMessage == null || qMessage.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_queueMessages.offer(qMessage);
	}

	public ShellSessionStateConsole stateConsole(String qccSourcePath) {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		m_lockStates.lock();
		try {
			ShellSessionStateConsole vState = m_zmPath_StateConsole.get(qccSourcePath);
			if (vState == null) {
				vState = new ShellSessionStateConsole();
				m_zmPath_StateConsole.put(qccSourcePath, vState);
			}
			return vState;
		} finally {
			m_lockStates.unlock();
		}
	}

	public ShellSessionStateDebug stateDebug(String qccSourcePath) {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		m_lockStates.lock();
		try {
			ShellSessionStateDebug vState = m_zmPath_StateDebug.get(qccSourcePath);
			if (vState == null) {
				vState = new ShellSessionStateDebug();
				m_zmPath_StateDebug.put(qccSourcePath, vState);
			}
			return vState;
		} finally {
			m_lockStates.unlock();
		}
	}

	@Override
	public String toString() {
		return m_idSupport.toString();
	}

	public long tsLastRequest() {
		return m_tsLastRequest.get();
	}

	public ShellSession(BerylliumSupportId idSupport) {
		if (idSupport == null) throw new IllegalArgumentException("object is null");
		m_idSupport = idSupport;
		m_tsLastRequest = new AtomicLong(System.currentTimeMillis());
		m_queueMessages = new ArrayBlockingQueue<String>(CNeonShell.MaxSessionMessages);
	}

	private final BerylliumSupportId m_idSupport;
	private final AtomicLong m_tsLastRequest;
	private final Queue<String> m_queueMessages;
	private final Lock m_lockStates = new ReentrantLock();
	private final Map<String, ShellSessionStateDebug> m_zmPath_StateDebug = new HashMap<String, ShellSessionStateDebug>();
	private final Map<String, ShellSessionStateConsole> m_zmPath_StateConsole = new HashMap<String, ShellSessionStateConsole>();
}
