package xyz.brassgoggledcoders.dimensionallychallenged.api.setting;

import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IDimensionalSetting {
    @Nullable
    RegistryKey<World> above();

    @Nullable
    RegistryKey<World> below();

    LocationStatus getStatus(Entity entity);
}
