package ing.pipebomb.alphamericaadv.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ing.pipebomb.alphamericaadv.AlphamericaAdv;
import net.minecraft.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.List;
import java.util.stream.IntStream;

public record PolygonCheck(LootContext.EntityTarget target, List<Point> points) implements LootItemCondition {

    public static final MapCodec<PolygonCheck> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(PolygonCheck::target),
                    Point.CODEC.listOf().fieldOf("points").forGetter(PolygonCheck::points)
            ).apply(instance, PolygonCheck::new));
    @Override
    public LootItemConditionType getType() {
        return AlphamericaAdv.IN_POLYGON.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity ent = lootContext.getParamOrNull(this.target.getParam());
        if (ent == null) return false;
//        for (PointPair i : this.points) {
//            AlphamericaAdv.LOGGER.info("[{}, {}]",i.x,i.z);
//        }
        return testPointInPoly(new Point((int) ent.getX(),(int) ent.getZ()), points);
    }

    public boolean testPointInPoly(Point pT, List<Point> polygon) {
        // This method does the ray-cast thing but with optimizations for 2d polygons
        boolean inside = false;
        for (int i = 0,j=polygon.size()-1;i < polygon.size(); j = i++) {
            Point pI = polygon.get(i);
            Point pJ = polygon.get(j);
            if (
                    ((pI.z > pT.z) != (pJ.z > pT.z))
                    && (pT.x < (((pJ.x-pI.x)*(pT.z-pI.z))/(pJ.z-pI.z)) + pI.x)
            ) {
                inside = !inside;
            }
        }
        return inside;
    }

    public record Point(int x, int z) {
        public static final Codec<Point> CODEC = Codec.INT_STREAM.comapFlatMap(
                intStream -> Util.fixedSize(intStream,2).map(ints -> new Point(ints[0],ints[1])),
                p -> IntStream.of(p.x,p.z)
        );
    }
}
