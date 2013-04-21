/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import com.metservice.argon.ArgonPermissionException;

/**
 * @author roach
 */
class UArgonManagement {

	public static String qUserHome()
			throws ArgonPermissionException {
		final String ozUserHome = System.getProperty(SysPropName_UserHome);
		if (ozUserHome == null || ozUserHome.length() == 0) throw new ArgonPermissionException("Cannot determine user home");
		return ozUserHome;
	}

	public static String qUserName() {
		final String ozUserName = System.getProperty(SysPropName_User);
		return ozUserName == null || ozUserName.length() == 0 ? "unknown" : ozUserName;
	}

	public static void zeroLeft(StringBuilder dst, String zVal, int width) {
		assert dst != null;
		final int pad = width - zVal.length();
		for (int i = 0; i < pad; i++) {
			dst.append('0');
		}
		dst.append(zVal);
	}

	public static final String SysPropName_User = "user.name";
	public static final String SysPropName_UserDir = "user.dir";
	public static final String SysPropName_UserHome = "user.home";

}
