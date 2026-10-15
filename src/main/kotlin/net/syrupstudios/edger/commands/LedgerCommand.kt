package net.syrupstudios.edger.commands

import net.syrupstudios.edger.api.ExtensionManager
import net.syrupstudios.edger.commands.subcommands.InspectCommand
import net.syrupstudios.edger.commands.subcommands.PageCommand
import net.syrupstudios.edger.commands.subcommands.PlayerCommand
import net.syrupstudios.edger.commands.subcommands.PreviewCommand
import net.syrupstudios.edger.commands.subcommands.PurgeCommand
import net.syrupstudios.edger.commands.subcommands.RestoreCommand
import net.syrupstudios.edger.commands.subcommands.RollbackCommand
import net.syrupstudios.edger.commands.subcommands.SearchCommand
import net.syrupstudios.edger.commands.subcommands.StatusCommand
import net.syrupstudios.edger.commands.subcommands.TeleportCommand
import net.syrupstudios.edger.utility.BrigadierUtils
import net.syrupstudios.edger.utility.Dispatcher
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.Commands.literal

fun registerCommands(dispatcher: Dispatcher) {
    val rootNode =
        literal("ledger").requires(Permissions.require("ledger.commands.root", CommandConsts.PERMISSION_LEVEL))
            .build()

    dispatcher.root.addChild(rootNode)
    dispatcher.root.addChild(
        literal("lg")
            .requires(Permissions.require("ledger.commands.root", CommandConsts.PERMISSION_LEVEL)).redirect(rootNode)
            .build()
    )

    rootNode.addChild(InspectCommand.build())
    rootNode.addChild(BrigadierUtils.buildRedirect("i", InspectCommand.build()))

    rootNode.addChild(SearchCommand.build())
    rootNode.addChild(BrigadierUtils.buildRedirect("s", SearchCommand.build()))

    rootNode.addChild(PageCommand.build())
    rootNode.addChild(BrigadierUtils.buildRedirect("pg", PageCommand.build()))

    rootNode.addChild(RollbackCommand.build())
    rootNode.addChild(BrigadierUtils.buildRedirect("rb", RollbackCommand.build()))

    rootNode.addChild(PreviewCommand.build())
    rootNode.addChild(BrigadierUtils.buildRedirect("pv", PreviewCommand.build()))

    rootNode.addChild(RestoreCommand.build())

    rootNode.addChild(StatusCommand.build())

    rootNode.addChild(TeleportCommand.build())

    rootNode.addChild(PurgeCommand.build())

    rootNode.addChild(PlayerCommand.build())

    ExtensionManager.commands.forEach {
        it.registerSubcommands().forEach { command ->
            rootNode.addChild(command.build())
        }
    }
}
