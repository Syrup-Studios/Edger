package net.syrupstudios.edger.api

import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.config.config
import net.syrupstudios.edger.config.getDatabasePath
import net.minecraft.server.MinecraftServer
import javax.sql.DataSource

object ExtensionManager {
    private val _extensions = mutableListOf<LedgerExtension>()
    val extensions: List<LedgerExtension>
        get() = _extensions

    private var dataSource: DataSource? = null

    val commands = mutableListOf<CommandExtension>()

    fun registerExtension(extension: LedgerExtension) {
        _extensions.add(extension)

        if (extension is CommandExtension) {
            commands.add(extension)
        }
        extension.getConfigSpecs().forEach {
            config.addSpec(it)
        }
    }

    internal fun serverStarting(server: MinecraftServer) {
        extensions.forEach {
            if (it is DatabaseExtension) {
                if (dataSource == null) {
                    dataSource = it.getDataSource(config.getDatabasePath())
                } else {
                    failExtensionRegistration(it)
                }
            }
        }
    }

    private fun failExtensionRegistration(extension: LedgerExtension) {
        Ledger.logger.error("Unable to load extension ${extension.getResourceLocation()}")
    }

    fun getDataSource() = dataSource
}
