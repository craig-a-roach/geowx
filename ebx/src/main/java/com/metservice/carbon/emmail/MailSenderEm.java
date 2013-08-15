/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emmail;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.beryllium.BerylliumSmtpManager;
import com.metservice.beryllium.BerylliumSmtpRatePolicy;
import com.metservice.neon.EmViewObject;
import com.metservice.neon.EsExecutionContext;

/**
 * @author roach
 */
class MailSenderEm extends EmViewObject {

	public BerylliumSmtpManager imp() {
		return m_imp;
	}

	public BerylliumSmtpRatePolicy policy() {
		m_rwlock.readLock().lock();
		try {
			return m_policy;
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	@Override
	public void putProperties(EsExecutionContext ecx)
			throws InterruptedException {
		putView(CProp.id, m_qccId);
	}

	public String qccId() {
		return m_qccId;
	}

	public String qlcFromAddress() {
		m_rwlock.readLock().lock();
		try {
			return m_qlcFromAddress;
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public void setCcList(String ztwList) {
		if (ztwList == null) throw new IllegalArgumentException("object is null");
		m_rwlock.writeLock().lock();
		try {
			m_ztwCcList = ztwList;
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public void setFromAddress(String qlcAddress) {
		if (qlcAddress == null || qlcAddress.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_rwlock.writeLock().lock();
		try {
			m_qlcFromAddress = qlcAddress;
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public void setPolicy(BerylliumSmtpRatePolicy policy) {
		if (policy == null) throw new IllegalArgumentException("object is null");
		m_rwlock.writeLock().lock();
		try {
			m_policy = policy;
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public void setToList(String ztwList) {
		if (ztwList == null) throw new IllegalArgumentException("object is null");
		m_rwlock.writeLock().lock();
		try {
			m_ztwToList = ztwList;
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	@Override
	public String toString() {
		return m_imp.reportConnectionStatus(m_qccId);
	}

	public String ztwCcList() {
		m_rwlock.readLock().lock();
		try {
			return m_ztwCcList;
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public String ztwToList() {
		m_rwlock.readLock().lock();
		try {
			return m_ztwToList;
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public MailSenderEm(BerylliumSmtpManager imp, String qccId) {
		super(MailSenderEmClass.Instance);
		if (imp == null) throw new IllegalArgumentException("object is null");
		if (qccId == null || qccId.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_imp = imp;
		m_qccId = qccId;
		m_qlcFromAddress = imp.qlcDefaultAddressFrom();
		m_policy = RatePolicyFactory.Default;
		m_ztwToList = "";
		m_ztwCcList = "";
	}
	private final BerylliumSmtpManager m_imp;
	private final String m_qccId;
	private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
	private String m_qlcFromAddress;
	private BerylliumSmtpRatePolicy m_policy;
	private String m_ztwToList;
	private String m_ztwCcList;
}
