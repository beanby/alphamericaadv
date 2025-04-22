package ing.pipebomb.alphamericaadv.mixin;

import ing.pipebomb.alphamericaadv.AlphamericaAdv;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class PlayerMixin {

    @Inject(method = "die", at = @At("HEAD"))
    public void alphamericaadv$onPlayerDie(DamageSource cause, CallbackInfo ci) {
        ServerPlayer plr = (ServerPlayer) (Object) this;
        if (plr.experienceLevel >= 75) {
            AlphamericaAdv.playerDeathWithManyLevels(plr);
        }
    }
}
