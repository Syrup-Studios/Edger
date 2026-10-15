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
import net.syrupstudios.edger.utility.launchMain
import net.syrupstudios.edger.utility.literal
import kotlinx.coroutines.launch
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object RollbackCommand : BuildableCommand {
    override fun build(): LiteralNode {
        return Commands.literal("rollback")
            .requires(Permissions.require("ledger.commands.rollback", CommandConsts.PERMISSION_LEVEL))
            .then(
                SearchParamArgument.argument("params")
                    .executes { rollback(it, SearchParamArgument.get(it, "params")) }
            )
            .build()
    }

    fun rollback(context: Context, params: ActionSearchParams): Int {
        val source = context.source
        params.ensureSpecific()
        Ledger.launch {
            MessageUtils.warnBusy(source)
            val actions = DatabaseManager.selectRollback(params)

            if (actions.isEmpty()) {
                source.sendFailure(Component.translatable("error.edger.command.no_results"))
                return@launch
            }

            source.sendSuccess(
                {
                    Component.translatable(
                        "text.edger.rollback.start",
                        actions.size.toString().literal().setStyle(TextColorPallet.secondary)
                    ).setStyle(TextColorPallet.primary)
                },
                true
            )

            context.source.level.launchMain {
                val fails = HashMap<String, Int>()
                val actionIds = HashSet<Int>()
                for (action in actions) {
                    if (!action.rollback(context.source.server)) {
                        fails[action.identifier] = fails.getOrPut(action.identifier) { 0 } + 1
                    } else {
                        actionIds.add(action.id)
                    }
                }
                Ledger.launch {
                    DatabaseManager.rollbackActions(actionIds)
                }

                for (entry in fails.entries) {
                    source.sendSuccess(
                        {
                            Component.translatable("text.edger.rollback.fail", entry.key, entry.value).setStyle(
                                TextColorPallet.secondary
                            )
                        },
                        true
                    )
                }

                source.sendSuccess(
                    {
                        Component.translatable(
                            "text.edger.rollback.finish",
                            actions.size
                        ).setStyle(TextColorPallet.primary)
                    },
                    true
                )
            }
        }
        return 1
    }
}
