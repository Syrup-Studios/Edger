package net.syrupstudios.edger

import net.syrupstudios.edger.actionutils.ActionSearchParams
import net.syrupstudios.edger.actionutils.Preview
import net.syrupstudios.edger.api.ExtensionManager
import net.syrupstudios.edger.api.LedgerApi
import net.syrupstudios.edger.api.LedgerApiImpl
import net.syrupstudios.edger.commands.registerCommands
import net.syrupstudios.edger.config.CONFIG_PATH
import net.syrupstudios.edger.config.DatabaseSpec
import net.syrupstudios.edger.database.ActionQueueService
import net.syrupstudios.edger.database.DatabaseManager
import net.syrupstudios.edger.listeners.registerBlockListeners
import net.syrupstudios.edger.listeners.registerEntityListeners
import net.syrupstudios.edger.listeners.registerPlayerListeners
import net.syrupstudios.edger.listeners.registerWorldEventListeners
import net.syrupstudios.edger.network.Networking
import net.syrupstudios.edger.registry.ActionRegistry
import com.uchuhimo.konf.Config
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jetbrains.exposed.v1.core.vendors.SQLiteDialect
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import net.syrupstudios.edger.config.config as realConfig

object Ledger : DedicatedServerModInitializer, CoroutineScope {
    const val MOD_ID = "edger"
    val DEFAULT_DATABASE = SQLiteDialect.dialectName

    @JvmStatic
    val api: LedgerApi = LedgerApiImpl

    val logger: Logger = LogManager.getLogger("Edger")
    lateinit var config: Config
    lateinit var server: MinecraftServer
    val searchCache = ConcurrentHashMap<String, ActionSearchParams>()

    @JvmField // Required for mixin access
    val previewCache = ConcurrentHashMap<UUID, Preview>()

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName("Edger")

    override fun onInitializeServer() {
        val version = FabricLoader.getInstance().getModContainer(MOD_ID).get().metadata.version
        logInfo("Initializing Edger ${version.friendlyString}")

        if (!Files.exists(FabricLoader.getInstance().configDir.resolve(CONFIG_PATH))) {
            logInfo("No config file, Creating")
            Files.copy(
                FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath(CONFIG_PATH).get(),
                FabricLoader.getInstance().configDir.resolve(CONFIG_PATH)
            )
        }
        realConfig.validateRequired()
        config = realConfig

        ServerLifecycleEvents.SERVER_STARTING.register(::serverStarting)
        ServerLifecycleEvents.SERVER_STOPPED.register(::serverStopped)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> registerCommands(dispatcher) }
    }

    private fun serverStarting(server: MinecraftServer) {
        this.server = server
        ExtensionManager.serverStarting(server)
        DatabaseManager.setup(ExtensionManager.getDataSource())
        DatabaseManager.ensureTables()

        ActionRegistry.registerDefaultTypes()
        initListeners()
        Networking

        Ledger.launch {
            val idSet = setOf<ResourceLocation>()
                .plus(BuiltInRegistries.BLOCK.keySet())
                .plus(BuiltInRegistries.ITEM.keySet())
                .plus(BuiltInRegistries.ENTITY_TYPE.keySet())

            logInfo("Inserting ${idSet.size} registry keys into the database...")
            DatabaseManager.insertResourceLocations(idSet)
            logInfo("Registry insert complete")

            DatabaseManager.setupCache()
            DatabaseManager.autoPurge()
        }.invokeOnCompletion {
            ActionQueueService.start()
        }
    }

    private fun serverStopped(server: MinecraftServer) {
        runBlocking {
            try {
                withTimeout(config[DatabaseSpec.queueTimeoutMin].minutes) {
                    val progressLogger = launch {
                        while (ActionQueueService.size > 0) {
                            logInfo(
                                "Database is still busy. If you exit now data WILL be lost. " +
                                        "Actions in queue: ${ActionQueueService.size}"
                            )

                            delay(config[DatabaseSpec.queueCheckDelaySec].seconds)
                        }
                    }
                    try {
                        ActionQueueService.drainAll()
                        logInfo("Successfully drained database queue")
                    } finally {
                        progressLogger.cancel()
                    }
                }
            } catch (e: TimeoutCancellationException) {
                logWarn(
                    "Database drain timed out. ${ActionQueueService.size} actions still in queue. Data may be lost."
                )
            }
        }
    }

    private fun initListeners() {
        registerWorldEventListeners()
        registerPlayerListeners()
        registerBlockListeners()
        registerEntityListeners()
    }

    fun identifier(path: String) = ResourceLocation(MOD_ID, path)
}

fun logDebug(message: String) = Ledger.logger.debug(message)
fun logInfo(message: String) = Ledger.logger.info(message)
fun logWarn(message: String) = Ledger.logger.warn(message)
fun logWarn(message: String, throwable: Throwable) = Ledger.logger.warn(message, throwable)
fun logFatal(message: String) = Ledger.logger.warn(message)
