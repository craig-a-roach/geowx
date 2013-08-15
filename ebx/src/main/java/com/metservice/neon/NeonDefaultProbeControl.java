/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.metservice.argon.management.ArgonLogger;
import com.metservice.argon.management.ArgonProbeFormatter;

/**
 * @author roach
 */
public class NeonDefaultProbeControl implements INeonProbeControl {

	@Override
	public INeonLogger createLogger(NeonSpaceId id, NeonSpaceCfg cfg) {
		return new DefaultLogger(id);
	}

	@Override
	public void jmxRegister(NeonSpace space, NeonSpaceId id)
			throws InstanceAlreadyExistsException {
		if (space == null) throw new IllegalArgumentException("object is null");
		if (id == null) throw new IllegalArgumentException("object is null");

		final ObjectName objectName = id.spaceObjectName();
		try {
			m_mbs.registerMBean(space, objectName);
		} catch (final MBeanRegistrationException exMR) {
			exMR.printStackTrace();
		} catch (final NotCompliantMBeanException exNC) {
			exNC.printStackTrace();
		}
	}

	@Override
	public void jmxUnregister(NeonSpaceId id) {
		if (id == null) throw new IllegalArgumentException("object is null");

		final ObjectName objectName = id.spaceObjectName();
		if (!m_mbs.isRegistered(objectName)) return;
		try {
			m_mbs.unregisterMBean(objectName);
		} catch (final MBeanRegistrationException exMR) {
			exMR.printStackTrace();
		} catch (final InstanceNotFoundException exINF) {
			exINF.printStackTrace();
		}
	}

	@Override
	public INeonProbeFormatter newFormatter(NeonSpaceId id, NeonSpaceCfg cfg) {
		return new DefaultFormatter(id);
	}

	public NeonDefaultProbeControl() {
		m_mbs = ManagementFactory.getPlatformMBeanServer();
	}

	public NeonDefaultProbeControl(MBeanServer mbs) {
		if (mbs == null) throw new IllegalArgumentException("object is null");
		m_mbs = mbs;
	}

	private final MBeanServer m_mbs;

	private static class DefaultFormatter implements INeonProbeFormatter {

		@Override
		public String console(long ts, String type, String keyword, String message) {
			return m_pf.console(ts, type, keyword, message);
		}

		@Override
		public String jmx(long ts, String type, String keyword, String message) {
			return m_pf.jmx(keyword, message);
		}

		@Override
		public String logger(long ts, String type, String keyword, String message) {
			return m_pf.logger(ts, type, keyword, message);
		}

		public DefaultFormatter(NeonSpaceId id) {
			m_pf = ArgonProbeFormatter.newInstance(CNeon.ServiceId, id);
		}

		final ArgonProbeFormatter m_pf;
	}

	private static class DefaultLogger implements INeonLogger {

		@Override
		public void failure(String message) {
			m_lgr.failure(message);
		}

		@Override
		public void information(String message) {
			m_lgr.information(message);
		}

		@Override
		public void live(String message) {
			m_lgr.live(message);
		}

		@Override
		public void warning(String message) {
			m_lgr.warning(message);
		}

		public DefaultLogger(NeonSpaceId id) {
			m_lgr = new ArgonLogger(CNeon.ServiceId, id);
		}

		private final ArgonLogger m_lgr;
	}
}
