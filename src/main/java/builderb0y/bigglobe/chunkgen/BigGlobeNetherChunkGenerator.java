package builderb0y.bigglobe.chunkgen;

import java.util.Optional;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.block.BlockState;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Util;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import builderb0y.autocodec.annotations.EncodeInline;
import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseCoder;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.bigglobe.BigGlobeMod;
import builderb0y.bigglobe.blocks.BlockStates;
import builderb0y.bigglobe.codecs.BigGlobeAutoCodec;
import builderb0y.bigglobe.columns.ChunkOfColumns;
import builderb0y.bigglobe.columns.NetherColumn;
import builderb0y.bigglobe.math.BigGlobeMath;
import builderb0y.bigglobe.math.Interpolator;
import builderb0y.bigglobe.noise.Permuter;
import builderb0y.bigglobe.scripting.ColumnYRandomToDoubleScript;
import builderb0y.bigglobe.scripting.ColumnYToDoubleScript;
import builderb0y.bigglobe.settings.NetherSettings;
import builderb0y.bigglobe.settings.NetherSettings.LocalNetherSettings;
import builderb0y.bigglobe.settings.NetherSettings.NetherSurfaceSettings;
import builderb0y.bigglobe.util.SemiThreadLocal;

@UseCoder(name = "createCoder", usage = MemberUsage.METHOD_IS_FACTORY)
public class BigGlobeNetherChunkGenerator extends BigGlobeChunkGenerator {

	public static final int
		LOWER_BEDROCK_AMOUNT = 16,
		UPPER_BEDROCK_AMOUNT = 16;

	public static final AutoCoder<BigGlobeNetherChunkGenerator> NETHER_CODER = BigGlobeAutoCodec.AUTO_CODEC.createCoder(BigGlobeNetherChunkGenerator.class);
	public static final Codec<BigGlobeNetherChunkGenerator> NETHER_CODEC = BigGlobeAutoCodec.AUTO_CODEC.createDFUCodec(NETHER_CODER);

	@EncodeInline
	public final NetherSettings settings;

	public transient SemiThreadLocal<ChunkOfColumns<NetherColumn>> chunkColumnCache;

	public BigGlobeNetherChunkGenerator(
		NetherSettings settings,
		Registry<StructureSet> structureSetRegistry
	) {
		super(
			structureSetRegistry,
			Optional.empty(),
			new ColumnBiomeSource(
				settings.local_settings().elements.stream().map(LocalNetherSettings::biome)
			)
		);
		this.settings = settings;
	}

	public static void init() {
		Registry.register(Registry.CHUNK_GENERATOR, BigGlobeMod.modID("nether"), NETHER_CODEC);
	}

	public static AutoCoder<BigGlobeNetherChunkGenerator> createCoder(FactoryContext<BigGlobeNetherChunkGenerator> context) {
		return BigGlobeChunkGenerator.createCoder(context, "bigglobe", "the_nether");
	}

	@Override
	public void setSeed(long seed) {
		super.setSeed(seed);
		this.chunkColumnCache = SemiThreadLocal.weak(4, () -> {
			return new ChunkOfColumns<>(NetherColumn[]::new, this::column);
		});
	}

	@Override
	public NetherColumn column(int x, int z) {
		return new NetherColumn(this.settings, this.seed, x, z);
	}

	public void runCaveOverriders(NetherColumn column) {
		LocalNetherSettings settings = column.getLocalCell().settings;
		int minY = this.settings.min_y();
		int maxY = this.settings.max_y();
		int effectiveMinY = minY + LOWER_BEDROCK_AMOUNT;
		int effectiveMaxY = maxY - UPPER_BEDROCK_AMOUNT;
		Integer lowerPadding = settings.caves().lower_padding();
		int actualLowerPadding = minY + (lowerPadding != null ? lowerPadding : column.getLocalCell().lavaLevel);
		double[] caveNoise = column.getCaveNoise();
		ColumnYToDoubleScript.Holder widthScript = settings.caves().width();
		for (int y = minY; y < actualLowerPadding; y++) {
			caveNoise[y - minY] += BigGlobeMath.squareD(widthScript.evaluate(column, y) * Interpolator.unmixLinear((double)(actualLowerPadding), (double)(effectiveMinY), (double)(y)));
		}
		double topWidth = widthScript.evaluate(column, effectiveMaxY);
		int actualUpperPadding = BigGlobeMath.ceilI(effectiveMaxY - topWidth);
		for (int y = actualUpperPadding; y < maxY; y++) {
			caveNoise[y - minY] += BigGlobeMath.squareD(widthScript.evaluate(column, y) * Interpolator.unmixLinear((double)(actualUpperPadding), (double)(effectiveMaxY), (double)(y)));
		}
		int distanceBetweenBiomes = this.settings.biome_placement().distance;
		double edginess = column.getEdginess() * distanceBetweenBiomes;
		for (int y = minY; y < maxY; y++) {
			double width = widthScript.evaluate(column, y);
			double sidePadding = Interpolator.unmixLinear(distanceBetweenBiomes - width, distanceBetweenBiomes, edginess);
			//problem: cave noise is vastly more likely to be 0 near edges than cavern noise is.
			//this results in very thin borders between cells.
			//occasionally, you can even see gaps between cells when the noise lines up just right.
			//solution: make the exclusion start farther back. that's what the + 0.75D is for.
			//problem #2: the width alone is not large enough to produce nice walls.
			//the walls are a bit too flat.
			//solution #2: make the exclusion have a more gradual slope to it.
			//that's what the * 0.5D is for.
			sidePadding = sidePadding * 0.5D + 0.75D;
			if (sidePadding > 0.0D) {
				caveNoise[y - minY] += BigGlobeMath.squareD(width * sidePadding);
			}
		}
	}

	public void runCavernOverriders(NetherColumn column) {
		LocalNetherSettings settings = column.getLocalCell().settings;
		int minY = settings.caverns().min_y();
		int maxY = settings.caverns().max_y();
		int lowerPaddingMaxY = minY + settings.caverns().lower_padding();
		int upperPaddingMinY = maxY - settings.caverns().upper_padding();
		double[] noise = column.getCavernNoise();
		double maxNoise = -settings.caverns().noise().minValue();
		for (int y = minY; y <= lowerPaddingMaxY; y++) {
			noise[y - minY] += maxNoise * BigGlobeMath.squareD(Interpolator.unmixLinear((double)(lowerPaddingMaxY), (double)(minY), (double)(y)));
		}
		for (int y = maxY; --y >= upperPaddingMinY;) {
			noise[y - minY] += maxNoise * BigGlobeMath.squareD(Interpolator.unmixLinear((double)(upperPaddingMinY), (double)(maxY), (double)(y)));
		}
		int distanceBetweenBiomes = this.settings.biome_placement().distance;
		double sidePadding = Interpolator.unmixLinear(distanceBetweenBiomes - settings.caverns().side_padding(), distanceBetweenBiomes, column.getEdginess() * distanceBetweenBiomes);
		if (sidePadding > 0.0D) {
			sidePadding = sidePadding * sidePadding * maxNoise;
			for (int y = minY; y < maxY; y++) {
				noise[y - minY] += sidePadding;
			}
		}
	}

	public void generateRawSections(Chunk chunk, ChunkOfColumns<NetherColumn> columns, StructureAccessor structures) {
		this.generateSectionsParallel(chunk, this.settings.min_y(), this.settings.max_y(), columns, context -> {
			BlockState previousFiller = null;
			int fillerID = -1;
			int lavaID = context.id(BlockStates.LAVA);
			PaletteStorage storage = context.storage();

			int startY = context.startY();
			for (int horizontalIndex = 0; horizontalIndex < 256; horizontalIndex++) {
				NetherColumn column = columns.getColumn(horizontalIndex);
				double[] caveNoise = column.getCaveNoise();
				int caveLowerY = column.settings.min_y();
				double[] cavernNoise = column.getCavernNoise();
				LocalNetherSettings localSettings = column.getLocalCell().settings;
				int cavernLowerY = localSettings.caverns().min_y();
				int cavernUpperY = localSettings.caverns().max_y();
				ColumnYToDoubleScript.Holder widthScript = localSettings.caves().width();
				int lavaLevel = column.getLocalCell().lavaLevel;
				if (localSettings.filler() != previousFiller) {
					previousFiller = localSettings.filler();
					fillerID = context.id(previousFiller);
					if (storage != (storage = context.storage())) { //resize
						fillerID = context.id(previousFiller);
						lavaID = context.id(BlockStates.LAVA);
						assert storage == context.storage();
					}
				}

				for (int verticalIndex = 0; verticalIndex < 16; verticalIndex++) {
					int y = startY | verticalIndex;
					double caveWidth = widthScript.evaluate(column, y);
					int index = horizontalIndex | (verticalIndex << 8);
					if (
						caveNoise[y - caveLowerY] < caveWidth * caveWidth || (
							y >= cavernLowerY &&
							y <  cavernUpperY &&
							cavernNoise[y - cavernLowerY] < 0.0D
						)
					) {
						if (y < lavaLevel) {
							storage.set(index, lavaID);
							context.addLight(index);
						}
					}
					else {
						storage.set(index, fillerID);
					}
				}
			}
		});
	}

	public void generateSurface(Chunk chunk, ChunkOfColumns<NetherColumn> columns) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		Permuter permuter = new Permuter(0L);
		long chunkSeed = Permuter.permute(this.seed ^ 0x8B9B939B4728BD6EL, chunk.getPos());
		for (int horizontalIndex = 0; horizontalIndex < 256; horizontalIndex++) {
			long columnSeed = Permuter.permute(chunkSeed, horizontalIndex);
			pos.setX(horizontalIndex & 15).setZ(horizontalIndex >>> 4);
			NetherColumn column = columns.getColumn(horizontalIndex);
			BlockState expect = column.getLocalCell().settings.filler();
			NetherSurfaceSettings surface = column.getLocalCell().settings.caverns().surface();
			ColumnYRandomToDoubleScript.Holder depthScript = surface.depth();
			IntList floors = column.cavernFloors;
			for (int floorIndex = 0, floorCount = floors.size(); floorIndex < floorCount; floorIndex++) {
				int y = floors.getInt(floorIndex) - 1;
				permuter.setSeed(Permuter.permute(columnSeed, y));
				int depth = BigGlobeMath.floorI(depthScript.evaluate(column, y, permuter));
				boolean top = true;
				for (int i = 0; i < depth; i++) {
					pos.setY(y - i);
					BlockState existingState = chunk.getBlockState(pos);
					if (existingState == expect) {
						chunk.setBlockState(pos, top ? surface.top_state() : surface.under_state(), false);
						top = false;
					}
					else if (existingState.isAir()) {
						top = true;
					}
				}
			}
		}
	}

	@Override
	public void generateRawTerrain(Executor executor, Chunk chunk, StructureAccessor structureAccessor, boolean distantHorizons) {
		ChunkOfColumns<NetherColumn> columns = this.chunkColumnCache.get();
		try {
			this.profiler.run("initial terrain column values", () -> {
				columns.setPosAndPopulate(chunk.getPos().getStartX(), chunk.getPos().getStartZ(), column -> {
					column.getLocalCell();
					column.getCaveNoise();
					this.runCaveOverriders(column);
					column.getCavernNoise();
					this.runCavernOverriders(column);
					column.getEdginess();
					column.populateCaveAndCavernFloors();
				});
			});
			//if (chunk instanceof PositionCacheHolder holder) {
			//	holder.bigglobe_setPositionCache(new NetherPositionCache(columns));
			//}
			this.profiler.run("set raw terrain blocks", () -> {
				this.generateRawSections(chunk, columns, structureAccessor);
			});
			this.profiler.run("Recount", () -> {
				this.generateSectionsParallelSimple(
					chunk,
					chunk.getBottomY(),
					chunk.getTopY(),
					columns,
					SectionGenerationContext::recalculateCounts
				);
			});
			this.profiler.run("Init heightmaps", () -> {
				int maxY = this.settings.max_y();
				this.setHeightmaps(chunk, (index, includeWater) -> maxY);
			});
			this.profiler.run("Surface", () -> {
				this.generateSurface(chunk, columns);
			});
		}
		finally {
			this.chunkColumnCache.reclaim(columns);
		}
	}

	@Override
	public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {

	}

	@Override
	public Codec<? extends ChunkGenerator> getCodec() {
		return NETHER_CODEC;
	}

	@Override
	public void populateEntities(ChunkRegion region) {

	}

	@Override
	public int getWorldHeight() {
		return this.settings.height();
	}

	@Override
	public int getSeaLevel() {
		return this.settings.min_y();
	}

	@Override
	public int getMinimumY() {
		return this.settings.min_y();
	}

	@Override
	public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
		return this.settings.max_y();
	}

	@Override
	public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
		NetherColumn column = this.column(x, z);
		int worldMinY = this.settings.min_y();
		int worldMaxY = this.settings.max_y();
		return new VerticalBlockSample(worldMinY, Util.make(new BlockState[worldMaxY - worldMinY], states -> {
			LocalNetherSettings localSettings = column.getLocalCell().settings;
			int lavaLevel = column.getLocalCell().lavaLevel;
			ColumnYToDoubleScript.Holder widthScript = localSettings.caves().width();
			double[] caveNoise = column.getCaveNoise();
			double[] cavernNoise = column.getCavernNoise();
			int cavernLowerY = localSettings.caverns().min_y();
			int cavernUpperY = localSettings.caverns().max_y();
			for (int index = 0, length = states.length; index < length; index++) {
				int y = index + worldMinY;
				double caveWidth = widthScript.evaluate(column, y);
				if (
					caveNoise[y - worldMinY] < caveWidth * caveWidth || (
						y >= cavernLowerY &&
						y < cavernUpperY &&
						cavernNoise[y - cavernLowerY] < 0.0D
					)
				) {
					states[index] = y < lavaLevel ? BlockStates.LAVA : BlockStates.AIR;
				}
				else {
					states[index] = BlockStates.NETHERRACK;
				}
			}
		}));
	}
}