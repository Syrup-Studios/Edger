package net.syrupstudios.edger.actions

import net.syrupstudios.edger.actionutils.Preview
import net.syrupstudios.edger.utility.UUID
import net.syrupstudios.edger.utility.getWorld
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.TagParser
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

class EntityKillActionType : AbstractActionType() {
    override val identifier = "entity-kill"

    override fun getTranslationType() = "entity"

    private fun getEntity(world: ServerLevel): Entity? {
        val entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(objectResourceLocation)
        if (entityType.isEmpty) return null

        val entity = entityType.get().create(world)!!
        entity.load(TagParser.parseTag(extraData!!))
        entity.setDeltaMovement(Vec3.ZERO)
        entity.setRemainingFireTicks(0)
        if (entity is LivingEntity) entity.health = entity.maxHealth

        return entity
    }

    override fun previewRollback(preview: Preview, player: ServerPlayer) {
        val world = player.serverLevel().server.getWorld(world)!!
        val entity = getEntity(world) ?: return

        val entityTrackerEntry = ServerEntity(world, entity, 1, false) { }
        entityTrackerEntry.addPairing(player)
        preview.spawnedEntityTrackers.add(entityTrackerEntry)
    }

    override fun previewRestore(preview: Preview, player: ServerPlayer) {
        val world = player.serverLevel().server.getWorld(world)

        val tag = TagParser.parseTag(extraData!!)
        if (tag.hasUUID("UUID")) {
            val uuid = tag.getUUID("UUID")
            val entity = world?.getEntity(uuid)
            entity?.let {
                val entityTrackerEntry = ServerEntity(world, entity, 1, false) {}
                entityTrackerEntry.removePairing(player)
                preview.removedEntityTrackers.add(entityTrackerEntry)
            }
        }
    }

    override fun rollback(server: MinecraftServer): Boolean {
        val world = server.getWorld(world)!!
        val entity = getEntity(world) ?: return false
        world.addFreshEntity(entity)
        return true
    }

    override fun restore(server: MinecraftServer): Boolean {
        val world = server.getWorld(world)

        val tag = TagParser.parseTag(extraData!!)
        if (!tag.hasUUID(UUID)) return false
        val entity = world?.getEntity(tag.getUUID(UUID))

        if (entity != null) {
            entity.remove(Entity.RemovalReason.DISCARDED)
            return true
        }

        return false
    }
}
