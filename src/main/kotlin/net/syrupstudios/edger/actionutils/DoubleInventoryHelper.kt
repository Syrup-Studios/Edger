package net.syrupstudios.edger.actionutils

import net.minecraft.world.Container

interface DoubleInventoryHelper {
    fun getInventory(slot: Int): Container
}
