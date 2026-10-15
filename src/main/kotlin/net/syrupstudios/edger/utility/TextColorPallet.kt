package net.syrupstudios.edger.utility

import net.syrupstudios.edger.config.ColorSpec
import net.syrupstudios.edger.config.config
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor

@Suppress("MagicNumber")
object TextColorPallet {
    val primary: Style
        get() = Style.EMPTY.withColor(TextColor.parseColor(config[ColorSpec.primary]))

    val primaryVariant: Style
        get() = Style.EMPTY.withColor(
        TextColor.parseColor(config[ColorSpec.primaryVariant])
    )
    val secondary: Style get() = Style.EMPTY.withColor(TextColor.parseColor(config[ColorSpec.secondary]))
    val secondaryVariant: Style
        get() = Style.EMPTY.withColor(
        TextColor.parseColor(config[ColorSpec.secondaryVariant])
    )
    val light: Style get() = Style.EMPTY.withColor(TextColor.parseColor(config[ColorSpec.light]))
}
