/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonEncoder;
import com.metservice.argon.text.ArgonTransformer;
import com.metservice.beryllium.BerylliumPath;
import com.metservice.beryllium.BerylliumQuery;
import com.metservice.beryllium.BerylliumUrlBuilder;

/**
 * @author roach
 */
class UNeonHtmlEncode {

	public static final String EOL = "\n";
	public static final String PropertyName_TAG = "TAG";
	public static final String PropertyName_TEXT = "TEXT";
	public static final String PropertyName_NODELIST = "NODELIST";
	public static final String PropertyName_ESCAPE = "ESCAPE";
	public static final String PropertyName_ENCODE_SP = "ENCODE_SP";
	public static final String PropertyName_ENCODE_LF = "ENCODE_LF";
	public static final String PropertyName_DATUM_CLASS = "DATUM_CLASS";
	public static final String PropertyName_DATUM_FOCUS = "DATUM_FOCUS";
	public static final String PropertyName_DATUM_JSON_DEPTH = "DATUM_JSON_DEPTH";

	public static final String PropertyName_URI_SCHEME = "_SCHEME";
	public static final String PropertyName_URI_HOST = "_HOST";
	public static final String PropertyName_URI_PORT = "_PORT";
	public static final String PropertyName_URI_PATH = "_PATH";
	public static final String PropertyName_URI_FRAGMENT = "_FRAGMENT";

	public static final boolean PropertyDefault_ESCAPE = true;
	public static final String PropertyDefault_ENCODE_SP = " ";
	public static final String PropertyDefault_ENCODE_LF = "\n";
	public static final String PropertyDefault_DATUM_CLASS = "datum";
	public static final boolean PropertyDefault_DATUM_FOCUS = true;
	public static final int PropertyDefault_DATUM_JSON_DEPTH = 5;

	public static final String DatumClassSuffixJson = "Json";
	public static final String DatumClassSuffixDump = "Dump";
	public static final String DatumClassSuffixTree = "Tree";

	private static final Set<String> Reserved = newNameSet(PropertyName_TAG, PropertyName_ESCAPE, PropertyName_TEXT,
			PropertyName_NODELIST, PropertyName_ENCODE_SP, PropertyName_ENCODE_LF, PropertyName_DATUM_CLASS,
			PropertyName_DATUM_FOCUS, PropertyName_DATUM_JSON_DEPTH);

	private static final Set<String> PropertyURI = newNameSet("ACTION", "BACKGROUND", "CITE", "CLASSID", "CODEBASE", "DATA",
			"HREF", "LONGDESC", "PROFILE", "SRC", "USEMAP");

	private static final Set<String> PropertyCSS = newNameSet("ID", "CLASS");

	private static final String NoEnumPrefix = "_";

	private static final Set<String> EndForbidden = newNameSet("AREA", "BASE", "BR", "COL", "FRAME", "HR", "IMG", "INPUT",
			"LINK", "META", "PARAM");

	private static final Set<String> SpaceSensitive = newNameSet("A", "ABBR", "ACRONYM", "ADDRESS", "B", "BIG", "CITE", "CODE",
			"DD", "DFN", "DT", "EM", "I", "KBD", "Q", "SAMP", "SMALL", "SPAN", "STRONG", "SUB", "SUP", "TT", "VAR");

	private static Set<String> newNameSet(String... names) {
		final int c = names.length;
		final Set<String> zs = new HashSet<String>(c);
		for (int i = 0; i < c; i++) {
			zs.add(names[i].trim().toUpperCase());
		}
		return zs;
	}

	static boolean cssPropertyName(String pname) {
		return PropertyCSS.contains(pname);
	}

	static boolean docTypeRequired(String quctwTag) {
		return quctwTag.equals("HTML");
	}

	static boolean elementEndForbidden(String quctwTag) {
		return EndForbidden.contains(quctwTag);
	}

	static boolean noEnumPropertyName(String pname) {
		return pname.startsWith(NoEnumPrefix);
	}

	static boolean reservedPropertyName(String pname) {
		return Reserved.contains(pname);
	}

	static boolean spaceSensitive(String quctwTag) {
		return SpaceSensitive.contains(quctwTag);
	}

	static boolean uriPropertyName(String pname) {
		return PropertyURI.contains(pname);
	}

	public static String encode(EsExecutionContext ecx, Args args)
			throws InterruptedException {
		final Encoder e = new Encoder(ecx, args);
		e.encode();
		return e.out.toString();
	}

	public static Args newArgs() {
		return new Args();
	}

	private static class Format {

		public int depth() {
			return m_depth;
		}

		public void disableIndent() {
			m_indenting = false;
		}

		public Format down() {
			return new Format(m_depth + 1, m_indenting);
		}

		public boolean indenting() {
			return m_indenting;
		}

		@Override
		public String toString() {
			return "depth=" + m_depth + " indenting=" + m_indenting;
		}

		private Format(int depth, boolean indenting) {
			m_depth = depth;
			m_indenting = indenting;
		}

		public Format() {
			this(0, true);
		}
		private final int m_depth;
		private boolean m_indenting;
	}

	private static class SourceAttribute implements Comparable<SourceAttribute> {

		@Override
		public int compareTo(SourceAttribute rhs) {
			return qlctwName.compareTo(rhs.qlctwName);
		}

		@Override
		public String toString() {
			return qlctwName + "=\"" + zValue + "\"";
		}

		public String zTransformedValue(TextTrans t) {
			return t.transformAttributeValue(zValue);
		}

		public SourceAttribute(String qlctwName, String zValue) {
			assert qlctwName != null && qlctwName.length() > 0;
			assert zValue != null;
			this.qlctwName = qlctwName;
			this.zValue = zValue;
		}
		public final String qlctwName;
		public final String zValue;
	}

	private static class TextTrans {

		private boolean isTreeArray(EsObject src, int length, int depth) {
			for (int i = 0; i < length; i++) {
				final IEsOperand esValue = src.getByIndex(i);
				if (isTreeValue(esValue, depth)) return true;
			}
			return false;
		}

		private boolean isTreeEnum(EsObject src, List<String> zlPropertyNamesAsc, int depth) {
			final int pcount = zlPropertyNamesAsc.size();
			for (int i = 0; i < pcount; i++) {
				final IEsOperand esValue = src.esGet(zlPropertyNamesAsc.get(i));
				if (isTreeValue(esValue, depth)) return true;
			}
			return false;
		}

		private boolean isTreeValue(IEsOperand esValue, int depth) {
			final EsType estValue = esValue.esType();
			if (!estValue.isDatum) return false;
			if (estValue.isPrimitiveDatum) return false;
			if (esValue instanceof EsFunction) return false;
			if (esValue instanceof EsIntrinsicBinary) return false;
			if (depth >= datumJsonDepth) return false;
			return true;
		}

		private void makeCell(StringBuilder sb, String zValue) {
			if (zValue.length() == 0) return;
			final String zTrans = transformString(zValue);
			sb.append(zTrans);
		}

		private void makeCellClose(StringBuilder sb, boolean isTree) {
			if (isTree) {
				sb.append("</DIV>");
			} else {
				sb.append("</SPAN></DIV>");
			}
		}

		private void makeCellOpen(StringBuilder sb, String pname, boolean isTree, String zClassSuffix) {
			if (isTree) {
				sb.append("<H3><A HREF=\"#\">").append(pname).append("</A></H3>");
				sb.append("<DIV class=\"");
				sb.append(qDatumClass);
				sb.append(zClassSuffix);
				sb.append("\">");
			} else {
				sb.append("<DIV>");
				sb.append("<EM>").append(pname).append(": </EM>");
				sb.append("<SPAN");
				if (zClassSuffix.length() > 0) {
					sb.append(" class=\"").append(qDatumClass).append(zClassSuffix).append("\"");
				}
				sb.append(">");
			}
		}

		private void makeDatumClass(StringBuilder sb, boolean isTree) {
			sb.append(" class=\"");
			sb.append(qDatumClass);
			if (isTree) {
				sb.append(DatumClassSuffixTree);
			}
			sb.append("\">");
		}

		private void makeObject(EsExecutionContext ecx, StringBuilder sb, EsObject src, String zHeader, int depth)
				throws InterruptedException {
			if (zHeader.length() > 0) {
				sb.append("<H3>");
				if (datumFocus) {
					sb.append("<A HREF=\"#\">");
				}
				sb.append(zHeader);
				if (datumFocus) {
					sb.append("</A>");
				}
				sb.append("</H3>");
			}
			final int depthNeo = depth + 1;
			final int length = UNeon.length(ecx, src);
			sb.append("<DIV");
			if (length > 0) {
				final boolean isTree = isTreeArray(src, length, depthNeo);
				makeDatumClass(sb, isTree);
				makeObjectArray(ecx, sb, src, length, zHeader, depthNeo, isTree);
			} else {
				final List<String> zlPropertyNamesAsc = src.esPropertyNames();
				final boolean isTree = isTreeEnum(src, zlPropertyNamesAsc, depthNeo);
				makeDatumClass(sb, isTree);
				makeObjectEnum(ecx, sb, src, zlPropertyNamesAsc, zHeader, depthNeo, isTree);
			}
			sb.append("</DIV>");
		}

		private void makeObjectArray(EsExecutionContext ecx, StringBuilder sb, EsObject src, int length, String zHeader,
				int depth, boolean isTree)
				throws InterruptedException {
			for (int i = 0; i < length; i++) {
				final String pname = UNeon.toPropertyName(i);
				final IEsOperand esValue = src.esGet(pname);
				final String zHeadSuffix = "[" + pname + "]";
				final String zHeadNeo = zHeader.length() == 0 ? zHeadSuffix : (zHeader + " " + zHeadSuffix);
				makeValue(ecx, sb, pname, esValue, zHeadNeo, depth, isTree);
			}
		}

		private void makeObjectEnum(EsExecutionContext ecx, StringBuilder sb, EsObject src, List<String> zlPropertyNamesAsc,
				String zHeader, int depth, boolean isTree)
				throws InterruptedException {
			final int pcount = zlPropertyNamesAsc.size();
			for (int i = 0; i < pcount; i++) {
				final String pname = zlPropertyNamesAsc.get(i);
				final IEsOperand esValue = src.esGet(pname);
				final String zHeadNeo = zHeader.length() == 0 ? pname : (zHeader + "." + pname);
				makeValue(ecx, sb, pname, esValue, zHeadNeo, depth, isTree);
			}
		}

		private void makeValue(EsExecutionContext ecx, StringBuilder sb, String pname, IEsOperand esValue, String zHeader,
				int depth, boolean isTree)
				throws InterruptedException {
			final EsType estValue = esValue.esType();
			if (!estValue.isDatum) return;
			if (estValue.isPrimitiveDatum) {
				makeCellOpen(sb, pname, isTree, "");
				makeCell(sb, esValue.toCanonicalString(ecx));
				makeCellClose(sb, isTree);
				return;
			}

			final EsObject esObjectValue = esValue.toObject(ecx);
			if (esObjectValue instanceof EsFunction) return;
			if (esObjectValue instanceof EsIntrinsicBinary) {
				makeCellOpen(sb, pname, isTree, DatumClassSuffixDump);
				makeCell(sb, zBinaryValue((EsIntrinsicBinary) esObjectValue));
				makeCellClose(sb, isTree);
				return;
			}

			if (depth >= datumJsonDepth) {
				makeCellOpen(sb, pname, isTree, DatumClassSuffixJson);
				makeCell(sb, zJsonValue(ecx, esObjectValue));
				makeCellClose(sb, isTree);
				return;
			}
			makeObject(ecx, sb, esObjectValue, zHeader, depth);
		}

		private String zBinaryValue(EsIntrinsicBinary esBinaryValue) {
			return esBinaryValue.value().dump(8);
		}

		private String zJsonValue(EsExecutionContext ecx, EsObject esObjectValue)
				throws InterruptedException {
			final IJsonNative oJsonNative = esObjectValue.createJsonNative();
			if (oJsonNative == null) return esObjectValue.toCanonicalString(ecx);
			return JsonEncoder.Default.encode(oJsonNative);
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(escape ? "escapeOn" : "escapeOff");
			sb.append(" sp'");
			sb.append(qSP);
			sb.append("' lf'");
			sb.append(qLF);
			sb.append("'");
			sb.append(" datumClass'");
			sb.append(qDatumClass);
			sb.append("' datumJsonDepth");
			sb.append(datumJsonDepth);
			return sb.toString();
		}

		public String transformAttributeValue(String zIn) {
			if (escape) return ArgonTransformer.zHtmlEncodeATTVAL(zIn);
			return ArgonTransformer.zNoControl(zIn, false, false);
		}

		public String transformNode(EsExecutionContext ecx, IEsOperand textDatum)
				throws InterruptedException {
			assert textDatum != null;
			final EsType estDatum = textDatum.esType();
			if (!estDatum.isDatum) return "";
			if (estDatum.isPrimitiveDatum) {
				final String zDatum = textDatum.toCanonicalString(ecx);
				return transformString(zDatum);
			}
			final EsObject textObject = textDatum.toObject(ecx);
			if (textObject instanceof EsIntrinsicBinary) {
				final EsIntrinsicBinary binaryTextObject = (EsIntrinsicBinary) textObject;
				final String zDatum = binaryTextObject.value().newStringUTF8();
				return transformString(zDatum);
			}
			if (datumJsonDepth == 0) {
				final String zDatum = zJsonValue(ecx, textObject);
				return transformString(zDatum);
			}
			final StringBuilder sb = new StringBuilder();
			makeObject(ecx, sb, textObject, "", 0);
			return sb.toString();
		}

		public String transformString(String zIn) {
			if (escape) return ArgonTransformer.zHtmlEncode(zIn, false, qSP, qLF, "\t", "\'", "\"");
			return ArgonTransformer.zNoControl(zIn, true, true);
		}

		public TextTrans transition(Boolean oEscape, String ozSP, String ozLF, String ozDatumClass, Boolean oDatumFocus,
				Integer oDatumJsonDepth) {
			boolean clean = true;

			final boolean escapeNeo;
			if (oEscape == null) {
				escapeNeo = escape;
			} else {
				escapeNeo = oEscape.booleanValue();
				clean = false;
			}
			final String qSPNeo;
			if (ozSP == null) {
				qSPNeo = qSP;
			} else {
				qSPNeo = ozSP.length() == 0 ? PropertyDefault_ENCODE_SP : ozSP;
				clean = false;
			}
			final String qLFNeo;
			if (ozLF == null) {
				qLFNeo = qLF;
			} else {
				qLFNeo = ozLF.length() == 0 ? PropertyDefault_ENCODE_LF : ozLF;
				clean = false;
			}
			final String qDatumClassNeo;
			if (ozDatumClass == null) {
				qDatumClassNeo = qDatumClass;
			} else {
				qDatumClassNeo = ozDatumClass.length() == 0 ? PropertyDefault_DATUM_CLASS : ozDatumClass;
				clean = false;
			}
			final boolean datumFocusNeo;
			if (oDatumFocus == null) {
				datumFocusNeo = datumFocus;
			} else {
				datumFocusNeo = oDatumFocus.booleanValue();
				clean = false;
			}

			final int datumJsonDepthNeo;
			if (oDatumJsonDepth == null) {
				datumJsonDepthNeo = datumJsonDepth;
			} else {
				final int djd = oDatumJsonDepth.intValue();
				datumJsonDepthNeo = djd < 0 ? PropertyDefault_DATUM_JSON_DEPTH : djd;
				clean = false;
			}

			if (clean) return this;

			return new TextTrans(escapeNeo, qSPNeo, qLFNeo, qDatumClassNeo, datumFocusNeo, datumJsonDepthNeo);
		}

		private TextTrans(boolean escape, String qSP, String qLF, String qDatumClass, boolean datumFocus, int datumJsonDepth) {
			this.escape = escape;
			this.qSP = qSP;
			this.qLF = qLF;
			this.qDatumClass = qDatumClass;
			this.datumFocus = datumFocus;
			this.datumJsonDepth = datumJsonDepth;
		}

		public TextTrans() {
			this.escape = PropertyDefault_ESCAPE;
			this.qSP = PropertyDefault_ENCODE_SP;
			this.qLF = PropertyDefault_ENCODE_LF;
			this.qDatumClass = PropertyDefault_DATUM_CLASS;
			this.datumFocus = PropertyDefault_DATUM_FOCUS;
			this.datumJsonDepth = PropertyDefault_DATUM_JSON_DEPTH;
		}

		public final boolean escape;
		public final String qSP;
		public final String qLF;
		public final String qDatumClass;
		public final boolean datumFocus;
		public final int datumJsonDepth;
	}

	public static class Args {

		void validate() {
		}

		public Args() {
		}

		public EsObject oRoot;
		public String oqtwDocType;
		public String oqtwDtdLocation;
		public int indent;
	}

	public static class Encoder {

		private String cssSafe(String zIn) {
			return ArgonTransformer.zSanitized(zIn, "#.", "_");
		}

		private void emitClose(String quctwTag, Format f) {
			out.append("</");
			out.append(quctwTag);
			out.append('>');
			if (spaceSensitive(quctwTag)) {
				f.disableIndent();
			}
		}

		private void emitDocType() {
			out.append("<!DOCTYPE HTML");
			if (oqtwDocType != null) {
				out.append(" PUBLIC \"");
				out.append(oqtwDocType);
				out.append("\"");
			}
			if (oqtwDtdLocation != null) {
				out.append(" \"");
				out.append(oqtwDtdLocation);
				out.append("\"");
			}
			out.append('>');
			out.append(EOL);
		}

		private void emitHead(String quctwTag, List<SourceAttribute> zlAttributes, TextTrans t, Format f) {
			assert quctwTag != null && quctwTag.length() > 0;
			assert zlAttributes != null;
			if (f.depth() == 0 && docTypeRequired(quctwTag)) {
				emitDocType();
			}
			emitIndent(f);
			out.append('<');
			out.append(quctwTag);
			final int acount = zlAttributes.size();
			for (int i = 0; i < acount; i++) {
				final SourceAttribute attr = zlAttributes.get(i);
				out.append(' ');
				out.append(attr.qlctwName);
				out.append("=\"");
				out.append(attr.zTransformedValue(t));
				out.append("\"");
			}
			out.append('>');
		}

		private void emitIndent(Format f) {
			assert f != null;
			if (!f.indenting()) return;
			final int pad = f.depth() * indentUnits;
			if (pad > 0) {
				out.append(EOL);
			}
			for (int i = 0; i < pad; i++) {
				out.append(' ');
			}
		}

		private void emitNodeList(IEsOperand[] xptNodeListData, TextTrans t, Format f)
				throws InterruptedException {
			assert xptNodeListData != null;
			assert t != null;
			assert f != null;
			for (int i = 0; i < xptNodeListData.length; i++) {
				final IEsOperand esNode = xptNodeListData[i];
				final EsType estNode = esNode.esType();
				if (estNode.isPrimitiveDatum) {
					emitText(esNode, t, f);
				} else {
					final EsObject esNodeObject = esNode.toObject(ecx);
					encodeNode(esNodeObject, t, f);
				}
			}
		}

		private void emitText(IEsOperand textDatum, TextTrans t, Format f)
				throws InterruptedException {
			assert textDatum != null;
			final String zTrans = t.transformNode(ecx, textDatum);
			if (zTrans.length() > 0) {
				out.append(zTrans);
				f.disableIndent();
			}
		}

		private void emitText(IEsOperand[] xptNodeListData, TextTrans t, Format f)
				throws InterruptedException {
			assert xptNodeListData != null;
			for (int i = 0; i < xptNodeListData.length; i++) {
				emitText(xptNodeListData[i], t, f);
			}
		}

		void encodeNode(EsObject node, TextTrans ct, Format cf)
				throws InterruptedException {
			assert node != null;
			assert ct != null;
			assert cf != null;
			final String oquctwTag = oquctwTag(node);
			final TextTrans nt = newTransformer(node, ct);
			final IEsOperand oTextDatum = oTextDatum(node);
			final IEsOperand[] oxptNodeListData = oxptNodeListData(node);
			final List<SourceAttribute> zlAttributes = zlAttributesAsc(node, oquctwTag);
			if (oquctwTag == null) {
				if (oTextDatum == null && oxptNodeListData == null) {
					emitText(node, nt, cf);
				} else {
					if (oTextDatum != null) {
						emitText(oTextDatum, nt, cf);
					}
					if (oxptNodeListData != null) {
						emitText(oxptNodeListData, nt, cf);
					}
				}
			} else {
				emitHead(oquctwTag, zlAttributes, nt, cf);
				if (!elementEndForbidden(oquctwTag)) {
					final Format nf = cf.down();
					if (oTextDatum != null) {
						emitText(oTextDatum, nt, nf);
					}
					if (oxptNodeListData != null) {
						emitNodeList(oxptNodeListData, nt, nf);
					}
					emitClose(oquctwTag, cf);
				}
			}
		}

		String encodeUri(EsObject uri)
				throws InterruptedException {
			assert uri != null;
			final List<String> zlPropertyNamesAsc = uri.esPropertyNames();
			final int pcount = zlPropertyNamesAsc.size();
			final BerylliumUrlBuilder urlBuilder = new BerylliumUrlBuilder();
			final List<Object> zlNameValuePairs = new ArrayList<Object>(pcount * 2);
			for (int i = 0; i < pcount; i++) {
				final String pname = zlPropertyNamesAsc.get(i);
				final IEsOperand esValue = uri.esGet(pname);
				if (pname.equals(PropertyName_URI_PATH)) {
					final String zcctwPath = esValue.toCanonicalString(ecx).trim();
					if (zcctwPath.length() > 0) {
						urlBuilder.setPath(BerylliumPath.newInstance(zcctwPath));
					}
					continue;
				}
				if (pname.equals(PropertyName_URI_SCHEME)) {
					urlBuilder.setScheme(esValue.toCanonicalString(ecx).trim());
					continue;
				}
				if (pname.equals(PropertyName_URI_HOST)) {
					urlBuilder.setHost(esValue.toCanonicalString(ecx).trim());
					continue;
				}
				if (pname.equals(PropertyName_URI_PORT)) {
					urlBuilder.setPort(esValue.toNumber(ecx).intVerified());
					continue;
				}
				if (pname.equals(PropertyName_URI_FRAGMENT)) {
					urlBuilder.setFragment(esValue.toCanonicalString(ecx).trim());
					continue;
				}
				if (noEnumPropertyName(pname)) {
					continue;
				}
				final EsType estValue = esValue.esType();
				if (!estValue.isDatum) {
					continue;
				}
				if (estValue.isPrimitiveDatum) {
					zlNameValuePairs.add(pname);
					zlNameValuePairs.add(esValue.toCanonicalString(ecx));
					continue;
				}
				if (esValue instanceof EsFunction) {
					continue;
				}
				final EsObject esValueObject = esValue.toObject(ecx);
				final int length = UNeon.length(ecx, esValueObject);
				if (length > 0) {
					final IEsOperand[] zptValues = UNeon.zptOperandsOnly(ecx, esValueObject, true, true);
					for (int ival = 0; ival < zptValues.length; ival++) {
						zlNameValuePairs.add(pname);
						zlNameValuePairs.add(zptValues[ival].toCanonicalString(ecx));
					}
				} else {
					zlNameValuePairs.add(pname);
					zlNameValuePairs.add(esValueObject.toCanonicalString(ecx));
				}
			}
			if (!zlNameValuePairs.isEmpty()) {
				final BerylliumQuery query = BerylliumQuery.newInstance(zlNameValuePairs);
				urlBuilder.setQuery(query);
			}
			return urlBuilder.qtwEncoded();
		}

		TextTrans newTransformer(EsObject node, TextTrans container)
				throws InterruptedException {
			final Boolean oEscape = nodePropertyBoolean(node, PropertyName_ESCAPE);
			final String ozSP = nodePropertyString(node, PropertyName_ENCODE_SP);
			final String ozLF = nodePropertyString(node, PropertyName_ENCODE_LF);
			final String ozDatumClass = nodePropertyString(node, PropertyName_DATUM_CLASS);
			final Boolean oDatumFocus = nodePropertyBoolean(node, PropertyName_DATUM_FOCUS);
			final Integer oDatumJsonDepth = nodePropertyInteger(node, PropertyName_DATUM_JSON_DEPTH);
			return container.transition(oEscape, ozSP, ozLF, ozDatumClass, oDatumFocus, oDatumJsonDepth);
		}

		Boolean nodePropertyBoolean(EsObject node, String pname) {
			final IEsOperand esValue = node.esGet(pname);
			if (!esValue.esType().isDatum) return null;
			return Boolean.valueOf(esValue.toCanonicalBoolean());
		}

		Integer nodePropertyInteger(EsObject node, String pname)
				throws InterruptedException {
			final IEsOperand esValue = node.esGet(pname);
			if (!esValue.esType().isDatum) return null;
			return Integer.valueOf(esValue.toNumber(ecx).intVerified());
		}

		String nodePropertyString(EsObject node, String pname)
				throws InterruptedException {
			final IEsOperand esValue = node.esGet(pname);
			if (!esValue.esType().isDatum) return null;
			return esValue.toCanonicalString(ecx);
		}

		String oquctwTag(EsObject node)
				throws InterruptedException {
			final IEsOperand esTag = node.esGet(PropertyName_TAG);
			final EsType estTag = esTag.esType();
			if (!estTag.isDatum) return null;
			final String zuctwTag = esTag.toCanonicalString(ecx).trim().toUpperCase();
			return zuctwTag.length() == 0 ? null : zuctwTag;
		}

		IEsOperand oTextDatum(EsObject node)
				throws InterruptedException {
			final IEsOperand esText = node.esGet(PropertyName_TEXT);
			final EsType estText = esText.esType();
			return (estText.isDefined) ? esText : null;
		}

		final IEsOperand[] oxptNodeListData(EsObject node)
				throws InterruptedException {
			final IEsOperand esNodeList = node.esGet(PropertyName_NODELIST);
			final EsType estNodeList = esNodeList.esType();
			if (!estNodeList.isDatum) return null;
			if (estNodeList.isPrimitiveDatum) {
				final IEsOperand[] xptNodeList = new IEsOperand[1];
				xptNodeList[0] = esNodeList;
				return xptNodeList;
			}
			final EsObject esNodeListObject = esNodeList.toObject(ecx);
			final int length = UNeon.length(ecx, esNodeListObject);
			if (length > 0) {
				final IEsOperand[] zpt = UNeon.zptOperandsOnly(ecx, esNodeListObject, true, true);
				return zpt.length == 0 ? null : zpt;
			}
			final IEsOperand[] xptNodeList = new IEsOperand[1];
			xptNodeList[0] = esNodeListObject;
			return xptNodeList;
		}

		final List<SourceAttribute> zlAttributesAsc(EsObject node, String oquctwTag)
				throws InterruptedException {
			assert node != null;
			if (oquctwTag == null) return Collections.emptyList();

			final List<String> zlPropertyNamesAsc = node.toObject(ecx).esPropertyNames();
			final int pcount = zlPropertyNamesAsc.size();
			final List<SourceAttribute> zlAsc = new ArrayList<SourceAttribute>(pcount);
			for (int i = 0; i < pcount; i++) {
				final String pname = zlPropertyNamesAsc.get(i);
				if (noEnumPropertyName(pname)) {
					continue;
				}
				if (reservedPropertyName(pname)) {
					continue;
				}
				final IEsOperand esValue = node.esGet(pname);
				final EsType estValue = esValue.esType();
				if (!estValue.isDatum) {
					continue;
				}
				if (esValue instanceof EsFunction) {
					continue;
				}
				final boolean uriEncode = estValue.isObject && uriPropertyName(pname);
				final String zValue;
				if (uriEncode) {
					final EsObject uriObject = esValue.toObject(ecx);
					zValue = encodeUri(uriObject);
				} else {
					final String zValueCssUnsafe = esValue.toCanonicalString(ecx);
					if (cssPropertyName(pname)) {
						zValue = cssSafe(zValueCssUnsafe);
					} else {
						zValue = zValueCssUnsafe;
					}
				}
				final String qlctwName = pname.toLowerCase();
				zlAsc.add(new SourceAttribute(qlctwName, zValue));
			}
			Collections.sort(zlAsc);
			return zlAsc;
		}

		public void encode()
				throws InterruptedException {
			if (oRoot != null) {
				final TextTrans t = new TextTrans();
				final Format f = new Format();
				encodeNode(oRoot, t, f);
			}
		}

		@Override
		public String toString() {
			return out.toString();
		}

		public Encoder(EsExecutionContext ecx, Args args) {
			if (args == null) throw new IllegalArgumentException("object is null");
			args.validate();
			this.ecx = ecx;
			this.oRoot = args.oRoot;
			this.oqtwDocType = args.oqtwDocType;
			this.oqtwDtdLocation = args.oqtwDtdLocation;
			this.indentUnits = Math.max(0, args.indent);
			this.out = new StringBuilder(4096);
		}
		final EsExecutionContext ecx;
		final EsObject oRoot;
		final String oqtwDocType;
		final String oqtwDtdLocation;
		final int indentUnits;
		final StringBuilder out;
	}
}
