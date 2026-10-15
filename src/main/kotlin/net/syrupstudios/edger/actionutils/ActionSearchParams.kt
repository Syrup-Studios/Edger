package net.syrupstudios.edger.actionutils

import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.config.SearchSpec
import net.syrupstudios.edger.utility.Negatable
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.structure.BoundingBox
import java.time.Instant
import java.util.*
import kotlin.math.max

data class ActionSearchParams(
    val bounds: BoundingBox?,
    val before: Instant?,
    val after: Instant?,
    val rolledBack: Boolean?,
    var actions: MutableSet<Negatable<String>>?,
    var objects: MutableSet<Negatable<ResourceLocation>>?,
    var sourceNames: MutableSet<Negatable<String>>?,
    var sourcePlayerIds: MutableSet<Negatable<UUID>>?,
    var worlds: MutableSet<Negatable<ResourceLocation>>?
) {
    private constructor(builder: Builder) : this(
        builder.bounds,
        builder.before,
        builder.after,
        builder.rolledBack,
        builder.actions,
        builder.objects,
        builder.sourceNames,
        builder.sourcePlayerIds,
        builder.worlds
    )

    fun ensureSpecific() {
        if (bounds == null) {
            throw SimpleCommandExceptionType(Component.translatable("error.edger.unspecific.range")).create()
        }
        val range = (max(bounds.xSpan, max(bounds.ySpan, bounds.zSpan)) + 1) / 2
        if (range > Ledger.config[SearchSpec.maxRange] && bounds != GLOBAL) {
            throw SimpleCommandExceptionType(
                Component.translatable("error.edger.unspecific.range_to_big", Ledger.config[SearchSpec.maxRange])
            ).create()
        }
        if (sourceNames == null && sourcePlayerIds == null && after == null && before == null) {
            throw SimpleCommandExceptionType(Component.translatable("error.edger.unspecific.source_or_time")).create()
        }
    }

    companion object {
        val GLOBAL: BoundingBox =
            BoundingBox(-Int.MAX_VALUE, -Int.MAX_VALUE, -Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        var bounds: BoundingBox? = null
        var before: Instant? = null
        var after: Instant? = null
        var rolledBack: Boolean? = null
        var actions: MutableSet<Negatable<String>>? = null
        var objects: MutableSet<Negatable<ResourceLocation>>? = null
        var sourceNames: MutableSet<Negatable<String>>? = null
        var sourcePlayerIds: MutableSet<Negatable<UUID>>? = null
        var worlds: MutableSet<Negatable<ResourceLocation>>? = null

        fun build() = ActionSearchParams(this)
    }
}
