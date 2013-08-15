/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class BerylliumSmtpConnectionFactory {

	public static final String DefaultHost = "localhost";
	public static final String ProtocolNameNormal = "smtp";
	public static final String ProtocolNameSecure = "smtps";
	public static final int SavePort = 1;

	private static final String TryCreate = "Create an SMTP connection";
	private static final String TryConnect = "Connect to SMTP transport";
	private static final String CsqLogNull = "Log failure and return null connection";

	private static BerylliumSmtpConnection newSaveConnection(BerylliumSmtpUrl url, String zcctwPassword, boolean secure)
			throws BerylliumSmtpPlatformException, BerylliumSmtpAuthenticationException, BerylliumSmtpTransportException {
		if (url == null) throw new IllegalArgumentException("object is null");
		if (zcctwPassword == null) throw new IllegalArgumentException("object is null");
		if (zcctwPassword.equals("*")) {
			final String msg = "SMTP (save) credentials not accepted: " + url.credential(zcctwPassword);
			throw new BerylliumSmtpAuthenticationException(msg);
		}
		final Properties p = new Properties();
		final Session session = Session.getInstance(p);
		final URLName urlName = new URLName(protocolName(secure), url.qlctwHost(), SavePort, "", url.qcctwUserName(),
				zcctwPassword);
		final Transport transport = new SaveTransport(session, urlName);
		return new BerylliumSmtpConnection(url, session, transport);
	}

	private static String protocolName(boolean secure) {
		return secure ? ProtocolNameSecure : ProtocolNameNormal;
	}

	private static boolean useSaveTransport(BerylliumSmtpUrl url) {
		assert url != null;
		final Integer oPort = url.getPort();
		if (oPort == null) return false;
		final int port = oPort.intValue();
		return port == SavePort;
	}

	public static BerylliumSmtpConnection createConnection(IBerylliumSmtpProbe oprobe, BerylliumSmtpUrl url,
			String zcctwPassword, boolean secure) {
		try {
			final BerylliumSmtpConnection cx = newConnection(url, zcctwPassword, secure);
			if (oprobe != null) {
				oprobe.infoSmtp("Connected to SMTP server", url);
			}
			return cx;
		} catch (final BerylliumSmtpPlatformException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo(TryCreate, ex, CsqLogNull);
				oprobe.failSmtp(ds);
			}
		} catch (final BerylliumSmtpAuthenticationException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo(TryCreate, ex, CsqLogNull);
				oprobe.failSmtp(ds);
			}
		} catch (final BerylliumSmtpTransportException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo(TryCreate, ex, CsqLogNull);
				oprobe.failSmtp(ds);
			}
		}
		return null;
	}

	public static BerylliumSmtpConnection newConnection(BerylliumSmtpUrl url, String zcctwPassword, boolean secure)
			throws BerylliumSmtpPlatformException, BerylliumSmtpAuthenticationException, BerylliumSmtpTransportException {
		if (url == null) throw new IllegalArgumentException("object is null");
		if (zcctwPassword == null) throw new IllegalArgumentException("object is null");
		if (useSaveTransport(url)) return newSaveConnection(url, zcctwPassword, secure);
		final String qlctwHost = url.qlctwHost();
		final String qcctwUserName = url.qcctwUserName();
		final Integer oPort = url.getPort();
		final Properties p = new Properties();
		final Session session = Session.getInstance(p);
		try {
			final Transport transport = session.getTransport(protocolName(secure));
			if (oPort == null) {
				transport.connect(qlctwHost, qcctwUserName, zcctwPassword);
			} else {
				transport.connect(qlctwHost, oPort.intValue(), qcctwUserName, zcctwPassword);
			}
			return new BerylliumSmtpConnection(url, session, transport);
		} catch (final NoSuchProviderException ex) {
			final String msg = "SMTP provider not available on platform..." + Ds.format(ex);
			throw new BerylliumSmtpPlatformException(msg);
		} catch (final AuthenticationFailedException ex) {
			final String msg = "SMTP credentials not accepted: " + url.credential(zcctwPassword);
			throw new BerylliumSmtpAuthenticationException(msg);
		} catch (final MessagingException ex) {
			final Ds ds = Ds.triedTo(TryConnect, ex, BerylliumSmtpTransportException.class);
			ds.a("url", url);
			throw new BerylliumSmtpTransportException(ds.s(), ex);
		}
	}

	private BerylliumSmtpConnectionFactory() {
	}

	static class SaveTransport extends Transport {

		public ConcurrentLinkedQueue<Message> messageQueue() {
			return m_messageQueue;
		}

		@Override
		public void sendMessage(Message msg, Address[] addresses)
				throws MessagingException {
			m_messageQueue.add(msg);
		}

		public SaveTransport(Session session, URLName urlname) {
			super(session, urlname);
			setConnected(true);
		}
		private final ConcurrentLinkedQueue<Message> m_messageQueue = new ConcurrentLinkedQueue<Message>();
	}
}
