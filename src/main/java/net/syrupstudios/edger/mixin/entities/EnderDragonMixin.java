package net.syrupstudios.edger.mixin.entities;

import net.syrupstudios.edger.callbacks.BlockBreakCallback;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin {
    @Inject(
            method = "checkWalls",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"
            )
    )
    private void logEnderDragonBreakingBlocks(AABB aABB, CallbackInfoReturnable<Boolean> cir, @Local BlockPos blockPos) {
        EnderDragon entity = (EnderDragon) (Object) this;
        Level world = entity.level();
        BlockBreakCallback.EVENT.invoker().breakBlock(world, blockPos, world.getBlockState(blockPos), world.getBlockEntity(blockPos), BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath());
    }
}
