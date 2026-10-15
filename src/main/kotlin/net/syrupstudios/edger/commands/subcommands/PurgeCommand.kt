package net.syrupstudios.edger.commands.subcommands

import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.actionutils.ActionSearchParams
import net.syrupstudios.edger.commands.BuildableCommand
import net.syrupstudios.edger.commands.CommandConsts
import net.syrupstudios.edger.commands.arguments.SearchParamArgument
import net.syrupstudios.edger.config.SearchSpec
import net.syrupstudios.edger.config.config
import net.syrupstudios.edger.database.DatabaseManager
import net.syrupstudios.edger.utility.Context
import net.syrupstudios.edger.utility.LiteralNode
import net.syrupstudios.edger.utility.TextColorPallet
import kotlinx.coroutines.launch
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component

object PurgeCommand : BuildableCommand {
    override fun build(): LiteralNode {
        return literal("purge")
            .requires(Permissions.require("ledger.commands.purge", config[SearchSpec.purgePermissionLevel]))
            .then(
                SearchParamArgument.argument(CommandConsts.PARAMS).executes {
                runPurge(it, SearchParamArgument.get(it, CommandConsts.PARAMS))
            }
            )
            .build()
    }

    private fun runPurge(ctx: Context, params: ActionSearchParams): Int {
        val source = ctx.source
        source.sendSuccess(
            { Component.translatable("text.edger.purge.starting").setStyle(TextColorPallet.secondary) },
            true
        )
        Ledger.launch {
            DatabaseManager.purgeActions(params)
            source.sendSuccess(
                { Component.translatable("text.edger.purge.complete").setStyle(TextColorPallet.secondary) },
                true
            )
        }
        return 1
    }
}
