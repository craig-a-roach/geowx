/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.util.regex.Pattern;

/**
 * @author roach
 */
public class CKryptonArg {

	public static final Pattern PathDelimiter = Pattern.compile("[|]");
	public static final Pattern AcceptPattern_Text = Pattern.compile(".+[.]txt");

	public static final Pattern AcceptPattern_Parameter = Pattern.compile("G[12].+[.]xml");
	public static final String DecodePaths_Parameter = "decoderPathParameter";

	public static final String DecodePaths_Centre = "decoderPathCentre";
	public static final String DecodePaths_SubCentre = "decoderPathSubCentre";
	public static final String DecodePaths_GeneratingProcess = "decoderPathGeneratingProcess";

	private CKryptonArg() {
	}

}
