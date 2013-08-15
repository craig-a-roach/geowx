/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emmail;

import com.metservice.argon.ArgonTuple2;
import com.metservice.argon.net.ArgonPlatform;

/**
 * @author airflo
 * 
 */
class CDefault {

	public static final String ConfigurationId = "default";
	public static final String Host = "localhost";
	public static final int Port = -1;
	public static final String Password = "";
	public static final boolean Secure = false;

	public static final String UserName = ArgonPlatform.qcctwUserName();
	public static final ArgonTuple2<String, Integer> HostPort = new ArgonTuple2<String, Integer>(Host, Port);

	private CDefault() {
	}
}
