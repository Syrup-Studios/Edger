package net.syrupstudios.edger.network.packet.response

import net.syrupstudios.edger.network.packet.LedgerPacket
import net.syrupstudios.edger.network.packet.LedgerPacketTypes
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class ResponsePacket : LedgerPacket<ResponseContent> {
    override var buf: FriendlyByteBuf = PacketByteBufs.create()
    override val channel: ResourceLocation = LedgerPacketTypes.RESPONSE.id
    override fun populate(content: ResponseContent) {
        // Packet type, rollback response would be `ledger.rollback`
        buf.writeResourceLocation(content.type)
        // Response code
        buf.writeInt(content.response)
    }

    companion object {
        fun sendResponse(content: ResponseContent, sender: PacketSender) {
            val response = ResponsePacket()
            response.populate(content)
            sender.sendPacket(LedgerPacketTypes.RESPONSE.id, response.buf)
        }
    }
}
