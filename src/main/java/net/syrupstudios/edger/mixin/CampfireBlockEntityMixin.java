package net.syrupstudios.edger.mixin;

import net.syrupstudios.edger.callbacks.BlockChangeCallback;
import net.syrupstudios.edger.utility.Sources;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin {

    @Inject(method = "cookTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
    private static void logCampfireRemoveItem(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        BlockChangeCallback.EVENT.invoker().changeBlock(level, pos, state, level.getBlockState(pos), campfire, level.getBlockEntity(pos), Sources.REMOVE);
    }

}