package net.syrupstudios.edger.network.packet

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation

interface LedgerPacket<T> {
    val channel: ResourceLocation
    var buf: FriendlyByteBuf
    fun populate(content: T)
}
