package ing.pipebomb.alphamericaadv.mixin;

import ing.pipebomb.alphamericaadv.AdvancementUtil;
import ing.pipebomb.alphamericaadv.AlphamericaAdv;
import ing.pipebomb.alphamericaadv.stats.StatUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownPotion.class)
public class ThrownPotionMixin {

    @Inject(method = "onHitBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownPotion;getItem()Lnet/minecraft/world/item/ItemStack;"))
    public void alphamericaadv$whenInWater(BlockHitResult result, CallbackInfo ci) {
        Entity owner = ((ThrownPotion) (Object) this).getOwner();
        if ((owner instanceof ServerPlayer player)) {
            Level level = player.level();
            BlockPos blockIn = result.getBlockPos().relative(result.getDirection());

            BlockState blockState = level.getBlockState(blockIn);
            if (blockState.is(Blocks.WATER)) {
                player.awardStat(StatUtil.BOSTON_POTION_STAT.get(), 1);
                if (player.getStats().getValue(Stats.CUSTOM.get(StatUtil.BOSTON_POTION_STAT.get())) >= 100 && !player.getAdvancements().getOrStartProgress(AdvancementUtil.getPlayersAdvancement(player, ResourceLocation.fromNamespaceAndPath(AlphamericaAdv.MODID, "dummy/tea"))).isDone()) {
                    AdvancementUtil.grantAdvancement(player, ResourceLocation.fromNamespaceAndPath(AlphamericaAdv.MODID, "dummy/tea"));
                }
            }
        }
    }
}
