package net.syrupstudios.edger.mixin;

import net.syrupstudios.edger.callbacks.BlockChangeCallback;
import net.syrupstudios.edger.utility.Sources;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.JukeboxBlock.HAS_RECORD;

@Mixin(RecordItem.class)
public abstract class RecordItemMixin {

    @Inject(method = "useOn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/JukeboxBlockEntity;setFirstItem(Lnet/minecraft/world/item/ItemStack;)V"))
    private static void ledgerPlayerInsertMusicDisc(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir) {
        Level world = useOnContext.getLevel();
        BlockPos pos = useOnContext.getClickedPos();
        Player player = useOnContext.getPlayer();
        BlockState blockState = world.getBlockState(pos);

        BlockChangeCallback.EVENT.invoker().changeBlock(
                world,
                pos,
                blockState.setValue(HAS_RECORD, false),
                blockState,
                null,
                world.getBlockEntity(pos),
                Sources.INTERACT,
                player);
    }
}
