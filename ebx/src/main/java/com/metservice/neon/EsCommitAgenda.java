/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.List;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class EsCommitAgenda {

	private static final String TryTask = "Run commit task";
	private static final String CsqContinue = "Task may not have completed; try next task";

	private void task(ISpaceProbe probe, IEsCommitTask task, boolean commit) {
		try {
			if (commit) {
				task.commit();
			} else {
				task.rollback();
			}
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo(TryTask, ex, CsqContinue);
			ds.a("commit", commit);
			ds.a("task", task);
			probe.failSoftware(ex);
		}
	}

	private void tasks(ISpaceProbe probe, boolean commit) {
		if (m_lzyTasks == null) return;
		final int taskCount = m_lzyTasks.size();
		for (int i = 0; i < taskCount; i++) {
			task(probe, m_lzyTasks.get(i), commit);
		}
	}

	public void add(IEsCommitTask task) {
		if (task == null) throw new IllegalArgumentException("object is null");
		if (m_lzyTasks == null) {
			m_lzyTasks = new ArrayList<IEsCommitTask>();
		}
		m_lzyTasks.add(task);
	}

	public void commit(ISpaceProbe probe) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		tasks(probe, true);
	}

	public void rollback(ISpaceProbe probe) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		tasks(probe, false);
	}

	public EsCommitAgenda() {
	}
	private List<IEsCommitTask> m_lzyTasks;
}
