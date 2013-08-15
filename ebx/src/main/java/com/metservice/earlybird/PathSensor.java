/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.metservice.argon.ArgonPlatformException;
import com.metservice.argon.ArgonRunnable;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonRunProbe;
import com.metservice.argon.management.IArgonService;

/**
 * @author roach
 */
class PathSensor implements IArgonService {

	private static final String TryConstruct = "Construct a watch service for path";
	private static final String TryClose = "Close a watch service for path";
	private static final String TryRegister = "Register path with watch service";
	private static final String RsnOverflow = "Watch service overflow event";
	private static final String RsnInvalidKind = "Invalid watch event kind";
	private static final String RsnInvalidContext = "Invalid watch event context";
	private static final String CsqNoWatch = "Path cannot be watched";
	private static final String CsqClose = "Possible resource leak";
	private static final String CsqSkipped = "Path event(s) skipped";

	static void close(ISpaceProbe probe, WatchService oWatchService) {
		try {
			if (oWatchService != null) {
				oWatchService.close();
			}
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo(TryClose, ex, CsqClose);
			probe.warnFileWatch(ds);
		} catch (final ClosedWatchServiceException ex) {
		}
	}

	static void close(ISpaceProbe probe, WatchService[] ztWatchServices) {
		for (int i = 0; i < ztWatchServices.length; i++) {
			close(probe, ztWatchServices[i]);
		}
	}

	public static PathSensor newInstance(KernelCfg kc)
			throws ArgonPlatformException {
		final Path[] xptPaths = kc.cfgPathSensor.xptPaths();
		final int pathCount = xptPaths.length;
		final WatchService[] xptWatchServices = new WatchService[pathCount];
		boolean watchFail = false;
		try {
			for (int i = 0; i < pathCount; i++) {
				final Path path = xptPaths[i];
				if (!path.toFile().canRead()) {
					final String m = "Cannot read from path '" + path + "'";
					throw new ArgonPlatformException(m);
				}
				final FileSystem fileSystem = path.getFileSystem();
				try {
					xptWatchServices[i] = fileSystem.newWatchService();
				} catch (final IOException ex) {
					watchFail = true;
					final Ds ds = Ds.triedTo(TryConstruct, ex, ArgonPlatformException.class);
					ds.a("path", path);
					kc.probe.failFileWatch(ds);
					final String m = "Could not construct a watch service for '" + path + "'";
					throw new ArgonPlatformException(m);
				} catch (final UnsupportedOperationException ex) {
					watchFail = true;
					final Ds ds = Ds.triedTo(TryConstruct, ex, ArgonPlatformException.class);
					ds.a("path", path);
					kc.probe.failFileWatch(ds);
					final String m = "File system for '" + path + "' does not support watch services";
					throw new ArgonPlatformException(m);
				}
			}
			final Watcher[] xptWatcher = new Watcher[pathCount];
			for (int i = 0; i < pathCount; i++) {
				final Path path = xptPaths[i];
				final WatchService watchService = xptWatchServices[i];
				xptWatcher[i] = new Watcher(kc, path, watchService);
			}
			return new PathSensor(xptWatcher);
		} finally {
			if (watchFail) {
				close(kc.probe, xptWatchServices);
			}
		}
	}

	private void serviceEnd(Watcher watcher) {
		watcher.requestEndInterrupt();
		watcher.closeWatchService();
		watcher.awaitEnd();
	}

	private void serviceStart(ExecutorService xc, Watcher watcher)
			throws InterruptedException {
		watcher.executeAwait(xc);
	}

	@Override
	public String name() {
		return "PathSensor";
	}

	@Override
	public void serviceEnd()
			throws InterruptedException {
		for (int i = m_xptWatcher.length - 1; i >= 0; i--) {
			serviceEnd(m_xptWatcher[i]);
		}
	}

	@Override
	public void serviceStart(ExecutorService xc)
			throws ArgonPlatformException, InterruptedException {
		for (int i = 0; i < m_xptWatcher.length; i++) {
			serviceStart(xc, m_xptWatcher[i]);
		}
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("watchers", m_xptWatcher);
		return ds.s();
	}

	private PathSensor(Watcher[] xptWatcher) {
		if (xptWatcher == null || xptWatcher.length == 0) throw new IllegalArgumentException("array is null or empty");
		m_xptWatcher = xptWatcher;
	}

	private final Watcher[] m_xptWatcher;

	private static class Watcher extends ArgonRunnable {

		private void detectedCreate(Path path) {
			System.out.println("CREATED " + path);
		}

		private void detectedDelete(Path path) {
			System.out.println("DELETED " + path);
		}

		private void handle(WatchEvent<?> watchEvent) {
			assert watchEvent != null;
			final Object oContext = watchEvent.context();
			final Kind<?> kind = watchEvent.kind();
			if (kind.equals(StandardWatchEventKinds.OVERFLOW)) {
				final Ds ds = Ds.invalidBecause(RsnOverflow, CsqSkipped);
				ds.a("path", m_path);
				ds.a("eventContext", oContext);
				kc.probe.warnFileWatch(ds);
				return;
			}
			if (!(oContext instanceof Path)) {
				final String contextClass = (oContext == null) ? "null" : oContext.getClass().getName();
				final Ds ds = Ds.invalidBecause(RsnInvalidContext, CsqSkipped);
				ds.a("path", m_path);
				ds.a("eventContext.class", contextClass);
				ds.a("eventContext.toString", oContext);
				kc.probe.warnFileWatch(ds);
				return;
			}
			final Path contextPath = (Path) oContext;
			final Path eventPath = m_path.resolve(contextPath);
			if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
				detectedCreate(eventPath);
			} else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
				detectedDelete(eventPath);
			} else {
				final Ds ds = Ds.invalidBecause(RsnInvalidKind, CsqSkipped);
				ds.a("path", m_path);
				ds.a("eventContext", oContext);
				kc.probe.warnFileWatch(ds);
				return;
			}
		}

		private WatchKey register() {
			try {
				return m_path.register(m_watchService, StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_DELETE);
			} catch (final IOException ex) {
				final Ds ds = Ds.triedTo(TryRegister, ex, CsqNoWatch);
				ds.a("path", m_path);
				kc.probe.failFileWatch(ds);
			} catch (final UnsupportedOperationException ex) {
				final Ds ds = Ds.triedTo(TryRegister, ex, CsqNoWatch);
				ds.a("path", m_path);
				kc.probe.failFileWatch(ds);
			}
			return null;
		}

		@Override
		protected IArgonRunProbe getRunProbe() {
			return kc.probe;
		}

		@Override
		protected void runImp()
				throws InterruptedException {
			try {
				final WatchKey oRegistered = register();
				boolean more = oRegistered != null && oRegistered.isValid();
				while (more) {
					final WatchKey oWatchKey = m_watchService.take();
					if (oWatchKey == null || !oWatchKey.isValid()) {
						more = false;
					} else {
						final List<WatchEvent<?>> pollEvents = oWatchKey.pollEvents();
						for (final WatchEvent<?> watchEvent : pollEvents) {
							handle(watchEvent);
						}
						if (!oWatchKey.reset()) {
							more = false;
						}
					}
				}
			} catch (final ClosedWatchServiceException ex) {
			} finally {
				closeWatchService();
			}
		}

		public void closeWatchService() {
			close(kc.probe, m_watchService);
		}

		@Override
		public String toString() {
			return m_path.toString();
		}

		Watcher(KernelCfg kc, Path path, WatchService watchService) {
			assert path != null;
			assert watchService != null;
			this.kc = kc;
			m_path = path;
			m_watchService = watchService;
		}
		private final KernelCfg kc;
		private final Path m_path;
		private final WatchService m_watchService;
	}

}
