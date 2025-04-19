package ing.pipebomb.alphamericaadv.mixin;

import ing.pipebomb.alphamericaadv.AlphamericaAdv;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "addRespawnedPlayer", at = @At("TAIL"))
    public void alphamerica$PlayerRespawned(ServerPlayer plr, CallbackInfo ci){
        if (AlphamericaAdv.SHOULD_GET_LEVELS.contains(plr)) {
            plr.giveExperienceLevels(23);
            AlphamericaAdv.SHOULD_GET_LEVELS.remove(plr);
        }
    }
}
