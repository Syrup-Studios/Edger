package net.syrupstudios.edger.actions

import net.syrupstudios.edger.actionutils.Preview
import net.syrupstudios.edger.logWarn
import net.syrupstudios.edger.utility.NbtUtils
import net.syrupstudios.edger.utility.TextColorPallet
import net.syrupstudios.edger.utility.getWorld
import net.syrupstudios.edger.utility.literal
import net.minecraft.Util
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.HolderGetter
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

open class BlockChangeActionType : AbstractActionType() {
    override val identifier = "block-change"

    override fun rollback(server: MinecraftServer): Boolean {
        val world = server.getWorld(world)
        world?.setBlockAndUpdate(pos, oldBlockState(world.holderLookup(Registries.BLOCK)))
        world?.getBlockEntity(pos)?.load(TagParser.parseTag(extraData!!))
        world?.chunkSource?.blockChanged(pos)

        return true
    }

    override fun previewRollback(preview: Preview, player: ServerPlayer) {
        if (player.level().dimension().location() == world) {
            player.connection.send(
                ClientboundBlockUpdatePacket(
                    pos,
                    oldBlockState(player.level().holderLookup(Registries.BLOCK))
                )
            )
            preview.positions.add(pos)
        }
    }

    override fun restore(server: MinecraftServer): Boolean {
        val world = server.getWorld(world)

        world?.setBlockAndUpdate(pos, newBlockState(world.holderLookup(Registries.BLOCK)))

        return true
    }

    override fun previewRestore(preview: Preview, player: ServerPlayer) {
        if (player.level().dimension().location() == world) {
            player.connection.send(
                ClientboundBlockUpdatePacket(
                    pos,
                    newBlockState(player.level().holderLookup(Registries.BLOCK))
                )
            )
            preview.positions.add(pos)
        }
    }

    override fun getTranslationType() = "block"

    override fun getObjectMessage(source: CommandSourceStack): Component {
        val text = Component.literal("")
        text.append(
            Component.translatable(
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
        )
        if (oldObjectResourceLocation != objectResourceLocation) {
            text.append(" → ".literal())
            text.append(
                Component.translatable(
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
            )
        }
        return text
    }

    fun oldBlockState(blockLookup: HolderGetter<Block>) = checkForBlockState(
        oldObjectResourceLocation,
        oldObjectState?.let {
            NbtUtils.blockStateFromProperties(
                TagParser.parseTag(it),
                oldObjectResourceLocation,
                blockLookup
            )
        }
    )

    fun newBlockState(blockLookup: HolderGetter<Block>) = checkForBlockState(
        objectResourceLocation,
        objectState?.let {
            NbtUtils.blockStateFromProperties(
                TagParser.parseTag(it),
                objectResourceLocation,
                blockLookup
            )
        }
    )

    private fun checkForBlockState(identifier: ResourceLocation, checkState: BlockState?): BlockState {
        val block = BuiltInRegistries.BLOCK.getOptional(identifier)
        if (block.isEmpty) {
            logWarn("Unknown block $identifier")
            return Blocks.AIR.defaultBlockState()
        }

        var state = block.get().defaultBlockState()
        if (checkState != null) state = checkState

        return state
    }
}
