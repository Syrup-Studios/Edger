package net.syrupstudios.edger.commands

import net.syrupstudios.edger.utility.LiteralNode

interface BuildableCommand {
    fun build(): LiteralNode
}
