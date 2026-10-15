package net.syrupstudios.edger.mixin.entities;

import net.syrupstudios.edger.listeners.EntityCallbackListenerKt;
import net.syrupstudios.edger.utility.Sources;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(
            method = "convertTo",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;discard()V"
            )
    )
    private <T extends Mob> void ledgerEntityConversion(EntityType<T> entityType, boolean bl, CallbackInfoReturnable<T> cir) {
        Mob entity = (Mob) (Object) this;
        EntityCallbackListenerKt.onKill(entity.level(), entity.blockPosition(), entity, Sources.CONVERSION);
    }
}
