package net.syrupstudios.edger.network.packet.receiver

import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.commands.CommandConsts
import net.syrupstudios.edger.commands.arguments.SearchParamArgument
import net.syrupstudios.edger.database.DatabaseManager
import net.syrupstudios.edger.network.packet.LedgerPacketTypes
import net.syrupstudios.edger.network.packet.Receiver
import net.syrupstudios.edger.network.packet.response.ResponseCodes
import net.syrupstudios.edger.network.packet.response.ResponseContent
import net.syrupstudios.edger.network.packet.response.ResponsePacket
import kotlinx.coroutines.launch
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

class PurgeReceiver : Receiver {
    override fun receive(
        server: MinecraftServer,
        player: ServerPlayer,
        handler: ServerGamePacketListenerImpl,
        buf: FriendlyByteBuf,
        sender: PacketSender
    ) {
        if (!Permissions.check(player, "ledger.networking", CommandConsts.PERMISSION_LEVEL) ||
            !Permissions.check(player, "ledger.commands.purge", CommandConsts.PERMISSION_LEVEL)
        ) {
            ResponsePacket.sendResponse(
                ResponseContent(LedgerPacketTypes.PURGE.id, ResponseCodes.NO_PERMISSION.code),
                sender
            )
            return
        }

        val params = SearchParamArgument.get(buf.readUtf(), player.createCommandSourceStack())

        ResponsePacket.sendResponse(ResponseContent(LedgerPacketTypes.PURGE.id, ResponseCodes.EXECUTING.code), sender)

        Ledger.launch {
            DatabaseManager.purgeActions(params)

            ResponsePacket.sendResponse(
                ResponseContent(LedgerPacketTypes.PURGE.id, ResponseCodes.COMPLETED.code),
                sender
            )
        }
    }
}
