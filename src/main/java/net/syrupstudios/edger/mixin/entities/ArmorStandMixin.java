package net.syrupstudios.edger.mixin.entities;

import net.syrupstudios.edger.callbacks.EntityKillCallback;
import net.syrupstudios.edger.callbacks.EntityModifyCallback;
import net.syrupstudios.edger.utility.NbtUtils;
import net.syrupstudios.edger.utility.Sources;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin {
    @Unique
    private CompoundTag oldEntityTags;
    @Unique
    private ItemStack oldEntityStack;

    @Inject(method = "swapItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"))
    private void legerLogOldEntity(Player player, EquipmentSlot slot, ItemStack playerStack, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        this.oldEntityTags = NbtUtils.INSTANCE.createNbt(entity);
        this.oldEntityStack = entity.getItemBySlot(slot);
    }

    @Inject(method = "swapItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void ledgerArmorStandInteract(Player player, EquipmentSlot slot, ItemStack playerStack, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!oldEntityStack.isEmpty()) {
            EntityModifyCallback.EVENT.invoker().modify(player.level(), entity.blockPosition(), oldEntityTags, entity, oldEntityStack, player, Sources.REMOVE);
        }
        if (!playerStack.isEmpty()) {
            EntityModifyCallback.EVENT.invoker().modify(player.level(), entity.blockPosition(), oldEntityTags, entity, playerStack, player, Sources.EQUIP);
        }
    }

    @Inject(method = "causeDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V"))
    private void ledgerArmorStandKill(DamageSource damageSource, float f, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        EntityKillCallback.EVENT.invoker().kill(entity.level(), entity.blockPosition(), entity, damageSource);
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V"))
    private void ledgerArmorStandKill(DamageSource source, float f, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        EntityKillCallback.EVENT.invoker().kill(entity.level(), entity.blockPosition(), entity, source);
    }
}
