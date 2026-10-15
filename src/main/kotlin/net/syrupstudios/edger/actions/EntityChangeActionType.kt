package net.syrupstudios.edger.actions

import net.syrupstudios.edger.utility.TextColorPallet
import net.syrupstudios.edger.utility.UUID
import net.syrupstudios.edger.utility.getWorld
import net.syrupstudios.edger.utility.literal
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.Util
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.HangingEntity
import net.minecraft.world.entity.decoration.ItemFrame
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack

class EntityChangeActionType : AbstractActionType() {
    override val identifier = "entity-change"

    override fun getTranslationType(): String {
        val item = getStack(RegistryAccess.EMPTY).item
        return if (item is BlockItem) {
            "block"
        } else {
            "item"
        }
    }

    private fun getStack(registryManager: RegistryAccess): ItemStack {
        if (extraData == null) return ItemStack.EMPTY
        try {
            return ItemStack.CODEC.parse(
                NbtOps.INSTANCE,
                TagParser.parseTag(extraData!!)
            ).result().orElse(ItemStack.EMPTY)
        } catch (_: CommandSyntaxException) {
            // In an earlier version of ledger extraData only stored the item id
            val item = BuiltInRegistries.ITEM.get(ResourceLocation(extraData!!))
            return item.defaultInstance
        }
    }

    override fun getObjectMessage(source: CommandSourceStack): Component {
        val text = Component.literal("")
        text.append(
            Component.translatable(
                Util.makeDescriptionId(
                    "entity",
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

        val stack = getStack(source.registryAccess())
        if (!stack.isEmpty) {
            text.append(
                Component.literal(" ").append(Component.translatable("text.edger.action_message.with")).append(" ")
            )
            text.append(
                Component.translatable(
                    stack.item.descriptionId
                ).setStyle(TextColorPallet.secondaryVariant).withStyle {
                    it.withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_ITEM,
                            HoverEvent.ItemStackInfo(stack)
                        )
                    )
                }
            )
        }
        return text
    }

    override fun rollback(server: MinecraftServer): Boolean {
        val world = server.getWorld(world)

        val oldEntity = TagParser.parseTag(oldObjectState!!)
        if (!oldEntity.hasUUID(UUID)) return false
        val entity = world?.getEntity(oldEntity.getUUID(UUID))

        if (entity != null) {
            if (entity is ItemFrame) {
                entity.item = ItemStack.EMPTY
            }
            when (entity) {
                is LivingEntity -> entity.load(oldEntity)
                is HangingEntity -> entity.load(oldEntity)
            }
            return true
        }
        return false
    }

    override fun restore(server: MinecraftServer): Boolean {
        val world = server.getWorld(world)
        val newEntity = TagParser.parseTag(objectState!!)
        if (!newEntity.hasUUID(UUID)) return false
        val entity = world?.getEntity(newEntity.getUUID(UUID))

        if (entity != null) {
            if (entity is ItemFrame) {
                entity.item = ItemStack.EMPTY
            }
            when (entity) {
                is LivingEntity -> entity.load(newEntity)
                is HangingEntity -> entity.load(newEntity)
            }
            return true
        }
        return false
    }
}
