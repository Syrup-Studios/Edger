package net.syrupstudios.edger.actions

import net.syrupstudios.edger.actionutils.Preview
import net.syrupstudios.edger.utility.MessageUtils
import net.syrupstudios.edger.utility.Sources
import net.syrupstudios.edger.utility.TextColorPallet
import net.syrupstudios.edger.utility.literal
import com.mojang.authlib.GameProfile
import net.minecraft.ChatFormatting
import net.minecraft.Util
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import java.time.Instant
import kotlin.time.ExperimentalTime

abstract class AbstractActionType : ActionType {
    override var id: Int = -1
    override var timestamp: Instant = Instant.now()
    override var pos: BlockPos = BlockPos.ZERO
    override var world: ResourceLocation? = null
    override var objectResourceLocation: ResourceLocation = ResourceLocation("air")
    override var oldObjectResourceLocation: ResourceLocation = ResourceLocation("air")
    override var objectState: String? = null
    override var oldObjectState: String? = null
    override var sourceName: String = Sources.UNKNOWN
    override var sourceProfile: GameProfile? = null
    override var extraData: String? = null
    override var rolledBack: Boolean = false

    override fun rollback(server: MinecraftServer): Boolean = false
    override fun previewRollback(preview: Preview, player: ServerPlayer) = Unit
    override fun previewRestore(preview: Preview, player: ServerPlayer) = Unit
    override fun restore(server: MinecraftServer): Boolean = false

    @ExperimentalTime
    override fun getMessage(source: CommandSourceStack): Component {
        val message = Component.translatable(
            "text.edger.action_message",
            getTimeMessage(),
            getSourceMessage(),
            getActionMessage(),
            getObjectMessage(source),
            getLocationMessage()
        )
        message.style = TextColorPallet.light

        if (rolledBack) {
            message.withStyle(ChatFormatting.STRIKETHROUGH)
        }

        return message
    }

    @ExperimentalTime
    open fun getTimeMessage(): Component = MessageUtils.instantToText(timestamp)

    open fun getSourceMessage(): Component {
        if (sourceProfile == null) {
            return "@$sourceName".literal().setStyle(TextColorPallet.secondary)
        }

        if (sourceName == Sources.PLAYER) {
            return sourceProfile!!.name.literal().setStyle(TextColorPallet.secondary)
        }

        return "@$sourceName (${sourceProfile!!.name})".literal().setStyle(TextColorPallet.secondary)
    }

    open fun getActionMessage(): Component = Component.translatable("text.edger.action.$identifier")
        .withStyle {
            it.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    identifier.literal()
                )
            )
        }

    open fun getObjectMessage(source: CommandSourceStack): Component = Component.translatable(
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

    open fun getLocationMessage(): Component = "${pos.x} ${pos.y} ${pos.z}".literal()
        .setStyle(TextColorPallet.secondary)
        .withStyle {
            it.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Component.literal(world?.let { "$it\n" } ?: "")
                        .append(Component.translatable("text.edger.action_message.location.hover"))
                )
            ).withClickEvent(
                ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/lg tp ${world ?: Level.OVERWORLD.location()} ${pos.x} ${pos.y} ${pos.z}"
                )
            )
        }
}
