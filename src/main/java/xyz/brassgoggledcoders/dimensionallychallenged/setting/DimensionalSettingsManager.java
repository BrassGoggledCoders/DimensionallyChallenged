package xyz.brassgoggledcoders.dimensionallychallenged.setting;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IServerWorld;
import xyz.brassgoggledcoders.dimensionallychallenged.DimensionallyChallenged;
import xyz.brassgoggledcoders.dimensionallychallenged.api.setting.IDimensionalSetting;
import xyz.brassgoggledcoders.dimensionallychallenged.api.setting.IDimensionalSettingsManager;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Map.Entry;

public class DimensionalSettingsManager extends JsonReloadListener implements IDimensionalSettingsManager {
    private final Map<ResourceLocation, DimensionalSetting> dimensionalSettings = Maps.newHashMap();

    public DimensionalSettingsManager() {
        super(new Gson(), "dimensionally_challenged");
    }

    @Override
    public boolean contains(IServerWorld serverWorld) {
        return dimensionalSettings.containsKey(serverWorld.getLevel()
                .dimension()
                .location()
        );
    }

    @Override
    @Nullable
    public IDimensionalSetting get(IServerWorld serverWorld) {
        return dimensionalSettings.get(serverWorld.getLevel()
                .dimension()
                .location()
        );
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void apply(Map<ResourceLocation, JsonElement> pObject, IResourceManager pResourceManager, IProfiler pProfiler) {
        Map<ResourceLocation, DimensionalSetting> newDimensionalSettings = Maps.newHashMap();
        dimensionalSettings.clear();
        for (Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation name = entry.getKey();
            JsonObject rootObject = JSONUtils.convertToJsonObject(entry.getValue(), "Root");
            try {
                LevelEdge above = parseLevelEdge(rootObject, true);
                LevelEdge below = parseLevelEdge(rootObject, false);

                if (above == null && below == null) {
                    throw new JsonParseException("Found neither 'above' nor 'below', at least one is required");
                } else if (above != null && below != null) {
                    if (above.getHeight() < below.getHeight()) {
                        throw new JsonParseException("above.height must be greater than below.height");
                    }
                }

                newDimensionalSettings.put(name, new DimensionalSetting(above, below));
            } catch (JsonParseException e) {
                DimensionallyChallenged.LOGGER.warn("Failed to parse: '" + name.toString() + "'", e);
            }
        }
        DimensionallyChallenged.LOGGER.info("Loaded: " + newDimensionalSettings.size() + " dimensional settings");
        dimensionalSettings.putAll(newDimensionalSettings);
    }

    private LevelEdge parseLevelEdge(JsonObject rootObject, boolean above) {
        String memberName = above ? "above" : "below";
        if (rootObject.has(memberName)) {
            JsonObject edgeObject = JSONUtils.getAsJsonObject(rootObject, memberName);
            String name = JSONUtils.getAsString(edgeObject, "name");
            ResourceLocation levelName = ResourceLocation.tryParse(name);
            if (levelName == null) {
                throw new JsonParseException("'name' was not a valid ResourceLocation");
            }
            int height = JSONUtils.getAsInt(edgeObject, "height");
            return new LevelEdge(levelName, height, above);
        }
        return null;
    }
}
