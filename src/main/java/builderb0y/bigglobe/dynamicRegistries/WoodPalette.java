package builderb0y.bigglobe.dynamicRegistries;

import java.util.*;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Range;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Properties;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import builderb0y.autocodec.annotations.SingletonArray;
import builderb0y.autocodec.annotations.UseName;
import builderb0y.autocodec.annotations.VerifyNullable;
import builderb0y.bigglobe.BigGlobeMod;
import builderb0y.bigglobe.randomLists.IRandomList;
import builderb0y.bigglobe.util.ServerValue;
import builderb0y.bigglobe.util.UnregisteredObjectException;

@SuppressWarnings("unused")
public class WoodPalette {

	public static final ServerValue<Map<RegistryKey<Biome>, List<RegistryEntry<WoodPalette>>>>
		BIOME_CACHE = new ServerValue<>(WoodPalette::computeBiomeCache);

	public final EnumMap<WoodPaletteType, @SingletonArray IRandomList<@UseName("block") Block>> blocks;
	public final @VerifyNullable RegistryEntry<ConfiguredFeature<?, ?>> sapling_grow_feature;
	/** a tag containing biomes whose trees are made of this wood palette. */
	public final @VerifyNullable TagKey<Biome> biomes;
	public transient Set<RegistryKey<Biome>> biomeSet;

	public WoodPalette(
		EnumMap<WoodPaletteType, IRandomList<Block>> blocks,
		@VerifyNullable RegistryEntry<ConfiguredFeature<?, ?>> sapling_grow_feature,
		@VerifyNullable TagKey<Biome> biomes
	) {
		this.blocks = blocks;
		this.sapling_grow_feature = sapling_grow_feature;
		this.biomes = biomes;
	}

	public Set<RegistryKey<Biome>> getBiomeSet() {
		if (this.biomeSet == null) {
			if (this.biomes != null) {
				Optional<RegistryEntryList.Named<Biome>> list = BigGlobeMod.getCurrentServer().getRegistryManager().get(RegistryKeys.BIOME).getEntryList(this.biomes);
				if (list.isPresent()) {
					this.biomeSet = list.get().stream().map(UnregisteredObjectException::getKey).collect(Collectors.toSet());
				}
				else {
					this.biomeSet = Collections.emptySet();
				}
			}
			else {
				this.biomeSet = Collections.emptySet();
			}
		}
		return this.biomeSet;
	}

	public static Map<RegistryKey<Biome>, List<RegistryEntry<WoodPalette>>> computeBiomeCache() {
		Map<RegistryKey<Biome>, List<RegistryEntry<WoodPalette>>> map = new HashMap<>();
		BigGlobeMod
		.getCurrentServer()
		.getRegistryManager()
		.get(BigGlobeDynamicRegistries.WOOD_PALETTE_REGISTRY_KEY)
		.streamEntries()
		.sequential()
		.forEach((RegistryEntry<WoodPalette> entry) -> {
			entry.value().getBiomeSet().forEach((RegistryKey<Biome> key) -> {
				map.computeIfAbsent(key, $ -> new ArrayList<>(8)).add(entry);
			});
		});
		return map;
	}

	//////////////////////////////// block ////////////////////////////////

	public Block getBlock(RandomGenerator random, WoodPaletteType type) {
		Block block = this.blocks.get(type).getRandomElement(random);
		if (block != null) return block;
		else throw new IllegalStateException("WoodPaletteType not present: " + type);
	}

	public Block logBlock          (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.LOG           ); }
	public Block woodBlock         (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.WOOD          ); }
	public Block strippedLogBlock  (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.STRIPPED_LOG  ); }
	public Block strippedWoodBlock (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.STRIPPED_WOOD ); }
	public Block planksBlock       (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.PLANKS        ); }
	public Block stairsBlock       (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.STAIRS        ); }
	public Block slabBlock         (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.SLAB          ); }
	public Block fenceBlock        (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.FENCE         ); }
	public Block fenceGateBlock    (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.FENCE_GATE    ); }
	public Block doorBlock         (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.DOOR          ); }
	public Block trapdoorBlock     (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.TRAPDOOR      ); }
	public Block pressurePlateBlock(RandomGenerator random) { return this.getBlock(random, WoodPaletteType.PRESSURE_PLATE); }
	public Block buttonBlock       (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.BUTTON        ); }
	public Block leavesBlock       (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.LEAVES        ); }
	public Block saplingBlock      (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.SAPLING       ); }
	public Block pottedSaplingBlock(RandomGenerator random) { return this.getBlock(random, WoodPaletteType.POTTED_SAPLING); }
	public Block standingSignBlock (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.STANDING_SIGN ); }
	public Block wallSignBlock     (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.WALL_SIGN     ); }

	//////////////////////////////// blocks ////////////////////////////////

	public IRandomList<Block> getBlocks(WoodPaletteType type) {
		IRandomList<Block> block = this.blocks.get(type);
		if (block != null) return block;
		else throw new IllegalStateException("WoodPaletteType not present: " + type);
	}

	public IRandomList<Block> logBlocks          () { return this.getBlocks(WoodPaletteType.LOG           ); }
	public IRandomList<Block> woodBlocks         () { return this.getBlocks(WoodPaletteType.WOOD          ); }
	public IRandomList<Block> strippedLogBlocks  () { return this.getBlocks(WoodPaletteType.STRIPPED_LOG  ); }
	public IRandomList<Block> strippedWoodBlocks () { return this.getBlocks(WoodPaletteType.STRIPPED_WOOD ); }
	public IRandomList<Block> planksBlocks       () { return this.getBlocks(WoodPaletteType.PLANKS        ); }
	public IRandomList<Block> stairsBlocks       () { return this.getBlocks(WoodPaletteType.STAIRS        ); }
	public IRandomList<Block> slabBlocks         () { return this.getBlocks(WoodPaletteType.SLAB          ); }
	public IRandomList<Block> fenceBlocks        () { return this.getBlocks(WoodPaletteType.FENCE         ); }
	public IRandomList<Block> fenceGateBlocks    () { return this.getBlocks(WoodPaletteType.FENCE_GATE    ); }
	public IRandomList<Block> doorBlocks         () { return this.getBlocks(WoodPaletteType.DOOR          ); }
	public IRandomList<Block> trapdoorBlocks     () { return this.getBlocks(WoodPaletteType.TRAPDOOR      ); }
	public IRandomList<Block> pressurePlateBlocks() { return this.getBlocks(WoodPaletteType.PRESSURE_PLATE); }
	public IRandomList<Block> buttonBlocks       () { return this.getBlocks(WoodPaletteType.BUTTON        ); }
	public IRandomList<Block> leavesBlocks       () { return this.getBlocks(WoodPaletteType.LEAVES        ); }
	public IRandomList<Block> saplingBlocks      () { return this.getBlocks(WoodPaletteType.SAPLING       ); }
	public IRandomList<Block> pottedSaplingBlocks() { return this.getBlocks(WoodPaletteType.POTTED_SAPLING); }
	public IRandomList<Block> standingSignBlocks () { return this.getBlocks(WoodPaletteType.STANDING_SIGN ); }
	public IRandomList<Block> wallSignBlocks     () { return this.getBlocks(WoodPaletteType.WALL_SIGN     ); }

	//////////////////////////////// states ////////////////////////////////

	public BlockState getState(RandomGenerator random, WoodPaletteType type) {
		return this.getBlock(random, type).getDefaultState();
	}

	public BlockState logState(RandomGenerator random, Axis axis) {
		return (
			this.getState(random, WoodPaletteType.LOG)
			.withIfExists(Properties.AXIS, axis)
		);
	}

	public BlockState woodState(RandomGenerator random, Axis axis) {
		return (
			this.getState(random, WoodPaletteType.WOOD)
			.withIfExists(Properties.AXIS, axis)
		);
	}

	public BlockState strippedLogState(RandomGenerator random, Axis axis) {
		return (
			this.getState(random, WoodPaletteType.STRIPPED_LOG)
			.withIfExists(Properties.AXIS, axis)
		);
	}

	public BlockState strippedWoodState(RandomGenerator random, Axis axis) {
		return (
			this.getState(random, WoodPaletteType.STRIPPED_WOOD)
			.withIfExists(Properties.AXIS, axis)
		);
	}

	public BlockState planksState(RandomGenerator random) {
		return (
			this.getState(random, WoodPaletteType.PLANKS)
		);
	}

	public BlockState stairsState(RandomGenerator random, Direction facing, BlockHalf half, StairShape shape, boolean waterlogged) {
		return (
			this.getState(random, WoodPaletteType.STAIRS)
			.withIfExists(Properties.HORIZONTAL_FACING, facing)
			.withIfExists(Properties.BLOCK_HALF, half)
			.withIfExists(Properties.STAIR_SHAPE, shape)
			.withIfExists(Properties.WATERLOGGED, waterlogged)
		);
	}

	public BlockState slabState(RandomGenerator random, BlockHalf half, boolean waterlogged) {
		return (
			this.getState(random, WoodPaletteType.SLAB)
			.withIfExists(Properties.BLOCK_HALF, half)
			.withIfExists(Properties.WATERLOGGED, waterlogged)
		);
	}

	public BlockState fenceState(RandomGenerator random, boolean north, boolean east, boolean south, boolean west, boolean waterlogged) {
		return (
			this.getState(random, WoodPaletteType.FENCE)
			.withIfExists(Properties.NORTH, north)
			.withIfExists(Properties.EAST, east)
			.withIfExists(Properties.SOUTH, south)
			.withIfExists(Properties.WEST, west)
			.withIfExists(Properties.WATERLOGGED, waterlogged)
		);
	}

	public BlockState fenceGateState(RandomGenerator random, Direction facing, boolean open, boolean in_wall, boolean powered) {
		return (
			this.getState(random, WoodPaletteType.FENCE_GATE)
			.withIfExists(Properties.HORIZONTAL_FACING, facing)
			.withIfExists(Properties.OPEN, open)
			.withIfExists(Properties.IN_WALL, in_wall)
			.withIfExists(Properties.POWERED, powered)
		);
	}

	public BlockState doorState(RandomGenerator random, Direction facing, DoubleBlockHalf half, DoorHinge hinge, boolean open, boolean powered) {
		return (
			this.getState(random, WoodPaletteType.DOOR)
			.withIfExists(Properties.HORIZONTAL_FACING, facing)
			.withIfExists(Properties.DOUBLE_BLOCK_HALF, half)
			.withIfExists(Properties.DOOR_HINGE, hinge)
			.withIfExists(Properties.OPEN, open)
			.withIfExists(Properties.POWERED, powered)
		);
	}

	public BlockState trapdoorState(RandomGenerator random, Direction facing, BlockHalf half, boolean open, boolean powered, boolean waterlogged) {
		return (
			this.getState(random, WoodPaletteType.TRAPDOOR)
			.withIfExists(Properties.HORIZONTAL_FACING, facing)
			.withIfExists(Properties.BLOCK_HALF, half)
			.withIfExists(Properties.OPEN, open)
			.withIfExists(Properties.POWERED, powered)
			.withIfExists(Properties.WATERLOGGED, waterlogged)
		);
	}

	public BlockState pressurePlateState(RandomGenerator random, boolean powered) {
		return (
			this.getState(random, WoodPaletteType.PRESSURE_PLATE)
			.withIfExists(Properties.POWERED, powered)
		);
	}

	public BlockState buttonState(RandomGenerator random, WallMountLocation face, Direction facing, boolean powered) {
		return (
			this.getState(random, WoodPaletteType.BUTTON)
			.withIfExists(Properties.WALL_MOUNT_LOCATION, face)
			.withIfExists(Properties.HORIZONTAL_FACING, facing)
			.withIfExists(Properties.POWERED, powered)
		);
	}

	public BlockState leavesState(RandomGenerator random, @Range(from = 1, to = 7) int distance, boolean persistent, boolean waterlogged) {
		return (
			this.getState(random, WoodPaletteType.LEAVES)
			.withIfExists(Properties.DISTANCE_1_7, distance)
			.withIfExists(Properties.PERSISTENT, persistent)
			.withIfExists(Properties.WATERLOGGED, waterlogged)
		);
	}

	public BlockState saplingState(RandomGenerator random, @Range(from = 0, to = 1) int stage) {
		return (
			this.getState(random, WoodPaletteType.SAPLING)
			.withIfExists(Properties.STAGE, stage)
		);
	}

	public BlockState pottedSaplingState(RandomGenerator random) {
		return (
			this.getState(random, WoodPaletteType.POTTED_SAPLING)
		);
	}

	public BlockState standingSignState(RandomGenerator random, int rotation, boolean waterlogged) {
		return (
			this.getState(random, WoodPaletteType.STANDING_SIGN)
			.withIfExists(Properties.ROTATION, rotation)
			.withIfExists(Properties.WATERLOGGED, waterlogged)
		);
	}

	public BlockState wallSignState(RandomGenerator random, Direction facing, boolean waterlogged) {
		return (
			this.getState(random, WoodPaletteType.WALL_SIGN)
			.withIfExists(Properties.HORIZONTAL_FACING, facing)
			.withIfExists(Properties.WATERLOGGED, waterlogged)
		);
	}

	//////////////////////////////// types ////////////////////////////////

	public static enum WoodPaletteType implements StringIdentifiable {
		LOG,
		WOOD,
		STRIPPED_LOG,
		STRIPPED_WOOD,
		PLANKS,
		STAIRS,
		SLAB,
		FENCE,
		FENCE_GATE,
		DOOR,
		TRAPDOOR,
		PRESSURE_PLATE,
		BUTTON,
		LEAVES,
		SAPLING,
		POTTED_SAPLING,
		STANDING_SIGN,
		WALL_SIGN;

		public static final WoodPaletteType[] VALUES = values();
		public static final Map<String, WoodPaletteType> LOWER_CASE_LOOKUP = (
			Arrays
			.stream(VALUES)
			.collect(
				Collectors.toMap(
					(WoodPaletteType type) -> type.lowerCaseName,
					Function.identity()
				)
			)
		);

		public final String lowerCaseName = this.name().toLowerCase(Locale.ROOT);

		@Override
		public String asString() {
			return this.lowerCaseName;
		}
	}
}