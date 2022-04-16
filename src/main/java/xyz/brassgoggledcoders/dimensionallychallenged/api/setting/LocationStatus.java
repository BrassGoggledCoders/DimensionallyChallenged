package xyz.brassgoggledcoders.dimensionallychallenged.api.setting;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public enum LocationStatus {
    ABOVE(IDimensionalSetting::findAbove),
    STAY((dimensionalSetting, entity) -> null),
    BELOW(IDimensionalSetting::findBelow);

    private final BiFunction<IDimensionalSetting, Entity, ResourceKey<Level>> goTo;

    LocationStatus(BiFunction<IDimensionalSetting, Entity, ResourceKey<Level>> goTo) {
        this.goTo = goTo;
    }

    @Nullable
    public ServerLevel findPlacement(ServerLevel serverLevel, Entity entity, IDimensionalSetting dimensionalSetting) {
        ResourceKey<Level> levelName = goTo.apply(dimensionalSetting, entity);
        if (levelName != null) {
            return serverLevel.getLevel()
                    .getServer()
                    .getLevel(levelName);
        } else {
            return null;
        }
    }
}
