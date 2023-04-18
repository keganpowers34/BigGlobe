package builderb0y.bigglobe.scripting.wrappers;

import java.lang.invoke.MethodHandles;
import java.util.random.RandomGenerator;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.Structure;

import builderb0y.bigglobe.scripting.ConstantFactory;
import builderb0y.scripting.bytecode.TypeInfo;

public record StructureTagKey(TagKey<Structure> key) implements TagWrapper<Structure, StructureEntry> {

	public static final TypeInfo TYPE = TypeInfo.of(StructureTagKey.class);
	public static final ConstantFactory CONSTANT_FACTORY = ConstantFactory.autoOfString();

	public static StructureTagKey of(MethodHandles.Lookup caller, String name, Class<?> type, String id) {
		return of(id);
	}

	public static StructureTagKey of(String id) {
		return new StructureTagKey(TagKey.of(RegistryKeys.STRUCTURE, new Identifier(id)));
	}

	@Override
	public StructureEntry wrap(RegistryEntry<Structure> entry) {
		return new StructureEntry(entry);
	}

	@Override
	public StructureEntry random(RandomGenerator random) {
		return this.randomImpl(random);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || (
			obj instanceof StructureTagKey that &&
			this.key.id().equals(that.key.id())
		);
	}

	@Override
	public int hashCode() {
		return this.key.id().hashCode();
	}

	@Override
	public String toString() {
		return "StructureTag: { " + this.key.id() + " }";
	}
}