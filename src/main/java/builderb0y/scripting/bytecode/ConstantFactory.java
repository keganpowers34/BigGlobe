package builderb0y.scripting.bytecode;

import java.lang.StackWalker.Option;
import java.lang.invoke.MethodHandles;

import builderb0y.bigglobe.scripting.ScriptLogger;
import builderb0y.scripting.bytecode.tree.ConstantValue;
import builderb0y.scripting.bytecode.tree.InsnTree;
import builderb0y.scripting.bytecode.tree.InvalidOperandException;
import builderb0y.scripting.environments.MutableScriptEnvironment;
import builderb0y.scripting.environments.MutableScriptEnvironment.CastResult;
import builderb0y.scripting.parsing.ExpressionParser;
import builderb0y.scripting.parsing.ScriptParsingException;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public class ConstantFactory implements MutableScriptEnvironment.FunctionHandler {

	public static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

	public final MethodInfo constantMethod, variableMethod;
	public final TypeInfo type;

	public ConstantFactory(Class<?> owner, String name, Class<?> inType, Class<?> outType) {
		this.constantMethod = MethodInfo.findMethod(owner, name, outType, MethodHandles.Lookup.class, String.class, Class.class, inType);
		this.variableMethod = MethodInfo.findMethod(owner, name, outType, inType);
		this.type           = type(outType);
	}

	/**
	factory method for the most common case,
	where the owner is the caller class, the name is "of",
	inType is String.class, and outType is also the caller class.
	*/
	public static ConstantFactory autoOfString() {
		Class<?> caller = STACK_WALKER.getCallerClass();
		return new ConstantFactory(caller, "of", String.class, caller);
	}

	@Override
	public CastResult create(ExpressionParser parser, String name, InsnTree[] arguments) throws ScriptParsingException {
		if (arguments.length != 1) return null;
		return this.create(parser, arguments[0], false);
	}

	public CastResult create(ExpressionParser parser, InsnTree argument, boolean implicit) {
		if (argument.getTypeInfo().simpleEquals(this.variableMethod.paramTypes[0])) {
			if (argument.getConstantValue().isConstant()) {
				return new CastResult(ldc(this.constantMethod, argument.getConstantValue()), true);
			}
			else {
				if (implicit) ScriptLogger.LOGGER.warn("Non-constant String input for implicit cast to " + this.type + ". This will be worse on performance. Use an explicit cast to suppress this warning. " + ScriptParsingException.appendContext(parser.input));
				return new CastResult(invokeStatic(this.variableMethod, argument), true);
			}
		}
		else if (argument.getTypeInfo().simpleEquals(this.type)) {
			return new CastResult(argument, false);
		}
		else {
			throw new InvalidOperandException("Must be a " + this.variableMethod.paramTypes[0] + " or a " + this.type + "; was " + argument.getTypeInfo());
		}
	}

	public InsnTree create(ConstantValue constant) {
		return ldc(this.constantMethod, constant);
	}
}