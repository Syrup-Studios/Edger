package net.syrupstudios.edger.utility

import net.minecraft.world.inventory.AbstractContainerMenu

interface HandledSlot {
    fun getHandler(): AbstractContainerMenu
    fun setHandler(handler: AbstractContainerMenu)
}
