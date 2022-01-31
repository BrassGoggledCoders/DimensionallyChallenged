package xyz.brassgoggledcoders.dimensionallychallenged.api.setting;

import net.minecraft.world.IServerWorld;

import javax.annotation.Nullable;

public interface IDimensionalSettingsManager {
    boolean contains(IServerWorld serverWorld);

    @Nullable
    IDimensionalSetting get(IServerWorld serverWorld);
}
