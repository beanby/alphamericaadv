package ing.pipebomb.alphamericaadv;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public class AdvancementUtil {

    public static AdvancementHolder fromResourceLocation(ResourceLocation id, MinecraftServer server) {
        return server.getAdvancements().get(id);
    }

    public static void grantAdvancement(ServerPlayer player, AdvancementHolder advancement) {
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        for (String criteria : progress.getRemainingCriteria()) { // grant advancement
            player.getAdvancements().award(advancement, criteria);
        }
    }

    public static void grantAdvancement(ServerPlayer player, ResourceLocation advancement) {
        if (player.getServer() == null) return;
        grantAdvancement(player, fromResourceLocation(advancement, player.getServer()));
    }

    public static AdvancementHolder getPlayersAdvancement(ServerPlayer player, ResourceLocation location) {
        return Objects.requireNonNull(player.getServer()).getAdvancements().get(location);
    }
}
