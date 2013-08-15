/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

/**
 * @author roach
 */
class CProp {

	public static final String GlobalHttpRequest = "httpRequest";

	public static final String statusCode = "statusCode";
	public static final String content = "content";

	public static final String contentType = "Content-Type";
	public static final String location = "Location";
	public static final String cacheControl = "Cache-Control";
	public static final String contentEncoding = "Content-Encoding";
	public static final String expires = "Expires";
	public static final String lastModified = "Last-Modified";

	public static final String method = "method";
	public static final String path = "path";
	public static final String serverHost = "serverHost";
	public static final String serverPort = "serverPort";
	public static final String remoteAddress = "remoteAddress";

	private CProp() {
	}

}
