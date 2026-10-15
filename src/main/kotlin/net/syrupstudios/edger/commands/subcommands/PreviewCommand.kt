package net.syrupstudios.edger.commands.subcommands

import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.actionutils.ActionSearchParams
import net.syrupstudios.edger.actionutils.Preview
import net.syrupstudios.edger.commands.BuildableCommand
import net.syrupstudios.edger.commands.CommandConsts
import net.syrupstudios.edger.commands.arguments.SearchParamArgument
import net.syrupstudios.edger.database.DatabaseManager
import net.syrupstudios.edger.utility.Context
import net.syrupstudios.edger.utility.LiteralNode
import net.syrupstudios.edger.utility.MessageUtils
import kotlinx.coroutines.launch
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object PreviewCommand : BuildableCommand {
    override fun build(): LiteralNode {
        return Commands.literal("preview")
            .requires(Permissions.require("ledger.commands.preview", CommandConsts.PERMISSION_LEVEL))
            .then(
                Commands.literal("rollback")
                .then(
                    SearchParamArgument.argument(CommandConsts.PARAMS)
                        .executes {
                            preview(
                                it,
                                SearchParamArgument.get(it, CommandConsts.PARAMS),
                                Preview.Type.ROLLBACK
                            )
                        }
                )
            )
            .then(
                Commands.literal("restore")
                .then(
                    SearchParamArgument.argument(CommandConsts.PARAMS)
                        .executes {
                            preview(
                                it,
                                SearchParamArgument.get(it, CommandConsts.PARAMS),
                                Preview.Type.RESTORE
                            )
                        }
                )
            )
            .then(Commands.literal("apply").executes { apply(it) })
            .then(Commands.literal("cancel").executes { cancel(it) })
            .build()
    }

    private fun preview(context: Context, params: ActionSearchParams, type: Preview.Type): Int {
        val source = context.source
        val player = source.playerOrException
        params.ensureSpecific()
        Ledger.launch {
            MessageUtils.warnBusy(source)
            val actions = DatabaseManager.previewActions(params, type)

            if (actions.isEmpty()) {
                source.sendFailure(Component.translatable("error.edger.command.no_results"))
                return@launch
            }

            Ledger.previewCache[player.uuid]?.cancel(player)
            Ledger.previewCache[player.uuid] = Preview(params, actions, player, type)
        }
        return 1
    }

    private fun apply(context: Context): Int {
        val uuid = context.source.playerOrException.uuid

        if (Ledger.previewCache.containsKey(uuid)) {
            Ledger.previewCache[uuid]?.apply(context)
            Ledger.previewCache.remove(uuid)
        } else {
            context.source.sendFailure(Component.translatable("error.edger.no_preview"))
            return -1
        }

        return 1
    }

    private fun cancel(context: Context): Int {
        val uuid = context.source.playerOrException.uuid

        if (Ledger.previewCache.containsKey(uuid)) {
            Ledger.previewCache[uuid]?.cancel(context.source.playerOrException)
            Ledger.previewCache.remove(uuid)
        } else {
            context.source.sendFailure(Component.translatable("error.edger.no_preview"))
            return -1
        }

        return 1
    }
}
