/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import java.io.File;

import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.neon.EmClass;
import com.metservice.neon.EmException;
import com.metservice.neon.EmMethod;
import com.metservice.neon.EsApiCodeException;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsMethodAccessor;
import com.metservice.neon.EsObject;
import com.metservice.neon.EsPrimitiveBoolean;
import com.metservice.neon.EsPrimitiveNull;
import com.metservice.neon.EsPrimitiveNumberInteger;
import com.metservice.neon.EsPrimitiveString;
import com.metservice.neon.IEsOperand;

/**
 * @author roach
 */
class FileSystemEmClass extends EmClass {

	private static final int DefaultQuotaBc = 256 * CArgon.M;
	private static final int QuotaScriptBc = 16 * CArgon.M;

	private static final String Name = CClass.FileSystem;
	static final EmMethod[] Methods = { new method_getFileSize(), new method_loadFile(), new method_canReadHomePath(),
			new method_getHomeFileSize(), new method_loadHomeFile(), new method_newLinesFromTemplate(), new method_toString() };

	static final FileSystemEmClass Instance = new FileSystemEmClass(Name, Methods);

	static Binary getBinary(File srcFile, int bcQuota) {
		assert srcFile != null;
		try {
			return Binary.createFromFile(srcFile, bcQuota);
		} catch (final ArgonQuotaException ex) {
			throw new EsApiCodeException(ex);
		} catch (final ArgonStreamReadException ex) {
			throw new EmException(ex);
		}
	}

	static FileSystemEm self(EsExecutionContext ecx) {
		return ecx.thisObject(Name, FileSystemEm.class);
	}

	private FileSystemEmClass(String qccClassName, EmMethod[] ozptMethods) {
		super(qccClassName, ozptMethods);
	}

	static abstract class amethod_home extends EmMethod {

		protected static final String[] StdArgs = { CArg.home, CArg.path };
		protected static final int ArgBase = StdArgs.length;

		protected File homePath(EsExecutionContext ecx, EsMethodAccessor ac)
				throws InterruptedException {
			assert ac != null;
			final String qccHomeName = ac.qtwStringValue(0);
			final String qtwRelative = ac.qtwStringValue(1);
			final EmBoronFileSystemHomes fileSystemHomes = self(ecx).fileSystemHomes();
			final File oHomePath = fileSystemHomes.find(qccHomeName);
			if (oHomePath == null) throw new EsApiCodeException("Undefined file system home '" + qccHomeName + '"');
			return new File(oHomePath, qtwRelative);
		}

		protected amethod_home(String qccName, int requiredArgumentCount, String... zptFormalParameterNames) {
			super(qccName, ArgBase + requiredArgumentCount, StdArgs, zptFormalParameterNames);
		}
	}

	static abstract class amethod_software extends EmMethod {

		protected static final String[] StdArgs = { CArg.path };
		protected static final int ArgBase = StdArgs.length;

		protected File softwarePath(EsExecutionContext ecx, EsMethodAccessor ac)
				throws InterruptedException {
			assert ac != null;
			final String qtwRelative = ac.qtwStringValue(0);
			final EmBoronFileSystemHomes fileSystemHomes = self(ecx).fileSystemHomes();
			final File vHomePath = fileSystemHomes.findSoftware();
			if (vHomePath == null) throw new EsApiCodeException("Undefined file system software home");
			return new File(vHomePath, qtwRelative);
		}

		protected amethod_software(String qccName, int requiredArgumentCount, String... zptFormalParameterNames) {
			super(qccName, ArgBase + requiredArgumentCount, StdArgs, zptFormalParameterNames);
		}
	}

	static class method_canReadHomePath extends amethod_home {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final File srcFile = homePath(ecx, ac);
			return EsPrimitiveBoolean.instance(srcFile.canRead());
		}

		public method_canReadHomePath() {
			super("canReadHomePath", 0);
		}
	}

	static class method_getFileSize extends amethod_home {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final File file = new File(ac.qtwStringValue(0));
			return EsPrimitiveNumberInteger.newInstance(file.length());
		}

		public method_getFileSize() {
			super("getFileSize", 1, CArg.path);
		}
	}

	static class method_getHomeFileSize extends amethod_home {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final File srcFile = homePath(ecx, ac);
			return EsPrimitiveNumberInteger.newInstance(srcFile.length());
		}

		public method_getHomeFileSize() {
			super("getHomeFileSize", 0);
		}
	}

	static class method_loadFile extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final File file = new File(ac.qtwStringValue(0));
			final int bcQuota = ac.defaulted(1) ? DefaultQuotaBc : ac.intValue(1);
			final Binary oContent = getBinary(file, bcQuota);
			return oContent == null ? EsPrimitiveNull.Instance : ecx.global().newIntrinsicBinary(oContent);
		}

		public method_loadFile() {
			super("loadFile", 1, CArg.path, CArg.quota);
		}
	}

	static class method_loadHomeFile extends amethod_home {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final File srcFile = homePath(ecx, ac);
			final int bcQuota = ac.defaulted(ArgBase) ? DefaultQuotaBc : ac.intValue(ArgBase);
			final Binary oContent = getBinary(srcFile, bcQuota);
			return oContent == null ? EsPrimitiveNull.Instance : ecx.global().newIntrinsicBinary(oContent);
		}

		public method_loadHomeFile() {
			super("loadHomeFile", 0, CArg.quota);
		}
	}

	static class method_newLinesFromTemplate extends amethod_software {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final File srcFile = softwarePath(ecx, ac);
			final EsObject oBindSource = ac.defaulted(ArgBase) ? null : ac.esoObject(ArgBase);
			final Binary oContent = getBinary(srcFile, QuotaScriptBc);
			if (oContent == null) throw new EsApiCodeException("Script template '" + srcFile + "' not found");
			final FileSystemEm self = self(ecx);
			final EsPrimitiveString espContent = new EsPrimitiveString(oContent.newStringUTF8());
			final EsPrimitiveString espFs = espContent.bind(ecx, self, null);
			final EsPrimitiveString espB = oBindSource == null ? espFs : espFs.bind(ecx, oBindSource, null);
			return espB.toLines(ecx, true, false, null, null);
		}

		public method_newLinesFromTemplate() {
			super("newLinesFromTemplate", 0, CArg.bindSource);
		}
	}

	static class method_toString extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			return new EsPrimitiveString(self(ecx).fileSystemHomes());
		}

		public method_toString() {
			super(StdName_toString);
		}
	}
}
