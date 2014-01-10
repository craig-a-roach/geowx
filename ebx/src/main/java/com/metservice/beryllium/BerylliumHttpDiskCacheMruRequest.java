/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.Date;

import org.eclipse.jetty.client.Address;

import com.metservice.argon.cache.disk.IArgonDiskCacheMruRequest;

/**
 * @author roach
 */
public class BerylliumHttpDiskCacheMruRequest implements IArgonDiskCacheMruRequest {

	@Override
	public boolean isValid(Date now, String zContentValidator) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String qccResourceId() {
		// TODO Auto-generated method stub
		return null;
	}

	public BerylliumHttpDiskCacheMruRequest(Address addr, BerylliumPathQuery uri) {

	}

}
