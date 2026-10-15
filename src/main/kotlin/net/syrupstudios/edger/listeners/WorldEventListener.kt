package net.syrupstudios.edger.listeners

import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.actionutils.ActionFactory
import net.syrupstudios.edger.callbacks.ItemInsertCallback
import net.syrupstudios.edger.callbacks.ItemRemoveCallback
import net.syrupstudios.edger.database.ActionQueueService
import net.syrupstudios.edger.database.DatabaseManager
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

fun registerWorldEventListeners() {
    ItemInsertCallback.EVENT.register(::onItemInsert)
    ItemRemoveCallback.EVENT.register(::onItemRemove)
    ServerWorldEvents.LOAD.register(::onWorldLoad)
}

fun onWorldLoad(server: MinecraftServer, world: ServerLevel) {
    Ledger.launch {
        DatabaseManager.registerWorld(world.dimension().location())
    }
}

private fun onItemRemove(
    stack: ItemStack,
    pos: BlockPos,
    world: ServerLevel,
    source: String,
    entity: LivingEntity?
) {
    if (entity != null) {
        ActionQueueService.addToQueue(
            ActionFactory.itemRemoveAction(world, stack, pos, entity)
        )
    } else {
        ActionQueueService.addToQueue(
            ActionFactory.itemRemoveAction(world, stack, pos, source)
        )
    }
}

private fun onItemInsert(
    stack: ItemStack,
    pos: BlockPos,
    world: ServerLevel,
    source: String,
    entity: LivingEntity?
) {
    if (entity != null) {
        ActionQueueService.addToQueue(
            ActionFactory.itemInsertAction(world, stack, pos, entity)
        )
    } else {
        ActionQueueService.addToQueue(
            ActionFactory.itemInsertAction(world, stack, pos, source)
        )
    }
}
