package xyz.brassgoggledcoders.dimensionallychallenged.setting;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import xyz.brassgoggledcoders.shadyskies.conditional.conditional.ConditionalTarget;
import xyz.brassgoggledcoders.shadyskies.conditional.conditional.IConditional;

import java.util.List;
import java.util.function.DoublePredicate;

public class LevelEdge {
    private final ResourceKey<Level> levelName;
    private final int height;
    private final DoublePredicate check;
    private final List<IConditional> conditionals;

    public LevelEdge(ResourceKey<Level> levelName, int height, boolean above, List<IConditional> predicates) {
        this.levelName = levelName;
        this.height = height;
        this.check = above ? value -> value > this.getHeight() : value -> value < this.getHeight();
        this.conditionals = predicates;
    }

    public boolean pastEdge(Entity entity) {
        return check.test(entity.getY());
    }

    public ResourceKey<Level>  getLevelName() {
        return levelName;
    }

    public int getHeight() {
        return height;
    }

    public List<IConditional> getConditionals() {
        return conditionals;
    }

    public boolean isValid(Entity entity) {
        ConditionalTarget target = new ConditionalTarget(entity);
        return this.getConditionals().stream().allMatch(predicate -> predicate.test(target));
    }
}
