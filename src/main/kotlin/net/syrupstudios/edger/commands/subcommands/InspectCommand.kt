package net.syrupstudios.edger.commands.subcommands

import net.syrupstudios.edger.commands.BuildableCommand
import net.syrupstudios.edger.commands.CommandConsts
import net.syrupstudios.edger.utility.Context
import net.syrupstudios.edger.utility.LiteralNode
import net.syrupstudios.edger.utility.inspectBlock
import net.syrupstudios.edger.utility.inspectOff
import net.syrupstudios.edger.utility.inspectOn
import net.syrupstudios.edger.utility.isInspecting
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.core.BlockPos

object InspectCommand : BuildableCommand {
    override fun build(): LiteralNode =
        literal("inspect")
            .requires(Permissions.require("ledger.commands.inspect", CommandConsts.PERMISSION_LEVEL))
            .executes { toggleInspect(it) }
            .then(
                literal("on")
                    .executes { it.source.playerOrException.inspectOn() }
            )
            .then(
                literal("off")
                    .executes { it.source.playerOrException.inspectOff() }
            )
            .then(
                argument("pos", BlockPosArgument.blockPos())
                    .executes { inspectBlock(it, BlockPosArgument.getBlockPos(it, "pos")) }
            )
            .build()

    private fun toggleInspect(context: Context): Int {
        val source = context.source
        val player = source.playerOrException

        return if (player.isInspecting()) {
            player.inspectOff()
        } else {
            player.inspectOn()
        }
    }

    private fun inspectBlock(context: Context, pos: BlockPos): Int {
        val source = context.source

        source.inspectBlock(pos)
        return 1
    }
}
