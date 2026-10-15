package net.syrupstudios.edger.utility

import com.mojang.logging.LogUtils
import net.minecraft.core.HolderGetter
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.TagParser
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

const val ITEM_NBT_DATA_VERSION = 3817
const val ITEM_COMPONENTS_DATA_VERSION = 3825

const val PROPERTIES = "Properties" // BlockState
const val COUNT_PRE_1_20_5 = "Count" // ItemStack
const val COUNT = "count" // ItemStack
const val UUID = "UUID" // Entity

val LOGGER = LogUtils.getLogger()

object NbtUtils {
    fun blockStateToProperties(state: BlockState): CompoundTag? {
        val stateTag = NbtUtils.writeBlockState(state)
        if (state.block.defaultBlockState() == state) return null // Don't store default block state
        return stateTag.getCompound(PROPERTIES)
    }

    fun blockStateFromProperties(
        tag: CompoundTag,
        name: ResourceLocation,
        blockLookup: HolderGetter<Block>
    ): BlockState {
        val stateTag = CompoundTag()
        stateTag.putString("Name", name.toString())
        stateTag.put(PROPERTIES, tag)
        return NbtUtils.readBlockState(blockLookup, stateTag)
    }

    fun itemFromProperties(tag: String?, name: ResourceLocation, registries: HolderLookup.Provider): ItemStack {
        val itemTag = TagParser.parseTag(tag ?: "{}")
        return ItemStack.CODEC.parse(NbtOps.INSTANCE, itemTag).result().orElse(ItemStack.EMPTY)
    }

    fun BlockEntity.createNbt(registries: HolderLookup.Provider) = this.saveWithId()

    fun Entity.createNbt() = this.saveWithoutId(CompoundTag())

    fun ItemStack.createNbt(registries: HolderLookup.Provider) =
        ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElseThrow() as CompoundTag
}
