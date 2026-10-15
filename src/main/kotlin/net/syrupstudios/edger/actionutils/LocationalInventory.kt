package net.syrupstudios.edger.actionutils

import net.minecraft.core.BlockPos

interface LocationalInventory {
    fun getLocation(): BlockPos
}
