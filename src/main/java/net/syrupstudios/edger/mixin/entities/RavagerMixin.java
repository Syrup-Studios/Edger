package net.syrupstudios.edger.mixin.entities;

import net.syrupstudios.edger.callbacks.BlockBreakCallback;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Ravager.class)
public abstract class RavagerMixin {
    @ModifyArgs(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    public void logRavagerBreakingLeaves(Args args) {
        BlockPos pos = args.get(0);
        var world = ((Ravager) (Object) this).level();
        BlockBreakCallback.EVENT.invoker().breakBlock(world, pos, world.getBlockState(pos), world.getBlockEntity(pos), BuiltInRegistries.ENTITY_TYPE.getKey(((Ravager) (Object) this).getType()).getPath());
    }
}
