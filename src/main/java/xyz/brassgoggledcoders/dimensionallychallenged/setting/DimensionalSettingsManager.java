package xyz.brassgoggledcoders.dimensionallychallenged.setting;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.util.JsonUtils;
import xyz.brassgoggledcoders.dimensionallychallenged.DimensionallyChallenged;
import xyz.brassgoggledcoders.dimensionallychallenged.api.setting.IDimensionalSetting;
import xyz.brassgoggledcoders.dimensionallychallenged.api.setting.IDimensionalSettingsManager;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Map.Entry;

public class DimensionalSettingsManager extends SimpleJsonResourceReloadListener implements IDimensionalSettingsManager {
    private final Map<ResourceLocation, DimensionalSetting> dimensionalSettings = Maps.newHashMap();

    public DimensionalSettingsManager() {
        super(new Gson(), "dimensionally_challenged");
    }

    @Override
    public boolean contains(ServerLevel serverLevel) {
        return dimensionalSettings.containsKey(serverLevel.getLevel()
                .dimension()
                .location()
        );
    }

    @Override
    @Nullable
    public IDimensionalSetting get(ServerLevel serverLevel) {
        return dimensionalSettings.get(serverLevel.getLevel()
                .dimension()
                .location()
        );
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<ResourceLocation, DimensionalSetting> newDimensionalSettings = Maps.newHashMap();
        dimensionalSettings.clear();
        for (Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation name = entry.getKey();
            JsonObject rootObject = GsonHelper.convertToJsonObject(entry.getValue(), "Root");
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
            JsonObject edgeObject = GsonHelper.getAsJsonObject(rootObject, memberName);
            String name = GsonHelper.getAsString(edgeObject, "name");
            ResourceLocation levelName = ResourceLocation.tryParse(name);
            if (levelName == null) {
                throw new JsonParseException("'name' was not a valid ResourceLocation");
            }
            int height = GsonHelper.getAsInt(edgeObject, "height");
            return new LevelEdge(levelName, height, above);
        }
        return null;
    }
}
