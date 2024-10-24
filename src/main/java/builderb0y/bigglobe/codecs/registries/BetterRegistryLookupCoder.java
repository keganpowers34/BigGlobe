package builderb0y.bigglobe.codecs.registries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.*;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.bigglobe.dynamicRegistries.BetterRegistry;
import builderb0y.bigglobe.dynamicRegistries.BetterRegistry.BetterHardCodedRegistry;

import net.minecraft.registry.entry.RegistryEntryOwner;
import builderb0y.bigglobe.dynamicRegistries.BetterRegistry.BetterDynamicRegistry;

public class BetterRegistryLookupCoder extends NamedCoder<BetterRegistry.Lookup> {

	public static final BetterRegistryLookupCoder INSTANCE = new BetterRegistryLookupCoder("BetterRegistryLookupCoder.INSTANCE");

	public BetterRegistryLookupCoder(@NotNull String toString) {
		super(toString);
	}

	@Override
	public <T_Encoded> BetterRegistry.@Nullable Lookup decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.ops instanceof RegistryOps<T_Encoded> registryOps) {
			return fromOps(registryOps);
		}
		else {
			throw new DecodeException(() -> "Not a RegistryOps: " + context.ops);
		}
	}

	public static BetterRegistry.Lookup fromOps(RegistryOps<?> registryOps) {
		return new BetterRegistry.Lookup() {

			@Override
			public <T> BetterRegistry<T> getRegistry(RegistryKey<Registry<T>> key) {
				RegistryEntryLookup<T> lookup = registryOps.getEntryLookup(key).orElse(null);
				if (lookup == null) {
					throw new IllegalStateException("Missing registry: " + key.getValue());
				}
				RegistryEntryOwner<T> owner = registryOps.getOwner(key).orElse(null);
				if (!(owner instanceof RegistryWrapper.Impl<T> impl)) {
					throw new IllegalStateException("Owner is not a RegistryWrapper.Impl: " + owner + " in registry " + key.getValue());
				}
				return new BetterDynamicRegistry<>(impl, lookup);
			}
		};
	}

	@Override
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, BetterRegistry.Lookup> context) throws EncodeException {
		return context.empty();
	}
}