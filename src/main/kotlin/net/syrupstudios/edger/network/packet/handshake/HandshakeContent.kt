package net.syrupstudios.edger.network.packet.handshake

data class HandshakeContent(val protocolVersion: Int, val ledgerVersion: String, val actions: List<String>)
