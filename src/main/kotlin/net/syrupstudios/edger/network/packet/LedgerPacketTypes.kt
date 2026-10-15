package net.syrupstudios.edger.network.packet

import net.syrupstudios.edger.Ledger
import net.minecraft.resources.ResourceLocation

enum class LedgerPacketTypes(val id: ResourceLocation) {
    ACTION(Ledger.identifier("action")),
    INSPECT_POS(Ledger.identifier("inspect")),
    SEARCH(Ledger.identifier("search")),
    HANDSHAKE(Ledger.identifier("handshake")),
    RESPONSE(Ledger.identifier("response")),
    ROLLBACK(Ledger.identifier("rollback")),
    PURGE(Ledger.identifier("purge")),
}
