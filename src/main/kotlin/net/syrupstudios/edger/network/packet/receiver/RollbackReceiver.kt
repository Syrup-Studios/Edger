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
import net.syrupstudios.edger.utility.MessageUtils
import net.syrupstudios.edger.utility.launchMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

class RollbackReceiver : Receiver {
    override fun receive(
        server: MinecraftServer,
        player: ServerPlayer,
        handler: ServerGamePacketListenerImpl,
        buf: FriendlyByteBuf,
        sender: PacketSender
    ) {
        if (!Permissions.check(player, "ledger.networking", CommandConsts.PERMISSION_LEVEL) ||
            !Permissions.check(player, "ledger.commands.rollback", CommandConsts.PERMISSION_LEVEL)
        ) {
            ResponsePacket.sendResponse(
                ResponseContent(LedgerPacketTypes.ROLLBACK.id, ResponseCodes.NO_PERMISSION.code),
                sender
            )
            return
        }

        val source = player.createCommandSourceStack()
        val restore = buf.readBoolean()
        val args = buf.readUtf()

        val params = SearchParamArgument.get(args, source)

        ResponsePacket.sendResponse(
            ResponseContent(LedgerPacketTypes.ROLLBACK.id, ResponseCodes.EXECUTING.code),
            sender
        )

        Ledger.launch(Dispatchers.IO) {
            MessageUtils.warnBusy(source)
            if (restore) {
                val actions = DatabaseManager.restoreActions(params)

                source.level.launchMain {
                    for (action in actions) {
                        action.restore(source.server)
                        action.rolledBack = false
                    }

                    ResponsePacket.sendResponse(
                        ResponseContent(LedgerPacketTypes.ROLLBACK.id, ResponseCodes.COMPLETED.code),
                        sender
                    )
                }
            } else {
                val actions = DatabaseManager.rollbackActions(params)

                source.level.launchMain {
                    for (action in actions) {
                        action.rollback(source.server)
                        action.rolledBack = true
                    }

                    ResponsePacket.sendResponse(
                        ResponseContent(LedgerPacketTypes.ROLLBACK.id, ResponseCodes.COMPLETED.code),
                        sender
                    )
                }
            }
        }
    }
}
