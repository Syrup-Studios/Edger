package net.syrupstudios.edger.actions

import net.syrupstudios.edger.utility.NbtUtils
import net.syrupstudios.edger.utility.TextColorPallet
import net.syrupstudios.edger.utility.UUID
import net.syrupstudios.edger.utility.getWorld
import net.syrupstudios.edger.utility.literal
import net.minecraft.commands.CommandSourceStack
import net.minecraft.nbt.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.ItemEntity

open class ItemPickUpActionType : AbstractActionType() {
    override val identifier = "item-pick-up"

    // Not used
    override fun getTranslationType(): String = "item"

    private fun getStack(server: MinecraftServer) = NbtUtils.itemFromProperties(
        extraData,
        objectResourceLocation,
        server.registryAccess()
    )

    override fun getObjectMessage(source: CommandSourceStack): Component {
        val stack = getStack(source.server)

        return "${stack.count} ".literal().append(
            stack.hoverName
        ).setStyle(TextColorPallet.secondaryVariant).withStyle {
            it.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_ITEM,
                    HoverEvent.ItemStackInfo(stack)
                )
            )
        }
    }

    override fun rollback(server: MinecraftServer): Boolean {
        val world = server.getWorld(world)!!

        val oldEntity = TagParser.parseTag(oldObjectState!!)
        if (!oldEntity.hasUUID(UUID)) return false
        val entity = world.getEntity(oldEntity.getUUID(UUID))

        if (entity == null) {
            val entity = ItemEntity(EntityType.ITEM, world)
            entity.load(oldEntity)
            world.addFreshEntity(entity)
        }
        return true
    }

    override fun restore(server: MinecraftServer): Boolean {
        val world = server.getWorld(world)

        val oldEntity = TagParser.parseTag(oldObjectState!!)
        if (!oldEntity.hasUUID(UUID)) return false
        val entity = world?.getEntity(oldEntity.getUUID(UUID))

        if (entity != null) {
            entity.remove(Entity.RemovalReason.DISCARDED)
            return true
        }
        return false
    }
}
