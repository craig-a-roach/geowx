/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class BerylliumSmtpConnection implements Comparable<BerylliumSmtpConnection> {

	private static final String TryClose = "Close transport connection";
	private static final String TryHeaders = "Set message headers";
	private static final String TryAddress = "Set message address header";
	private static final String TryContent = "Set message content";
	private static final String TryTransport = "Send message via transport connection";
	private static final String TrySend = "Construct and send message";
	private static final String CsqLogFlag = "Log failure and return failure flag";
	private static final String CsqLeak = "Potential resource leak";

	private static Address[] new_xptAddress(String[] xptqlctw)
			throws AddressException {
		assert xptqlctw != null;
		final int count = xptqlctw.length;
		final Address[] zpt = new Address[count];
		for (int i = 0; i < count; i++) {
			zpt[i] = newAddress(xptqlctw[i]);
		}
		return zpt;
	}

	private static Address newAddress(String qlctw)
			throws AddressException {
		assert qlctw != null && qlctw.length() > 0;
		return new InternetAddress(qlctw, true);
	}

	private boolean applyEnvelope(Message m, BerylliumSmtpEnvelope envelope, BerylliumSmtpBorder oBorder)
			throws BerylliumSmtpTransportException {
		assert m != null;
		assert envelope != null;
		try {
			final String qcctwSubjectBody = envelope.qcctwSubject();
			final String qcctwSubject = oBorder == null ? qcctwSubjectBody : oBorder.composeSubject(qcctwSubjectBody);
			m.setSubject(qcctwSubject);
			final String[] xptqlctwToAsc = envelope.xptqlctwToAsc();
			m.setRecipients(RecipientType.TO, new_xptAddress(xptqlctwToAsc));
			final String[] zptqlctwCcAsc = envelope.zptqlctwCcAsc();
			if (zptqlctwCcAsc.length > 0) {
				m.setRecipients(RecipientType.CC, new_xptAddress(zptqlctwCcAsc));
			}
			m.setFrom(newAddress(envelope.qlctwFrom()));
			return true;
		} catch (final AddressException ex) {
			final Ds ds = Ds.triedTo(TryAddress, ex, BerylliumSmtpTransportException.class);
			envelope.describe(ds);
			ds.a("message", m);
			throw new BerylliumSmtpTransportException(ds.sm());
		} catch (final MessagingException ex) {
			final Ds ds = Ds.triedTo(TryHeaders, ex, BerylliumSmtpTransportException.class);
			envelope.describe(ds);
			ds.a("message", m);
			throw new BerylliumSmtpTransportException(ds.sm());
		}
	}

	private Message newContentHtml(MimeMessage mm, IBerylliumSmtpHtml content, BerylliumSmtpBorder oBorder)
			throws MessagingException {
		assert mm != null;
		assert content != null;
		final String zeBody = content.zeHtml();
		final String zeMerge = oBorder == null ? zeBody : oBorder.composeHtml(zeBody);
		mm.setText(zeMerge, ArgonText.CHARSET_NAME_UTF8, "html");
		return mm;
	}

	private Message newContentSingle(MimeMessage mm, IBerylliumSmtpSingle content, BerylliumSmtpBorder oBorder)
			throws BerylliumSmtpTransportException {
		try {
			if (content instanceof IBerylliumSmtpText) return newContentText(mm, (IBerylliumSmtpText) content, oBorder);
			if (content instanceof IBerylliumSmtpHtml) return newContentHtml(mm, (IBerylliumSmtpHtml) content, oBorder);
			final String m = "Content type (single) '" + content.getClass().getName() + "' not yet implemented";
			throw new IllegalStateException(m);
		} catch (final MessagingException ex) {
			final Ds ds = Ds.triedTo(TryContent, ex, BerylliumSmtpTransportException.class);
			ds.aclass("content.class", content);
			throw new BerylliumSmtpTransportException(ds.s());
		}
	}

	private Message newContentText(MimeMessage mm, IBerylliumSmtpText content, BerylliumSmtpBorder oBorder)
			throws MessagingException {
		assert mm != null;
		assert content != null;
		final String zBody = content.zText();
		final String zMerge = oBorder == null ? zBody : oBorder.composeText(zBody);
		mm.setText(zMerge, ArgonText.CHARSET_NAME_UTF8);
		return mm;
	}

	private Message newMessage(BerylliumSmtpEnvelope envelope, IBerylliumSmtpContent content, BerylliumSmtpBorder oBorder)
			throws BerylliumSmtpTransportException {
		if (content instanceof IBerylliumSmtpSingle)
			return newMessageSingle(envelope, (IBerylliumSmtpSingle) content, oBorder);
		final String m = "Content type '" + content.getClass().getName() + "' not yet implemented";
		throw new IllegalStateException(m);
	}

	private Message newMessageSingle(BerylliumSmtpEnvelope envelope, IBerylliumSmtpSingle content, BerylliumSmtpBorder oBorder)
			throws BerylliumSmtpTransportException {
		assert content != null;
		final MimeMessage mm = new MimeMessage(m_session);
		applyEnvelope(mm, envelope, oBorder);
		return newContentSingle(mm, content, oBorder);
	}

	private void sendImp(BerylliumSmtpEnvelope envelope, IBerylliumSmtpContent content, BerylliumSmtpBorder oBorder)
			throws BerylliumSmtpTransportException {
		if (envelope == null) throw new IllegalArgumentException("object is null");
		if (content == null) throw new IllegalArgumentException("object is null");
		final Message msg = newMessage(envelope, content, oBorder);
		sendMessage(envelope, content, msg);
	}

	private void sendMessage(BerylliumSmtpEnvelope envelope, IBerylliumSmtpContent content, Message msg)
			throws BerylliumSmtpTransportException {
		try {
			final Address[] xptRecipients = msg.getAllRecipients();
			m_transport.sendMessage(msg, xptRecipients);
		} catch (final MessagingException ex) {
			final Ds ds = Ds.triedTo(TryTransport, ex, BerylliumSmtpTransportException.class);
			envelope.describe(ds);
			ds.a("message", msg);
			ds.aclass("content.class", content);
			throw new BerylliumSmtpTransportException(ds.s());
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo(TryTransport, ex, BerylliumSmtpTransportException.class);
			envelope.describe(ds);
			ds.a("message", msg);
			ds.aclass("content.class", content);
			throw new BerylliumSmtpTransportException(ds.s());
		}
	}

	public void close(IBerylliumSmtpProbe oprobe) {
		final String preClose = toString();
		try {
			m_transport.close();
			if (oprobe != null) {
				oprobe.infoSmtp("Disconnected from SMTP server", m_url);
			}
		} catch (final MessagingException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo(TryClose, ex, CsqLeak);
				ds.a("preClose", preClose);
				oprobe.warnSmtp(ds);
			}
		}
	}

	@Override
	public int compareTo(BerylliumSmtpConnection rhs) {
		return m_url.compareTo(rhs.m_url);
	}

	public ConcurrentLinkedQueue<Message> getMessageQueue() {
		if (m_transport instanceof BerylliumSmtpConnectionFactory.SaveTransport) {
			final BerylliumSmtpConnectionFactory.SaveTransport st = (BerylliumSmtpConnectionFactory.SaveTransport) m_transport;
			return st.messageQueue();
		}
		return null;
	}

	public boolean isConnected() {
		return m_transport.isConnected();
	}

	public void send(BerylliumSmtpEnvelope envelope, IBerylliumSmtpContent content)
			throws BerylliumSmtpTransportException {
		send(envelope, content, null);
	}

	public void send(BerylliumSmtpEnvelope envelope, IBerylliumSmtpContent content, BerylliumSmtpBorder oBorder)
			throws BerylliumSmtpTransportException {
		if (envelope == null) throw new IllegalArgumentException("object is null");
		if (content == null) throw new IllegalArgumentException("object is null");
		sendImp(envelope, content, oBorder);
	}

	public boolean send(IBerylliumSmtpProbe oprobe, BerylliumSmtpEnvelope envelope, IBerylliumSmtpContent content) {
		return send(oprobe, envelope, content, null);
	}

	public boolean send(IBerylliumSmtpProbe oprobe, BerylliumSmtpEnvelope envelope, IBerylliumSmtpContent content,
			BerylliumSmtpBorder oBorder) {
		if (envelope == null) throw new IllegalArgumentException("object is null");
		if (content == null) throw new IllegalArgumentException("object is null");
		try {
			sendImp(envelope, content, oBorder);
			if (oprobe != null && oprobe.isLiveSmtp()) {
				oprobe.liveSmtp(toString(), envelope.toString());
			}
			return true;
		} catch (final BerylliumSmtpTransportException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo(TrySend, ex, CsqLogFlag);
				ds.a("connection", toString());
				oprobe.failSmtp(ds);
			}
		}
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (m_transport.isConnected()) {
			sb.append(m_transport.getURLName());
			sb.append(" CONNECTED");
		} else {
			sb.append(m_url);
			sb.append(" DISCONNECTED");
		}
		return sb.toString();
	}

	BerylliumSmtpConnection(BerylliumSmtpUrl url, Session session, Transport transport) {
		if (url == null) throw new IllegalArgumentException("object is null");
		if (session == null) throw new IllegalArgumentException("object is null");
		if (transport == null) throw new IllegalArgumentException("object is null");
		m_url = url;
		m_session = session;
		m_transport = transport;
	}
	private final BerylliumSmtpUrl m_url;
	private final Session m_session;
	private final Transport m_transport;
}
