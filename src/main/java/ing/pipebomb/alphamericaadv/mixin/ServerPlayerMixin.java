package ing.pipebomb.alphamericaadv.mixin;

import ing.pipebomb.alphamericaadv.AdvancementUtil;
import ing.pipebomb.alphamericaadv.AlphamericaAdv;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "awardKillScore", at = @At(value = "INVOKE",shift = At.Shift.AFTER, target = "Lnet/minecraft/advancements/critereon/KilledTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)V"))
    public void alphamericaadv$checkIfGunKill(Entity killed, int scoreValue, DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (BuiltInRegistries.ITEM.getKey(player.getWeaponItem().getItem()).getNamespace().equals("pointblank")) { // whatever
            if (killed.getType().equals(EntityType.WITHER)) {
                AdvancementUtil.grantAdvancement(player, ResourceLocation.fromNamespaceAndPath(AlphamericaAdv.MODID, "dummy/wither_gun"));
            }
        }
    }
}
