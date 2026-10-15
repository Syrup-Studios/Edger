package net.syrupstudios.edger.network

import net.syrupstudios.edger.config.NetworkingSpec
import net.syrupstudios.edger.config.config
import net.syrupstudios.edger.network.packet.LedgerPacketTypes
import net.syrupstudios.edger.network.packet.Receiver
import net.syrupstudios.edger.network.packet.receiver.HandshakePacketReceiver
import net.syrupstudios.edger.network.packet.receiver.InspectReceiver
import net.syrupstudios.edger.network.packet.receiver.PurgeReceiver
import net.syrupstudios.edger.network.packet.receiver.RollbackReceiver
import net.syrupstudios.edger.network.packet.receiver.SearchReceiver
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

object Networking {
    // List of players who have a compatible client mod
    private var networkedPlayers = mutableSetOf<ServerPlayer>()
    const val PROTOCOL_VERSION = 2

    init {
        if (config[NetworkingSpec.networking]) {
            register(LedgerPacketTypes.INSPECT_POS.id, InspectReceiver())
            register(LedgerPacketTypes.SEARCH.id, SearchReceiver())
            register(LedgerPacketTypes.HANDSHAKE.id, HandshakePacketReceiver())
            register(LedgerPacketTypes.ROLLBACK.id, RollbackReceiver())
            register(LedgerPacketTypes.PURGE.id, PurgeReceiver())
        }
    }

    private fun register(channel: ResourceLocation, receiver: Receiver) {
        ServerPlayNetworking.registerGlobalReceiver(channel) {
                server: MinecraftServer,
                player: ServerPlayer,
                handler: ServerGamePacketListenerImpl,
                buf: FriendlyByteBuf,
                sender: PacketSender ->

            receiver.receive(server, player, handler, buf, sender)
        }
    }

    fun ServerPlayer.hasNetworking() = networkedPlayers.contains(this)

    fun ServerPlayer.enableNetworking() = networkedPlayers.add(this)

    fun ServerPlayer.disableNetworking() = networkedPlayers.remove(this)
}
