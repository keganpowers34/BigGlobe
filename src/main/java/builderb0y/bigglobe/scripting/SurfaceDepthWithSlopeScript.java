package builderb0y.bigglobe.scripting;

import java.util.random.RandomGenerator;

import builderb0y.autocodec.annotations.Wrapper;
import builderb0y.bigglobe.columns.ColumnValue;
import builderb0y.bigglobe.columns.WorldColumn;
import builderb0y.scripting.environments.MathScriptEnvironment;
import builderb0y.scripting.environments.MutableScriptEnvironment;
import builderb0y.scripting.parsing.Script;
import builderb0y.scripting.parsing.ScriptParser;
import builderb0y.scripting.parsing.ScriptParsingException;
import builderb0y.scripting.util.TypeInfos;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public interface SurfaceDepthWithSlopeScript extends Script {

	public abstract double evaluate(WorldColumn column, double y, double slopeSquared, RandomGenerator random);

	@Wrapper
	public static class Holder extends ScriptHolder<SurfaceDepthWithSlopeScript> implements SurfaceDepthWithSlopeScript {

		public Holder(String script) throws ScriptParsingException {
			super(
				new ScriptParser<>(SurfaceDepthWithSlopeScript.class, script)
				.addEnvironment(
					ColumnScriptEnvironment.createFixedXYZ(
						ColumnValue.REGISTRY,
						load("column", 1, type(WorldColumn.class)),
						load("y", 2, TypeInfos.DOUBLE)
					)
					.addXZ("x", "z")
					.addY("y")
					.mutable
				)
				.addEnvironment(MathScriptEnvironment.INSTANCE)
				.addEnvironment(RandomScriptEnvironment.create(
					load("random", 6, type(RandomGenerator.class))
				))
				.addEnvironment(
					new MutableScriptEnvironment()
					.addVariableLoad("slope_squared", 4, TypeInfos.DOUBLE)
				)
				.parse()
			);
		}

		@Override
		public double evaluate(WorldColumn column, double y, double slopeSquared, RandomGenerator random) {
			try {
				return this.script.evaluate(column, y, slopeSquared, random);
			}
			catch (Throwable throwable) {
				this.onError(throwable);
				return 0.0D;
			}
		}
	}
}