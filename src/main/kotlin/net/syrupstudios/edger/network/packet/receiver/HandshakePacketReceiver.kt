package net.syrupstudios.edger.network.packet.receiver

import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.commands.CommandConsts
import net.syrupstudios.edger.logInfo
import net.syrupstudios.edger.network.Networking
import net.syrupstudios.edger.network.Networking.enableNetworking
import net.syrupstudios.edger.network.packet.Receiver
import net.syrupstudios.edger.network.packet.handshake.HandshakeContent
import net.syrupstudios.edger.network.packet.handshake.HandshakePacket
import net.syrupstudios.edger.network.packet.handshake.ModInfo
import net.syrupstudios.edger.registry.ActionRegistry
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.*

class HandshakePacketReceiver : Receiver {
    override fun receive(
        server: MinecraftServer,
        player: ServerPlayer,
        handler: ServerGamePacketListenerImpl,
        buf: FriendlyByteBuf,
        sender: PacketSender
    ) {
        if (!Permissions.check(player, "ledger.networking", CommandConsts.PERMISSION_LEVEL)) return
        // This should be sent by the client whenever a player joins with a client mod
        val nbt = buf.readNbt()
        // We do some validation on the packet to make sure it's complete and intact
        val info = readInfo(nbt)
        if (info.isPresent) {
            val modid = info.get().modid
            val modVersion = info.get().version
            val ledgerVersion = FabricLoader.getInstance().getModContainer(
                Ledger.MOD_ID
            ).get().metadata.version.friendlyString
            if (Networking.PROTOCOL_VERSION == info.get().protocolVersion) {
                logInfo("${player.name.string} joined the server with a Ledger compatible client mod")
                logInfo("Mod: $modid, Version: $modVersion")

                // Player has networking permissions so we send a response
                val packet = HandshakePacket()
                packet.populate(
                    HandshakeContent(Networking.PROTOCOL_VERSION, ledgerVersion, ActionRegistry.getTypes().toList())
                )
                ServerPlayNetworking.send(player, packet.channel, packet.buf)
                player.enableNetworking()
            } else {
                player.sendSystemMessage(
                    Component.translatable(
                        "text.edger.network.protocols_mismatched",
                        Networking.PROTOCOL_VERSION,
                        info.get().protocolVersion
                    ),
                        false
                )
                logInfo(
                    "${player.name.string} joined the server with a Ledger compatible client mod, " +
                        "but has a mismatched protocol: Ledger protocol version: ${Networking.PROTOCOL_VERSION}" +
                        ", Client mod protocol version ${info.get().protocolVersion}"
                )
            }
        } else {
            player.sendSystemMessage(Component.translatable("text.edger.network.no_mod_info"), false)
        }
    }

    private fun readInfo(nbt: CompoundTag?): Optional<ModInfo> {
        if (nbt == null) {
            return Optional.empty()
        }

        return Optional.of(ModInfo(nbt.getString("modid"), nbt.getString("version"), nbt.getInt("protocol_version")))
    }
}
