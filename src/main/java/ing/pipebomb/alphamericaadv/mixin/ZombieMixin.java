package ing.pipebomb.alphamericaadv.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Zombie.class)
public class ZombieMixin {

    @Redirect(method = "killedEntity",at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextBoolean()Z"))
    public boolean alwaysZombie(RandomSource instance) {
        return false;
    }
}
