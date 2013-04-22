/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.xenon;

import com.metservice.argon.ArgonServiceId;

/**
 * @author roach
 */
class CXenon {

	static final String Vendor = "com.metservice";
	static final String ServiceNode = "xenon";

	static final ArgonServiceId ServiceId = new ArgonServiceId(Vendor, ServiceNode);

}
