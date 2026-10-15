package net.syrupstudios.edger.actions

import net.syrupstudios.edger.actionutils.Preview
import net.syrupstudios.edger.config.ActionsSpec
import net.syrupstudios.edger.config.config
import com.mojang.authlib.GameProfile
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.time.Instant
import kotlin.time.ExperimentalTime

interface ActionType {
    var id: Int
    val identifier: String
    var timestamp: Instant
    var pos: BlockPos
    var world: ResourceLocation?
    var objectResourceLocation: ResourceLocation
    var oldObjectResourceLocation: ResourceLocation
    var objectState: String?
    var oldObjectState: String?
    var sourceName: String
    var sourceProfile: GameProfile?
    var extraData: String?
    var rolledBack: Boolean

    fun rollback(server: MinecraftServer): Boolean
    fun restore(server: MinecraftServer): Boolean
    fun previewRollback(preview: Preview, player: ServerPlayer)
    fun previewRestore(preview: Preview, player: ServerPlayer)
    fun getTranslationType(): String

    @ExperimentalTime
    fun getMessage(source: CommandSourceStack): Component

    fun isBlacklisted() = config[ActionsSpec.typeBlacklist].contains(identifier) ||
            config[ActionsSpec.objectBlacklist].contains(objectResourceLocation) ||
            config[ActionsSpec.objectBlacklist].contains(oldObjectResourceLocation) ||
            config[ActionsSpec.sourceBlacklist].contains(sourceName) ||
            config[ActionsSpec.sourceBlacklist].contains("@${sourceProfile?.name}") ||
            config[ActionsSpec.worldBlacklist].contains(world)
}
