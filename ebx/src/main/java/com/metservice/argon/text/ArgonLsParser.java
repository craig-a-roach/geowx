/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.DateFactory;
import com.metservice.argon.DateFormatter;
import com.metservice.argon.TimeFactors;

/**
 * @author roach
 */
public class ArgonLsParser {

	public static final Comparator<Node> ByName = new Comparator<Node>() {

		@Override
		public int compare(Node lhs, Node rhs) {
			return lhs.qccName.compareTo(rhs.qccName);
		}
	};

	public static final Comparator<Node> ByLastModified = new Comparator<Node>() {

		@Override
		public int compare(Node lhs, Node rhs) {
			return ArgonCompare.fwd(lhs.tsLastModified, rhs.tsLastModified);
		}
	};

	public static final Comparator<Node> BySize = new Comparator<Node>() {

		@Override
		public int compare(Node lhs, Node rhs) {
			return ArgonCompare.fwd(lhs.bcSize, rhs.bcSize);
		}
	};

	private static Pair createPair(String ztw) {
		final int wpos = ztw.indexOf(' ');
		if (wpos < 1) return null;
		final String ztwHead = ztw.substring(0, wpos).trim();
		if (ztwHead.length() == 0) return null;
		final String ztwTail = ztw.substring(wpos + 1).trim();
		return new Pair(ztwHead, ztwTail);
	}

	private static boolean directory(NodeBuilder n, String qtw) {
		final char ch = qtw.charAt(0);
		if (ch == 'd') {
			n.isDirectory = true;
			return true;
		}
		if (ch == '-') {
			n.isDirectory = false;
			return true;
		}
		return false;
	}

	private static boolean lastModified(NodeBuilder n, String qtwA, String qtwB, String qtwC, TimeZone timeZone, TimeFactors now) {
		try {
			final Date lm = DateFactory.newRecent(qtwA, qtwB, qtwC, timeZone, now.year, now.moyJan0 + 1);
			n.tsLastModified = lm.getTime();
			return true;
		} catch (final ArgonFormatException ex) {
		}
		return false;
	}

	private static boolean name(NodeBuilder n, String qtw) {
		n.qccName = qtw;
		return true;
	}

	private static boolean size(NodeBuilder n, String qtw) {
		try {
			n.bcSize = Long.parseLong(qtw);
			return true;
		} catch (final NumberFormatException ex) {
		}
		return false;
	}

	public static Node createNode(String qtwLine, TimeZone timeZone, TimeFactors now) {
		if (qtwLine == null || qtwLine.length() == 0) throw new IllegalArgumentException("string is null or empty");

		Pair oP = createPair(qtwLine); // perms
		if (oP == null) return null;
		final String qtwPerms = oP.qtwHead;
		oP = createPair(oP.ztwTail); // ref count
		if (oP == null) return null;
		oP = createPair(oP.ztwTail); // owning user
		if (oP == null) return null;
		oP = createPair(oP.ztwTail); // owning group
		if (oP == null) return null;
		oP = createPair(oP.ztwTail); // size
		if (oP == null) return null;
		final String qtwSize = oP.qtwHead;
		oP = createPair(oP.ztwTail); // last mod 1
		if (oP == null) return null;
		final String qtwLmA = oP.qtwHead;
		oP = createPair(oP.ztwTail); // last mod 2
		if (oP == null) return null;
		final String qtwLmB = oP.qtwHead;
		oP = createPair(oP.ztwTail); // last mod 3
		if (oP == null) return null;
		final String qtwLmC = oP.qtwHead;
		if (oP.ztwTail.length() == 0) return null;
		final String qtwName = oP.ztwTail;

		final NodeBuilder nb = new NodeBuilder();
		if (directory(nb, qtwPerms)) {
			if (size(nb, qtwSize)) {
				if (lastModified(nb, qtwLmA, qtwLmB, qtwLmC, timeZone, now)) {
					if (name(nb, qtwName)) return nb.newNode();
				}
			}
		}
		return null;
	}

	public static List<Node> zlNodes(String[] zptqtwLines, TimeZone timeZone, TimeFactors now, Comparator<Node> oSort) {
		if (zptqtwLines == null) throw new IllegalArgumentException("object is null");
		if (timeZone == null) throw new IllegalArgumentException("object is null");
		if (now == null) throw new IllegalArgumentException("object is null");
		final int lineCount = zptqtwLines.length;
		final List<Node> zlNodes = new ArrayList<Node>(lineCount);
		for (int i = 0; i < lineCount; i++) {
			final String qtwLine = zptqtwLines[i];
			final Node oNode = createNode(qtwLine, timeZone, now);
			if (oNode != null) {
				zlNodes.add(oNode);
			}
		}
		if (oSort != null) {
			Collections.sort(zlNodes, oSort);
		}
		return zlNodes;
	}

	private static class NodeBuilder {

		public Node newNode() {
			return new Node(isDirectory, qccName, tsLastModified, bcSize);
		}

		NodeBuilder() {
		}
		boolean isDirectory;
		String qccName;
		long tsLastModified;
		long bcSize;
	}

	private static class Pair {

		Pair(String qtwHead, String ztwTail) {
			this.qtwHead = qtwHead;
			this.ztwTail = ztwTail;
		}
		final String qtwHead;
		final String ztwTail;
	}

	public static class Node {

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(isDirectory ? "d" : "f");
			sb.append(" ");
			sb.append(DateFormatter.newT8FromTs(tsLastModified));
			sb.append(" ");
			sb.append(bcSize);
			sb.append("bytes");
			return sb.toString();
		};

		Node(boolean isDirectory, String qccName, long tsLastModified, long bcSize) {
			assert qccName != null && qccName.length() > 0;
			this.isDirectory = isDirectory;
			this.qccName = qccName;
			this.tsLastModified = tsLastModified;
			this.bcSize = bcSize;
		}
		public final boolean isDirectory;
		public final String qccName;
		public final long tsLastModified;
		public final long bcSize;
	}
}
