package net.syrupstudios.edger.api

import java.nio.file.Path
import javax.sql.DataSource

interface DatabaseExtension : LedgerExtension {
    fun getDataSource(savePath: Path): DataSource
}
