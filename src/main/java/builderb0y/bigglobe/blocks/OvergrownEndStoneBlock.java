package builderb0y.bigglobe.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class OvergrownEndStoneBlock extends Block implements Fertilizable {

	public OvergrownEndStoneBlock(Settings settings) {
		super(settings);
	}

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
		return true;
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		world.setBlockState(pos, BigGlobeBlocks.CHORUS_NYLIUM.getDefaultState());
	}
}