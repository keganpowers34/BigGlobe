package builderb0y.scripting.bytecode.tree.flow.loop;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;

import builderb0y.scripting.bytecode.MethodCompileContext;
import builderb0y.scripting.bytecode.ScopeContext.Scope;
import builderb0y.scripting.bytecode.VarInfo;
import builderb0y.scripting.bytecode.tree.InsnTree;
import builderb0y.scripting.bytecode.tree.VariableDeclarationInsnTree;
import builderb0y.scripting.bytecode.tree.conditions.LongCompareConditionTree;
import builderb0y.scripting.bytecode.tree.instructions.binary.AddInsnTree;
import builderb0y.scripting.bytecode.tree.instructions.binary.SubtractInsnTree;
import builderb0y.scripting.util.TypeInfos;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public class ForLongRangeInsnTree extends AbstractForRangeInsnTree {

	public ForLongRangeInsnTree(
		String loopName,
		VariableDeclarationInsnTree variable,
		boolean ascending,
		InsnTree lowerBound,
		boolean lowerBoundInclusive,
		InsnTree upperBound,
		boolean upperBoundInclusive,
		InsnTree step,
		InsnTree body
	) {
		super(
			loopName,
			variable,
			ascending,
			lowerBound,
			lowerBoundInclusive,
			upperBound,
			upperBoundInclusive,
			step,
			body
		);
	}

	@Override
	public void emitBytecode(MethodCompileContext method) {
		LabelNode continuePoint = labelNode();
		Scope mainLoop = method.scopes.pushLoop(this.loopName, continuePoint);

		this.variable.emitBytecode(method);
		InsnTree lowerBound = this.lowerBound, upperBound = this.upperBound;
		if (!lowerBound.getConstantValue().isConstant() || !upperBound.getConstantValue().isConstant()) {
			VarInfo lowerBoundVariable = null, upperBoundVariable = null;
			if (!lowerBound.getConstantValue().isConstant()) {
				lowerBoundVariable = method.newVariable("$lowerBound", TypeInfos.LONG);
			}
			if (!upperBound.getConstantValue().isConstant()) {
				upperBoundVariable = method.newVariable("$upperBound", TypeInfos.LONG);
			}
			method.scopes.pushScope();
			if (lowerBoundVariable != null) {
				store(lowerBoundVariable, lowerBound).emitBytecode(method);
				lowerBound = load(lowerBoundVariable);
			}
			if (upperBoundVariable != null) {
				store(upperBoundVariable, upperBound).emitBytecode(method);
				upperBound = load(upperBoundVariable);
			}
			method.scopes.popScope();
		}
		InsnTree step = this.step;
		if (!step.getConstantValue().isConstant()) {
			VarInfo stepVariable = method.newVariable("$step", TypeInfos.LONG);
			store(stepVariable, step).emitBytecode(method);
			step = load(stepVariable);
		}
		if (this.ascending) {
			store(this.variable.variable, lowerBound).emitBytecode(method);
			if (this.lowerBoundInclusive) {
				Label start = label();
				method.node.visitLabel(start);
				if (this.upperBoundInclusive) {
					LongCompareConditionTree.lessThanOrEqual(load(this.variable.variable), upperBound).emitBytecode(method, null, mainLoop.end.getLabel());
				}
				else {
					LongCompareConditionTree.lessThan(load(this.variable.variable), upperBound).emitBytecode(method, null, mainLoop.end.getLabel());
				}
				this.body.emitBytecode(method);
				method.node.instructions.add(continuePoint);
				store(this.variable.variable, new AddInsnTree(load(this.variable.variable), step, LADD)).emitBytecode(method);
				method.node.visitJumpInsn(GOTO, start);
			}
			else {
				method.node.instructions.add(continuePoint);
				store(this.variable.variable, new AddInsnTree(load(this.variable.variable), step, LADD)).emitBytecode(method);
				if (this.upperBoundInclusive) {
					LongCompareConditionTree.lessThanOrEqual(load(this.variable.variable), upperBound).emitBytecode(method, null, mainLoop.end.getLabel());
				}
				else {
					LongCompareConditionTree.lessThan(load(this.variable.variable), upperBound).emitBytecode(method, null, mainLoop.end.getLabel());
				}
				this.body.emitBytecode(method);
				method.node.visitJumpInsn(GOTO, continuePoint.getLabel());
			}
		}
		else {
			store(this.variable.variable, upperBound).emitBytecode(method);
			if (this.upperBoundInclusive) {
				Label start = label();
				method.node.visitLabel(start);
				if (this.lowerBoundInclusive) {
					LongCompareConditionTree.greaterThanOrEqual(load(this.variable.variable), lowerBound).emitBytecode(method, null, mainLoop.end.getLabel());
				}
				else {
					LongCompareConditionTree.greaterThan(load(this.variable.variable), lowerBound).emitBytecode(method, null, mainLoop.end.getLabel());
				}
				this.body.emitBytecode(method);
				method.node.instructions.add(continuePoint);
				store(this.variable.variable, new SubtractInsnTree(load(this.variable.variable), step, LSUB)).emitBytecode(method);
				method.node.visitJumpInsn(GOTO, start);
			}
			else {
				method.node.instructions.add(continuePoint);
				store(this.variable.variable, new SubtractInsnTree(load(this.variable.variable), step, LSUB)).emitBytecode(method);
				if (this.lowerBoundInclusive) {
					LongCompareConditionTree.greaterThanOrEqual(load(this.variable.variable), lowerBound).emitBytecode(method, null, mainLoop.end.getLabel());
				}
				else {
					LongCompareConditionTree.greaterThan(load(this.variable.variable), lowerBound).emitBytecode(method, null, mainLoop.end.getLabel());
				}
				this.body.emitBytecode(method);
				method.node.visitJumpInsn(GOTO, continuePoint.getLabel());
			}
		}

		method.scopes.popScope();
	}
}