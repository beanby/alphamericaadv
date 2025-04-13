package ing.pipebomb.alphamericaadv;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;

public record DistanceCheck(LootContext.EntityTarget entityTarget, BlockPos position, IntRange intRange) implements LootItemCondition {

    private static final MapCodec<BlockPos> POSITION_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("x").forGetter(Vec3i::getX),
                    Codec.INT.optionalFieldOf("y", 0).forGetter(Vec3i::getY), // just like, completely unused
                    Codec.INT.fieldOf("z").forGetter(Vec3i::getZ)
            ).apply(instance,BlockPos::new)
    );
    public static final MapCodec<DistanceCheck> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(DistanceCheck::entityTarget),
                    POSITION_CODEC.forGetter(DistanceCheck::position),
                    IntRange.CODEC.fieldOf("range").forGetter(DistanceCheck::intRange)
            ).apply(instance,DistanceCheck::new)
    );

    @Override
    public LootItemConditionType getType() {
        return AlphamericaAdv.DISTANCE_TO.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        //AlphamericaAdv.LOGGER.info("TEST RAN");
        Entity entity = lootContext.getParamOrNull(this.entityTarget.getParam());
        if (entity == null) return false;
        double dist = entity.position().distanceTo(Vec3.atLowerCornerOf(this.position.atY((int) entity.position().y)));
        AlphamericaAdv.LOGGER.info(String.valueOf(dist));
        return this.intRange.test(lootContext, (int) dist);
    }
}
