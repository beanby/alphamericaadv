package ing.pipebomb.alphamericaadv;

import com.mojang.datafixers.util.Pair;
import ing.pipebomb.alphamericaadv.predicates.DistanceCheck;
import ing.pipebomb.alphamericaadv.predicates.PolygonCheck;
import ing.pipebomb.alphamericaadv.stats.StatUtil;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import java.util.*;
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

    public static Set<ServerPlayer> SHOULD_GET_LEVELS = new HashSet<>();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AlphamericaAdv(IEventBus modEventBus, ModContainer modContainer)
    {
        LOGGER.info("Advancements mod loaded");

        LOOT_PREDICATE.register(modEventBus);
        StatUtil.CUSTOM_STAT_REGISTER.register(modEventBus);

        NeoForge.EVENT_BUS.register(AlphamericaAdv.class);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(ProjectionUtil.ProjectionCommand.getCommand());
        event.getDispatcher().register(ProjectionUtil.getPlayerSpawnCommand());
    }

    @SubscribeEvent
    public static void playerJoins(PlayerEvent.PlayerLoggedInEvent event) {
        Player plr = event.getEntity();
        if (plr instanceof ServerPlayer player) {
            UUID id = plr.getUUID();

            ServerLevel lvl = Objects.requireNonNull(plr.getServer()).overworld();
            ProjectionUtil.PlayerSpawnData data = ProjectionUtil.PlayerSpawnData.fromServerLevel(lvl);

            Map<UUID, Pair<Integer,Integer>> locations = data.spawns;
            if (locations.containsKey(id)) {
                Pair<Integer, Integer> pair = locations.get(id);
                BlockPos pos = new BlockPos(pair.getFirst(),0,pair.getSecond());
                LevelChunk chunk = lvl.getChunkAt(pos);
                int spawnHeight = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(),pos.getZ()) + 1;
                player.setRespawnPosition(lvl.dimension(),pos.atY(spawnHeight),0,false,false);
                player.teleportTo(lvl,pos.getX(),spawnHeight,pos.getZ(),0,0);

                data.removePlayer(id);
            }
        }
    }

    public static void playerDeathWithManyLevels(ServerPlayer plr) {
        AdvancementHolder adv = AdvancementUtil.getPlayersAdvancement(plr, ResourceLocation.fromNamespaceAndPath(MODID, "dummy/healthcare"));
        assert adv != null;
        AdvancementProgress progress = plr.getAdvancements().getOrStartProgress(adv);
        if (!progress.isDone()) {
            //Player doesn't have advancement
            AdvancementUtil.grantAdvancement(plr,adv);
            SHOULD_GET_LEVELS.add(plr);
        }
    }
}
