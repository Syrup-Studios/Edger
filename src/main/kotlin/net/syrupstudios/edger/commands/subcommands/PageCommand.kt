package net.syrupstudios.edger.commands.subcommands

import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.commands.BuildableCommand
import net.syrupstudios.edger.database.DatabaseManager
import net.syrupstudios.edger.utility.Context
import net.syrupstudios.edger.utility.LiteralNode
import net.syrupstudios.edger.utility.MessageUtils
import net.syrupstudios.edger.utility.TextColorPallet
import com.mojang.brigadier.arguments.IntegerArgumentType
import kotlinx.coroutines.launch
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component

object PageCommand : BuildableCommand {
    override fun build(): LiteralNode =
        literal("page")
            .then(
                argument("page", IntegerArgumentType.integer(1))
                    .executes { page(it, IntegerArgumentType.getInteger(it, "page")) }
            )
            .build()

    private fun page(context: Context, page: Int): Int {
        val source = context.source

        val params = Ledger.searchCache[source.textName]
        if (params != null) {
            Ledger.launch {
                MessageUtils.warnBusy(source)
                val results = DatabaseManager.searchActions(params, page)

                if (results.page > results.pages) {
                    source.sendFailure(Component.translatable("error.edger.no_more_pages"))
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
        } else {
            source.sendFailure(Component.translatable("error.edger.no_cached_params"))
            return -1
        }
    }
}
