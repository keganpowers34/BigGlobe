package builderb0y.bigglobe.columns.scripted.entries;

import builderb0y.bigglobe.columns.scripted.schemas.AccessSchema.TypeContext;
import builderb0y.bigglobe.columns.scripted.compile.DataCompileContext;
import builderb0y.bigglobe.columns.scripted.MappedRangeNumberArray;
import builderb0y.bigglobe.columns.scripted.Valids._3DValid;
import builderb0y.bigglobe.noise.NumberArray;
import builderb0y.scripting.bytecode.FieldCompileContext;
import builderb0y.scripting.bytecode.LazyVarInfo;
import builderb0y.scripting.bytecode.MethodCompileContext;
import builderb0y.scripting.bytecode.tree.ConstantValue;
import builderb0y.scripting.bytecode.tree.InsnTree;
import builderb0y.scripting.bytecode.tree.conditions.BooleanToConditionTree;
import builderb0y.scripting.bytecode.tree.conditions.ConditionTree;
import builderb0y.scripting.bytecode.tree.conditions.ConstantConditionTree;
import builderb0y.scripting.bytecode.tree.conditions.IntCompareConditionTree;
import builderb0y.scripting.bytecode.tree.flow.IfElseInsnTree;
import builderb0y.scripting.bytecode.tree.instructions.fields.PutFieldInsnTree;
import builderb0y.scripting.environments.MutableScriptEnvironment;
import builderb0y.scripting.parsing.ScriptParsingException;
import builderb0y.scripting.util.TypeInfos;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public abstract class Basic3DColumnEntry implements ColumnEntry {

	public static final ColumnEntryMemory.Key<MethodCompileContext>
		COMPUTE_ONE = new ColumnEntryMemory.Key<>("computeOne"),
		COMPUTE_ALL = new ColumnEntryMemory.Key<>("computeAll"),
		EXTRACT     = new ColumnEntryMemory.Key<>("extract"),
		VALID_MIN_Y = new ColumnEntryMemory.Key<>("validMinY"),
		VALID_MAX_Y = new ColumnEntryMemory.Key<>("validMaxY");

	public abstract _3DValid valid();

	@Override
	public void populateField(ColumnEntryMemory memory, DataCompileContext context, FieldCompileContext getterMethod) {
		ColumnEntry.super.populateField(memory, context, getterMethod);
		new PutFieldInsnTree(
			context.loadSelf(),
			memory.getTyped(ColumnEntryMemory.FIELD).info,
			newInstance(
				MappedRangeNumberArray.CONSTRUCT,
				getStatic(
					ACC_PUBLIC | ACC_STATIC | ACC_FINAL,
					type(NumberArray.class),
					"EMPTY_" + memory.getTyped(ColumnEntryMemory.TYPE).exposedType().getSort().name(),
					type(NumberArray.class)
				)
			)
		)
		.emitBytecode(context.constructor);
	}

	@Override
	public void populateGetter(ColumnEntryMemory memory, DataCompileContext context, MethodCompileContext getterMethod) {
		_3DValid valid = this.valid();
		TypeContext type = memory.getTyped(ColumnEntryMemory.TYPE);
		String internalName = memory.getTyped(ColumnEntryMemory.INTERNAL_NAME);
		if (this.hasField()) {
			FieldCompileContext valueField = memory.getTyped(ColumnEntryMemory.FIELD);
			int flagIndex = memory.getTyped(ColumnEntryMemory.FLAGS_INDEX);
			MethodCompileContext computeAllMethod = context.mainClass.newMethod(ACC_PUBLIC, "compute_" + internalName, TypeInfos.VOID);
			MethodCompileContext actuallyComputeAll = context.mainClass.newMethod(ACC_PUBLIC, "actually_compute_" + internalName, TypeInfos.VOID);
			memory.putTyped(COMPUTE_ALL, actuallyComputeAll);
			MethodCompileContext extractMethod = context.mainClass.newMethod(ACC_PUBLIC, "extract_" + internalName, type.exposedType(), new LazyVarInfo("y", TypeInfos.INT));
			memory.putTyped(EXTRACT, extractMethod);
			MethodCompileContext computeOneMethod = context.mainClass.newMethod(ACC_PUBLIC, "actually_compute_" + internalName, type.exposedType(), new LazyVarInfo("y", TypeInfos.INT));
			memory.putTyped(COMPUTE_ONE, computeOneMethod);

			getterMethod.setCode(
				"""
				int oldFlags = flagsField
				int newFlags = oldFlags | flagsBitmask
				if (oldFlags != newFlags:
					flagsField = newFlags
					compute()
				)
				return(extract(y))
				""",
				new MutableScriptEnvironment()
				.addVariableRenamedGetField(context.loadSelf(), "flagsField", context.flagsField(flagIndex))
				.addVariableConstant("flagsBitmask", DataCompileContext.flagsFieldBitmask(flagIndex))
				.addFunctionInvoke("compute", context.loadSelf(), computeAllMethod.info)
				.addFunctionInvoke("extract", context.loadSelf(), extractMethod.info)
				.addVariableLoad("y", TypeInfos.INT)
			);
			String source;
			MutableScriptEnvironment environment = (
				new MutableScriptEnvironment()
				.addVariableRenamedGetField(context.loadSelf(), "valueField", valueField.info)
				.addMethodInvokes(MappedRangeNumberArray.class, "reallocateNone", "reallocateMin", "reallocateMax", "reallocateBoth", "invalidate")
				.addVariable("this", context.loadSelf())
				.addVariable("column", context.loadColumn())
				.addFunctionInvoke("actuallyCompute", context.loadSelf(), actuallyComputeAll.info)
			);
			if (valid != null) {
				if (valid.where() != null) {
					MethodCompileContext test = context.mainClass.newMethod(ACC_PUBLIC, "test_" + internalName, TypeInfos.BOOLEAN);
					memory.putTyped(ColumnEntryMemory.VALID_WHERE, test);
					environment.addFunctionInvoke("test", context.loadSelf(), test.info);
				}
				if (valid.min_y() != null) {
					MethodCompileContext minY = context.mainClass.newMethod(ACC_PUBLIC, "minY_" + internalName, TypeInfos.INT);
					memory.putTyped(VALID_MIN_Y, minY);
					environment.addFunctionInvoke("minY", context.loadSelf(), minY.info);
				}
				if (valid.max_y() != null) {
					MethodCompileContext maxY = context.mainClass.newMethod(ACC_PUBLIC, "maxY_" + internalName, TypeInfos.INT);
					memory.putTyped(VALID_MAX_Y, maxY);
					environment.addFunctionInvoke("maxY", context.loadSelf(), maxY.info);
				}
				if (valid.where() != null) {
					if (valid.min_y() != null) {
						if (valid.max_y() != null) {
							source = """
								if (test():
									valueField.reallocateBoth(column, minY(), maxY())
									actuallyCompute()
								)
								else (
									valueField.invalidate()
								)
							""";
						}
						else {
							source = """
								if (test():
									valueField.reallocateMin(column, minY())
									actuallyCompute()
								)
								else (
									valueField.invalidate()
								)
							""";
						}
					}
					else {
						if (valid.max_y() != null) {
							source = """
								if (test():
									valueField.reallocateMax(column, maxY())
									actuallyCompute()
								)
								else (
									valueField.invalidate()
								)
							""";
						}
						else {
							source = """
								if (test():
									valueField.reallocateNone(column, )
									actuallyCompute()
								)
								else (
									valueField.invalidate()
								)
							""";
						}
					}
				}
				else {
					if (valid.min_y() != null) {
						if (valid.max_y() != null) {
							source = """
								valueField.reallocateBoth(column, minY(), maxY())
								actuallyCompute()
							""";
						}
						else {
							source = """
								valueField.reallocateMin(column, minY())
								actuallyCompute()
							""";
						}
					}
					else {
						if (valid.max_y() != null) {
							source = """
								valueField.reallocateMax(column, maxY())
								actuallyCompute()
							""";
						}
						else {
							source = """
								valueField.reallocateNone(column)
								actuallyCompute()
							""";
						}
					}
				}
			}
			else {
				source = """
					valueField.reallocateNone(column)
					actuallyCompute()
				""";
			}
			computeAllMethod.setCode(source, environment);
			extractMethod.setCode(
				"""
				var array = arrayField
				unless (array.valid: return(fallback))
				if (y >= array.minCached && y < array.maxCached:
					return(array.array.get(y - array.minCached))
				)
				"""
				+ (
					valid != null
					? (
						valid.min_y() != null
						? (
							valid.max_y() != null
							? "if (y >= array.minAccessible && y < array.maxAccessible: return(compute(y))\nreturn(fallback)"
							: "if (y >= array.minAccessible: return(compute(y))\nreturn(fallback)"
						)
						: (
							valid.max_y() != null
							? "if (y < array.maxAccessible: return(compute(y)))\nreturn(fallback)"
							: "return(compute(y))"
						)
					)
					: "return(compute(y))"
				),
				new MutableScriptEnvironment()
				.addVariableLoad("y", TypeInfos.INT)
				.addVariable("arrayField", getField(context.loadSelf(), valueField.info))
				.addFieldGet("valid", MappedRangeNumberArray.VALID)
				.addFieldGet("minCached", MappedRangeNumberArray.MIN_CACHED)
				.addFieldGet("maxCached", MappedRangeNumberArray.MAX_CACHED)
				.addFieldGet("minAccessible", MappedRangeNumberArray.MIN_ACCESSIBLE)
				.addFieldGet("maxAccessible", MappedRangeNumberArray.MAX_ACCESSIBLE)
				.addFieldGet("array", MappedRangeNumberArray.ARRAY)
				.addMethodInvoke("get", switch (type.exposedType().getSort()) {
					case BYTE    -> MappedRangeNumberArray.GET_B;
					case SHORT   -> MappedRangeNumberArray.GET_S;
					case INT     -> MappedRangeNumberArray.GET_I;
					case LONG    -> MappedRangeNumberArray.GET_L;
					case FLOAT   -> MappedRangeNumberArray.GET_F;
					case DOUBLE  -> MappedRangeNumberArray.GET_D;
					case BOOLEAN -> MappedRangeNumberArray.GET_Z;
					default -> throw new IllegalStateException("Unsupported type: " + type);
				})
				.addVariableConstant("fallback", valid != null ? valid.getFallback(type.exposedType()) : ConstantValue.of(0))
				.addFunctionInvoke("compute", context.loadSelf(), computeOneMethod.info)
			);

			this.populateComputeAll(memory, context, actuallyComputeAll);
		}
		else {
			if (valid != null) {
				ConditionTree condition = ConstantConditionTree.TRUE;
				InsnTree y = load("y", TypeInfos.INT);
				if (valid.where() != null) {
					MethodCompileContext test = context.mainClass.newMethod(ACC_PUBLIC, "test_" + internalName, TypeInfos.BOOLEAN);
					memory.putTyped(ColumnEntryMemory.VALID_WHERE, test);
					condition = and(condition, new BooleanToConditionTree(invokeInstance(context.loadSelf(), test.info)));
				}
				if (valid.min_y() != null) {
					MethodCompileContext minY = context.mainClass.newMethod(ACC_PUBLIC, "minY_" + internalName, TypeInfos.INT);
					memory.putTyped(VALID_MIN_Y, minY);
					condition = and(condition, IntCompareConditionTree.greaterThanOrEqual(y, invokeInstance(context.loadSelf(), minY.info)));
				}
				if (valid.max_y() != null) {
					MethodCompileContext maxY = context.mainClass.newMethod(ACC_PUBLIC, "maxY_" + internalName, TypeInfos.INT);
					memory.putTyped(VALID_MAX_Y, maxY);
					condition = and(condition, IntCompareConditionTree.lessThan(y, invokeInstance(context.loadSelf(), maxY.info)));
				}
				if (condition != ConstantConditionTree.TRUE) {
					MethodCompileContext computeOneMethod = context.mainClass.newMethod(ACC_PUBLIC, "actually_compute_" + internalName, type.exposedType(), new LazyVarInfo("y", TypeInfos.INT));
					memory.putTyped(COMPUTE_ONE, computeOneMethod);

					LazyVarInfo self = new LazyVarInfo("this", getterMethod.clazz.info);
					new IfElseInsnTree(
						condition,
						return_(invokeInstance(load(self), computeOneMethod.info, y)),
						return_(ldc(valid.getFallback(type.exposedType()))),
						TypeInfos.VOID
					)
					.emitBytecode(getterMethod);
					getterMethod.endCode();
				}
				else {
					memory.putTyped(COMPUTE_ONE, getterMethod);
				}
			}
			else {
				memory.putTyped(COMPUTE_ONE, getterMethod);
			}
		}
	}

	public abstract void populateComputeAll(ColumnEntryMemory memory, DataCompileContext context, MethodCompileContext computeAllMethod);

	@Override
	public void populateSetter(ColumnEntryMemory memory, DataCompileContext context, MethodCompileContext setterMethod) {
		TypeContext type = memory.getTyped(ColumnEntryMemory.TYPE);
		setterMethod.setCode(
			"""
			var array = valueField
			if (y >= array.minCached && y < array.maxCached:
				array.array.set(y, value)
			)
			""",
			new MutableScriptEnvironment()
			.addVariableRenamedGetField(context.loadSelf(), "valueField", memory.getTyped(ColumnEntryMemory.FIELD).info)
			.addVariableLoad("y", TypeInfos.INT)
			.addVariableLoad("value", type.exposedType())
			.addFieldGet("minCached", MappedRangeNumberArray.MIN_CACHED)
			.addFieldGet("maxCached", MappedRangeNumberArray.MAX_CACHED)
			.addFieldGet("array", MappedRangeNumberArray.ARRAY)
			.addMethodInvoke("set", switch (type.exposedType().getSort()) {
				case BYTE    -> MappedRangeNumberArray.SET_B;
				case SHORT   -> MappedRangeNumberArray.SET_S;
				case INT     -> MappedRangeNumberArray.SET_I;
				case LONG    -> MappedRangeNumberArray.SET_L;
				case FLOAT   -> MappedRangeNumberArray.SET_F;
				case DOUBLE  -> MappedRangeNumberArray.SET_D;
				case BOOLEAN -> MappedRangeNumberArray.SET_Z;
				default -> throw new IllegalStateException("Unsupported type: " + type);
			})
		);
	}

	public abstract void populateComputeOne(ColumnEntryMemory memory, DataCompileContext context, MethodCompileContext computeOneMethod) throws ScriptParsingException;

	@Override
	public void emitComputer(ColumnEntryMemory memory, DataCompileContext context) throws ScriptParsingException {
		_3DValid valid = this.valid();
		if (valid != null) {
			if (valid.where() != null) {
				context.setMethodCode(memory.getTyped(ColumnEntryMemory.VALID_WHERE), valid.where(), false);
			}
			if (valid.min_y() != null) {
				context.setMethodCode(memory.getTyped(VALID_MIN_Y), valid.min_y(), false);
			}
			if (valid.max_y() != null) {
				context.setMethodCode(memory.getTyped(VALID_MAX_Y), valid.max_y(), false);
			}
		}
		this.populateComputeOne(memory, context, memory.getTyped(COMPUTE_ONE));
	}
}