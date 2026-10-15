package net.syrupstudios.edger.actionutils

import net.syrupstudios.edger.actions.ActionType
import net.syrupstudios.edger.actions.BlockBreakActionType
import net.syrupstudios.edger.actions.BlockChangeActionType
import net.syrupstudios.edger.actions.BlockPlaceActionType
import net.syrupstudios.edger.actions.EntityChangeActionType
import net.syrupstudios.edger.actions.EntityDismountActionType
import net.syrupstudios.edger.actions.EntityKillActionType
import net.syrupstudios.edger.actions.EntityMountActionType
import net.syrupstudios.edger.actions.ItemDropActionType
import net.syrupstudios.edger.actions.ItemInsertActionType
import net.syrupstudios.edger.actions.ItemPickUpActionType
import net.syrupstudios.edger.actions.ItemRemoveActionType
import net.syrupstudios.edger.utility.NbtUtils
import net.syrupstudios.edger.utility.NbtUtils.createNbt
import net.syrupstudios.edger.utility.Sources
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

object ActionFactory {
    fun blockBreakAction(
        world: Level,
        pos: BlockPos,
        state: BlockState,
        source: String,
        entity: BlockEntity? = null
    ): BlockBreakActionType {
        val action = BlockBreakActionType()
        setBlockData(action, pos, world, Blocks.AIR.defaultBlockState(), state, source, entity)

        return action
    }

    fun blockBreakAction(
        world: Level,
        pos: BlockPos,
        state: BlockState,
        player: Player,
        entity: BlockEntity? = null,
        source: String = Sources.PLAYER
    ): BlockChangeActionType {
        val action = blockBreakAction(world, pos, state, source, entity)
        action.sourceProfile = player.gameProfile

        return action
    }

    fun blockPlaceAction(
        world: Level,
        pos: BlockPos,
        state: BlockState,
        source: String,
        entity: BlockEntity? = null
    ): BlockChangeActionType {
        val action = BlockPlaceActionType()
        setBlockData(action, pos, world, state, Blocks.AIR.defaultBlockState(), source, entity)

        return action
    }

    fun blockPlaceAction(
        world: Level,
        pos: BlockPos,
        state: BlockState,
        player: Player,
        entity: BlockEntity? = null,
        source: String = Sources.PLAYER
    ): BlockChangeActionType {
        val action = blockPlaceAction(world, pos, state, source, entity)
        action.sourceProfile = player.gameProfile

        return action
    }

    private fun setBlockData(
        action: ActionType,
        pos: BlockPos,
        world: Level,
        state: BlockState,
        oldState: BlockState,
        source: String,
        entity: BlockEntity? = null
    ) {
        action.pos = pos
        action.world = world.dimension().location()
        action.objectResourceLocation = BuiltInRegistries.BLOCK.getKey(state.block)
        action.oldObjectResourceLocation = BuiltInRegistries.BLOCK.getKey(oldState.block)
        action.objectState = NbtUtils.blockStateToProperties(state)?.toString()
        action.oldObjectState = NbtUtils.blockStateToProperties(oldState)?.toString()
        action.sourceName = source
        action.extraData = entity?.saveWithoutMetadata()?.toString()
    }

    fun itemInsertAction(world: Level, stack: ItemStack, pos: BlockPos, source: String): ItemInsertActionType {
        val action = ItemInsertActionType()
        setItemData(action, pos, world, stack, source)

        return action
    }

    fun itemInsertAction(
        world: Level,
        stack: ItemStack,
        pos: BlockPos,
        source: LivingEntity
    ): ItemInsertActionType {
        val action = ItemInsertActionType()
        var sourceType = Sources.UNKNOWN
        if (source is Player) {
            sourceType = Sources.PLAYER
            action.sourceProfile = source.gameProfile
        }
        setItemData(action, pos, world, stack, sourceType)

        return action
    }

    fun itemRemoveAction(world: Level, stack: ItemStack, pos: BlockPos, source: String): ItemRemoveActionType {
        val action = ItemRemoveActionType()
        setItemData(action, pos, world, stack, source)

        return action
    }

    fun itemRemoveAction(
        world: Level,
        stack: ItemStack,
        pos: BlockPos,
        source: LivingEntity
    ): ItemRemoveActionType {
        val action = ItemRemoveActionType()
        var sourceType = Sources.UNKNOWN
        if (source is Player) {
            sourceType = Sources.PLAYER
            action.sourceProfile = source.gameProfile
        }
        setItemData(action, pos, world, stack, sourceType)

        return action
    }

    fun itemPickUpAction(
        entity: ItemEntity,
        source: Player
    ): ItemPickUpActionType {
        val action = ItemPickUpActionType()

        setItemData(action, entity.blockPosition(), entity.level(), entity.item, Sources.PLAYER)

        action.oldObjectState = entity.createNbt().toString()
        action.sourceProfile = source.gameProfile

        return action
    }

    fun itemDropAction(
        entity: ItemEntity,
        source: LivingEntity
    ): ItemDropActionType {
        val action = ItemDropActionType()

        setItemData(action, entity.blockPosition(), entity.level(), entity.item, Sources.PLAYER)

        action.objectState = entity.createNbt().toString()
        if (source is Player) {
            action.sourceProfile = source.gameProfile
        }

        return action
    }

    fun blockChangeAction(
        world: Level,
        pos: BlockPos,
        oldState: BlockState,
        newState: BlockState,
        oldBlockEntity: BlockEntity?,
        source: String,
        player: Player?
    ): ActionType {
        val action = BlockChangeActionType()
        setBlockData(action, pos, world, newState, oldState, source, oldBlockEntity)
        action.sourceProfile = player?.gameProfile
        return action
    }

    private fun setItemData(
        action: ActionType,
        pos: BlockPos,
        world: Level,
        stack: ItemStack,
        source: String
    ) {
        action.pos = pos
        action.world = world.dimension().location()
        action.objectResourceLocation = BuiltInRegistries.ITEM.getKey(stack.item)
        action.sourceName = source
        if (!stack.isEmpty) {
            action.extraData = stack.createNbt(world.registryAccess()).toString()
        }
    }

    fun entityKillAction(world: Level, pos: BlockPos, entity: Entity, cause: DamageSource): EntityKillActionType {
        val killer = cause.entity
        val action = EntityKillActionType()

        when {
            killer is Player -> {
                setEntityData(action, pos, world, entity, Sources.PLAYER)
                action.sourceProfile = killer.gameProfile
            }

            killer != null -> {
                val source = BuiltInRegistries.ENTITY_TYPE.getKey(killer.type).path
                setEntityData(action, pos, world, entity, source)
            }

            else -> {
                setEntityData(action, pos, world, entity, cause.msgId)
            }
        }

        return action
    }

    fun entityKillAction(world: Level, pos: BlockPos, entity: Entity, source: String): EntityKillActionType {
        val action = EntityKillActionType()
        setEntityData(action, pos, world, entity, source)
        return action
    }

    private fun setEntityData(
        action: ActionType,
        pos: BlockPos,
        world: Level,
        entity: Entity,
        source: String
    ) {
        action.pos = pos
        action.world = world.dimension().location()
        action.objectResourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)
        action.sourceName = source
        action.extraData = entity.createNbt().toString()
    }

    fun entityChangeAction(
        world: Level,
        pos: BlockPos,
        oldEntityTags: CompoundTag,
        entity: Entity,
        itemStack: ItemStack?,
        entityActor: Entity?,
        sourceType: String
    ): EntityChangeActionType {
        val action = EntityChangeActionType()

        action.pos = pos
        action.world = world.dimension().location()
        action.objectResourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)
        action.oldObjectResourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)

        if (itemStack != null && !itemStack.isEmpty) {
            action.extraData = itemStack.createNbt(world.registryAccess()).toString()
        }
        action.oldObjectState = oldEntityTags.toString()
        action.objectState = entity.createNbt().toString()
        action.sourceName = sourceType

        if (entityActor is Player) {
            action.sourceProfile = entityActor.gameProfile
        }

        return action
    }

    fun entityMountAction(
        entity: Entity,
        player: Player,
    ): EntityMountActionType {
        val world = entity.level()

        val action = EntityMountActionType()

        action.pos = entity.blockPosition()
        action.world = world.dimension().location()
        action.objectResourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)
        action.oldObjectResourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)

        action.objectState = entity.createNbt().toString()
        action.sourceName = Sources.PLAYER

        action.sourceProfile = player.gameProfile

        return action
    }

    fun entityDismountAction(
        entity: Entity,
        player: Player,
    ): EntityDismountActionType {
        val world = entity.level()

        val action = EntityDismountActionType()

        action.pos = entity.blockPosition()
        action.world = world.dimension().location()
        action.objectResourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)
        action.oldObjectResourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)

        action.objectState = entity.createNbt().toString()
        action.sourceName = Sources.PLAYER

        action.sourceProfile = player.gameProfile

        return action
    }
}
