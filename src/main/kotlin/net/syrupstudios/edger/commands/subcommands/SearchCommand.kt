package net.syrupstudios.edger.commands.subcommands

import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.actionutils.ActionSearchParams
import net.syrupstudios.edger.commands.BuildableCommand
import net.syrupstudios.edger.commands.CommandConsts
import net.syrupstudios.edger.commands.arguments.SearchParamArgument
import net.syrupstudios.edger.database.DatabaseManager
import net.syrupstudios.edger.utility.Context
import net.syrupstudios.edger.utility.LiteralNode
import net.syrupstudios.edger.utility.MessageUtils
import net.syrupstudios.edger.utility.TextColorPallet
import kotlinx.coroutines.launch
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component

object SearchCommand : BuildableCommand {
    override fun build(): LiteralNode {
        return literal("search")
            .requires(Permissions.require("ledger.commands.search", CommandConsts.PERMISSION_LEVEL))
            .then(
                SearchParamArgument.argument("params")
                    .executes { search(it, SearchParamArgument.get(it, "params")) }
            )
            .build()
    }

    private fun search(context: Context, params: ActionSearchParams): Int {
        val source = context.source

        Ledger.launch {
            Ledger.searchCache[source.textName] = params

            MessageUtils.warnBusy(source)
            val results = DatabaseManager.searchActions(params, 1)

            if (results.actions.isEmpty()) {
                source.sendFailure(Component.translatable("error.edger.command.no_results"))
                return@launch
            }

            MessageUtils.sendSearchResults(
                source,
                results,
                Component.translatable(
                    "text.edger.header.search"
                ).setStyle(TextColorPallet.primary)
            )
        }

        return 1
    }
}
