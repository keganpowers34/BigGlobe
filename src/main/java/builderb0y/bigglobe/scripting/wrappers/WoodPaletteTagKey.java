package builderb0y.bigglobe.scripting.wrappers;

import java.lang.invoke.MethodHandles;
import java.util.random.RandomGenerator;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import builderb0y.bigglobe.dynamicRegistries.BigGlobeDynamicRegistries;
import builderb0y.bigglobe.dynamicRegistries.WoodPalette;
import builderb0y.bigglobe.versions.IdentifierVersions;
import builderb0y.scripting.bytecode.ConstantFactory;
import builderb0y.scripting.bytecode.TypeInfo;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public record WoodPaletteTagKey(TagKey<WoodPalette> key) implements TagWrapper<WoodPalette, WoodPaletteEntry> {

	public static final TypeInfo TYPE = type(WoodPaletteTagKey.class);
	public static final ConstantFactory CONSTANT_FACTORY = ConstantFactory.autoOfString();

	public static WoodPaletteTagKey of(MethodHandles.Lookup caller, String name, Class<?> type, String id) {
		return of(id);
	}

	public static WoodPaletteTagKey of(String id) {
		if (id == null) return null;
		return new WoodPaletteTagKey(TagKey.of(BigGlobeDynamicRegistries.WOOD_PALETTE_REGISTRY_KEY, IdentifierVersions.create(id)));
	}

	@Override
	public WoodPaletteEntry wrap(RegistryEntry<WoodPalette> entry) {
		return new WoodPaletteEntry(entry);
	}

	@Override
	public WoodPaletteEntry random(RandomGenerator random) {
		return this.randomImpl(random);
	}

	@Override
	public WoodPaletteEntry random(long seed) {
		return this.randomImpl(seed);
	}
}