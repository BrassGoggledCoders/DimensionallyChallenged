package xyz.brassgoggledcoders.dimensionallychallenged.setting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.DoublePredicate;

public class LevelEdge {
    private final ResourceLocation levelName;
    private final int height;
    private final DoublePredicate check;

    public LevelEdge(ResourceLocation levelName, int height, boolean above) {
        this.levelName = levelName;
        this.height = height;
        this.check = above ? value -> value > this.getHeight() : value -> value < this.getHeight();
    }

    public boolean pastEdge(Entity entity) {
        return check.test(entity.getY());
    }

    public ResourceLocation getLevelName() {
        return levelName;
    }

    public int getHeight() {
        return height;
    }
}
