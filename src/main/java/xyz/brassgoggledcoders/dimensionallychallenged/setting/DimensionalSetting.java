package xyz.brassgoggledcoders.dimensionallychallenged.setting;

import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import xyz.brassgoggledcoders.dimensionallychallenged.api.setting.IDimensionalSetting;
import xyz.brassgoggledcoders.dimensionallychallenged.api.setting.LocationStatus;

import javax.annotation.Nullable;

public class DimensionalSetting implements IDimensionalSetting {
    private final LevelEdge above;
    private final LevelEdge below;

    private final RegistryKey<World> aboveKey;
    private final RegistryKey<World> belowKey;

    public DimensionalSetting(LevelEdge above, LevelEdge below) {
        this.above = above;
        this.below = below;
        this.aboveKey = above != null ? RegistryKey.create(Registry.DIMENSION_REGISTRY, above.getLevelName()) : null;
        this.belowKey = below != null ? RegistryKey.create(Registry.DIMENSION_REGISTRY, below.getLevelName()) : null;
    }

    @Override
    @Nullable
    public RegistryKey<World> above() {
        return aboveKey;
    }

    @Override
    @Nullable
    public RegistryKey<World> below() {
        return belowKey;
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
