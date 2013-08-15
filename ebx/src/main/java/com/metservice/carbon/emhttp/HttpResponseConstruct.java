/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class HttpResponseConstruct {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("statusCode", oStatusCode);
		ds.a("mimeContent", oMimeContent);
		return ds.s();
	}

	public HttpResponseConstruct(Integer oStatusCode, MimeContent oMimeContent) {
		this.oStatusCode = oStatusCode;
		this.oMimeContent = oMimeContent;
	}
	public final Integer oStatusCode;
	public final MimeContent oMimeContent;
}
