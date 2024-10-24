package builderb0y.bigglobe.scripting.wrappers;

import java.lang.invoke.MethodHandles;
import java.util.random.RandomGenerator;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import builderb0y.bigglobe.dynamicRegistries.BigGlobeDynamicRegistries;
import builderb0y.bigglobe.structures.scripted.ScriptedStructure.CombinedStructureScripts;
import builderb0y.bigglobe.versions.IdentifierVersions;
import builderb0y.scripting.bytecode.ConstantFactory;
import builderb0y.scripting.bytecode.TypeInfo;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public record StructurePlacementScriptTagKey(TagKey<CombinedStructureScripts> key) implements TagWrapper<CombinedStructureScripts, StructurePlacementScriptEntry> {

	public static final TypeInfo TYPE = type(StructurePlacementScriptTagKey.class);
	public static final ConstantFactory CONSTANT_FACTORY = ConstantFactory.autoOfString();

	public static StructurePlacementScriptTagKey of(MethodHandles.Lookup lookup, String name, Class<?> type, String id) {
		return of(id);
	}

	public static StructurePlacementScriptTagKey of(String id) {
		if (id == null) return null;
		return new StructurePlacementScriptTagKey(TagKey.of(BigGlobeDynamicRegistries.SCRIPT_STRUCTURE_PLACEMENT_REGISTRY_KEY, IdentifierVersions.create(id)));
	}

	@Override
	public StructurePlacementScriptEntry wrap(RegistryEntry<CombinedStructureScripts> entry) {
		return new StructurePlacementScriptEntry(entry);
	}

	@Override
	public StructurePlacementScriptEntry random(RandomGenerator random) {
		return this.randomImpl(random);
	}

	@Override
	public StructurePlacementScriptEntry random(long seed) {
		return this.randomImpl(seed);
	}
}