package ing.pipebomb.alphamericaadv.stats;

import ing.pipebomb.alphamericaadv.AlphamericaAdv;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class StatUtil {
    public static final DeferredRegister<ResourceLocation> CUSTOM_STAT_REGISTER = DeferredRegister.create(BuiltInRegistries.CUSTOM_STAT,AlphamericaAdv.MODID);

    public static Supplier<ResourceLocation> BOSTON_POTION_STAT = makeCustomStat("boston_potions");
    public static Supplier<ResourceLocation> PLANT_CORN         = makeCustomStat("plant_corn");

    private static Supplier<ResourceLocation> makeCustomStat(String key) {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(AlphamericaAdv.MODID, key);
        return CUSTOM_STAT_REGISTER.register(key, () -> loc);
    }
}
