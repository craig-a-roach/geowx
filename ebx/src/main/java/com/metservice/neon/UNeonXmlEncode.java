/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.metservice.argon.EnumDecoder;
import com.metservice.argon.text.ArgonTransformer;

/**
 * @author roach
 */
class UNeonXmlEncode {

	public static final EnumDecoder<ValidationMethod> DecoderValidationMethod = new EnumDecoder<ValidationMethod>(
			ValidationMethod.values(), "Document Validation Method");

	public static final EnumDecoder<DefaultForm> DecoderDefaultForm = new EnumDecoder<DefaultForm>(DefaultForm.values(),
			"Default Form");

	static final NSTag Tag_xmlns = new NSTag("xmlns");

	static final NSTag Tag_xmlns_xsi = new NSTag("xmlns", "xsi");

	static final NSTag Tag_xsi_schemaLocation = new NSTag("xsi", "schemaLocation");
	public static final String EOL = "\n";
	public static final String PropertyName_TAG = "TAG";

	public static final String PropertyName_TEXT = "TEXT";
	public static final String PropertyName_CDATA = "CDATA";
	public static final String PropertyName_NODELIST = "NODELIST";
	public static final String Prefix_NOXML = "NOXML_";
	public static final String Prefix_ELEMENT = "ELEMENT_";
	public static final int PrefixL_ELEMENT = Prefix_ELEMENT.length();
	public static final String Prefix_ATTRIBUTE = "ATTRIBUTE_";
	public static final int PrefixL_ATTRIBUTE = Prefix_ATTRIBUTE.length();
	public static final String Prefix_NAMESPACE = "NS";
	public static final int PrefixL_NAMESPACE = Prefix_NAMESPACE.length();
	public static final String Suffix_CDATA = "_CDATA";
	public static final int SuffixL_CDATA = Suffix_CDATA.length();

	public static String encode(EsExecutionContext ecx, Args args)
			throws InterruptedException {
		final Encoder e = new Encoder(ecx, args);
		e.encode();
		return e.out.toString();
	}

	public static Args newArgs(String qtwRootTag) {
		return new Args(qtwRootTag);
	}

	private static enum ContentModel {
		Mixed, Text, Element
	}

	private static class Encoder {

		private void classifyProperty(EsObject container, DefaultForm cform, String pname, List<SourceAttribute> attributes,
				List<SourceNode> nodes)
				throws InterruptedException {
			final IEsOperand oNonNull = UNeon.esoproperty(container, pname, true, true);
			if (oNonNull == null) return;
			if (oNonNull instanceof EsFunction) return;
			final ISource oSource = createSource(cform, pname, oNonNull);
			if (oSource == null) return;
			if (oSource instanceof SourceNode) {
				nodes.add((SourceNode) oSource);
				return;
			}
			if (oSource instanceof SourceAttribute) {
				attributes.add((SourceAttribute) oSource);
				return;
			}
		}

		private ContentModel contentModel(IEsOperand[] xptNonNull) {
			int countObject = 0;
			int countPrimitiveDatum = 0;
			for (int i = 0; i < xptNonNull.length; i++) {
				final IEsOperand nonNull = xptNonNull[i];
				if (nonNull.esType().isObject) {
					countObject++;
				} else {
					countPrimitiveDatum++;
				}
			}
			if (countObject > 0 && countPrimitiveDatum == 0) return ContentModel.Element;
			if (countObject == 0 && countPrimitiveDatum > 0) return ContentModel.Text;
			return ContentModel.Mixed;
		}

		private ISource createSource(DefaultForm cform, String pname, IEsOperand nonNull)
				throws InterruptedException {
			assert pname != null && pname.length() > 0;

			if (pname.startsWith(Prefix_NOXML)) return null;
			if (pname.equals(PropertyName_TAG)) return null;

			if (pname.equals(PropertyName_TEXT)) return createSourceText(nonNull);
			if (pname.equals(PropertyName_CDATA)) return createSourceCData(nonNull);
			if (pname.equals(PropertyName_NODELIST)) return createSourceNodeList(nonNull);

			if (pname.startsWith(Prefix_ELEMENT)) {
				final String ztwNoPrefix = pname.substring(PrefixL_ELEMENT);
				if (ztwNoPrefix.length() > 0) return createSourceElement(ztwNoPrefix, nonNull);
			}

			if (pname.startsWith(Prefix_ATTRIBUTE)) {
				final String ztwNoPrefix = pname.substring(PrefixL_ATTRIBUTE);
				if (ztwNoPrefix.length() > 0) return createSourceAttribute(ztwNoPrefix, nonNull);
			}

			final boolean isObject = nonNull.esType().isObject;
			if (isObject) return createSourceElement(pname, nonNull);
			if (cform == DefaultForm.Attribute) return createSourceAttribute(pname, nonNull);
			return createSourceElement(pname, nonNull);
		}

		private ISource createSourceAttribute(String pnameNoPrefix, IEsOperand nonNull)
				throws InterruptedException {
			assert pnameNoPrefix != null && pnameNoPrefix.length() > 0;
			assert nonNull != null;
			final String zValue = nonNull.toCanonicalString(ecx);
			final NSTag nstag = newNSTag(pnameNoPrefix);
			return new SourceAttribute(nstag, zValue);
		}

		private ISource createSourceCData(IEsOperand nonNull)
				throws InterruptedException {
			assert nonNull != null;
			final String zValue = nonNull.toCanonicalString(ecx);
			return new SourceNodeDatum(null, 0, true, zValue);
		}

		private ISource createSourceElement(String pnameNoPrefix, IEsOperand nonNull)
				throws InterruptedException {
			assert pnameNoPrefix != null && pnameNoPrefix.length() > 0;
			final int len = pnameNoPrefix.length();
			final StringBuilder bordinal = new StringBuilder(8);
			boolean consumeOrdinal = true;
			int pos = 0;
			while (pos < len && consumeOrdinal) {
				final char ch = pnameNoPrefix.charAt(pos);
				if (ch == '_') {
					pos++;
					consumeOrdinal = false;
				} else if (Character.isDigit(ch)) {
					bordinal.append(ch);
					pos++;
				} else {
					consumeOrdinal = false;
				}
			}
			String qNoOrdinal = pnameNoPrefix;
			int ordinal = 0;
			if (pos < len && bordinal.length() > 0) {
				try {
					qNoOrdinal = pnameNoPrefix.substring(pos);
					ordinal = Integer.parseInt(bordinal.toString());
				} catch (final NumberFormatException ex) {
				}
			}
			return createSourceElement(qNoOrdinal, ordinal, nonNull);
		}

		private ISource createSourceElement(String pnameNoOrdinalNoSuffix, int ordinal, boolean cdata, IEsOperand nonNull)
				throws InterruptedException {
			final NSTag nstag = newNSTag(pnameNoOrdinalNoSuffix);
			if (nonNull instanceof EsObject) {
				if (nonNull instanceof EsIntrinsicArray) {
					final IEsOperand[] zptNonNull = UNeon.zptOperandsOnly(ecx, (EsIntrinsicArray) nonNull, true, true);
					if (zptNonNull.length == 0) return null;
					final ContentModel contentModel = contentModel(zptNonNull);
					return new SourceNodeList(nstag, ordinal, cdata, zptNonNull, contentModel);
				}
				return new SourceNodeElement(nstag, ordinal, (EsObject) nonNull);
			}
			final String zValue = nonNull.toCanonicalString(ecx);
			return new SourceNodeDatum(nstag, ordinal, cdata, zValue);
		}

		private ISource createSourceElement(String pnameNoOrdinal, int ordinal, IEsOperand nonNull)
				throws InterruptedException {
			assert pnameNoOrdinal != null && pnameNoOrdinal.length() > 0;
			final int len = pnameNoOrdinal.length();
			boolean hasSuffix = false;
			String qNoSuffix = pnameNoOrdinal;
			if (pnameNoOrdinal.endsWith(Suffix_CDATA)) {
				final String zNoSuffix = pnameNoOrdinal.substring(0, len - SuffixL_CDATA);
				if (zNoSuffix.length() > 0) {
					qNoSuffix = zNoSuffix;
					hasSuffix = true;
				}
			}
			final boolean cdata = hasSuffix ? true : (defaultForm == DefaultForm.ElementCData);
			return createSourceElement(qNoSuffix, ordinal, cdata, nonNull);
		}

		private ISource createSourceNodeList(IEsOperand nonNull)
				throws InterruptedException {
			final IEsOperand[] zptNonNull;
			if (nonNull instanceof EsIntrinsicArray) {
				zptNonNull = UNeon.zptOperandsOnly(ecx, (EsIntrinsicArray) nonNull, true, true);
			} else {
				zptNonNull = new IEsOperand[1];
				zptNonNull[0] = nonNull;
			}
			if (zptNonNull.length == 0) return null;
			final ContentModel contentModel = contentModel(zptNonNull);
			return new SourceNodeList(null, 0, false, zptNonNull, contentModel);
		}

		private ISource createSourceText(IEsOperand nonNull)
				throws InterruptedException {
			assert nonNull != null;
			final String zValue = nonNull.toCanonicalString(ecx);
			return new SourceNodeDatum(null, 0, false, zValue);
		}

		private void emit(NSTag nstag) {
			if (nstag.zccPrefix.length() > 0) {
				out.append(nstag.zccPrefix);
				out.append(':');
			}
			out.append(nstag.qccTag);
		}

		private void emitAttribute(NSTag nstag, String zValue) {
			out.append(" ");
			emit(nstag);
			out.append("=\"");
			out.append(ArgonTransformer.zXmlEncode(zValue, false, true, true, true));
			out.append("\"");
		}

		private void emitAttributes(List<SourceAttribute> zlAttributesAsc) {
			assert zlAttributesAsc != null;
			final int attcount = zlAttributesAsc.size();
			for (int i = 0; i < attcount; i++) {
				final SourceAttribute attribute = zlAttributesAsc.get(i);
				emitAttribute(attribute.nstag, attribute.zValue);
			}
		}

		private void emitCData(String zValue) {
			if (zValue.length() > 0) {
				out.append("<![CDATA[");
				out.append(ArgonTransformer.zXmlEncode(zValue, false, false, false, false));
				out.append("]]>");
			}
		}

		private void emitDoctype() {
			switch (validationMethod) {
				case DTD: {
					if (oqtwDtdLocation == null) throw new EsApiCodeException("Missing DTD location");
					out.append(EOL);
					out.append("<!DOCTYPE ").append(qtwRootTag);
					out.append(" SYSTEM \"").append(oqtwDtdLocation).append("\">");
				}
				break;
				default:
			}
		}

		private void emitIndent(int depth) {
			final int pad = depth * indentUnits;
			for (int i = 0; i < pad; i++) {
				out.append(' ');
			}
		}

		private void emitProlog() {
			out.append("<?xml version=\"1.0\" encoding=\"").append(charset.name()).append("\"?>");
		}

		private void emitSchemaAttributes() {
			switch (validationMethod) {
				case W3Schema: {
					if (oqtwNamespaceUri == null) throw new EsApiCodeException("Missing Namespace URI");

					emitAttribute(Tag_xmlns, oqtwNamespaceUri);
					if (oqtwSchemaLocation != null) {
						emitAttribute(Tag_xmlns_xsi, "http://www.w3.org/2001/XMLSchema-instance");
						emitAttribute(Tag_xsi_schemaLocation, oqtwNamespaceUri + " " + oqtwSchemaLocation);
					}
				}
				break;
				default:
			}
		}

		private void emitTagClose(NSTag nstag, int depth) {
			if (m_indenting) {
				out.append(EOL);
				emitIndent(depth);
			}
			out.append("</");
			emit(nstag);
			out.append(">");
		}

		private void emitTagCloseDatum(NSTag nstag) {
			out.append("</");
			emit(nstag);
			out.append(">");
		}

		private void emitTagOpenDatum(NSTag nstag, int depth, boolean hasLength) {
			if (m_indenting) {
				out.append(EOL);
				emitIndent(depth);
			}
			out.append("<");
			emit(nstag);
			if (hasLength) {
				out.append(">");
			} else {
				out.append("/>");
			}
		}

		private void emitTagOpenEnd(boolean hasNodes) {
			if (hasNodes) {
				out.append(">");
			} else {
				out.append("/>");
			}
		}

		private void emitTagOpenStart(NSTag nstag, int depth) {
			if (m_indenting) {
				out.append(EOL);
				emitIndent(depth);
			}
			out.append("<");
			emit(nstag);
		}

		private void emitText(String zValue) {
			if (zValue.length() > 0) {
				out.append(ArgonTransformer.zXmlEncode(zValue, false, false, false, false));
			}
		}

		private NSTag newNSTag(String qtwSpec) {
			assert qtwSpec != null && qtwSpec.length() > 0;
			final int posColon = qtwSpec.indexOf(':');
			final String ztwPreColon;
			final String ztwPostColon;
			if (posColon < 0) {
				ztwPreColon = "";
				ztwPostColon = qtwSpec;
			} else {
				ztwPreColon = qtwSpec.substring(0, posColon).trim();
				ztwPostColon = qtwSpec.substring(posColon + 1).trim();
			}
			if (ztwPostColon.length() == 0) throw new EsApiCodeException("Malformed tag '" + qtwSpec + "'");

			if (validationMethod != ValidationMethod.W3Schema) return new NSTag(ztwPostColon);
			if (ztwPreColon.length() > 0) return new NSTag(ztwPreColon, ztwPostColon);

			if (!ztwPostColon.startsWith(Prefix_NAMESPACE)) return new NSTag(ztwPostColon);
			final String ztwPostPrefix = ztwPostColon.substring(PrefixL_NAMESPACE);
			if (ztwPostPrefix.length() == 0) return new NSTag(ztwPostColon);
			final int posSep = ztwPostPrefix.indexOf('_');
			if (posSep < 1) return new NSTag(ztwPostColon);
			final String qccPrefix = ztwPostPrefix.substring(0, posSep);
			final String zccTag = ztwPostPrefix.substring(posSep + 1);
			if (zccTag.length() == 0) return new NSTag(ztwPostColon);
			return new NSTag(qccPrefix, zccTag);
		}

		private DefaultForm selectContainerForm(List<String> zlPropertyKeys) {
			final int pcount = zlPropertyKeys.size();
			for (int i = 0; i < pcount; i++) {
				final String pname = zlPropertyKeys.get(i);
				if (pname.equals(PropertyName_TEXT)) return DefaultForm.Attribute;
				if (pname.equals(PropertyName_CDATA)) return DefaultForm.Attribute;
				if (pname.equals(PropertyName_NODELIST)) return DefaultForm.Attribute;
			}
			return defaultForm;
		}

		void encodeDatum(NSTag oNsTag, String zValue, boolean cdata, int depth) {
			final boolean hasTag = oNsTag != null;
			final boolean hasLength = zValue.length() > 0;
			if (!hasTag && hasLength) {
				m_indenting = false;
			}
			if (hasTag) {
				emitTagOpenDatum(oNsTag, depth, hasLength);
			}
			if (hasLength) {
				if (cdata) {
					emitCData(zValue);
				} else {
					emitText(zValue);
				}
				if (hasTag) {
					emitTagCloseDatum(oNsTag);
				}
			}
		}

		void encodeDatum(SourceNodeDatum datum, int depth) {
			assert datum != null;
			encodeDatum(datum.oNsTag, datum.zValue, datum.cdata, depth);
		}

		void encodeElement(NSTag nstag, EsObject container, int depth)
				throws InterruptedException {
			assert container != null;
			final List<String> zlPropertyNamesAsc = container.toObject(ecx).esPropertyNames();
			final DefaultForm cform = selectContainerForm(zlPropertyNamesAsc);
			final int pcount = zlPropertyNamesAsc.size();
			final List<SourceAttribute> zlAttributesAsc = new ArrayList<SourceAttribute>(pcount);
			final List<SourceNode> zlNodesAsc = new ArrayList<SourceNode>(pcount);
			for (int i = 0; i < pcount; i++) {
				final String pname = zlPropertyNamesAsc.get(i);
				classifyProperty(container, cform, pname, zlAttributesAsc, zlNodesAsc);
			}
			Collections.sort(zlNodesAsc);
			Collections.sort(zlAttributesAsc);
			final int nodeCount = zlNodesAsc.size();
			final boolean hasNodes = nodeCount > 0;
			emitTagOpenStart(nstag, depth);
			if (depth == 0) {
				emitSchemaAttributes();
			}
			emitAttributes(zlAttributesAsc);
			emitTagOpenEnd(hasNodes);
			final boolean saveIndenting = m_indenting;
			for (int i = 0; i < nodeCount; i++) {
				final SourceNode node = zlNodesAsc.get(i);
				encodeNode(node, depth + 1);
			}
			if (hasNodes) {
				emitTagClose(nstag, depth);
			}
			m_indenting = saveIndenting;
		}

		void encodeNode(SourceNode node, int depth)
				throws InterruptedException {
			assert node != null;
			if (node instanceof SourceNodeDatum) {
				encodeDatum((SourceNodeDatum) node, depth);
				return;
			}
			if (node instanceof SourceNodeElement) {
				final SourceNodeElement element = (SourceNodeElement) node;
				encodeElement(element.nstag, element.container, depth);
				return;
			}
			if (node instanceof SourceNodeList) {
				encodeNodeList((SourceNodeList) node, depth);
				return;
			}
			throw new IllegalArgumentException("unsupported node>" + node.getClass() + "<");
		}

		void encodeNodeList(SourceNodeList nodeList, int depth)
				throws InterruptedException {
			assert nodeList != null;
			switch (nodeList.contentModel) {
				case Element:
					for (int i = 0; i < nodeList.xptNonNull.length; i++) {
						encodeNodeListElement(nodeList, i, depth);
					}
				break;
				case Text:
					for (int i = 0; i < nodeList.xptNonNull.length; i++) {
						encodeNodeListText(nodeList, i, depth);
					}
				break;
				default:
					m_indenting = false;
					for (int i = 0; i < nodeList.xptNonNull.length; i++) {
						encodeNodeListMixed(nodeList, i, depth);
					}
			}
		}

		void encodeNodeListElement(SourceNodeList nodeList, int index, int depth)
				throws InterruptedException {
			final IEsOperand nonNull = nodeList.xptNonNull[index];
			final EsObject neo = nonNull.toObject(ecx);
			final String oqtwNeoTagSpec = UNeon.property_oqtwString(ecx, neo, PropertyName_TAG);
			NSTag oNeoNSTag = nodeList.oNsTag;
			if (oqtwNeoTagSpec != null) {
				oNeoNSTag = newNSTag(oqtwNeoTagSpec);
			}
			if (oNeoNSTag == null) {
				emitText(nonNull.toCanonicalString(ecx));
			} else {
				encodeElement(oNeoNSTag, neo, depth);
			}
		}

		void encodeNodeListMixed(SourceNodeList nodeList, int index, int depth)
				throws InterruptedException {
			final IEsOperand nonNull = nodeList.xptNonNull[index];
			if (nonNull instanceof EsObject) {
				encodeNodeListElement(nodeList, index, depth);
			} else {
				emitText(nonNull.toCanonicalString(ecx));
			}
		}

		void encodeNodeListText(SourceNodeList nodeList, int index, int depth)
				throws InterruptedException {
			final IEsOperand nonNull = nodeList.xptNonNull[index];
			final String zValue = nonNull.toCanonicalString(ecx);
			if (nodeList.oNsTag == null) {
				emitText(nonNull.toCanonicalString(ecx));
			} else {
				encodeDatum(nodeList.oNsTag, zValue, nodeList.cdata, depth);
			}
		}

		public void encode()
				throws InterruptedException {
			emitProlog();
			emitDoctype();
			if (oRoot == null) {
				out.append("<");
				out.append(qtwRootTag);
				emitSchemaAttributes();
				out.append("/>");
			} else {
				final NSTag rootTag = new NSTag(qtwRootTag);
				encodeElement(rootTag, oRoot, 0);
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
			this.qtwRootTag = args.qtwRootTag;
			this.oRoot = args.oRoot;
			this.oqtwNamespaceUri = args.oqtwNamespaceUri;
			this.oqtwSchemaLocation = args.oqtwSchemaLocation;
			this.oqtwDtdLocation = args.oqtwDtdLocation;
			this.validationMethod = args.validationMethod;
			this.defaultForm = args.defaultForm;
			this.charset = args.charset;
			this.indentUnits = Math.max(0, args.indent);
			this.out = new StringBuilder(4096);
			m_indenting = true;
		}

		final EsExecutionContext ecx;
		final String qtwRootTag;
		final EsObject oRoot;
		final String oqtwNamespaceUri;
		final String oqtwSchemaLocation;
		final String oqtwDtdLocation;
		final ValidationMethod validationMethod;
		final DefaultForm defaultForm;
		final Charset charset;
		final int indentUnits;
		final StringBuilder out;
		private boolean m_indenting;
	}

	private static interface ISource {
	}

	private static class NSTag implements Comparable<NSTag> {

		@Override
		public int compareTo(NSTag rhs) {
			final int c0 = zccPrefix.compareTo(rhs.zccPrefix);
			if (c0 != 0) return c0;
			final int c1 = qccTag.compareTo(rhs.qccTag);
			return c1;
		}

		@Override
		public String toString() {
			return zccPrefix.length() == 0 ? qccTag : (zccPrefix + ":" + qccTag);
		}

		public NSTag(String qccTag) {
			this("", qccTag);
		}

		public NSTag(String zccPrefix, String qccTag) {
			if (zccPrefix == null) throw new IllegalArgumentException("object is null");
			if (qccTag == null || qccTag.length() == 0) throw new IllegalArgumentException("string is null or empty");
			this.zccPrefix = zccPrefix;
			this.qccTag = qccTag;
		}
		public final String zccPrefix;
		public final String qccTag;
	}

	private static class SourceAttribute implements ISource, Comparable<SourceAttribute> {

		@Override
		public int compareTo(SourceAttribute rhs) {
			return nstag.compareTo(rhs.nstag);
		}

		public SourceAttribute(NSTag nstag, String zValue) {
			assert nstag != null;
			assert zValue != null;
			this.nstag = nstag;
			this.zValue = zValue;
		}
		public final NSTag nstag;
		public final String zValue;
	}

	private static abstract class SourceNode implements ISource, Comparable<SourceNode> {

		@Override
		public int compareTo(SourceNode rhs) {
			final int lhsOrdinal = ordinal();
			final int rhsOrdinal = rhs.ordinal();
			if (lhsOrdinal < rhsOrdinal) return -1;
			if (lhsOrdinal > rhsOrdinal) return +1;
			final NSTag oLhsNSTag = getNSTag();
			final NSTag oRhsNSTag = rhs.getNSTag();
			if (oLhsNSTag == null) return -1;
			if (oRhsNSTag == null) return +1;
			return oLhsNSTag.compareTo(oRhsNSTag);
		}

		public abstract NSTag getNSTag();

		public abstract int ordinal();

		protected SourceNode() {
		}
	}

	private static class SourceNodeDatum extends SourceNode {

		@Override
		public NSTag getNSTag() {
			return oNsTag;
		}

		@Override
		public int ordinal() {
			return ordinal;
		}

		public SourceNodeDatum(NSTag oNsTag, int ordinal, boolean cdata, String zValue) {
			assert zValue != null;
			this.oNsTag = oNsTag;
			this.ordinal = ordinal;
			this.cdata = cdata;
			this.zValue = zValue;
		}
		public final NSTag oNsTag;
		public final int ordinal;
		public final boolean cdata;
		public final String zValue;
	}

	private static class SourceNodeElement extends SourceNode {

		@Override
		public NSTag getNSTag() {
			return nstag;
		}

		@Override
		public int ordinal() {
			return ordinal;
		}

		public SourceNodeElement(NSTag nstag, int ordinal, EsObject container) {
			this.nstag = nstag;
			this.ordinal = ordinal;
			this.container = container;
		}
		public final NSTag nstag;
		public final int ordinal;
		public final EsObject container;
	}

	private static class SourceNodeList extends SourceNode {

		@Override
		public NSTag getNSTag() {
			return oNsTag;
		}

		@Override
		public int ordinal() {
			return ordinal;
		}

		public SourceNodeList(NSTag oNsTag, int ordinal, boolean cdata, IEsOperand[] xptNonNull, ContentModel contentModel) {
			assert xptNonNull != null;
			assert contentModel != null;
			this.oNsTag = oNsTag;
			this.ordinal = ordinal;
			this.cdata = cdata;
			this.xptNonNull = xptNonNull;
			this.contentModel = contentModel;
		}
		public final NSTag oNsTag;
		public final int ordinal;
		public final boolean cdata;
		public final IEsOperand[] xptNonNull;
		public final ContentModel contentModel;
	}

	public static class Args {

		void validate() {
			if (qtwRootTag == null || qtwRootTag.length() == 0)
				throw new IllegalArgumentException("string is null or empty");
			if (validationMethod == null) throw new IllegalArgumentException("object is null");
			if (charset == null) throw new IllegalArgumentException("string is null or empty");
		}

		public Args(String qtwRootTag) {
			this.qtwRootTag = qtwRootTag;
		}

		public final String qtwRootTag;
		public EsObject oRoot;
		public String oqtwNamespaceUri;
		public String oqtwSchemaLocation;
		public String oqtwDtdLocation;
		public ValidationMethod validationMethod;
		public DefaultForm defaultForm;
		public Charset charset;
		public int indent;
	}

	public static enum DefaultForm {
		ElementText, ElementCData, Attribute
	}

	public static enum ValidationMethod {
		None, DTD, W3Schema
	}
}
