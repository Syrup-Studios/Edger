package net.syrupstudios.edger.mixin.entities;

import net.syrupstudios.edger.callbacks.EntityModifyCallback;
import net.syrupstudios.edger.utility.NbtUtils;
import net.syrupstudios.edger.utility.Sources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfMixin {
    @Unique
    private CompoundTag oldEntityTags;

    @Inject(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Wolf;setCollarColor(Lnet/minecraft/world/item/DyeColor;)V"))
    private void ledgerOldEntity(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        oldEntityTags = NbtUtils.INSTANCE.createNbt(entity);
    }

    @Inject(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Wolf;setCollarColor(Lnet/minecraft/world/item/DyeColor;)V", shift = At.Shift.AFTER))
    private void ledgerDogCollarColour(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        EntityModifyCallback.EVENT.invoker().modify(player.level(), entity.blockPosition(), oldEntityTags, entity, player.getItemInHand(hand), player, Sources.DYE);
    }
}
