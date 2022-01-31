package xyz.brassgoggledcoders.dimensionallychallenged.api.setting;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Function;

public enum LocationStatus {
    ABOVE(IDimensionalSetting::above),
    STAY((dimensionalSetting) -> null),
    BELOW(IDimensionalSetting::below);

    private final Function<IDimensionalSetting, RegistryKey<World>> goTo;

    LocationStatus(Function<IDimensionalSetting, RegistryKey<World>> goTo) {
        this.goTo = goTo;
    }

    @Nullable
    public IServerWorld findNewLevel(IServerWorld serverLevel, IDimensionalSetting dimensionalSetting) {
        RegistryKey<World> levelName = goTo.apply(dimensionalSetting);
        if (levelName != null) {
            return serverLevel.getLevel()
                    .getServer()
                    .getLevel(levelName);
        } else {
            return null;
        }
    }
}
