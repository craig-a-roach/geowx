/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.CArgon;
import com.metservice.argon.Elapsed;
import com.metservice.argon.json.JsonEncoder;

/**
 * @author roach
 */
public class CBeryllium {

	private static final int K = CArgon.K;
	private static final int M = CArgon.M;
	private static final int HR = CArgon.HR_TO_MS;

	public static final int FormDataBufferBc = 256;

	public static final int ResourceQuotaBc = 1024 * M;
	public static final int ResourceBufferBc = 8 * K;

	public static final int FileQuotaBc = 1536 * M;
	public static final int FileBufferBc = 8 * K;

	public static final int BinaryBufferBcLo = 1 * K;
	public static final int FileUploadBufferBc = 16 * K;
	public static final int JsonEstBc = 4 * K;

	public static final Elapsed DefaultResourceMaxAge = Elapsed.newInstance(2 * HR);
	public static final String OperatorContentType = "text/plain;charset=utf-8";

	public static final String RqUserAgent = "com.metservice.beryllium";
	public static final String PostCacheControl = "max-age=0";
	public static final String JsonAcceptContentType = "text/plain";

	public static final String JsonContentType = "text/plain;charset=us-ascii";
	public static final String BinaryContentType = "application/octet-stream";
	public static final String MirrorContentType = "application/vnd.metservice.beryllium.mirror";
	public static final String ExtGz1 = "gz";
	public static final String ExtGz2 = "gzip";

	public static final String UrlQueryArg_FormMode = "mode";

	public static final String UrlQueryVal_FormMode_Edit = "edit";
	public static final String Html4Prologue = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
	public static final String Html5Prologue = "<!DOCTYPE html>";
	public static final String UriNode_FavouriteIcon = "favicon.ico";
	public static final String RevResource = "rev.txt";

	public static final String FileUploadSuffix = ".wip";

	private static final JsonEncoder InternalJsonEncoder = new JsonEncoder(JsonEstBc, 0, false, true, false);
	private static final JsonEncoder StrictJsonEncoder = new JsonEncoder(JsonEstBc, 0, true, true, false);

	public static JsonEncoder jsonEncoder(boolean strict) {
		return strict ? StrictJsonEncoder : InternalJsonEncoder;
	}

	private CBeryllium() {
	}

}
