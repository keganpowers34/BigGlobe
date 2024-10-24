package builderb0y.bigglobe.scripting.environments;

import com.google.common.collect.ObjectArrays;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

import builderb0y.bigglobe.noise.Permuter;
import builderb0y.scripting.bytecode.TypeInfo.Sort;
import builderb0y.scripting.bytecode.tree.InsnTree;
import builderb0y.scripting.bytecode.tree.conditions.ConditionTree;
import builderb0y.scripting.environments.BuiltinScriptEnvironment;
import builderb0y.scripting.environments.MutableScriptEnvironment;
import builderb0y.scripting.environments.MutableScriptEnvironment.CastResult;
import builderb0y.scripting.environments.MutableScriptEnvironment.MemberKeywordHandler;
import builderb0y.scripting.environments.MutableScriptEnvironment.MethodHandler;
import builderb0y.scripting.environments.ScriptEnvironment.GetMethodMode;
import builderb0y.scripting.environments.ScriptEnvironment.MemberKeywordMode;
import builderb0y.scripting.parsing.ExpressionParser;
import builderb0y.scripting.parsing.ScriptParsingException;
import builderb0y.scripting.util.TypeInfos;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public class StatelessRandomScriptEnvironment {

	public static final MutableScriptEnvironment INSTANCE = (
		new MutableScriptEnvironment()
		.addMethod(type(long.class), "newSeed", new MethodHandler.Named("long.newSeed(int...)", (parser, receiver, name, mode, arguments) -> {
			//primitive long will never be null, so I don't need to check the mode here.
			if (arguments.length == 0) {
				return new CastResult(add(parser, receiver, ldc(Permuter.PHI64)), false);
			}
			return RandomScriptEnvironment.createSeed(parser, ObjectArrays.concat(receiver, arguments));
		}))
		.addMethodInvokeStatic("nextInt", RandomScriptEnvironment.PERMUTER_INFO.nextPositiveInt)
		.addMethodInvokeStatic("nextInt", RandomScriptEnvironment.PERMUTER_INFO.nextIntBound)
		.addMethodInvokeStatic("nextInt", RandomScriptEnvironment.PERMUTER_INFO.nextIntOriginBound)

		.addMethodInvokeStatic("nextLong", RandomScriptEnvironment.PERMUTER_INFO.nextPositiveLong)
		.addMethodInvokeStatic("nextLong", RandomScriptEnvironment.PERMUTER_INFO.nextLongBound)
		.addMethodInvokeStatic("nextLong", RandomScriptEnvironment.PERMUTER_INFO.nextLongOriginBound)

		.addMethodInvokeStatic("nextFloat", RandomScriptEnvironment.PERMUTER_INFO.nextPositiveFloat)
		.addMethodInvokeStatic("nextFloat", RandomScriptEnvironment.PERMUTER_INFO.nextFloatBound)
		.addMethodInvokeStatic("nextFloat", RandomScriptEnvironment.PERMUTER_INFO.nextFloatOriginBound)

		.addMethodInvokeStatic("nextDouble", RandomScriptEnvironment.PERMUTER_INFO.nextPositiveDouble)
		.addMethodInvokeStatic("nextDouble", RandomScriptEnvironment.PERMUTER_INFO.nextDoubleBound)
		.addMethodInvokeStatic("nextDouble", RandomScriptEnvironment.PERMUTER_INFO.nextDoubleOriginBound)

		.addMethodInvokeStatic("nextBoolean", RandomScriptEnvironment.PERMUTER_INFO.nextBoolean)
		.addMethodInvokeStatic("nextBoolean", RandomScriptEnvironment.PERMUTER_INFO.nextChancedBooleanF)
		.addMethodInvokeStatic("nextBoolean", RandomScriptEnvironment.PERMUTER_INFO.nextChancedBooleanD)

		.addMethodInvokeStatic("roundInt", RandomScriptEnvironment.PERMUTER_INFO.roundRandomlyIF)
		.addMethodInvokeStatic("roundInt", RandomScriptEnvironment.PERMUTER_INFO.roundRandomlyID)
		.addMethodInvokeStatic("roundLong", RandomScriptEnvironment.PERMUTER_INFO.roundRandomlyLF)
		.addMethodInvokeStatic("roundLong", RandomScriptEnvironment.PERMUTER_INFO.roundRandomlyLD)

		.addMemberKeyword(TypeInfos.LONG, "if", new MemberKeywordHandler.Named("seed.if (chance: body)", (ExpressionParser parser, InsnTree receiver, String name, MemberKeywordMode mode) -> {
			return wrapSeedIf(parser, receiver, false, mode);
		}))
		.addMemberKeyword(TypeInfos.LONG, "unless", new MemberKeywordHandler.Named("seed.unless (chance: body)", (ExpressionParser parser, InsnTree receiver, String name, MemberKeywordMode mode) -> {
			return wrapSeedIf(parser, receiver, true, mode);
		}))
		.addMethod(TypeInfos.LONG, "switch", new MethodHandler.Named("seed.switch (choices...)", (ExpressionParser parser, InsnTree receiver, String name, GetMethodMode mode, InsnTree... arguments) -> {
			if (arguments.length < 2) {
				throw new ScriptParsingException("switch() requires at least 2 arguments", parser.input);
			}
			Int2ObjectSortedMap<InsnTree> cases = new Int2ObjectAVLTreeMap<>();
			for (int index = 0, length = arguments.length; index < length; index++) {
				cases.put(index, arguments[index]);
			}
			cases.defaultReturnValue(
				throw_(
					newInstance(
						RandomScriptEnvironment.ASSERT_FAIL,
						ldc("Random returned value out of range")
					)
				)
			);
			return new CastResult(
				(
					switch (mode) {
						case NORMAL -> MemberKeywordMode.NORMAL;
						case NULLABLE -> MemberKeywordMode.NULLABLE;
						case RECEIVER -> MemberKeywordMode.RECEIVER;
						case NULLABLE_RECEIVER -> MemberKeywordMode.NULLABLE_RECEIVER;
					}
				)
				.apply(receiver, (InsnTree actualReceiver) -> {
					return switch_(
						parser,
						invokeStatic(
							RandomScriptEnvironment.PERMUTER_INFO.nextIntBound,
							actualReceiver,
							ldc(arguments.length)
						),
						cases
					);
				}),
				false
			);
		})
	));

	public static InsnTree wrapSeedIf(ExpressionParser parser, InsnTree seed, boolean negate, MemberKeywordMode mode) throws ScriptParsingException {
		return mode.apply(seed, (InsnTree actualSeed) -> seedIf(parser, actualSeed, negate));
	}

	public static InsnTree seedIf(ExpressionParser parser, InsnTree seed, boolean negate) throws ScriptParsingException {
		parser.beginCodeBlock();
		InsnTree conditionInsnTree, body;
		InsnTree firstPart = parser.nextScript();
		if (parser.input.hasOperatorAfterWhitespace(":")) { //seed.if (a: b)
			Sort sort = firstPart.getTypeInfo().getSort();
			if (sort != Sort.FLOAT && sort != Sort.DOUBLE) {
				throw new ScriptParsingException("seed." + (negate ? "unless" : "if") + "() chance should be float or double, but was " + firstPart.getTypeInfo(), parser.input);
			}
			body = parser.nextScript();
			conditionInsnTree = (
				sort == Sort.FLOAT
				? RandomScriptEnvironment.PERMUTER_INFO.nextChancedBooleanF(seed, firstPart)
				: RandomScriptEnvironment.PERMUTER_INFO.nextChancedBooleanD(seed, firstPart)
			);
		}
		else { //seed.if (a)
			conditionInsnTree = RandomScriptEnvironment.PERMUTER_INFO.nextBoolean(seed);
			body = firstPart;
		}
		parser.endCodeBlock();
		ConditionTree conditionTree = condition(parser, conditionInsnTree);
		if (negate) conditionTree = not(conditionTree);

		if (parser.input.hasIdentifierAfterWhitespace("else")) {
			return ifElse(parser, conditionTree, body, BuiltinScriptEnvironment.tryParenthesized(parser));
		}
		else {
			return ifThen(conditionTree, body);
		}
	}
}