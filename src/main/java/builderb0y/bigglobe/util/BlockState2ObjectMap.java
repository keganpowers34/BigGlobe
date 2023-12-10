package builderb0y.bigglobe.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser.BlockResult;
import net.minecraft.state.property.Property;

import builderb0y.autocodec.annotations.UseImplementation;
import builderb0y.autocodec.annotations.Wrapper;
import builderb0y.bigglobe.versions.BlockArgumentParserVersions;

@Wrapper
public class BlockState2ObjectMap<V> {

	public final @UseImplementation(LinkedHashMap.class) Map<String, V> serializedStates;
	public final transient Map<BlockState, V> runtimeStates;

	public BlockState2ObjectMap(Map<String, V> serializedStates) throws CommandSyntaxException {
		this.serializedStates = serializedStates;
		this.runtimeStates = new HashMap<>(serializedStates.size());
		for (Map.Entry<String, V> serializedEntry : serializedStates.entrySet()) {
			BlockResult blockResult = BlockArgumentParserVersions.block(serializedEntry.getKey(), false);
			Block block = blockResult.blockState().getBlock();
			nextState:
			for (BlockState state : block.getStateManager().getStates()) {
				for (Map.Entry<Property<?>, Comparable<?>> propertyEntry : blockResult.properties().entrySet()) {
					if (state.get(propertyEntry.getKey()) != propertyEntry.getValue()) {
						continue nextState;
					}
				}
				this.runtimeStates.put(state, serializedEntry.getValue());
			}
		}
	}
}