package builderb0y.bigglobe.overriders.overworld;

import builderb0y.autocodec.annotations.Wrapper;
import builderb0y.bigglobe.columns.OverworldColumn;
import builderb0y.scripting.bytecode.FieldInfo;
import builderb0y.scripting.bytecode.tree.InsnTree;
import builderb0y.scripting.parsing.ScriptParser;
import builderb0y.scripting.parsing.ScriptParsingException;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public class DataOverworldHeightOverrider {

	@Wrapper
	public static class Holder extends DataOverrider.Holder {

		public Holder(String script) throws ScriptParsingException {
			super(
				new ScriptParser<>(DataOverrider.class, script)
				.addEnvironment(DataOverworldHeightOverrider.Environment.INSTANCE)
			);
		}
	}

	public static class Environment extends DataOverrider.Environment {

		public static final Environment INSTANCE = new Environment();

		public Environment() {
			super();

			InsnTree columnLoader = load("column", 2, type(OverworldColumn.class));
			this
			.addVariableRenamedGetField(columnLoader, "terrainY", FieldInfo.getField(OverworldColumn.class, "finalHeight"))
			.addVariableRenamedGetField(columnLoader, "snowY", FieldInfo.getField(OverworldColumn.class, "snowHeight"))
			;
		}
	}
}