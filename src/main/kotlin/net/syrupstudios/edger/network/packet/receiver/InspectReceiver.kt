package net.syrupstudios.edger.network.packet.receiver

import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.commands.CommandConsts
import net.syrupstudios.edger.database.DatabaseManager
import net.syrupstudios.edger.network.packet.LedgerPacketTypes
import net.syrupstudios.edger.network.packet.Receiver
import net.syrupstudios.edger.network.packet.action.ActionPacket
import net.syrupstudios.edger.network.packet.response.ResponseCodes
import net.syrupstudios.edger.network.packet.response.ResponseContent
import net.syrupstudios.edger.network.packet.response.ResponsePacket
import net.syrupstudios.edger.utility.getInspectResults
import kotlinx.coroutines.launch
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

class InspectReceiver : Receiver {
    override fun receive(
        server: MinecraftServer,
        player: ServerPlayer,
        handler: ServerGamePacketListenerImpl,
        buf: FriendlyByteBuf,
        sender: PacketSender
    ) {
        if (!Permissions.check(player, "ledger.networking", CommandConsts.PERMISSION_LEVEL) ||
            !Permissions.check(player, "ledger.commands.inspect", CommandConsts.PERMISSION_LEVEL)
        ) {
            ResponsePacket.sendResponse(
                ResponseContent(LedgerPacketTypes.INSPECT_POS.id, ResponseCodes.NO_PERMISSION.code),
                sender
            )
            return
        }

        val pos = buf.readBlockPos()
        ResponsePacket.sendResponse(
            ResponseContent(LedgerPacketTypes.INSPECT_POS.id, ResponseCodes.EXECUTING.code),
            sender
        )

        val pages = buf.readInt()

        Ledger.launch {
            val results = player.getInspectResults(pos)
            for (i in 1..pages) {
                val page = DatabaseManager.searchActions(results.searchParams, i)
                page.actions.forEach { action ->
                    val packet = ActionPacket()
                    packet.populate(action)
                    sender.sendPacket(packet.channel, packet.buf)
                }
            }
            ResponsePacket.sendResponse(
                ResponseContent(LedgerPacketTypes.INSPECT_POS.id, ResponseCodes.COMPLETED.code),
                sender
            )
        }
    }
}
