/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.metservice.argon.ArgonNumber;
import com.metservice.beryllium.BerylliumSupportId;

/**
 * 
 * @author roach
 */
class SourceCallable implements IEsCallable {

	private static LabelScopeStack labelScopeStack(LabelScopeStack lzy) {
		return (lzy == null) ? new LabelScopeStack() : lzy;
	}

	private static ListStack listStack(ListStack lzy) {
		return (lzy == null) ? new ListStack() : lzy;
	}

	private void debug(EsExecutionContext ecx, int pc, int lineIndex, boolean stepHere, IEsOperand oResult, EsRunException orex)
			throws InterruptedException {
		final ShellHook shellHook = ecx.global().shellHook;
		final BerylliumSupportId sid = shellHook.sid;
		final NeonDebugger debugger = shellHook.shell.debugger();
		final String qccSourcePath = m_source.qccPath();
		final DebugState state = new DebugState(this, ecx, shellHook, pc, lineIndex, stepHere, oResult, orex);
		debugger.pushState(sid, qccSourcePath, state);
	}

	public boolean add(VmInstruction instruction) {
		if (instruction instanceof VmDeclareFunction) {
			final VmDeclareFunction vmDeclareFunction = (VmDeclareFunction) instruction;
			return m_zmCallables.put(vmDeclareFunction.qccFunctionName(), vmDeclareFunction.callableCode()) == null;
		}
		m_zlInstructions.add(instruction);
		return true;
	}

	public IEsOperand call(EsExecutionContext ecx)
			throws InterruptedException {
		final ShellHook shellHook = ecx.global().shellHook;
		final boolean isDebugging = shellHook.enabledDebugging;
		final ProfileSample oProfileSample = shellHook.oProfileSample;
		ecx.populateVariableObject(m_zlFormalParameterNames, m_zmCallables, m_zsVariables);
		final OperandStack operandStack = new OperandStack();
		ListStack lzyListStack = null;
		LabelScopeStack lzyLabelScopeStack = null;
		final int instructionCount = m_zlInstructions.size();
		int saveLineIndex = -1;
		long ntLine = -1L;
		int pc = 0;
		while (pc < instructionCount) {
			final VmInstruction instruction = m_zlInstructions.get(pc);
			final int lineIndex = instruction.lineIndex();
			final boolean stepHere = instruction.stepHere();
			if (oProfileSample != null) {
				if (saveLineIndex < 0) {
					ntLine = System.nanoTime();
					saveLineIndex = lineIndex;
				} else if (lineIndex != saveLineIndex) {
					ntLine = oProfileSample.lineDone(ntLine, saveLineIndex);
					saveLineIndex = lineIndex;
				}
			}
			if (isDebugging) {
				debug(ecx, pc, lineIndex, stepHere, null, null);
			}
			try {
				if (instruction instanceof VmStackInstruction) {
					pc = ((VmStackInstruction) instruction).exec(ecx, operandStack, pc);
				} else if (instruction instanceof VmSetCompletion) {
					final CompletionType completionType = ((VmSetCompletion) instruction).completionType();
					switch (completionType) {
						case NORMAL: {
							operandStack.clear();
							pc++;
						}
						break;
						case RETURN: {
							pc = instructionCount;
						}
						break;
						case THROW: {
							final IEsOperand throwValue = operandStack.pop();
							operandStack.push(new EsCompletionThrow(throwValue));
							pc = instructionCount;
						}
						break;
						case CONTINUE: {
							lzyLabelScopeStack = labelScopeStack(lzyLabelScopeStack);
							pc = lzyLabelScopeStack.selectContinue().pc();
						}
						break;
						case BREAK: {
							lzyLabelScopeStack = labelScopeStack(lzyLabelScopeStack);
							pc = lzyLabelScopeStack.selectBreak().pc();
						}
						break;
						default: {
							throw new EsInterpreterException("Unsupported Completion Instruction: " + instruction);
						}
					}
				} else if (instruction instanceof VmLoopInstruction) {
					lzyListStack = listStack(lzyListStack);
					pc = ((VmLoopInstruction) instruction).exec(ecx, operandStack, lzyListStack, pc);
				} else if (instruction instanceof VmCall) {
					final long ntCallStart = oProfileSample == null ? -1L : System.nanoTime();
					final VmCall call = (VmCall) instruction;
					final EsExecutionContext neoExecutionContext = call.newExecutionContext(ecx, operandStack);
					final IEsCallable callable = neoExecutionContext.callee().callable();
					final IEsOperand callResult = callable.call(neoExecutionContext);
					if (isDebugging) {
						debug(ecx, pc, lineIndex, stepHere, callResult, null);
					}
					operandStack.push(callResult);
					if (callResult instanceof EsCompletionThrow) {
						pc = instructionCount;
					} else {
						pc++;
					}
					if (oProfileSample != null) {
						oProfileSample.lineCallDone(ntCallStart, lineIndex, callable);
					}
				} else if (instruction instanceof VmConstruct) {
					final long ntCallStart = oProfileSample == null ? -1L : System.nanoTime();
					final EsExecutionContext neoExecutionContext = ((VmConstruct) instruction).newExecutionContext(ecx,
							operandStack);
					final IEsCallable callable = neoExecutionContext.callee().callable();
					final IEsOperand callResult = callable.call(neoExecutionContext);
					if (callResult instanceof EsCompletionThrow) {
						if (isDebugging) {
							debug(ecx, pc, lineIndex, stepHere, callResult, null);
						}
						operandStack.push(callResult);
						pc = instructionCount;
					} else {
						final IEsOperand constructResult = neoExecutionContext.thisObject();
						if (isDebugging) {
							debug(ecx, pc, lineIndex, stepHere, constructResult, null);
						}
						operandStack.push(constructResult);
						pc++;
					}
					if (oProfileSample != null) {
						oProfileSample.lineCallDone(ntCallStart, lineIndex, callable);
					}
				} else if (instruction instanceof VmLabel) {
					if (instruction instanceof VmAddLabel) {
						final VmAddLabel vmAddLabel = (VmAddLabel) instruction;
						lzyLabelScopeStack = labelScopeStack(lzyLabelScopeStack);
						lzyLabelScopeStack.put(vmAddLabel.qccName(), vmAddLabel.getJump());
						pc++;
					} else if (instruction instanceof VmRemoveLabel) {
						final VmRemoveLabel vmRemoveLabel = (VmRemoveLabel) instruction;
						lzyLabelScopeStack = labelScopeStack(lzyLabelScopeStack);
						lzyLabelScopeStack.remove(vmRemoveLabel.qccName());
						pc++;
					} else if (instruction instanceof VmPushLabelScope) {
						lzyLabelScopeStack = labelScopeStack(lzyLabelScopeStack);
						lzyLabelScopeStack.pushScope();
						pc++;
					} else if (instruction instanceof VmPopLabelScope) {
						lzyLabelScopeStack = labelScopeStack(lzyLabelScopeStack);
						lzyLabelScopeStack.popScope();
						pc++;
					} else {
						final String m = "Unsupported Label Instruction: " + instruction;
						throw new EsInterpreterException(m);
					}
				} else {
					final String m = "Unsupported Instruction: " + instruction;
					throw new EsInterpreterException(m);
				}
			} catch (final EsRunException exES) {
				if (isDebugging) {
					debug(ecx, pc, lineIndex, stepHere, null, exES);
				}
				exES.unwind(m_source, lineIndex, ecx);
				throw exES;
			} catch (final RuntimeException exRT) {
				final EsInterpreterException exIN = new EsInterpreterException(exRT);
				exIN.unwind(m_source, lineIndex, ecx);
				throw exIN;
			}
		}

		if (oProfileSample != null) {
			if (saveLineIndex >= 0) {
				oProfileSample.lineDone(ntLine, saveLineIndex);
			}
		}

		final IEsOperand result = operandStack.isEmpty() ? EsPrimitiveUndefined.Instance : operandStack.pop();
		if (saveLineIndex >= 0) {
			if (isDebugging) {
				debug(ecx, pc, saveLineIndex, true, result, null);
			}
		}

		return result;
	}

	public void declareFormalParameter(String qccIdentifier) {
		if (qccIdentifier == null || qccIdentifier.length() == 0) throw new IllegalArgumentException("qccIdentifier is empty");
		m_zlFormalParameterNames.add(qccIdentifier);
	}

	public boolean declareVariable(String qccIdentifier) {
		if (qccIdentifier == null || qccIdentifier.length() == 0) throw new IllegalArgumentException("qccIdentifier is empty");
		return m_zsVariables.add(qccIdentifier);
	}

	public EsSource getSource() {
		return m_source;
	}

	public boolean isDeclared() {
		return m_isDeclared;
	}

	public boolean isIntrinsic() {
		return false;
	}

	public InstructionAddress nextAddress() {
		return new InstructionAddress(m_zlInstructions.size());
	}

	public String oqccName() {
		return m_oqccName;
	}

	public int requiredArgumentCount() {
		return 0;
	}

	public String show(int depth) {
		final StringBuilder b = new StringBuilder();
		if (m_oqccName != null) {
			b.append("Function: ");
			b.append(m_oqccName);
			b.append("\n");
		}
		b.append("Formal Parameters: ");
		final int formalParameterCount = m_zlFormalParameterNames.size();
		for (int i = 0; i < formalParameterCount; i++) {
			if (i > 0) {
				b.append(',');
			}
			b.append(m_zlFormalParameterNames.get(i));
		}
		b.append("\n");

		b.append("Functions:");
		for (final Map.Entry<String, IEsCallable> entry : m_zmCallables.entrySet()) {
			if (depth > 0) {
				b.append('\n');
				b.append(entry.getValue().show(depth - 1));
			} else {
				b.append(' ');
				b.append(entry.getKey());
			}
		}
		b.append("\n");

		b.append("Variables:");
		for (final String qccVariableName : m_zsVariables) {
			b.append(' ');
			b.append(qccVariableName);
		}
		b.append("\n");

		final int instructionCount = m_zlInstructions.size();
		b.append("Instructions:\n");
		for (int i = 0; i < instructionCount; i++) {
			b.append(ArgonNumber.intToDec4(i));
			b.append(' ');
			b.append(m_zlInstructions.get(i));
			b.append('\n');
		}
		if (m_oqccName != null) {
			b.append("End of ");
			b.append(m_oqccName);
			b.append("\n");
		}
		return b.toString();
	}

	@Override
	public String toString() {
		return show(0);
	}

	public List<String> zlFormalParameterNames() {
		return m_zlFormalParameterNames;
	}

	@Override
	public Map<String, IEsCallable> zmCallables() {
		return m_zmCallables;
	}

	public Set<String> zsVariableNames() {
		return m_zsVariables;
	}

	public SourceCallable(String oqccName, boolean isDeclared, EsSource source) {
		if (source == null) throw new IllegalArgumentException("source is null");
		m_oqccName = oqccName;
		m_isDeclared = isDeclared;
		m_source = source;
	}
	private final List<String> m_zlFormalParameterNames = new ArrayList<String>();
	private final Set<String> m_zsVariables = new HashSet<String>();
	private final Map<String, IEsCallable> m_zmCallables = new HashMap<String, IEsCallable>();
	private final List<VmInstruction> m_zlInstructions = new ArrayList<VmInstruction>();
	private final String m_oqccName;
	private final boolean m_isDeclared;
	private final EsSource m_source;
}
