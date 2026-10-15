package net.syrupstudios.edger.api

import net.syrupstudios.edger.commands.BuildableCommand

interface CommandExtension : LedgerExtension {
    fun registerSubcommands(): List<BuildableCommand>
}
