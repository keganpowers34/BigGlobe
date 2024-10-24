package builderb0y.bigglobe.scripting.wrappers;

import java.lang.invoke.MethodHandles;

import net.minecraft.registry.entry.RegistryEntry;

import builderb0y.bigglobe.BigGlobeMod;
import builderb0y.bigglobe.dynamicRegistries.BigGlobeDynamicRegistries;
import builderb0y.bigglobe.structures.scripted.ScriptedStructure.CombinedStructureScripts;
import builderb0y.scripting.bytecode.ConstantFactory;
import builderb0y.scripting.bytecode.TypeInfo;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public record StructurePlacementScriptEntry(RegistryEntry<CombinedStructureScripts> entry) implements EntryWrapper<CombinedStructureScripts, StructurePlacementScriptTagKey> {

	public static final TypeInfo TYPE = type(StructurePlacementScriptEntry.class);
	public static final ConstantFactory CONSTANT_FACTORY = ConstantFactory.autoOfString();

	public static StructurePlacementScriptEntry of(MethodHandles.Lookup caller, String name, Class<?> type, String id) {
		return of(id);
	}

	public static StructurePlacementScriptEntry of(String id) {
		if (id == null) return null;
		return new StructurePlacementScriptEntry(
			BigGlobeMod
			.getRegistry(BigGlobeDynamicRegistries.SCRIPT_STRUCTURE_PLACEMENT_REGISTRY_KEY)
			.getByName(id)
		);
	}

	@Override
	public boolean isIn(StructurePlacementScriptTagKey key) {
		return this.isInImpl(key);
	}
}