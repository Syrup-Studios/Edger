package net.syrupstudios.edger.actions

import net.syrupstudios.edger.utility.TextColorPallet
import net.syrupstudios.edger.utility.getWorld
import net.syrupstudios.edger.utility.literal
import net.minecraft.Util
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.server.MinecraftServer

class BlockPlaceActionType : BlockChangeActionType() {
    override val identifier = "block-place"

    override fun rollback(server: MinecraftServer): Boolean {
        val world = server.getWorld(world)
        world?.setBlockAndUpdate(pos, oldBlockState(world.holderLookup(Registries.BLOCK)))

        return world != null
    }

    override fun restore(server: MinecraftServer): Boolean {
        val world = server.getWorld(world)

        if (world != null) {
            val state = newBlockState(world.holderLookup(Registries.BLOCK))
            world.setBlockAndUpdate(pos, state)
            if (state.hasBlockEntity()) {
                world.getBlockEntity(pos)?.load(TagParser.parseTag(extraData!!))
            }
        }

        return world != null
    }

    override fun getObjectMessage(source: CommandSourceStack): Component = Component.translatable(
        Util.makeDescriptionId(
            this.getTranslationType(),
            objectResourceLocation
        )
    ).setStyle(TextColorPallet.secondaryVariant).withStyle {
        it.withHoverEvent(
            HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                objectResourceLocation.toString().literal()
            )
        )
    }
}
