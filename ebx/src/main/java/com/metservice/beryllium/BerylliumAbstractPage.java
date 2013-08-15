/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.metservice.argon.text.ArgonTransformer;

/**
 * @author roach
 */
public abstract class BerylliumAbstractPage {

	private static Logger logger() {
		return Log.getLogger("Page");
	}

	protected static void br(PrintWriter writer) {
		writer.println("<br>");
	}

	protected static void bulletsEnd(PrintWriter writer) {
		writer.println("</ul>");
	}

	protected static void bulletsStart(PrintWriter writer) {
		writer.println("<ul>");
	}

	protected static void buttonFile(PrintWriter writer, String name) {
		writer.print("<input type=\"FILE\" name=\"" + name + "\"/>");
	}

	protected static void buttonSubmit(PrintWriter writer, String label) {
		final String value = ArgonTransformer.zHtmlEncodeATTVAL(label);
		writer.print("<input type=\"SUBMIT\" value=\"" + value + "\"/>");
	}

	protected static void code(PrintWriter writer, String ozValue) {
		if (ozValue != null) {
			writer.println("<code>" + ArgonTransformer.zHtmlEncodePCDATA(ozValue) + "</code>");
		}
	}

	protected static void codeBlock(PrintWriter writer, String ozValue) {
		if (ozValue != null) {
			writer.println("<div class=\"codeblock\">" + ArgonTransformer.zHtmlEncodePCDATA(ozValue) + "</div>");
		}
	}

	protected static void contentEnd(PrintWriter writer) {
		writer.println("</div>");
	}

	protected static void contentRightEnd(PrintWriter writer) {
		writer.println("</div>");
	}

	protected static void contentRightStart(PrintWriter writer) {
		writer.println("<div id=\"contentR\">");
	}

	protected static void contentStart(PrintWriter writer) {
		writer.println("<div id=\"content\">");
	}

	protected static void cssLink(PrintWriter writer, BerylliumPath href) {
		final String qheHref = ArgonTransformer.zHtmlEncodeATTVAL(href.qtwEncodedPath());
		writer.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + qheHref + "\"/>");
	}

	protected static void fieldsetEnd(PrintWriter writer) {
		writer.println("</fieldset>");
	}

	protected static void fieldsetStart(PrintWriter writer, String oqLegend) {
		writer.println("<fieldset>");
		if (oqLegend != null) {
			writer.println("<legend>" + ArgonTransformer.zHtmlEncodePCDATA(oqLegend) + "</legend>");
		}
	}

	protected static void filelistEnd(PrintWriter writer) {
		writer.println("</ul>");
	}

	protected static void filelistLink(PrintWriter writer, BerylliumPath href, String zTitle) {
		itemStart(writer);
		link(writer, href, zTitle);
		itemEnd(writer);
	}

	protected static void filelistStart(PrintWriter writer) {
		writer.println("<ul class=\"filelist\">");
	}

	protected static void formEnd(PrintWriter writer) {
		writer.println("</form>");
	}

	protected static void formStart(PrintWriter writer, BerylliumPath href, boolean usePOST, boolean useMultiPart) {
		formStart(writer, new BerylliumPathQuery(href), usePOST, useMultiPart);
	}

	protected static void formStart(PrintWriter writer, BerylliumPathQuery href, boolean usePOST, boolean useMultiPart) {
		if (href == null) throw new IllegalArgumentException("object is null");
		final String method = usePOST ? "POST" : "GET";
		final BerylliumPathQuery neo = href.newPathQuery(CBeryllium.UrlQueryArg_FormMode, CBeryllium.UrlQueryVal_FormMode_Edit);
		final String qheHref = ArgonTransformer.zHtmlEncodeATTVAL(neo.toString());
		final String encType = useMultiPart ? "multipart/form-data" : MimeTypes.FORM_ENCODED;
		writer.println("<form method=\"" + method + "\" enctype=\"" + encType + "\" action=\"" + qheHref + "\">");
	}

	protected static void formStart(PrintWriter writer, boolean usePOST) {
		final String method = usePOST ? "POST" : "GET";
		writer.println("<form method=\"" + method + "\">");
	}

	protected static void inputCheckbox(PrintWriter writer, String name, boolean value) {
		final String zChecked = value ? " checked" : "";
		writer.print("<input type=\"CHECKBOX\" name=\"" + name + "\"" + zChecked + "/>");
	}

	protected static void inputText(PrintWriter writer, String name, String zValue, int width) {
		final String zheValue = ArgonTransformer.zHtmlEncodeATTVAL(zValue);
		writer.print("<input type=\"TEXT\" name=\"" + name + "\" size=\"" + width + "\" value=\"" + zheValue + "\"/>");
	}

	protected static void inputTextArea(PrintWriter writer, String oqId, String qName, int cols, int rows, String zValue) {
		final String zheValue = ArgonTransformer.zHtmlEncode(zValue, false, " ", "\n", "\t", "\'", "\"");
		final StringBuilder tag = new StringBuilder();
		tag.append("<textarea");
		if (oqId != null) {
			tag.append(" id=\"").append(oqId).append("\"");
		}
		tag.append(" name=\"").append(qName).append("\"");
		if (cols > 0) {
			tag.append(" cols=\"").append(cols).append("\"");
		}
		if (rows > 0) {
			tag.append(" rows=\"").append(rows).append("\"");
		}
		tag.append(">");
		writer.print(tag.toString());
		writer.print(zheValue);
		writer.println("</textarea>");
	}

	protected static boolean isChecked(String name, Request rq) {
		if (name == null || name.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (rq == null) throw new IllegalArgumentException("object is null");
		final String ozValue = rq.getParameter(name);
		return ozValue != null;
	}

	protected static boolean isChecked(String name, Request rq, boolean ex) {
		if (name == null || name.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (rq == null) throw new IllegalArgumentException("object is null");
		final boolean editMode = isEditMode(rq);
		final boolean checked = editMode ? isChecked(name, rq) : ex;
		return checked;
	}

	protected static boolean isEditMode(Request rq) {
		if (rq == null) throw new IllegalArgumentException("object is null");
		final String ozValue = rq.getParameter(CBeryllium.UrlQueryArg_FormMode);
		return ozValue != null && ozValue.equals(CBeryllium.UrlQueryVal_FormMode_Edit);
	}

	protected static void item(PrintWriter writer, String zValue) {
		writer.println("<li>" + ArgonTransformer.zHtmlEncodePCDATA(zValue) + "</li>");
	}

	protected static void itemAttention(PrintWriter writer, String zValue) {
		writer.println("<li class=\"attention\">" + ArgonTransformer.zHtmlEncodePCDATA(zValue) + "</li>");
	}

	protected static void itemAttention(PrintWriter writer, Throwable cause) {
		final String ozValue = cause.getMessage();
		final String qValue = ozValue == null || ozValue.length() == 0 ? cause.toString() : ozValue;
		writer.println("<li class=\"exception\">" + ArgonTransformer.zHtmlEncodePCDATA(qValue) + "</li>");
		logger().info("User Attention Message", cause);
	}

	protected static void itemEnd(PrintWriter writer) {
		writer.println("</li>");
	}

	protected static void itemStart(PrintWriter writer) {
		writer.println("<li>");
	}

	protected static void javascriptLoad(PrintWriter writer, BerylliumPath src) {
		final String qSrc = ArgonTransformer.zHtmlEncodeATTVAL(src.qtwEncodedPath());
		writer.println("<script language=\"javascript\" type=\"text/javascript\" src=\"" + qSrc + "\"></script>\n");
	}

	protected static void javascriptSource(PrintWriter writer, String zValue) {
		writer.println("<script language=\"javascript\" type=\"text/javascript\">");
		writer.println(zValue);
		writer.println("</script>");
	}

	protected static void link(PrintWriter writer, BerylliumPath href, String zValue) {
		final String qheHref = ArgonTransformer.zHtmlEncodeATTVAL(href.qtwEncodedPath());
		writer.println("<a href=\"" + qheHref + "\">" + ArgonTransformer.zHtmlEncodePCDATA(zValue) + "</a>");
	}

	protected static void link(PrintWriter writer, BerylliumPathQuery href, String zValue) {
		final String qheHref = ArgonTransformer.zHtmlEncodeATTVAL(href.ztwPathQueryEncoded());
		writer.println("<a href=\"" + qheHref + "\">" + ArgonTransformer.zHtmlEncodePCDATA(zValue) + "</a>");
	}

	protected static void link(PrintWriter writer, String qHref, String zValue) {
		final String qheHref = ArgonTransformer.zHtmlEncodeATTVAL(qHref);
		writer.println("<a href=\"" + qheHref + "\">" + ArgonTransformer.zHtmlEncodePCDATA(zValue) + "</a>");
	}

	protected static void menubar(PrintWriter writer, Object... hrefTitle) {
		writer.println("<ul id=\"menubar\">");
		final int len = hrefTitle.length;
		for (int i = 0; i < len; i += 2) {
			final int ihref = i;
			final Object ohref = hrefTitle[ihref];
			if (ohref == null) {
				continue;
			}
			final int ititle = i + 1;
			final Object otitle = hrefTitle[ititle];
			if (otitle == null) {
				continue;
			}
			final String ztitle = otitle.toString();
			itemStart(writer);
			if (ohref instanceof BerylliumPath) {
				link(writer, ((BerylliumPath) ohref), ztitle);
			} else if (ohref instanceof BerylliumPathQuery) {
				link(writer, ((BerylliumPathQuery) ohref), ztitle);
			} else {
				link(writer, ohref.toString(), ztitle);
			}
			itemEnd(writer);
		}
		writer.println("</ul>");
	}

	protected static void navlist(PrintWriter writer, Object... hrefTitle) {
		navlistStart(writer);
		final int len = hrefTitle.length;
		for (int i = 0; i < len; i += 2) {
			final int ihref = i;
			final Object ohref = hrefTitle[ihref];
			if (ohref == null) {
				continue;
			}
			final int ititle = i + 1;
			final String ztitle = ititle < len ? hrefTitle[ititle].toString() : "";
			itemStart(writer);
			if (ohref instanceof BerylliumPath) {
				link(writer, ((BerylliumPath) ohref), ztitle);
			} else if (ohref instanceof BerylliumPathQuery) {
				link(writer, ((BerylliumPathQuery) ohref), ztitle);
			} else {
				link(writer, ohref.toString(), ztitle);
			}
			itemEnd(writer);
		}
		navlistEnd(writer);
	}

	protected static void navlistEnd(PrintWriter writer) {
		writer.println("</ul>");
	}

	protected static void navlistStart(PrintWriter writer) {
		writer.println("<ul class=\"navlist\">");
	}

	protected static void numbersEnd(PrintWriter writer) {
		writer.println("</ol>");
	}

	protected static void numbersStart(PrintWriter writer) {
		writer.println("<ol>");
	}

	protected static void para(PrintWriter writer, String zValue) {
		writer.println("<p>" + ArgonTransformer.zHtmlEncodePCDATA(zValue) + "</p>");
	}

	protected static void paraAttention(PrintWriter writer, String zValue) {
		writer.println("<p class=\"attention\">" + ArgonTransformer.zHtmlEncodePCDATA(zValue) + "</p>");
	}

	protected static void row(PrintWriter writer, boolean firstHeader, String... zptValues) {
		writer.println("<tr>");
		for (int i = 0; i < zptValues.length; i++) {
			final boolean th = firstHeader && i == 0;
			writer.println(th ? "<th>" : "<td>");
			text(writer, zptValues[i]);
			writer.println(th ? "</th>" : "</td>");
		}
		writer.println("</tr>");
	}

	protected static void rowEnd(PrintWriter writer) {
		writer.println("</tr>");
	}

	protected static void rowHeader(PrintWriter writer, String... zptValues) {
		writer.println("<tr>");
		for (int i = 0; i < zptValues.length; i++) {
			writer.println("<th>");
			text(writer, zptValues[i]);
			writer.println("</th>");
		}
		writer.println("</tr>");
	}

	protected static void rowInputCheckbox(PrintWriter writer, String label, String name, boolean value) {
		writer.println("<tr><th>");
		text(writer, label);
		writer.println("</th><td>");
		inputCheckbox(writer, name, value);
		writer.println("</td></tr>");
	}

	protected static void rowInputFile(PrintWriter writer, String label, String name) {
		writer.println("<tr><th>");
		text(writer, label);
		writer.println("</th><td>");
		buttonFile(writer, name);
		writer.println("</td></tr>");
	}

	protected static void rowInputText(PrintWriter writer, String label, String name, String zValue, int width) {
		writer.println("<tr><th>");
		text(writer, label);
		writer.println("</th><td>");
		inputText(writer, name, zValue, width);
		writer.println("</td></tr>");
	}

	protected static void rowLinkText(PrintWriter writer, BerylliumPath href, String zLinkLabel, String... zptValues) {
		writer.println("<tr><td>");
		link(writer, href, zLinkLabel);
		writer.println("</td>");
		for (int i = 0; i < zptValues.length; i++) {
			writer.println("<td>");
			text(writer, zptValues[i]);
			writer.println("</td>");
		}
		writer.println("</tr>");
	}

	protected static void rowStart(PrintWriter writer) {
		writer.println("<tr>");
	}

	protected static void sidebarEnd(PrintWriter writer) {
		writer.println("</div>");
	}

	protected static void sidebarStart(PrintWriter writer) {
		writer.println("<div id=\"sidebar\">");
	}

	protected static void tableBodyEnd(PrintWriter writer) {
		writer.println("</tbody></table>");
	}

	protected static void tableBodyStart(PrintWriter writer, String ozCaption) {
		if (ozCaption == null || ozCaption.length() == 0) {
			writer.println("<table><tbody>");
		} else {
			writer.println("<table><caption>" + ArgonTransformer.zHtmlEncodePCDATA(ozCaption) + "</caption><tbody>");
		}
	}

	protected static void tableHeadEndBodyStart(PrintWriter writer) {
		writer.println("</thead><tbody>");
	}

	protected static void tableHeadStart(PrintWriter writer, String ozCaption) {
		if (ozCaption == null || ozCaption.length() == 0) {
			writer.println("<table><thead>");
		} else {
			writer.println("<table><caption>" + ArgonTransformer.zHtmlEncodePCDATA(ozCaption) + "</caption><thead>");
		}
	}

	protected static void tableSubmit(PrintWriter writer, String label) {
		writer.println("<table><tbody><tr><td align=\"right\">");
		buttonSubmit(writer, label);
		writer.println("</td></tr></tbody></table>");
	}

	protected static void tdEnd(PrintWriter writer) {
		writer.println("</td>");
	}

	protected static void tdStart(PrintWriter writer) {
		writer.println("<td>");
	}

	protected static void text(PrintWriter writer, String zValue) {
		writer.print(ArgonTransformer.zHtmlEncodePCDATA(zValue));
	}

	protected static void textArea(PrintWriter writer, String oqId, String zValue) {
		final String zheValue = ArgonTransformer.zHtmlEncode(zValue, false, " ", "\n", "\t", "\'", "\"");
		final StringBuilder tag = new StringBuilder();
		tag.append("<textarea");
		if (oqId != null) {
			tag.append(" id=\"").append(oqId).append("\"");
		}
		tag.append(">");
		writer.print(tag.toString());
		writer.print(zheValue);
		writer.println("</textarea>");
	}

	protected static void thEnd(PrintWriter writer) {
		writer.println("</th>");
	}

	protected static void thStart(PrintWriter writer) {
		writer.println("<th>");
	}

	protected static void title(PrintWriter writer, String zValue) {
		writer.println("<h2>" + ArgonTransformer.zHtmlEncodePCDATA(zValue) + "</h2>");
	}

	protected static void titlesub(PrintWriter writer, String zValue) {
		writer.println("<h3>" + ArgonTransformer.zHtmlEncodePCDATA(zValue) + "</h3>");
	}

	protected static String zFieldValue(String name, Request rq, String zEx) {
		if (name == null || name.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (rq == null) throw new IllegalArgumentException("object is null");
		if (zEx == null) throw new IllegalArgumentException("object is null");
		final boolean editMode = isEditMode(rq);
		final String zNeo;
		if (editMode) {
			final String ozValue = rq.getParameter(name);
			zNeo = ozValue == null ? "" : ozValue;
		} else {
			zNeo = zEx;
		}
		return zNeo;
	}

	protected static Map<String, String> zmFieldValue(String namePrefix, Request rq, Map<String, String> zmEx) {
		if (namePrefix == null || namePrefix.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (rq == null) throw new IllegalArgumentException("object is null");
		if (zmEx == null) throw new IllegalArgumentException("object is null");
		final boolean editMode = isEditMode(rq);
		final Map<String, String> zmNeo;
		if (editMode) {
			final int prefixLen = namePrefix.length();
			final Enumeration<?> parameterNames = rq.getParameterNames();
			zmNeo = new HashMap<String, String>(32);
			while (parameterNames.hasMoreElements()) {
				final Object oName = parameterNames.nextElement();
				if (oName == null) {
					continue;
				}
				final String ztwName = oName.toString().trim();
				if (!ztwName.startsWith(namePrefix)) {
					continue;
				}
				final String ztwTail = ztwName.substring(prefixLen);
				if (ztwTail.length() == 0) {
					continue;
				}
				final String ozValue = rq.getParameter(ztwName);
				final String zValue = ozValue == null ? "" : ozValue;
				zmNeo.put(ztwTail, zValue);
			}
		} else {
			zmNeo = zmEx;
		}
		return zmNeo;
	}

	protected BerylliumAbstractPage() {
	}
}
