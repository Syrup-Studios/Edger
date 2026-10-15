package net.syrupstudios.edger.commands.subcommands

import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.commands.BuildableCommand
import net.syrupstudios.edger.commands.CommandConsts
import net.syrupstudios.edger.database.DatabaseManager
import net.syrupstudios.edger.utility.LiteralNode
import net.syrupstudios.edger.utility.MessageUtils
import com.mojang.authlib.GameProfile
import kotlinx.coroutines.launch
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.GameProfileArgument

object PlayerCommand : BuildableCommand {
    override fun build(): LiteralNode {
        return literal("player")
            .requires(Permissions.require("ledger.commands.player", CommandConsts.PERMISSION_LEVEL))
            .then(
                argument("player", GameProfileArgument.gameProfile())
                .executes {
                    return@executes lookupPlayer(GameProfileArgument.getGameProfiles(it, "player"), it.source)
                }
            )
            .build()
    }

    private fun lookupPlayer(profiles: MutableCollection<GameProfile>, source: CommandSourceStack): Int {
        Ledger.launch {
            val players = DatabaseManager.searchPlayers(profiles.toSet())
            MessageUtils.sendPlayerMessage(source, players)
        }

        return 1
    }
}
