package xyz.brassgoggledcoders.dimensionallychallenged.setting;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import xyz.brassgoggledcoders.dimensionallychallenged.api.setting.IDimensionalSetting;
import xyz.brassgoggledcoders.dimensionallychallenged.api.setting.LocationStatus;

import javax.annotation.Nullable;

public class DimensionalSetting implements IDimensionalSetting {
    private final LevelEdge above;
    private final LevelEdge below;

    public DimensionalSetting(LevelEdge above, LevelEdge below) {
        this.above = above;
        this.below = below;
    }

    @Override
    @Nullable
    public ResourceKey<Level> findAbove(Entity entity) {
        return null;
    }

    @Override
    @Nullable
    public ResourceKey<Level> findBelow(Entity entity) {
        return null;
    }

    @Override
    public LocationStatus getStatus(Entity entity) {
        if (above != null && above.pastEdge(entity)) {
            return LocationStatus.ABOVE;
        } else if (below != null && below.pastEdge(entity)) {
            return LocationStatus.BELOW;
        } else {
            return LocationStatus.STAY;
        }
    }
}
