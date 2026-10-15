package net.syrupstudios.edger.commands.subcommands

import net.syrupstudios.edger.commands.BuildableCommand
import net.syrupstudios.edger.commands.CommandConsts
import net.syrupstudios.edger.utility.Context
import net.syrupstudios.edger.utility.LiteralNode
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.DimensionArgument
import net.minecraft.commands.arguments.coordinates.Coordinates
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.server.level.ServerLevel

object TeleportCommand : BuildableCommand {
    private const val BLOCK_CENTER_OFFSET = 0.5
    override fun build(): LiteralNode =
        Commands.literal("tp")
            .requires(Permissions.require("ledger.commands.tp", CommandConsts.PERMISSION_LEVEL))
            .then(
                Commands.argument("world", DimensionArgument.dimension())
                    .then(
                        Commands.argument("location", Vec3Argument.vec3())
                            .executes {
                                teleport(
                                    it,
                                    DimensionArgument.getDimension(it, "world"),
                                    Vec3Argument.getCoordinates(it, "location")
                                )
                            }
                    )
            )
            .build()

    private fun teleport(context: Context, world: ServerLevel, posArg: Coordinates): Int {
        val player = context.source.playerOrException
        val pos = posArg.getBlockPos(context.source)

        val x = pos.x.toDouble() + BLOCK_CENTER_OFFSET
        val z = pos.z.toDouble() + BLOCK_CENTER_OFFSET

        player.teleportTo(
            world,
            x,
            pos.y.toDouble(),
            z,
            emptySet(),
            player.yRot,
            player.xRot
        )

        return 1
    }
}
