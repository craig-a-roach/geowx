/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonText;
import com.metservice.argon.text.ArgonJoiner;
import com.metservice.argon.text.ArgonNumber;
import com.metservice.beryllium.BerylliumApiException;
import com.metservice.beryllium.BerylliumPath;
import com.metservice.beryllium.BerylliumSupportId;

/**
 * @author roach
 */
class ShellPageDebug extends ShellPageLeaf {

	private static final int CodeRevealMin = 4;

	private static final String FrunToLine = "runToLine";
	private static final String FbreakpointAdd = "breakpointAdd";

	private static final String Qnav = "nav";
	private static final String Qnav_stepNext = "stepNext";
	private static final String Qnav_stepOver = "stepOver";
	private static final String Qnav_stepOut = "stepOut";
	private static final String Qnav_stepToResult = "stepToResult";
	private static final String Qnav_continue = "continue";
	private static final String Qnav_resume = "resume";
	private static final String QcodeReveal = "codeReveal";
	private static final String QcodeReveal_more = "more";
	private static final String QcodeReveal_less = "less";
	private static final String QoperandDepth = "operandDepth";
	private static final String QoperandDepth_more = "more";
	private static final String QoperandDepth_less = "less";
	private static final String QwatchAdd = "watchAdd";
	private static final String QwatchRemove = "watchRemove";
	private static final String QbreakpointRemove = "breakpointRemove";

	private static String qTypeLabel(IEsOperand oValue) {
		if (oValue == null) return "-";
		if (oValue instanceof EsObject) return ((EsObject) oValue).esClass();
		return oValue.esType().toString().substring(1);
	}

	private static IEsOperand resolveOperandValue(DebugState ds, String qccPropertyName) {
		final EsReference reference = ds.ecx.scopeChain().resolve(qccPropertyName);
		final EsObject oBase = reference.getBase();
		return oBase == null ? null : oBase.esGet(qccPropertyName);
	}

	private static IEsOperand rowVar(PrintWriter writer, DebugState ds, String qccPropertyName, int depth) {
		final IEsOperand oValue = resolveOperandValue(ds, qccPropertyName);
		final String qTypeLabel = qTypeLabel(oValue);
		final String varValue;
		if (oValue == null) {
			varValue = "(out of scope)";
		} else {
			varValue = oValue.show(depth);
		}
		row(writer, true, qccPropertyName, qTypeLabel, varValue);
		return oValue;
	}

	private void applyControl(ShellSession sn, ShellGlobal g, Request rq, ShellSessionStateDebug sns)
			throws InterruptedException {

		final String ozRunToLine = rq.getParameter(FrunToLine);
		if (ozRunToLine != null) {
			applyControlRunToLine(sn, g, sns, ozRunToLine);
			return;
		}

		final String ozNav = rq.getParameter(Qnav);
		if (ozNav != null) {
			applyControlNav(sn, g, sns, ozNav);
			return;
		}

		final String ozBreakpointAdd = rq.getParameter(FbreakpointAdd);
		if (ozBreakpointAdd != null) {
			applyControlBreakpoint(sn, g, sns, ozBreakpointAdd, true);
		}

		final String ozBreakpointRemove = rq.getParameter(QbreakpointRemove);
		if (ozBreakpointRemove != null) {
			applyControlBreakpoint(sn, g, sns, ozBreakpointRemove, false);
		}

		final String ozCodeReveal = rq.getParameter(QcodeReveal);
		if (ozCodeReveal != null) {
			applyControlCodeReveal(sn, g, sns, ozCodeReveal);
		}

		final String ozOperandDepth = rq.getParameter(QoperandDepth);
		if (ozOperandDepth != null) {
			applyControlOperandDepth(sn, g, sns, ozOperandDepth);
		}

		for (final WatchMethod wm : WatchMethod.values()) {
			final String ozWatchAdd = rq.getParameter(QwatchAdd + wm);
			if (ozWatchAdd != null && ozWatchAdd.length() > 0) {
				sns.watchAdd(wm, ozWatchAdd);
			}
			final String ozWatchRemove = rq.getParameter(QwatchRemove + wm);
			if (ozWatchRemove != null && ozWatchRemove.length() > 0) {
				sns.watchRemove(wm, ozWatchRemove);
			}
		}
	}

	private void applyControlBreakpoint(ShellSession sn, ShellGlobal g, ShellSessionStateDebug sns, String zLineNo, boolean add) {
		if (zLineNo.length() == 0) {
			sn.sendMessage("Missing breakpoint line number");
			return;
		}
		try {
			final int lineNo = Integer.parseInt(zLineNo);
			if (add) {
				sns.breakpointAdd(lineNo);
			} else {
				sns.breakpointRemove(lineNo);
			}
		} catch (final NumberFormatException ex) {
			sn.sendMessage("Expecting a numeric breakpoint number (" + zLineNo + ")");
		}
	}

	private void applyControlCodeReveal(ShellSession sn, ShellGlobal g, ShellSessionStateDebug sns, String zCodeReveal) {
		assert sns != null;
		assert zCodeReveal != null;
		final int ex = sns.codeReveal();
		int neo = ex;
		if (zCodeReveal.equals(QcodeReveal_less)) {
			neo = Math.min(ex - 1, (ex / 5 * 4));
		} else if (zCodeReveal.equals(QcodeReveal_more)) {
			neo = Math.max(ex + 1, (ex / 4 * 5));
		} else {
			sn.sendMessage("Unknown code reveal command '" + zCodeReveal + "'");
			return;
		}
		final int cneo = Math.max(CodeRevealMin, neo);
		if (cneo != ex) {
			sns.codeReveal(cneo);
		}
	}

	private void applyControlNav(ShellSession sn, ShellGlobal g, ShellSessionStateDebug sns, String zNav)
			throws InterruptedException {
		assert zNav != null;
		final EsBreakpointLines bpl = sns.breakpointLines();
		final DebugCommand oCommand = createNavCommand(bpl, zNav);
		if (oCommand == null) {
			sn.sendMessage("Unknown navigation command '" + zNav + "'");
		} else {
			final BerylliumSupportId sid = sn.idSupport();
			final String qccSourcePath = qccSourcePath();
			g.debugger.apply(sid, qccSourcePath, oCommand);
		}
	}

	private void applyControlOperandDepth(ShellSession sn, ShellGlobal g, ShellSessionStateDebug sns, String zOperandDepth) {
		assert sns != null;
		assert zOperandDepth != null;
		final int ex = sns.operandShowDepth();
		int neo = ex;
		if (zOperandDepth.equals(QoperandDepth_less)) {
			neo = ex - 1;
		} else if (zOperandDepth.equals(QoperandDepth_more)) {
			neo = ex + 1;
		} else {
			sn.sendMessage("Unknown operand depth  command '" + zOperandDepth + "'");
			return;
		}
		final int cneo = Math.max(1, neo);
		if (cneo != ex) {
			sns.operandShowDepth(cneo);
		}
	}

	private void applyControlRunToLine(ShellSession sn, ShellGlobal g, ShellSessionStateDebug sns, String zLineNo)
			throws InterruptedException {
		if (zLineNo.length() == 0) {
			sn.sendMessage("Missing run-to line number");
			return;
		}
		try {
			final int lineNo = Integer.parseInt(zLineNo);
			final BerylliumSupportId sid = sn.idSupport();
			final String qccSourcePath = qccSourcePath();
			final EsBreakpointLines breakpointLines = sns.breakpointLines();
			g.debugger.apply(sid, qccSourcePath, new DebugCommandRunToLine(breakpointLines, lineNo));
		} catch (final NumberFormatException ex) {
			sn.sendMessage("Expecting a numeric run-to line number (" + zLineNo + ")");
		}
	}

	private DebugCommand createNavCommand(EsBreakpointLines bpl, String zNav) {
		if (zNav.equals(Qnav_stepNext)) return new DebugCommandStep(bpl, DebugCommandStep.Sense.Next);
		if (zNav.equals(Qnav_stepOver)) return new DebugCommandStep(bpl, DebugCommandStep.Sense.Over);
		if (zNav.equals(Qnav_stepOut)) return new DebugCommandStep(bpl, DebugCommandStep.Sense.Out);
		if (zNav.equals(Qnav_stepToResult)) return new DebugCommandStep(bpl, DebugCommandStep.Sense.Completion);
		if (zNav.equals(Qnav_continue)) return new DebugCommandStep(bpl, DebugCommandStep.Sense.Continue);
		if (zNav.equals(Qnav_resume)) return new DebugCommandResume();
		return null;
	}

	private void renderBreakpointRemoval(PrintWriter writer, ShellSessionStateDebug sns) {
		final EsBreakpointLines bpl = sns.breakpointLines();
		final int[] zptLineIndexAsc = bpl.zptLineIndexAsc();
		final int bpCount = zptLineIndexAsc.length;
		if (bpCount == 0) return;
		fieldsetStart(writer, "Remove Breakpoints...");
		for (int i = 0; i < bpCount; i++) {
			final int lineNo = zptLineIndexAsc[i] + 1;
			if (i > 0) {
				text(writer, ", ");
			}
			final String qLineNo = ArgonNumber.intToDec3(lineNo);
			link(writer, m_path.qtwPathQuery(QbreakpointRemove, qLineNo), qLineNo);
		}
		fieldsetEnd(writer);
	}

	private void renderControlMenu(PrintWriter writer, ShellSessionStateDebug sns, DebugState ds) {
		assert sns != null;
		assert ds != null;
		fieldsetStart(writer, "Run Control");
		tableBodyStart(writer, null);
		rowStart(writer);
		tdStart(writer);
		renderControlMenuStep(writer, ds);
		tdEnd(writer);
		tdStart(writer);
		renderControlMenuRunTo(writer);
		renderControlMenuBreakpointAdd(writer);
		tdEnd(writer);
		tdStart(writer);
		renderControlMenuCodeReveal(writer, sns, ds);
		tdEnd(writer);
		tdStart(writer);
		renderControlMenuOperandDepth(writer, sns);
		tdEnd(writer);
		rowEnd(writer);
		tableBodyEnd(writer);
		fieldsetEnd(writer);
	}

	private void renderControlMenuBreakpointAdd(PrintWriter writer) {
		formStart(writer, m_path, true, false);
		fieldsetStart(writer, "Breakpoints...");
		text(writer, "Line No.");
		inputText(writer, FbreakpointAdd, "", 6);
		buttonSubmit(writer, "Add");
		fieldsetEnd(writer);
		formEnd(writer);
	}

	private void renderControlMenuCodeReveal(PrintWriter writer, ShellSessionStateDebug sns, DebugState ds) {
		final int ex = sns.codeReveal();
		final int lineCount = ds.source.lineCount();
		final boolean more = ex < lineCount;
		final boolean less = ex > CodeRevealMin;
		if (more || less) {
			final String oHrefMore = more ? m_path.qtwPathQuery(QcodeReveal, QcodeReveal_more) : null;
			final String oHrefLess = less ? m_path.qtwPathQuery(QcodeReveal, QcodeReveal_less) : null;
			fieldsetStart(writer, "Code View");
			navlist(writer, oHrefMore, "More", oHrefLess, "Less");
			if (ex < lineCount) {
				para(writer, "Showing: " + ex + " of " + lineCount);
			} else {
				para(writer, "Showing all lines");
			}
			fieldsetEnd(writer);
		}
	}

	private void renderControlMenuOperandDepth(PrintWriter writer, ShellSessionStateDebug sns) {
		final int ex = sns.operandShowDepth();
		final boolean less = ex > 1;
		final String hrefMore = m_path.qtwPathQuery(QoperandDepth, QoperandDepth_more);
		final String oHrefLess = less ? m_path.qtwPathQuery(QoperandDepth, QoperandDepth_less) : null;
		fieldsetStart(writer, "Object Depth");
		navlist(writer, hrefMore, "More", oHrefLess, "Less");
		para(writer, "Currently: " + ex);
		fieldsetEnd(writer);
	}

	private void renderControlMenuRunTo(PrintWriter writer) {
		formStart(writer, m_path, true, false);
		fieldsetStart(writer, "Run To Line...");
		text(writer, "Line No.");
		inputText(writer, FrunToLine, "", 6);
		buttonSubmit(writer, "Run");
		fieldsetEnd(writer);
		formEnd(writer);
	}

	private void renderControlMenuStep(PrintWriter writer, DebugState ds) {
		final int depth = ds.ecx.depth();
		final String hrefStepNext = m_path.qtwPathQuery(Qnav, Qnav_stepNext);
		final String hrefStepOver = m_path.qtwPathQuery(Qnav, Qnav_stepOver);
		final String oHrefStepToResult = depth > 0 ? m_path.qtwPathQuery(Qnav, Qnav_stepToResult) : null;
		final String oHrefStepOut = depth > 0 ? m_path.qtwPathQuery(Qnav, Qnav_stepOut) : null;
		final String hrefContinue = m_path.qtwPathQuery(Qnav, Qnav_continue);
		final String hrefResume = m_path.qtwPathQuery(Qnav, Qnav_resume);
		fieldsetStart(writer, "Step...");
		navlist(writer, hrefStepNext, "Next Line", hrefStepOver, "Over Function", oHrefStepToResult, "To Return", oHrefStepOut,
				"Out Of Function", hrefContinue, "Next Breakpoint", hrefResume, "Resume");
		fieldsetEnd(writer);
	}

	private void renderDebugState(PrintWriter writer, ShellSessionStateDebug sns, DebugState ds) {
		assert sns != null;
		assert ds != null;

		renderDebugStateException(writer, sns, ds);
		renderDebugStateArguments(writer, sns, ds);
		renderDebugStateResult(writer, sns, ds);
		renderDebugStateThis(writer, sns, ds);
		renderDebugStateLocals(writer, sns, ds);
	}

	private void renderDebugStateArguments(PrintWriter writer, ShellSessionStateDebug sns, DebugState ds) {
		final List<String> zlFormalParameterNames = ds.callable.zlFormalParameterNames();
		final int fpnCount = zlFormalParameterNames.size();
		if (fpnCount == 0) return;
		final int depth = sns.operandShowDepth();
		fieldsetStart(writer, "Arguments");
		tableBodyStart(writer, null);
		for (int i = 0; i < fpnCount; i++) {
			final String varName = zlFormalParameterNames.get(i);
			rowVar(writer, ds, varName, depth);
		}
		tableBodyEnd(writer);
		fieldsetEnd(writer);
	}

	private void renderDebugStateException(PrintWriter writer, ShellSessionStateDebug sns, DebugState ds) {
		if (ds.oRunException == null) return;

		fieldsetStart(writer, "Runtime Error");
		codeBlock(writer, ds.oRunException.getMessage());
		fieldsetEnd(writer);
	}

	private void renderDebugStateLocals(PrintWriter writer, ShellSessionStateDebug sns, DebugState ds) {
		assert sns != null;
		assert ds != null;

		final Set<String> zsVariableNames = ds.callable.zsVariableNames();
		final int varCount = zsVariableNames.size();
		if (varCount == 0) return;
		final List<String> xlVariableNames = new ArrayList<String>(zsVariableNames);
		Collections.sort(xlVariableNames);

		final Set<String> zsCandidateWatches = new HashSet<String>();

		final int depth = sns.operandShowDepth();
		fieldsetStart(writer, "Local Variables");
		tableBodyStart(writer, null);
		for (int i = 0; i < varCount; i++) {
			final String qccVariableName = xlVariableNames.get(i);
			final IEsOperand oValue = rowVar(writer, ds, qccVariableName, depth);
			boolean watch = false;
			if (oValue instanceof EsIntrinsicObject) {
				watch = true;
			} else if (oValue instanceof EsIntrinsicArray) {
				watch = true;
			}
			if (watch) {
				zsCandidateWatches.add(qccVariableName);
			}
		}
		tableBodyEnd(writer);
		fieldsetEnd(writer);

		if (zsCandidateWatches.isEmpty()) return;
		renderWatchAvailable(writer, sns, WatchMethod.toString, zsCandidateWatches);
		renderWatchAvailable(writer, sns, WatchMethod.Xml, zsCandidateWatches);
	}

	private void renderDebugStateResult(PrintWriter writer, ShellSessionStateDebug sns, DebugState ds) {
		assert sns != null;
		assert ds != null;

		if (ds.oResult == null) return;

		final int depth = sns.operandShowDepth();
		final String qCompletion;
		final IEsOperand value;
		if (ds.oResult instanceof EsCompletionThrow) {
			qCompletion = "throw";
			value = ((EsCompletionThrow) ds.oResult).value();
		} else {
			qCompletion = "return";
			value = ds.oResult;
		}
		final String qTypeLabel = qTypeLabel(value);
		fieldsetStart(writer, qCompletion + " " + qTypeLabel);
		codeBlock(writer, value.show(depth));
		fieldsetEnd(writer);
	}

	private void renderDebugStateThis(PrintWriter writer, ShellSessionStateDebug sns, DebugState ds) {
		assert sns != null;
		assert ds != null;

		final EsObject thisObject = ds.ecx.thisObject();
		if (thisObject == ds.ecx.global()) return;

		final int depth = sns.operandShowDepth();
		final String qTypeLabel = qTypeLabel(thisObject);
		fieldsetStart(writer, "this " + qTypeLabel);
		codeBlock(writer, thisObject.show(depth));
		fieldsetEnd(writer);
	}

	private void renderSourceView(PrintWriter writer, ShellSessionStateDebug sns, DebugState ds) {
		assert sns != null;
		assert ds != null;

		final EsSourceHtml oHtml = ds.oHtml;
		if (oHtml == null) {
			codeBlock(writer, ds.lineHere());
		} else {
			final int codeReveal = sns.codeReveal();
			final EsBreakpointLines breakpointLines = sns.breakpointLines();
			oHtml.writeListingDebug(writer, ds.lineIndex, codeReveal, breakpointLines);
		}
	}

	private void renderStack(PrintWriter writer, ShellSessionStateDebug sns, DebugState ds) {
		fieldsetStart(writer, "Stack");
		final List<String> zl = new ArrayList<String>();
		EsExecutionContext oecx = ds.ecx;
		while (oecx != null && oecx.depth() > 0) {
			final EsFunction callee = oecx.callee();
			final String oqccCallableName = callee.callable().oqccName();
			zl.add(oqccCallableName == null ? "()" : oqccCallableName);
			oecx = oecx.getCalling();
		}
		zl.add("main");
		codeBlock(writer, ArgonJoiner.zComma(zl));
		fieldsetEnd(writer);
	}

	private void renderWatchAvailable(PrintWriter writer, ShellSessionStateDebug sns, WatchMethod wm, final Set<String> zsIn) {
		final Set<String> zsAvailable = sns.watchSubtract(wm, zsIn);
		final int availableCount = zsAvailable.size();
		if (availableCount == 0) return;
		final List<String> xlAvailableNames = new ArrayList<String>(zsAvailable);
		Collections.sort(xlAvailableNames);
		fieldsetStart(writer, "Watch " + wm);
		for (int i = 0; i < availableCount; i++) {
			final String qccVariableName = xlAvailableNames.get(i);
			if (i > 0) {
				text(writer, ", ");
			}
			link(writer, m_path.qtwPathQuery(QwatchAdd + wm, qccVariableName), qccVariableName);
		}
		fieldsetEnd(writer);
	}

	private void renderWatchValues(PrintWriter writer, ShellSessionStateDebug sns, WatchMethod wm, DebugState ds)
			throws InterruptedException {
		assert sns != null;
		assert wm != null;
		assert ds != null;

		final List<String> zlWatchAsc = sns.zlWatchAsc(wm);
		for (final String qccPropertyName : zlWatchAsc) {
			fieldsetStart(writer, qccPropertyName + " " + wm);
			final IEsOperand oValue = resolveOperandValue(ds, qccPropertyName);
			try {
				if (oValue == null) {
					para(writer, "out of scope");
				} else if (wm == WatchMethod.Xml && (oValue instanceof EsObject)) {
					final UNeonXmlEncode.Args args = UNeonXmlEncode.newArgs(qccPropertyName);
					args.oRoot = (EsObject) oValue;
					args.validationMethod = UNeonXmlEncode.ValidationMethod.None;
					args.defaultForm = UNeonXmlEncode.DefaultForm.ElementText;
					args.charset = ArgonText.UTF8;
					args.indent = 2;
					codeBlock(writer, UNeonXmlEncode.encode(ds.ecx, args));
				} else {
					codeBlock(writer, oValue.toCanonicalString(ds.ecx));
				}
			} catch (final Exception ex) {
				paraAttention(writer, "Evaluation Failed");
				codeBlock(writer, ex.getMessage());
			}
			link(writer, m_path.qtwPathQuery(QwatchRemove + wm, qccPropertyName), "Remove");
			fieldsetEnd(writer);
		}
	}

	@Override
	public String qTitle() {
		return "Debug /" + qccSourcePath();
	}

	@Override
	public void render(ShellSession sn, ShellGlobal g, Request rq, HttpServletResponse rp)
			throws BerylliumApiException, IOException, ServletException, InterruptedException {
		final BerylliumSupportId sid = sn.idSupport();
		final PrintWriter writer = rp.getWriter();
		final String qccSourcePath = qccSourcePath();
		final ShellSessionStateDebug sns = sn.stateDebug(qccSourcePath);
		applyControl(sn, g, rq, sns);
		menubar(writer, indexPath(), "Index", controlPath(), "Control", sourceJsPath(), "Source", editJsPath(), "Edit",
				helpPath(), "Help");
		final DebugState ods = g.debugger.getState(sid, qccSourcePath);
		if (ods == null) {
			final boolean isEnabled = g.debugger.isEnabled(sid, qccSourcePath);
			if (isEnabled) {
				paraAttention(writer, "Script not running");
				navlistStart(writer);
				navlist(writer, m_path, "Retry");
				navlist(writer, indexPath(), "Cancel");
				navlistEnd(writer);
			} else {
				paraAttention(writer, "Debugging is not enabled");
			}
		} else {
			contentStart(writer);
			renderControlMenu(writer, sns, ods);
			renderStack(writer, sns, ods);
			renderSourceView(writer, sns, ods);
			renderBreakpointRemoval(writer, sns);
			renderWatchValues(writer, sns, WatchMethod.toString, ods);
			renderWatchValues(writer, sns, WatchMethod.Xml, ods);
			contentEnd(writer);
			contentRightStart(writer);
			renderDebugState(writer, sns, ods);
			contentRightEnd(writer);
		}
	}

	public ShellPageDebug(BerylliumPath path) {
		super(path);
	}
}
