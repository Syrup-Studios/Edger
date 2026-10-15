package net.syrupstudios.edger.commands.subcommands

import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.commands.BuildableCommand
import net.syrupstudios.edger.commands.CommandConsts
import net.syrupstudios.edger.database.ActionQueueService
import net.syrupstudios.edger.database.DatabaseManager
import net.syrupstudios.edger.utility.Context
import net.syrupstudios.edger.utility.LiteralNode
import net.syrupstudios.edger.utility.TextColorPallet
import net.syrupstudios.edger.utility.literal
import kotlinx.coroutines.launch
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.SemanticVersion
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object StatusCommand : BuildableCommand {
    override fun build(): LiteralNode =
        Commands.literal("status")
            .requires(Permissions.require("ledger.commands.status", CommandConsts.PERMISSION_LEVEL))
            .executes { status(it) }
            .build()

    private fun status(context: Context): Int {
        Ledger.launch {
            val source = context.source
            source.sendSystemMessage(
                Component.translatable("text.edger.header.status")
                    .setStyle(TextColorPallet.primary)
            )
            source.sendSystemMessage(
                Component.translatable(
                    "text.edger.status.queue",
                    ActionQueueService.size.toString().literal()
                        .setStyle(TextColorPallet.secondaryVariant)
                ).setStyle(TextColorPallet.secondary)
            )
            source.sendSystemMessage(
                Component.translatable(
                    "text.edger.status.version",
                    getVersion().friendlyString.literal()
                        .setStyle(TextColorPallet.secondaryVariant)
                ).setStyle(TextColorPallet.secondary)
            )
            source.sendSystemMessage(
                Component.translatable(
                    "text.edger.status.db_type",
                    DatabaseManager.databaseType.literal()
                        .setStyle(TextColorPallet.secondaryVariant)
                ).setStyle(TextColorPallet.secondary)
            )
        }

        return 1
    }

    private fun getVersion() = SemanticVersion.parse(
        FabricLoader.getInstance().getModContainer(Ledger.MOD_ID).get().metadata.version.friendlyString
    )
}
