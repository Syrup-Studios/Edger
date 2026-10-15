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
import net.syrupstudios.edger.utility.TextColorPallet
import kotlinx.coroutines.launch
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

class SearchReceiver : Receiver {
    override fun receive(
        server: MinecraftServer,
        player: ServerPlayer,
        handler: ServerGamePacketListenerImpl,
        buf: FriendlyByteBuf,
        sender: PacketSender
    ) {
        if (!Permissions.check(player, "ledger.networking", CommandConsts.PERMISSION_LEVEL) ||
            !Permissions.check(player, "ledger.commands.search", CommandConsts.PERMISSION_LEVEL)
        ) {
            ResponsePacket.sendResponse(
                ResponseContent(LedgerPacketTypes.SEARCH.id, ResponseCodes.NO_PERMISSION.code),
                sender
            )
            return
        }
        val source = player.createCommandSourceStack()
        val input = buf.readUtf()
        val params = SearchParamArgument.get(input, source)

        val pages = buf.readInt()

        ResponsePacket.sendResponse(
            ResponseContent(LedgerPacketTypes.SEARCH.id, ResponseCodes.EXECUTING.code),
            sender
        )

        Ledger.launch {
            Ledger.searchCache[source.textName] = params

            MessageUtils.warnBusy(source)
            val results = DatabaseManager.searchActions(params, 1)

            for (i in 1..pages) {
                val page = DatabaseManager.searchActions(results.searchParams, i)
                MessageUtils.sendSearchResults(
                    source,
                    page,
                    Component.translatable(
                        "text.edger.header.search"
                    ).setStyle(TextColorPallet.primary)
                )
            }

            ResponsePacket.sendResponse(
                ResponseContent(LedgerPacketTypes.SEARCH.id, ResponseCodes.COMPLETED.code),
                sender
            )
        }
    }
}
