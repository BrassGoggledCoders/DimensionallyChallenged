package xyz.brassgoggledcoders.dimensionallychallenged.api.setting;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface IDimensionalSetting {
    @Nullable
    ResourceKey<Level> findAbove(Entity entity);

    @Nullable
    ResourceKey<Level> findBelow(Entity entity);

    LocationStatus getStatus(Entity entity);
}
