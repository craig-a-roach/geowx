/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

/**
 * @author roach
 */
public interface EarlybirdSpaceMBean {

	public String alterFilterConsole(String filterPattern);

	public String alterFilterJmx(String filterPattern);

	public String alterFilterLog(String filterPattern);

	public String getConfigInfo();

	public String getFilterPatternConsole();

	public String getFilterPatternJmx();

	public String getFilterPatternLog();

	public String getIdInfo();

	public String requestRestart();

	public void restoreFilterConsole();

	public void restoreFilterJmx();

	public void restoreFilterLog();
}
