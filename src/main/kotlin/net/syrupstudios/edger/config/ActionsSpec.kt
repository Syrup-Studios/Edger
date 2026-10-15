package net.syrupstudios.edger.config

import com.uchuhimo.konf.ConfigSpec
import net.minecraft.resources.ResourceLocation

object ActionsSpec : ConfigSpec() {
    val typeBlacklist by required<List<String>>()
    val worldBlacklist by required<List<ResourceLocation>>()
    val objectBlacklist by required<List<ResourceLocation>>()
    val sourceBlacklist by required<List<String>>()
}
