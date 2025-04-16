package ing.pipebomb.alphamericaadv;

import ing.pipebomb.alphamericaadv.predicates.DistanceCheck;
import ing.pipebomb.alphamericaadv.predicates.PolygonCheck;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AlphamericaAdv.MODID)
public class AlphamericaAdv
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "alphamericaadv";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<LootItemConditionType> LOOT_PREDICATE = DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE,MODID);

    public static final Supplier<LootItemConditionType> DISTANCE_TO = LOOT_PREDICATE.register("distance_to", () -> new LootItemConditionType(DistanceCheck.CODEC));

    public static final Supplier<LootItemConditionType> IN_POLYGON = LOOT_PREDICATE.register("in_polygon", () -> new LootItemConditionType(PolygonCheck.CODEC));

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AlphamericaAdv(IEventBus modEventBus, ModContainer modContainer)
    {
        LOGGER.info("Advancements mod loaded");

        LOOT_PREDICATE.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
    }
}
