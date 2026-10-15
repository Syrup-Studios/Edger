package net.syrupstudios.edger.actions

import net.syrupstudios.edger.utility.TextColorPallet
import net.syrupstudios.edger.utility.literal
import net.minecraft.Util
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent

class BlockBreakActionType : BlockChangeActionType() {
    override val identifier = "block-break"

    override fun getObjectMessage(source: CommandSourceStack): Component = Component.translatable(
        Util.makeDescriptionId(
            this.getTranslationType(),
            oldObjectResourceLocation
        )
    ).setStyle(TextColorPallet.secondaryVariant).withStyle {
        it.withHoverEvent(
            HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                oldObjectResourceLocation.toString().literal()
            )
        )
    }
}
