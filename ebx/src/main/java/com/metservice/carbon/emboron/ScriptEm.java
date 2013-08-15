/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import java.util.List;

import com.metservice.argon.Binary;
import com.metservice.neon.EmClass;
import com.metservice.neon.EmMutableObject;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsObject;

/**
 * @author roach
 */
class ScriptEm extends EmMutableObject<ScriptConstruct, ScriptTuple> {

	@Override
	protected ScriptTuple newTuple(EsExecutionContext ecx, ScriptConstruct construct)
			throws InterruptedException {
		return new ScriptTuple(ecx, construct, this);
	}

	@Override
	protected void putDefaultProperties(EsExecutionContext ecx) {
		putUpdate(CProp.interpreter, CProp.interpreter_default_primitive);
		putUpdate(CProp.exitTimeout, CProp.exitTimeout_default_primitive);
		putUpdateBoolean(CProp.redirectStdErrToOut, CProp.redirectStdErrToOut_default);
		putUpdate(CProp.stdioEncoding, CProp.stdioEncoding_default);
		putUpdate(CProp.stdioLineTerminator, CProp.stdioLineTerminator_default);
		putUpdate(CProp.maxProductQueueDepth, CProp.maxProductQueueDepth_default);
		putUpdate(CProp.maxFeedQueueDepth, CProp.maxFeedQueueDepth_default);
		putUpdate(CProp.stdOutBufferSize, CProp.stdOutBufferSize_default);
		putUpdate(CProp.stdErrBufferSize, CProp.stdErrBufferSize_default);
		putUpdate(CProp.stdInBufferSize, CProp.stdInBufferSize_default);
	}

	@Override
	protected void putInstanceProperties(EsExecutionContext ecx, ScriptConstruct c) {
	}

	public void addResource(String qtwPath, Binary content) {
		construct().addResource(qtwPath, content);
	}

	public void construct(List<String> xlzLines) {
		setConstruct(new ScriptConstruct(xlzLines));
	}

	@Override
	public EsObject createObject() {
		return new ScriptEm(this);
	}

	public ScriptEm(EmClass modelClass) {
		super(modelClass);
	}

	public ScriptEm(ScriptEm prototype) {
		super(prototype);
	}
}
