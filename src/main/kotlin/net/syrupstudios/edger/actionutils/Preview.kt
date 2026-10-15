package net.syrupstudios.edger.actionutils

import net.syrupstudios.edger.actions.ActionType
import net.syrupstudios.edger.commands.subcommands.RestoreCommand
import net.syrupstudios.edger.commands.subcommands.RollbackCommand
import net.syrupstudios.edger.mixin.preview.ServerEntityAccessor
import net.syrupstudios.edger.utility.Context
import net.syrupstudios.edger.utility.TextColorPallet
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

class Preview(
    private val params: ActionSearchParams,
    actions: List<ActionType>,
    player: ServerPlayer,
    private val type: Type
) {
    val positions = mutableSetOf<BlockPos>()

    // Preview entities that got spawned. Need to removed
    val spawnedEntityTrackers = mutableSetOf<ServerEntity>()

    // Preview entities that got removed. Need to be spawned
    val removedEntityTrackers = mutableSetOf<ServerEntity>()

    // Preview items that should be modified in screen handlers (true = added, false = removed)
    val modifiedItems = mutableMapOf<BlockPos, MutableList<Pair<ItemStack, Boolean>>>()

    init {
        player.displayClientMessage(
            Component.translatable(
                "text.edger.preview.start",
                actions.size
            ).setStyle(TextColorPallet.primary),
            false
        )

        for (action in actions) {
            when (type) {
                Type.ROLLBACK -> action.previewRollback(this, player)
                Type.RESTORE -> action.previewRestore(this, player)
            }
        }
    }

    fun cancel(player: ServerPlayer) {
        for (pos in positions) {
            player.connection.send(ClientboundBlockUpdatePacket(player.level(), pos))
        }
        cleanup(player)
    }

    private fun cleanup(player: ServerPlayer) {
        // Cleanup preview entities, to keep client and server in sync
        spawnedEntityTrackers.forEach {
            if (!isEntityPresent(it)) {
                it.removePairing(player)
            }
        }
        removedEntityTrackers.forEach {
            if (isEntityPresent(it)) {
                it.addPairing(player)
            }
        }
    }

    private fun isEntityPresent(entityTrackerEntry: ServerEntity): Boolean {
        val entity = (entityTrackerEntry as ServerEntityAccessor).entity
        return entity.level().getEntity(entity.id) != null
    }

    fun apply(context: Context) {
        cleanup(context.source.playerOrException)
        when (type) {
            Type.ROLLBACK -> RollbackCommand.rollback(context, params)
            Type.RESTORE -> RestoreCommand.restore(context, params)
        }
    }

    enum class Type {
        ROLLBACK,
        RESTORE
    }
}
