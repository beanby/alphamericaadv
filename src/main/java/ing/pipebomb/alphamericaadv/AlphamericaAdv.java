package ing.pipebomb.alphamericaadv;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AlphamericaAdv.MODID)
public class AlphamericaAdv
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "alphamericaadv";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AlphamericaAdv(IEventBus modEventBus, ModContainer modContainer)
    {
        LOGGER.info("Advancements mod loaded");
    }

}
