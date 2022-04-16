package xyz.brassgoggledcoders.dimensionallychallenged.api.setting;

import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

public interface IDimensionalSettingsManager {
    boolean contains(ServerLevel serverWorld);

    @Nullable
    IDimensionalSetting get(ServerLevel serverWorld);
}
