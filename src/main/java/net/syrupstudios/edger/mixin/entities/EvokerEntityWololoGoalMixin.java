package net.syrupstudios.edger.mixin.entities;

import net.syrupstudios.edger.callbacks.EntityModifyCallback;
import net.syrupstudios.edger.utility.NbtUtils;
import net.syrupstudios.edger.utility.Sources;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Evoker.EvokerWololoSpellGoal.class)
public abstract class EvokerEntityWololoGoalMixin {
    @Unique
    private CompoundTag oldEntityTags;

    @Inject(method = "performSpellCasting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Sheep;setColor(Lnet/minecraft/world/item/DyeColor;)V"))
    public void legerLogOldEntity(CallbackInfo ci, @Local Sheep sheepEntity) {
        if (sheepEntity.getColor() != DyeColor.RED) {
            this.oldEntityTags = NbtUtils.INSTANCE.createNbt(sheepEntity);
        }
    }

    @Inject(method = "performSpellCasting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Sheep;setColor(Lnet/minecraft/world/item/DyeColor;)V", shift = At.Shift.AFTER))
    public void ledgerEvokerDyeSheep(CallbackInfo ci, @Local Sheep sheepEntity) {
        if (oldEntityTags != null) {
            EntityModifyCallback.EVENT.invoker().modify(sheepEntity.level(), sheepEntity.blockPosition(), oldEntityTags, sheepEntity, Items.RED_DYE.getDefaultInstance(), null, Sources.DYE);
        }
    }
}
