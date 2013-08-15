/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author roach
 */
public interface IEsCallable {

	public IEsOperand call(EsExecutionContext ecx)
			throws InterruptedException;

	public EsSource getSource();

	public boolean isDeclared();

	public boolean isIntrinsic();

	public String oqccName();

	public int requiredArgumentCount();

	public String show(int depth);

	public List<String> zlFormalParameterNames();

	public Map<String, IEsCallable> zmCallables();

	public Set<String> zsVariableNames();
}
