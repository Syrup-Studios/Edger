package net.syrupstudios.edger.utility

import net.minecraft.world.entity.player.Player

interface PlayerCausable {
    val causingPlayer: Player?
}
