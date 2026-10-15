package net.syrupstudios.edger.api

import com.uchuhimo.konf.ConfigSpec
import net.minecraft.resources.ResourceLocation

interface LedgerExtension {
    fun getResourceLocation(): ResourceLocation

    // All extension configs should be entirely optional as the default config will not have fallback
    fun getConfigSpecs(): List<ConfigSpec>
}
